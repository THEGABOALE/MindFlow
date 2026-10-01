const pool = require("../database/connection");
const { withTransaction } = require("../database/transaction");
const missionRepository = require("../repositories/mission.repository");
const attemptRepository = require("../repositories/attempt.repository");
const levelProgressRepository = require("../repositories/level-progress.repository");
const groupRepository = require("../repositories/group.repository");
const userRepository = require("../repositories/user.repository");
const {
  REVIEW_PAY_WINDOW_HOURS,
  calculatePoints,
  exceededTimeLimit,
  gradeAttempt
} = require("../services/mission-grading.service");
const { missionStartBlock } = require("../services/mission-access.service");
const { normalizeTzOffset } = require("../services/streak.service");
const { loadStreak } = require("../services/streak-query.service");
const { respondServerError } = require("../utils/server-error");

// Devuelve el contenido jugable de una mision: preguntas con sus opciones
// (opcion multiple y verdadero/falso) o con sus pares (relacion de conceptos).
const getMissionContent = async (req, res) => {
  const { missionId } = req.params;

  if (!/^\d+$/.test(missionId)) {
    return res.status(400).json({
      message: "El ID de la misión debe ser numérico",
      status: "ERROR"
    });
  }

  try {
    const mission = await missionRepository.findPublishedMission(pool, missionId);

    if (!mission) {
      return res.status(404).json({
        message: "No se encontró la misión",
        status: "ERROR"
      });
    }

    const questionRows = await missionRepository.findQuestions(pool, missionId);
    const optionRows = await missionRepository.findOptions(pool, missionId);
    const pairRows = await missionRepository.findPairs(pool, missionId);

    const questions = questionRows.map((question) => ({
      id: question.id,
      prompt: question.question_text,
      type: question.question_type,
      feedback: question.feedback,
      orderIndex: question.order_index,
      points: question.points,
      options: optionRows
        .filter((option) => option.question_id === question.id)
        .map((option) => ({
          id: option.id,
          text: option.option_text,
          isCorrect: option.is_correct,
          feedback: option.feedback,
          orderIndex: option.order_index
        })),
      pairs: pairRows
        .filter((pair) => pair.question_id === question.id)
        .map((pair) => ({
          id: pair.id,
          term: pair.term,
          match: pair.match_text,
          orderIndex: pair.order_index
        }))
    }));

    return res.status(200).json({
      message: "Contenido de la misión obtenido correctamente",
      status: "OK",
      mission: {
        id: mission.id,
        levelId: mission.level_id,
        title: mission.title,
        description: mission.description,
        topic: mission.topic,
        orderIndex: mission.order_index,
        pointsReward: mission.points_reward,
        mechanic: mission.mechanic,
        timeLimitSeconds: mission.time_limit_seconds,
        maxPlumas: mission.max_plumas,
        questions
      }
    });
  } catch (error) {
    return respondServerError(res, "Error al obtener el contenido de la misión", error);
  }
};

// Abre un intento. El tiempo se mide en el servidor (started_at) para que el
// panel docente no dependa de lo que reporte el telefono. Solo puede abrirlo
// un estudiante (la ruta lo exige), de una mision de su nivel y con la
// anterior ya completada.
const startAttempt = async (req, res) => {
  const { missionId } = req.params;

  if (!/^\d+$/.test(missionId)) {
    return res.status(400).json({
      message: "El ID de la misión debe ser numérico",
      status: "ERROR"
    });
  }

  try {
    const mission = await missionRepository.findPublishedMission(pool, missionId);

    if (!mission) {
      return res.status(404).json({
        message: "No se encontró la misión",
        status: "ERROR"
      });
    }

    const studentGroup = await groupRepository.findActiveGroup(pool, req.user.id);
    const previousMission = await missionRepository.findPreviousMission(pool, mission.level_id, mission.order_index);
    const previousMissionCompleted =
      !previousMission || (await attemptRepository.hasCompletedMission(pool, req.user.id, previousMission.id));

    const block = missionStartBlock({ studentGroup, mission, previousMissionCompleted });

    if (block) {
      return res.status(403).json({ message: block, status: "ERROR" });
    }

    // Si ya la completó antes, este intento se muestra como repaso. Lo que
    // vale es lo que se decide al cerrarlo, porque puede haber otro intento
    // de la misma misión abierto a la vez.
    const isReview = await attemptRepository.hasCompletedMission(pool, req.user.id, missionId);

    const attempt = await attemptRepository.createAttempt(pool, {
      userId: req.user.id,
      missionId,
      isReview
    });

    return res.status(201).json({
      message: "Intento iniciado",
      status: "OK",
      attempt: {
        id: attempt.id,
        missionId: Number(missionId),
        isReview,
        maxPlumas: mission.max_plumas,
        timeLimitSeconds: mission.time_limit_seconds,
        startedAt: attempt.started_at
      }
    });
  } catch (error) {
    return respondServerError(res, "Error al iniciar el intento", error);
  }
};

