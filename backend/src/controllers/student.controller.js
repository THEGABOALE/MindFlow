const pool = require("../database/connection");
const groupRepository = require("../repositories/group.repository");
const attemptRepository = require("../repositories/attempt.repository");
const levelProgressRepository = require("../repositories/level-progress.repository");
const { canViewStudent } = require("../services/student-access.service");
const { normalizeTzOffset } = require("../services/streak.service");
const { loadStreak } = require("../services/streak-query.service");
const { respondServerError } = require("../utils/server-error");

const getStudentContext = async (req, res) => {
    const { studentId } = req.params;

    if (!studentId) {
        return res.status(404).json({
            message: "El ID del estudiante no ha sido proporcionado",
            status: "ERROR"
        });
    }

    if (!/^\d+$/.test(studentId)) { // Sin esto un id no numerico revienta la consulta con un 500
        return res.status(400).json({
            message: "El ID del estudiante debe ser numérico",
            status: "ERROR"
        });
    }

    try {
        const row = await groupRepository.findStudentContext(pool, studentId);

        if (!row) {
            return res.status(404).json({
                message: "No se encontró el contexto del estudiante",
                status: "ERROR"
            });
        }

        if (!canViewStudent(req.user, row)) {
            return res.status(403).json({
                message: "No tenés permiso para ver este estudiante",
                status: "ERROR"
            });
        }

        return res.status(200).json({
            message: "Contexto del estudiante obtenido exitosamente",
            status: "OK",
            student: {
                id: row.student_id,
                fullName: row.student_full_name,
                group: {
                    id: row.group_id,
                    name: row.group_name,
                    grade: row.group_grade,
                    section: row.group_section,
                    schoolYear: row.group_school_year
                },
                level: {
                    id: row.level_id,
                    name: row.level_name,
                    code: row.level_code,
                    description: row.level_description
                }
            }
        });
    } catch (error) {
        return respondServerError(res, "Hubo un error al obtener el contexto del estudiante", error);
    }
};

const getStudentProgress = async (req, res) => {
    const { studentId } = req.params;

    if (!/^\d+$/.test(studentId)) {
        return res.status(400).json({
            message: "El ID del estudiante debe ser numérico",
            status: "ERROR"
        });
    }

    try {
        const row = await groupRepository.findStudentContext(pool, studentId);

        if (!row) {
            return res.status(404).json({
                message: "No se encontró el contexto del estudiante",
                status: "ERROR"
            });
        }

        if (!canViewStudent(req.user, row)) {
            return res.status(403).json({
                message: "No tenés permiso para ver este estudiante",
                status: "ERROR"
            });
        }

        const totals = await attemptRepository.findStudentTotals(pool, studentId);
        const levels = await levelProgressRepository.findLevelsWithProgress(pool, studentId);

        // IDs de las misiones ya completadas, para que la app sepa cuales marcar
        // con check y cual es la siguiente desbloqueada sin adivinarlo por posicion.
        const completedMissionIds = await attemptRepository.findCompletedMissionIds(pool, studentId);

        // La app manda el desfase de su huso en tzOffsetMinutes para que el dia
        // de la racha sea el local y no cambie a las 7 de la tarde por usar UTC.
        const { streak } = await loadStreak(pool, studentId, normalizeTzOffset(req.query.tzOffsetMinutes));

        return res.status(200).json({
            message: "Progreso del estudiante obtenido exitosamente",
            status: "OK",
            student: {
                id: row.student_id,
                fullName: row.student_full_name,
                totalPoints: totals.totalPoints,
                missionsCompleted: totals.missionsCompleted,
                streak,
                completedMissionIds,
                levels: levels.map((level) => ({
                    id: level.id,
                    name: level.name,
                    code: level.code,
                    orderIndex: level.order_index,
                    progressPercentage: Number(level.progress_percentage),
                    status: level.status
                }))
            }
        });
    } catch (error) {
        return respondServerError(res, "Error al obtener el progreso del estudiante", error);
    }
};

module.exports = {
    getStudentContext,
    getStudentProgress
};
