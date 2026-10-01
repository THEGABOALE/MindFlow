// Califica un intento y deja todo guardado: respuestas, resultado y progreso
// del nivel. Lo usan el cierre de un intento abierto en el servidor y la
// sincronización de intentos jugados en el teléfono, para que las dos vías
// apliquen exactamente las mismas reglas.
//
// Se llama dentro de una transacción que ya bloqueó la fila del estudiante
// (userRepository.lockUser): así dos intentos de la misma misión no pueden
// decidir a la vez que ninguno es repaso.
const missionRepository = require("../repositories/mission.repository");
const attemptRepository = require("../repositories/attempt.repository");
const levelProgressRepository = require("../repositories/level-progress.repository");
const { REVIEW_PAY_WINDOW_HOURS, decideAttemptOutcome, gradeAttempt } = require("./mission-grading.service");

/**
 * @param db conexión de la transacción
 * @param {object} args
 * @param {(fields: {score:number, correctAnswers:number, wrongAnswers:number, pointsEarned:number, isReview:boolean, status:string}) => Promise<number>} args.save
 *   guarda el intento cerrado (actualiza el abierto o inserta uno nuevo) y devuelve su id
 * @param {Date} [args.finishedAt] cuándo se terminó el intento, si se jugó en el
 *   teléfono; si no viene, se toma la hora actual
 */
const settleAttempt = async (db, {
  userId, missionId, levelId, pointsReward, maxPlumas, timeLimitSeconds,
  answers, timedOut, elapsedSeconds, finishedAt = null, save
}) => {
  // La corrección recorre las preguntas reales de la misión, no lo que mande
  // el cliente: omitir o duplicar respuestas no cambia el puntaje.
  const { correctAnswers, wrongAnswers, answerRows } = gradeAttempt({
    questions: await missionRepository.findQuestions(db, missionId),
    options: await missionRepository.findOptions(db, missionId),
    pairs: await missionRepository.findPairs(db, missionId),
    answers
  });

  // Si es repaso se decide al cerrar y no al abrir: un intento abierto antes
  // de completar la misión por otro lado igual cuenta como repaso.
  const isReview = await attemptRepository.hasCompletedMission(db, userId, missionId);
  // Cada repaso cobrado cerca de este intento parte a la mitad lo que paga. Se
  // mide contra la hora en que se terminó (finishedAt en los que vienen del
  // teléfono; la hora actual en línea).
  const paidReviewsNearby = isReview
    ? await attemptRepository.countPaidReviewsNear(db, userId, missionId, REVIEW_PAY_WINDOW_HOURS, finishedAt)
    : 0;

  const outcome = decideAttemptOutcome({
    correctAnswers, wrongAnswers, maxPlumas, pointsReward,
    timedOut, elapsedSeconds, timeLimitSeconds, isReview, paidReviewsNearby
  });

  const attemptId = await save({
    score: outcome.score,
    correctAnswers,
    wrongAnswers,
    pointsEarned: outcome.pointsEarned,
    isReview,
    status: outcome.status
  });

  for (const row of answerRows) {
    await attemptRepository.insertAnswer(db, attemptId, row);
  }

  // El progreso del nivel solo puede subir: un repaso que salga peor, o una
  // misión perdida, nunca hacen retroceder la ruta de aprendizaje.
  let levelProgress = null;

  if (!outcome.failed) {
    const { total, completed } = await levelProgressRepository.countLevelMissions(db, levelId, userId);
    const progress = await levelProgressRepository.saveLevelProgress(db, {
      userId,
      levelId,
      percentage: total > 0 ? (completed / total) * 100 : 0,
      status: completed >= total ? "completed" : "in_progress"
    });

    levelProgress = {
      levelId: progress.level_id,
      progressPercentage: Number(progress.progress_percentage),
      status: progress.status
    };
  }

  return {
    attemptId,
    missionId,
    score: outcome.score,
    correctAnswers,
    wrongAnswers,
    plumasLeft: outcome.plumasLeft,
    pointsEarned: outcome.pointsEarned,
    isReview,
    status: outcome.status,
    levelProgress
  };
};

module.exports = { settleAttempt };
