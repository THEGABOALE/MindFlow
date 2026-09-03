const pool = require("../database/connection");

// Progreso general de todas las salas de un centro: cuantos alumnos tiene
// cada una, quien es su docente y el avance promedio en el nivel de esa sala.
// Lo usan tanto el coordinador (solo su propio centro) como el admin
// (cualquier centro), asi que vive en un solo lugar.
const getCenterRoomsOverview = async (centerId) => {
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
    [centerId]
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

  return {
    totalStudents: rooms.reduce((sum, room) => sum + room.studentCount, 0),
    rooms
  };
};

module.exports = {
  getCenterRoomsOverview
};
