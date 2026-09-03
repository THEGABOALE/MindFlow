const pool = require("../database/connection");

// El coordinador ve el progreso general de todas las salas de SU centro
// (requireRole ya exige rol "coordinator"; acá se filtra por center_id).
const getCenterOverview = async (req, res) => {
  if (!req.user.centerId) {
    return res.status(400).json({
      message: "Tu cuenta no tiene un centro educativo asignado",
      status: "ERROR"
    });
  }

  try {
    const roomsResult = await pool.query(
      `
      SELECT
        cg.id,
        cg.name,
        cg.grade,
        cg.section,
        cg.school_year,
        cg.level_id,
        u.full_name AS teacher_name,
        COUNT(DISTINCT sge.user_id) AS student_count,
        COALESCE(AVG(lp.progress_percentage), 0) AS average_progress
      FROM class_groups cg
      LEFT JOIN users u ON u.id = cg.teacher_id
      LEFT JOIN student_group_enrollments sge ON sge.group_id = cg.id AND sge.is_active = TRUE
      LEFT JOIN level_progress lp ON lp.user_id = sge.user_id AND lp.level_id = cg.level_id
      WHERE cg.center_id = $1 AND cg.is_active = TRUE
      GROUP BY cg.id, u.full_name
      ORDER BY cg.grade ASC, cg.section ASC;
      `,
      [req.user.centerId]
    );

    const rooms = roomsResult.rows.map((room) => ({
      id: room.id,
      name: room.name,
      grade: room.grade,
      section: room.section,
      schoolYear: room.school_year,
      levelId: room.level_id,
      teacherName: room.teacher_name,
      studentCount: Number(room.student_count),
      averageProgressPercentage: Number(room.average_progress)
    }));

    return res.status(200).json({
      message: "Resumen del centro obtenido exitosamente",
      status: "OK",
      totalStudents: rooms.reduce((sum, room) => sum + room.studentCount, 0),
      rooms
    });
  } catch (error) {
    return res.status(500).json({
      message: "Error al obtener el resumen del centro",
      status: "ERROR",
      error: error.message
    });
  }
};

module.exports = {
  getCenterOverview
};
