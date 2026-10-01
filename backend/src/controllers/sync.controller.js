const pool = require("../database/connection");
const { withTransaction } = require("../database/transaction");
const missionRepository = require("../repositories/mission.repository");
const attemptRepository = require("../repositories/attempt.repository");
const groupRepository = require("../repositories/group.repository");
const userRepository = require("../repositories/user.repository");
const { missionStartBlock } = require("../services/mission-access.service");
const { settleAttempt } = require("../services/attempt-settlement.service");
const { parseSyncBatch, checkAttemptTimes } = require("../services/attempt-sync.service");
const { loadStreak } = require("../services/streak-query.service");
const { respondServerError } = require("../utils/server-error");

const rejected = (clientAttemptId, message) => ({ clientAttemptId, status: "rejected", message });

const accepted = (clientAttemptId, attempt) => ({ clientAttemptId, status: "accepted", attempt });

// Un intento del lote, en su propia transacción. Devuelve accepted o
// rejected; un error inesperado sube y corta el lote (500).
const syncOne = (userId, item) =>
  withTransaction(async (db) => {
    // Las subidas de un mismo estudiante van de a una: si el mismo intento
    // llega dos veces a la vez, la segunda ya lo encuentra guardado.
    await userRepository.lockUser(db, userId);

    const existing = await attemptRepository.findByClientAttemptId(db, item.clientAttemptId);

    if (existing) {
      if (existing.user_id !== userId) return rejected(item.clientAttemptId, "Este intento no es tuyo");

      return accepted(item.clientAttemptId, {
        missionId: existing.mission_id,
        score: existing.score,
        correctAnswers: existing.correct_answers,
        wrongAnswers: existing.wrong_answers,
        plumasLeft: Math.max(existing.max_plumas - existing.wrong_answers, 0),
        pointsEarned: existing.points_earned,
        isReview: existing.is_review,
        status: existing.status
      });
    }

    const timeError = checkAttemptTimes({ startedAt: item.startedAt, finishedAt: item.finishedAt, now: new Date() });
    if (timeError) return rejected(item.clientAttemptId, timeError);

    const mission = await missionRepository.findPublishedMission(db, item.missionId);
    if (!mission) return rejected(item.clientAttemptId, "No se encontró la misión");

    const studentGroup = await groupRepository.findActiveGroup(db, userId);
    const previousMission = await missionRepository.findPreviousMission(db, mission.level_id, mission.order_index);
    const previousMissionCompleted =
      !previousMission || (await attemptRepository.hasCompletedMission(db, userId, previousMission.id));

    const block = missionStartBlock({ studentGroup, mission, previousMissionCompleted });
    if (block) return rejected(item.clientAttemptId, block);

    const settled = await settleAttempt(db, {
      userId,
      missionId: mission.id,
      levelId: mission.level_id,
      pointsReward: mission.points_reward,
      maxPlumas: mission.max_plumas,
      timeLimitSeconds: mission.time_limit_seconds,
      answers: item.answers,
      timedOut: item.timedOut,
      elapsedSeconds: (item.finishedAt.getTime() - item.startedAt.getTime()) / 1000,
      save: (fields) =>
        attemptRepository.insertSettledAttempt(db, {
          ...fields,
          userId,
          missionId: mission.id,
          clientAttemptId: item.clientAttemptId,
          startedAt: item.startedAt,
          finishedAt: item.finishedAt
        })
    });

    return accepted(item.clientAttemptId, {
      missionId: settled.missionId,
      score: settled.score,
      correctAnswers: settled.correctAnswers,
      wrongAnswers: settled.wrongAnswers,
      plumasLeft: settled.plumasLeft,
      pointsEarned: settled.pointsEarned,
      isReview: settled.isReview,
      status: settled.status
    });
  });

// Recibe los intentos que el estudiante jugó en el teléfono (con o sin
// conexión) y los guarda con las mismas reglas que el cierre en línea.
// Repetir el mismo lote no duplica nada: cada intento trae su propio id.
const syncAttempts = async (req, res) => {
  const batch = parseSyncBatch(req.body);

  if (batch.error) {
    return res.status(400).json({ message: batch.error, status: "ERROR" });
  }

  const userId = req.user.id;

  try {
    const before = await loadStreak(pool, userId, batch.tzOffsetMinutes);

    const results = [];
    for (const item of batch.attempts) {
      results.push(await syncOne(userId, item));
    }

    const after = await loadStreak(pool, userId, batch.tzOffsetMinutes);
    const totals = await attemptRepository.findStudentTotals(pool, userId);
    const completedMissionIds = await attemptRepository.findCompletedMissionIds(pool, userId);

    return res.status(200).json({
      message: "Intentos sincronizados",
      status: "OK",
      results,
      progress: {
        totalPoints: totals.totalPoints,
        missionsCompleted: totals.missionsCompleted,
        completedMissionIds,
        streak: after.streak,
        // El primer intento de hoy llegó en este lote: la app muestra el
        // momento de hielo a llama.
        justActivatedStreak: before.attemptsToday === 0 && after.attemptsToday > 0
      }
    });
  } catch (error) {
    return respondServerError(res, "Error al sincronizar los intentos", error);
  }
};

module.exports = { syncAttempts };