// Cierra el intento. La correccion se hace acá contra la base, nunca se
// confia en un puntaje que mande el cliente.
const finishAttempt = async (req, res) => {
  const { attemptId } = req.params;
  const { answers, timedOut } = req.body || {};
  const tzOffsetMinutes = normalizeTzOffset((req.body || {}).tzOffsetMinutes);

  if (!/^\d+$/.test(attemptId)) {
    return res.status(400).json({
      message: "El ID del intento debe ser numérico",
      status: "ERROR"
    });
  }

  if (!Array.isArray(answers)) {
    return res.status(400).json({
      message: "answers debe ser una lista",
      status: "ERROR"
    });
  }

  if (answers.some((answer) => answer === null || typeof answer !== "object" || Array.isArray(answer))) {
    return res.status(400).json({
      message: "Cada respuesta debe ser un objeto",
      status: "ERROR"
    });
  }

  try {
    // Todo el cierre va en una transaccion: o queda el intento corregido con
    // sus respuestas y el progreso del nivel, o no queda nada.
    const outcome = await withTransaction(async (db) => {
      // Los cierres de un mismo estudiante van de a uno: si dos intentos de la
      // misma misión se cierran a la vez, el segundo ya ve al primero
      // completado y cuenta como repaso en vez de cobrar la recompensa
      // completa otra vez.
      await userRepository.lockUser(db, req.user.id);

      const attempt = await attemptRepository.findAttemptWithMission(db, attemptId);

      if (!attempt) {
        return { httpStatus: 404, body: { message: "No se encontró el intento", status: "ERROR" } };
      }

      if (attempt.user_id !== req.user.id) {
        return { httpStatus: 403, body: { message: "Este intento no es tuyo", status: "ERROR" } };
      }

      if (attempt.status !== "in_progress") {
        return { httpStatus: 409, body: { message: "Este intento ya fue cerrado", status: "ERROR" } };
      }

      // Preguntas reales de la mision: la correccion recorre esta lista, no la
      // que mande el cliente, para que omitir o duplicar respuestas no cambie
      // el puntaje (cada pregunta/par sin responder cuenta como incorrecta).
      const { correctAnswers, wrongAnswers, answerRows } = gradeAttempt({
        questions: await missionRepository.findQuestions(db, attempt.mission_id),
        options: await missionRepository.findOptions(db, attempt.mission_id),
        pairs: await missionRepository.findPairs(db, attempt.mission_id),
        answers
      });

      for (const row of answerRows) {
        await attemptRepository.insertAnswer(db, attemptId, row);
      }

      const ranOutOfPlumas = wrongAnswers >= attempt.max_plumas;

      // La app avisa timedOut cuando su reloj llega a cero, pero eso solo
      // puede adelantar el fallo: el servidor también compara con su reloj.
      const ranOutOfTime =
        Boolean(timedOut) ||
        exceededTimeLimit({
          elapsedSeconds: Number(attempt.elapsed_seconds),
          timeLimitSeconds: attempt.time_limit_seconds
        });

      const failed = ranOutOfTime || ranOutOfPlumas;

      const totalAnswers = correctAnswers + wrongAnswers;
      const score = totalAnswers > 0 ? Math.round((correctAnswers / totalAnswers) * 100) : 0;

      // Si es repaso se decide ahora y no al abrir: un intento abierto antes
      // de completar la misión por otro lado igual cuenta como repaso.
      const isReview = await attemptRepository.hasCompletedMission(db, req.user.id, attempt.mission_id);
      const reviewAlreadyPaid =
        isReview &&
        (await attemptRepository.hasPaidReviewSince(db, req.user.id, attempt.mission_id, REVIEW_PAY_WINDOW_HOURS));

      const pointsEarned = failed
        ? 0
        : calculatePoints({
            pointsReward: attempt.points_reward,
            wrongAnswers,
            maxPlumas: attempt.max_plumas,
            isReview,
            reviewAlreadyPaid
          });

      await attemptRepository.closeAttempt(db, attemptId, {
        score,
        correctAnswers,
        wrongAnswers,
        pointsEarned,
        isReview,
        status: failed ? "failed" : "completed"
      });

      // El progreso del nivel solo puede subir: un repaso que salga peor, o una
      // mision perdida, nunca hacen retroceder la ruta de aprendizaje.
      let levelProgress = null;

      if (!failed) {
        const { total, completed } = await levelProgressRepository.countLevelMissions(
          db,
          attempt.level_id,
          req.user.id
        );

        const progress = await levelProgressRepository.saveLevelProgress(db, {
          userId: req.user.id,
          levelId: attempt.level_id,
          percentage: total > 0 ? (completed / total) * 100 : 0,
          status: completed >= total ? "completed" : "in_progress"
        });

        levelProgress = {
          levelId: progress.level_id,
          progressPercentage: Number(progress.progress_percentage),
          status: progress.status
        };
      }

      // Terminar un intento (pasado o no) cuenta para la racha. Si es el primero
      // del dia, este intento fue el que la encendio o la descongelo: la app lo
      // usa para mostrar la animacion de hielo a llama.
      const { streak, attemptsToday } = await loadStreak(db, req.user.id, tzOffsetMinutes);

      return {
        httpStatus: 200,
        body: {
          message: failed ? "Misión no superada" : "Misión completada",
          status: "OK",
          attempt: {
            id: Number(attemptId),
            missionId: attempt.mission_id,
            score,
            correctAnswers,
            wrongAnswers,
            plumasLeft: Math.max(attempt.max_plumas - wrongAnswers, 0),
            pointsEarned,
            isReview,
            status: failed ? "failed" : "completed"
          },
          levelProgress,
          streak: {
            days: streak.days,
            isActive: streak.isActive,
            justActivated: attemptsToday === 1
          }
        }
      };
    });

    return res.status(outcome.httpStatus).json(outcome.body);
  } catch (error) {
    return respondServerError(res, "Error al cerrar el intento", error);
  }
};

module.exports = {
  getMissionContent,
  startAttempt,
  finishAttempt
};
