// Intentos de mision y sus respuestas. Todas las funciones reciben la conexion
// (el pool o el client de una transaccion).

// Si ya la completo antes, el siguiente intento es un repaso.
const hasCompletedMission = async (db, userId, missionId) => {
  const result = await db.query(
    "SELECT 1 FROM mission_attempts WHERE user_id = $1 AND mission_id = $2 AND status = 'completed' LIMIT 1;",
    [userId, missionId]
  );

  return result.rows.length > 0;
};

const createAttempt = async (db, { userId, missionId, isReview }) => {
  const result = await db.query(
    `
    INSERT INTO mission_attempts (user_id, mission_id, is_review, status)
    VALUES ($1, $2, $3, 'in_progress')
    RETURNING id, started_at;
    `,
    [userId, missionId, isReview]
  );

  return result.rows[0];
};

// El intento junto con los datos de su mision que hacen falta para corregirlo.
//
// Deja bloqueada la fila del intento hasta que termine la transaccion: si
// llegan dos cierres del mismo intento a la vez, el segundo espera y despues
// lo ve ya cerrado.
const findAttemptWithMission = async (db, attemptId) => {
  const result = await db.query(
    `
    SELECT a.id, a.user_id, a.mission_id, a.is_review, a.status,
           m.level_id, m.points_reward, m.max_plumas
    FROM mission_attempts a
    JOIN missions m ON m.id = a.mission_id
    WHERE a.id = $1
    LIMIT 1
    FOR UPDATE OF a;
    `,
    [attemptId]
  );

  return result.rows[0] || null;
};

const insertAnswer = async (db, attemptId, { questionId, selectedOptionId, pairId, isCorrect }) => {
  await db.query(
    `
    INSERT INTO attempt_answers (attempt_id, question_id, selected_option_id, pair_id, is_correct)
    VALUES ($1, $2, $3, $4, $5);
    `,
    [attemptId, questionId, selectedOptionId, pairId, isCorrect]
  );
};

const closeAttempt = async (db, attemptId, { score, correctAnswers, wrongAnswers, pointsEarned, status }) => {
  await db.query(
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
    [score, correctAnswers, wrongAnswers, pointsEarned, status, attemptId]
  );
};

/**
 * Dias en que el estudiante termino al menos un intento (pasado o no), con
 * cuantos termino cada dia, en su fecha local: tzOffsetMinutes es su desfase
 * respecto a UTC.
 *
 * finished_at es un timestamp sin huso que se escribe con CURRENT_TIMESTAMP,
 * o sea, en la hora del huso de la base (UTC en Neon/Railway, pero la base
 * local usa el de la maquina). Por eso primero se le devuelve el huso de la
 * base para tener el instante real, luego se pasa a UTC y recien ahi se suma
 * el desfase del estudiante: asi el resultado no depende de como este
 * configurada la base.
 *
 * @returns {Promise<Array<{day:string, attempts:string}>>} day en "YYYY-MM-DD"
 */
const findActivityDays = async (db, userId, tzOffsetMinutes) => {
  const result = await db.query(
    `
    SELECT to_char(
             ((finished_at AT TIME ZONE current_setting('TimeZone')) AT TIME ZONE 'UTC')
               + make_interval(mins => $2),
             'YYYY-MM-DD'
           ) AS day,
           COUNT(*) AS attempts
    FROM mission_attempts
    WHERE user_id = $1 AND finished_at IS NOT NULL
    GROUP BY 1;
    `,
    [userId, tzOffsetMinutes]
  );

  return result.rows;
};

// Semillas ganadas en total (sumando repasos) y misiones completadas sin contar repasos.
const findStudentTotals = async (db, userId) => {
  const result = await db.query(
    `
    SELECT
      COALESCE(SUM(points_earned), 0) AS total_points,
      COUNT(*) FILTER (WHERE status = 'completed' AND is_review = FALSE) AS missions_completed
    FROM mission_attempts
    WHERE user_id = $1;
    `,
    [userId]
  );

  const { total_points: totalPoints, missions_completed: missionsCompleted } = result.rows[0];

  return { totalPoints: Number(totalPoints), missionsCompleted: Number(missionsCompleted) };
};

// IDs de las misiones que el estudiante ya completo, de cualquier nivel.
const findCompletedMissionIds = async (db, userId) => {
  const result = await db.query(
    `
    SELECT DISTINCT mission_id
    FROM mission_attempts
    WHERE user_id = $1 AND status = 'completed';
    `,
    [userId]
  );

  return result.rows.map((row) => row.mission_id);
};

module.exports = {
  findStudentTotals,
  findCompletedMissionIds,
  hasCompletedMission,
  createAttempt,
  findAttemptWithMission,
  insertAnswer,
  closeAttempt,
  findActivityDays
};
