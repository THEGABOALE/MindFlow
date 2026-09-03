const pool = require("../database/connection");

// Semillas que gana el estudiante al terminar una mision.
//
// Cada pluma perdida descuenta 1/(max_plumas + 1) de la recompensa base, asi
// que con las 3 plumas de siempre queda: 0 errores 100%, 1 error 75%,
// 2 errores 50%, y al tercer error se pierde la mision y no gana nada.
// El repaso (volver a jugar una mision ya completada) da la mitad, para que
// repetir hasta hacerlo perfecto siga valiendo la pena sin regalar semillas.
const REVIEW_FACTOR = 0.5;

const calculatePoints = ({ pointsReward, wrongAnswers, maxPlumas, isReview }) => {
  const plumasLeft = maxPlumas - wrongAnswers;

  if (plumasLeft <= 0) {
    return 0;
  }

  const penalty = wrongAnswers / (maxPlumas + 1);
  const earned = pointsReward * (1 - penalty);

  return Math.round(isReview ? earned * REVIEW_FACTOR : earned);
};

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
    const missionResult = await pool.query(
      `
      SELECT id, level_id, title, description, topic, order_index,
             points_reward, mechanic, time_limit_seconds, max_plumas
      FROM missions
      WHERE id = $1 AND is_published = TRUE
      LIMIT 1;
      `,
      [missionId]
    );

    const mission = missionResult.rows[0];

    if (!mission) {
      return res.status(404).json({
        message: "No se encontró la misión",
        status: "ERROR"
      });
    }

    const questionsResult = await pool.query(
      `
      SELECT id, question_text, question_type, feedback, order_index, points
      FROM questions
      WHERE mission_id = $1
      ORDER BY order_index ASC;
      `,
      [missionId]
    );

    const questionIds = questionsResult.rows.map((row) => row.id);

    const optionsResult = questionIds.length
      ? await pool.query(
          `
          SELECT id, question_id, option_text, is_correct, feedback, order_index
          FROM answer_options
          WHERE question_id = ANY($1::int[])
          ORDER BY question_id ASC, order_index ASC;
          `,
          [questionIds]
        )
      : { rows: [] };

    const pairsResult = questionIds.length
      ? await pool.query(
          `
          SELECT id, question_id, term, match_text, order_index
          FROM question_pairs
          WHERE question_id = ANY($1::int[])
          ORDER BY question_id ASC, order_index ASC;
          `,
          [questionIds]
        )
      : { rows: [] };

    const questions = questionsResult.rows.map((question) => ({
      id: question.id,
      prompt: question.question_text,
      type: question.question_type,
      feedback: question.feedback,
      orderIndex: question.order_index,
      points: question.points,
      options: optionsResult.rows
        .filter((option) => option.question_id === question.id)
        .map((option) => ({
          id: option.id,
          text: option.option_text,
          isCorrect: option.is_correct,
          feedback: option.feedback,
          orderIndex: option.order_index
        })),
      pairs: pairsResult.rows
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
    return res.status(500).json({
      message: "Error al obtener el contenido de la misión",
      status: "ERROR",
      error: error.message
    });
  }
};

// Abre un intento. El tiempo se mide en el servidor (started_at) para que el
// panel docente no dependa de lo que reporte el telefono.
const startAttempt = async (req, res) => {
  const { missionId } = req.params;

  if (!/^\d+$/.test(missionId)) {
    return res.status(400).json({
      message: "El ID de la misión debe ser numérico",
      status: "ERROR"
    });
  }

  try {
    const missionResult = await pool.query(
      "SELECT id, max_plumas, time_limit_seconds FROM missions WHERE id = $1 AND is_published = TRUE LIMIT 1;",
      [missionId]
    );

    const mission = missionResult.rows[0];

    if (!mission) {
      return res.status(404).json({
        message: "No se encontró la misión",
        status: "ERROR"
      });
    }

    // Si ya la completó antes, este intento es un repaso.
    const previousResult = await pool.query(
      "SELECT 1 FROM mission_attempts WHERE user_id = $1 AND mission_id = $2 AND status = 'completed' LIMIT 1;",
      [req.user.id, missionId]
    );

    const isReview = previousResult.rows.length > 0;

    const attemptResult = await pool.query(
      `
      INSERT INTO mission_attempts (user_id, mission_id, is_review, status)
      VALUES ($1, $2, $3, 'in_progress')
      RETURNING id, started_at;
      `,
      [req.user.id, missionId, isReview]
    );

    const attempt = attemptResult.rows[0];

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
    return res.status(500).json({
      message: "Error al iniciar el intento",
      status: "ERROR",
      error: error.message
    });
  }
};

// Cierra el intento. La correccion se hace acá contra la base, nunca se
// confia en un puntaje que mande el cliente.
const finishAttempt = async (req, res) => {
  const { attemptId } = req.params;
  const { answers, timedOut } = req.body || {};

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

  const client = await pool.connect();

  try {
    await client.query("BEGIN");

    const attemptResult = await client.query(
      `
      SELECT a.id, a.user_id, a.mission_id, a.is_review, a.status,
             m.level_id, m.points_reward, m.max_plumas
      FROM mission_attempts a
      JOIN missions m ON m.id = a.mission_id
      WHERE a.id = $1
      LIMIT 1;
      `,
      [attemptId]
    );

    const attempt = attemptResult.rows[0];

    if (!attempt) {
      await client.query("ROLLBACK");

      return res.status(404).json({
        message: "No se encontró el intento",
        status: "ERROR"
      });
    }

    if (attempt.user_id !== req.user.id) {
      await client.query("ROLLBACK");

      return res.status(403).json({
        message: "Este intento no es tuyo",
        status: "ERROR"
      });
    }

    if (attempt.status !== "in_progress") {
      await client.query("ROLLBACK");

      return res.status(409).json({
        message: "Este intento ya fue cerrado",
        status: "ERROR"
      });
    }

    const optionsResult = await client.query(
      `
      SELECT ao.id, ao.question_id, ao.is_correct
      FROM answer_options ao
      JOIN questions q ON q.id = ao.question_id
      WHERE q.mission_id = $1;
      `,
      [attempt.mission_id]
    );

    const pairsResult = await client.query(
      `
      SELECT qp.id, qp.question_id
      FROM question_pairs qp
      JOIN questions q ON q.id = qp.question_id
      WHERE q.mission_id = $1;
      `,
      [attempt.mission_id]
    );

    const optionsById = new Map(optionsResult.rows.map((row) => [row.id, row]));
    const pairsById = new Map(pairsResult.rows.map((row) => [row.id, row]));

    let correctAnswers = 0;
    let wrongAnswers = 0;

    for (const answer of answers) {
      const questionId = Number(answer.questionId);
      let isCorrect = false;
      let selectedOptionId = null;
      let pairId = null;

      if (answer.selectedOptionId != null) {
        const option = optionsById.get(Number(answer.selectedOptionId));

        // La opcion tiene que pertenecer a la pregunta que dice el cliente.
        if (option && option.question_id === questionId) {
          selectedOptionId = option.id;
          isCorrect = option.is_correct;
        }
      } else if (answer.pairId != null) {
        const pair = pairsById.get(Number(answer.pairId));

        if (pair && pair.question_id === questionId) {
          pairId = pair.id;
          // En relacion de conceptos acierta si unio el termino con su propio par.
          isCorrect = Number(answer.selectedPairId) === pair.id;
        }
      }

      if (isCorrect) {
        correctAnswers += 1;
      } else {
        wrongAnswers += 1;
      }

      await client.query(
        `
        INSERT INTO attempt_answers (attempt_id, question_id, selected_option_id, pair_id, is_correct)
        VALUES ($1, $2, $3, $4, $5);
        `,
        [attemptId, questionId, selectedOptionId, pairId, isCorrect]
      );
    }

    const ranOutOfPlumas = wrongAnswers >= attempt.max_plumas;
    const failed = Boolean(timedOut) || ranOutOfPlumas;

    const totalAnswers = correctAnswers + wrongAnswers;
    const score = totalAnswers > 0 ? Math.round((correctAnswers / totalAnswers) * 100) : 0;

    const pointsEarned = failed
      ? 0
      : calculatePoints({
          pointsReward: attempt.points_reward,
          wrongAnswers,
          maxPlumas: attempt.max_plumas,
          isReview: attempt.is_review
        });

    await client.query(
      `
      UPDATE mission_attempts
      SET score = $1,
          correct_answers = $2,
          wrong_answers = $3,
          points_earned = $4,
          status = $5,
          finished_at = CURRENT_TIMESTAMP
      WHERE id = $6;
      `,
      [score, correctAnswers, wrongAnswers, pointsEarned, failed ? "failed" : "completed", attemptId]
    );

    // El progreso del nivel solo puede subir: un repaso que salga peor, o una
    // mision perdida, nunca hacen retroceder la ruta de aprendizaje.
    let levelProgress = null;

    if (!failed) {
      const progressResult = await client.query(
        `
        SELECT
          (SELECT COUNT(*) FROM missions WHERE level_id = $1 AND is_published = TRUE) AS total,
          (SELECT COUNT(DISTINCT a.mission_id)
             FROM mission_attempts a
             JOIN missions m ON m.id = a.mission_id
            WHERE a.user_id = $2 AND m.level_id = $1 AND a.status = 'completed') AS completed;
        `,
        [attempt.level_id, req.user.id]
      );

      const { total, completed } = progressResult.rows[0];
      const percentage = Number(total) > 0 ? (Number(completed) / Number(total)) * 100 : 0;
      const status = Number(completed) >= Number(total) ? "completed" : "in_progress";

      const upsertResult = await client.query(
        `
        INSERT INTO level_progress (user_id, level_id, progress_percentage, status)
        VALUES ($1, $2, $3, $4)
        ON CONFLICT (user_id, level_id) DO UPDATE
          SET progress_percentage = GREATEST(level_progress.progress_percentage, EXCLUDED.progress_percentage),
              status = CASE WHEN EXCLUDED.progress_percentage >= level_progress.progress_percentage
                            THEN EXCLUDED.status ELSE level_progress.status END,
              updated_at = CURRENT_TIMESTAMP
        RETURNING level_id, progress_percentage, status;
        `,
        [req.user.id, attempt.level_id, percentage.toFixed(2), status]
      );

      const progress = upsertResult.rows[0];

      levelProgress = {
        levelId: progress.level_id,
        progressPercentage: Number(progress.progress_percentage),
        status: progress.status
      };
    }

    await client.query("COMMIT");

    return res.status(200).json({
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
        isReview: attempt.is_review,
        status: failed ? "failed" : "completed"
      },
      levelProgress
    });
  } catch (error) {
    await client.query("ROLLBACK");

    return res.status(500).json({
      message: "Error al cerrar el intento",
      status: "ERROR",
      error: error.message
    });
  } finally {
    client.release();
  }
};

module.exports = {
  getMissionContent,
  startAttempt,
  finishAttempt
};
