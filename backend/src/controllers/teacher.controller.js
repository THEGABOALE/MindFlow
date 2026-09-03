const pool = require("../database/connection");

// El docente solo ve la sala que tiene asignada (requireRole ya exige rol
// "teacher"; acá se filtra ademas por teacher_id para que solo vea la suya).
const getMyStudents = async (req, res) => {
  try {
    const groupsResult = await pool.query(
      `
      SELECT id, name, grade, section, school_year, level_id
      FROM class_groups
      WHERE teacher_id = $1 AND is_active = TRUE
      ORDER BY school_year DESC, name ASC;
      `,
      [req.user.id]
    );

    const rooms = [];

    for (const group of groupsResult.rows) {
      const studentsResult = await pool.query(
        `
        SELECT
          u.id,
          u.full_name,
          COALESCE(SUM(a.points_earned), 0) AS total_points,
          COUNT(a.id) FILTER (WHERE a.status = 'completed' AND a.is_review = FALSE) AS missions_completed,
          MAX(a.finished_at) AS last_attempt_at,
          (
            SELECT EXTRACT(EPOCH FROM (a2.finished_at - a2.started_at))
            FROM mission_attempts a2
            WHERE a2.user_id = u.id AND a2.finished_at IS NOT NULL
            ORDER BY a2.finished_at DESC
            LIMIT 1
          ) AS last_attempt_seconds
        FROM users u
        JOIN student_group_enrollments sge ON sge.user_id = u.id
        LEFT JOIN mission_attempts a ON a.user_id = u.id
        WHERE sge.group_id = $1 AND sge.is_active = TRUE AND u.is_active = TRUE
        GROUP BY u.id, u.full_name
        ORDER BY u.full_name ASC;
        `,
        [group.id]
      );

      // Promedio de aciertos por mecanica, para el resumen de la sala
      // ("Cuestionario verdadero o falso", "Relación de conceptos", etc).
      const resultsByMechanicResult = await pool.query(
        `
        SELECT m.mechanic, ROUND(AVG(a.score)) AS average_score
        FROM mission_attempts a
        JOIN missions m ON m.id = a.mission_id
        JOIN student_group_enrollments sge ON sge.user_id = a.user_id
        WHERE sge.group_id = $1
          AND sge.is_active = TRUE
          AND a.status = 'completed'
          AND a.is_review = FALSE
        GROUP BY m.mechanic;
        `,
        [group.id]
      );

      rooms.push({
        id: group.id,
        name: group.name,
        grade: group.grade,
        section: group.section,
        schoolYear: group.school_year,
        levelId: group.level_id,
        studentCount: studentsResult.rows.length,
        students: studentsResult.rows.map((student) => ({
          id: student.id,
          fullName: student.full_name,
          totalPoints: Number(student.total_points),
          missionsCompleted: Number(student.missions_completed),
          lastAttemptAt: student.last_attempt_at,
          lastAttemptSeconds: student.last_attempt_seconds !== null
            ? Math.round(Number(student.last_attempt_seconds))
            : null
        })),
        resultsByMechanic: resultsByMechanicResult.rows.map((row) => ({
          mechanic: row.mechanic,
          averageScore: Number(row.average_score)
        }))
      });
    }

    return res.status(200).json({
      message: "Estudiantes obtenidos exitosamente",
      status: "OK",
      rooms
    });
  } catch (error) {
    return res.status(500).json({
      message: "Error al obtener los estudiantes del docente",
      status: "ERROR",
      error: error.message
    });
  }
};

module.exports = {
  getMyStudents
};
