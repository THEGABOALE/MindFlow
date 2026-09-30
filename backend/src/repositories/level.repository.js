// Niveles educativos. Las funciones reciben la conexion (el pool o el client
// de una transaccion).

// Una fila por mision publicada, con los datos de su nivel; un nivel sin
// misiones publicadas sale una vez con los campos de la mision en null.
const findLevelsWithMissions = async (db) => {
  const result = await db.query(`
    SELECT
      el.id AS level_id,
      el.name AS level_name,
      el.code AS level_code,
      el.description AS level_description,
      el.order_index AS level_order,
      m.id AS mission_id,
      m.title AS mission_title,
      m.description AS mission_description,
      m.topic AS mission_topic,
      m.order_index AS mission_order,
      m.points_reward,
      m.mechanic,
      m.time_limit_seconds,
      m.max_plumas,
      m.is_published
    FROM educational_levels el
    LEFT JOIN missions m
      ON m.level_id = el.id
      AND m.is_published = TRUE
    ORDER BY el.order_index ASC, m.order_index ASC;
  `);

  return result.rows;
};

module.exports = {
  findLevelsWithMissions
};
