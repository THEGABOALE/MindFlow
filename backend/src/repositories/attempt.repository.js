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

// Cuantos repasos de esta mision ya cobraron a menos de `hours` horas (antes
// o despues) de `at`, la hora en que se termino el intento que se esta
// calificando. Sin `at` se usa la hora actual (intento cerrado en linea).
// Se mide contra la hora en que se jugo y no contra la de subida: si no, varios
// repasos jugados sin conexion y subidos dias despues cobrarian todos como el
// primero. Es simetrica para que no importe en que orden lleguen los intentos.
const countPaidReviewsNear = async (db, userId, missionId, hours, at = null) => {
  const result = await db.query(
    `
    WITH ref AS (
      SELECT COALESCE($4::timestamptz AT TIME ZONE current_setting('TimeZone'), LOCALTIMESTAMP) AS t
    )
    SELECT COUNT(*)::int AS paid
    FROM mission_attempts, ref
    WHERE user_id = $1
      AND mission_id = $2
      AND is_review = TRUE
      AND points_earned > 0
      AND finished_at > ref.t - make_interval(hours => $3)
      AND finished_at < ref.t + make_interval(hours => $3);
    `,
    [userId, missionId, hours, at ? at.toISOString() : null]
  );

  return Number(result.rows[0].paid);
};

// El intento junto con los datos de su mision que hacen falta para corregirlo,
// y cuantos segundos pasaron desde que se abrio. started_at se guardo con la
// hora de la sesion, asi que se compara con LOCALTIMESTAMP, que usa la misma.
//
// Deja bloqueada la fila del intento hasta que termine la transaccion: si
// llegan dos cierres del mismo intento a la vez, el segundo espera y despues
// lo ve ya cerrado.
const findAttemptWithMission = async (db, attemptId) => {
  const result = await db.query(
    `
    SELECT a.id, a.user_id, a.mission_id, a.is_review, a.status,
           EXTRACT(EPOCH FROM (LOCALTIMESTAMP - a.started_at)) AS elapsed_seconds,
           m.level_id, m.points_reward, m.max_plumas, m.time_limit_seconds
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

// Un intento por el id que le dio el teléfono, con lo necesario para volver
// a responder su resultado si el mismo intento llega dos veces.
const findByClientAttemptId = async (db, clientAttemptId) => {
  const result = await db.query(
    `
    SELECT a.id, a.user_id, a.mission_id, a.score, a.correct_answers, a.wrong_answers,
           a.points_earned, a.is_review, a.status, m.max_plumas
    FROM mission_attempts a
    JOIN missions m ON m.id = a.mission_id
    WHERE a.client_attempt_id = $1
    LIMIT 1;
    `,
    [clientAttemptId]
  );

  return result.rows[0] || null;
};

// Guarda un intento jugado en el teléfono ya calificado. Las horas llegan con
// su huso y se pasan a la hora de la sesión de la base, igual que las que se
// escriben con CURRENT_TIMESTAMP, para que la racha las lea igual.
const insertSettledAttempt = async (db, {
  userId, missionId, clientAttemptId, startedAt, finishedAt,
  score, correctAnswers, wrongAnswers, pointsEarned, isReview, status
}) => {
  const result = await db.query(
    `
    INSERT INTO mission_attempts (
      user_id, mission_id, client_attempt_id, started_at, finished_at,
      score, correct_answers, wrong_answers, points_earned, is_review, status
    )
    VALUES (
      $1, $2, $3,
      $4::timestamptz AT TIME ZONE current_setting('TimeZone'),
      $5::timestamptz AT TIME ZONE current_setting('TimeZone'),
      $6, $7, $8, $9, $10, $11
    )
    RETURNING id;
    `,
    [
      userId, missionId, clientAttemptId, startedAt.toISOString(), finishedAt.toISOString(),
      score, correctAnswers, wrongAnswers, pointsEarned, isReview, status
    ]
  );

  return result.rows[0].id;
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

const closeAttempt = async (db, attemptId, { score, correctAnswers, wrongAnswers, pointsEarned, isReview, status }) => {
  await db.query(
    `
    UPDATE mission_attempts
    SET score = $1,
        correct_answers = $2,
        wrong_answers = $3,
        points_earned = $4,
        is_review = $5,
        status = $6,
        finished_at = CURRENT_TIMESTAMP
    WHERE id = $7;
    `,
    [score, correctAnswers, wrongAnswers, pointsEarned, isReview, status, attemptId]
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
  countPaidReviewsNear,
  createAttempt,
  findAttemptWithMission,
  findByClientAttemptId,
  insertSettledAttempt,
  insertAnswer,
  closeAttempt,
  findActivityDays
};
