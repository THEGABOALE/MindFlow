const pool = require("../database/connection");
const groupRepository = require("../repositories/group.repository");
const { respondServerError } = require("../utils/server-error");

// El docente solo ve la sala que tiene asignada (requireRole ya exige rol
// "teacher"; acá se filtra ademas por teacher_id para que solo vea la suya).
const getMyStudents = async (req, res) => {
  try {
    const groups = await groupRepository.findGroupsByTeacher(pool, req.user.id);

    const rooms = [];

    for (const group of groups) {
      const students = await groupRepository.findStudentsSummary(pool, group.id);

      // Promedio de aciertos por mecanica, para el resumen de la sala
      // ("Cuestionario verdadero o falso", "Relación de conceptos", etc).
      const resultsByMechanic = await groupRepository.findAverageScoreByMechanic(pool, group.id);

      rooms.push({
        id: group.id,
        name: group.name,
        grade: group.grade,
        section: group.section,
        schoolYear: group.school_year,
        levelId: group.level_id,
        studentCount: students.length,
        students: students.map((student) => ({
          id: student.id,
          fullName: student.full_name,
          totalPoints: Number(student.total_points),
          missionsCompleted: Number(student.missions_completed),
          lastAttemptAt: student.last_attempt_at,
          lastAttemptSeconds: student.last_attempt_seconds !== null
            ? Math.round(Number(student.last_attempt_seconds))
            : null
        })),
        resultsByMechanic: resultsByMechanic.map((row) => ({
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
    return respondServerError(res, "Error al obtener los estudiantes del docente", error);
  }
};

module.exports = {
  getMyStudents
};
