// Avance del estudiante por nivel. Todas las funciones reciben la conexion
// (el pool o el client de una transaccion).

// Cuantas misiones publicadas tiene el nivel y cuantas distintas ya completo el estudiante.
const countLevelMissions = async (db, levelId, userId) => {
  const result = await db.query(
    `
    SELECT
      (SELECT COUNT(*) FROM missions WHERE level_id = $1 AND is_published = TRUE) AS total,
      (SELECT COUNT(DISTINCT a.mission_id)
         FROM mission_attempts a
         JOIN missions m ON m.id = a.mission_id
        WHERE a.user_id = $2 AND m.level_id = $1 AND a.status = 'completed') AS completed;
    `,
    [levelId, userId]
  );

  const { total, completed } = result.rows[0];

  return { total: Number(total), completed: Number(completed) };
};

// El progreso del nivel solo puede subir: si el nuevo porcentaje es menor que
// el guardado, se conserva el guardado con su estado.
const saveLevelProgress = async (db, { userId, levelId, percentage, status }) => {
  const result = await db.query(
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
    [userId, levelId, percentage.toFixed(2), status]
  );

  return result.rows[0];
};

module.exports = {
  countLevelMissions,
  saveLevelProgress
};
