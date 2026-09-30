// Centros educativos. Las funciones reciben la conexion (el pool o el client
// de una transaccion).

const findActiveCenter = async (db, centerId) => {
  const result = await db.query(
    "SELECT id, name FROM educational_centers WHERE id = $1 AND is_active = TRUE LIMIT 1;",
    [centerId]
  );

  return result.rows[0] || null;
};

// Cada centro activo con cuantas salas y estudiantes tiene y su avance promedio.
const findCentersOverview = async (db) => {
  const result = await db.query(
    `
    SELECT
      ec.id,
      ec.name,
      it.name AS institution_type,
      ec.department,
      ec.municipality,
      COUNT(DISTINCT cg.id) AS room_count,
      COUNT(DISTINCT sge.user_id) AS student_count,
      COALESCE(AVG(lp.progress_percentage), 0) AS average_progress
    FROM educational_centers ec
    LEFT JOIN institution_types it ON it.id = ec.institution_type_id
    LEFT JOIN class_groups cg ON cg.center_id = ec.id AND cg.is_active = TRUE
    LEFT JOIN student_group_enrollments sge ON sge.group_id = cg.id AND sge.is_active = TRUE
    LEFT JOIN level_progress lp ON lp.user_id = sge.user_id AND lp.level_id = cg.level_id
    WHERE ec.is_active = TRUE
    GROUP BY ec.id, it.name
    ORDER BY ec.name ASC;
    `
  );

  return result.rows;
};

module.exports = {
  findActiveCenter,
  findCentersOverview
};
