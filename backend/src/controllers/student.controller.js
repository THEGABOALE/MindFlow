const pool = require("../database/connection"); // importar la conexion a la base de datos

// El estudiante solo se ve a si mismo, el profesor solo a su sala y el
// coordinador solo a su centro. El admin (equipo MindFlow) ve todo.
const canViewStudent = (requester, row) => {
    if (requester.role === "admin") {
        return true;
    }

    if (requester.role === "student") {
        return Number(requester.id) === row.student_id;
    }

    if (requester.role === "teacher") {
        return Number(requester.id) === row.group_teacher_id;
    }

    if (requester.role === "coordinator") {
        return requester.centerId !== null && Number(requester.centerId) === row.group_center_id;
    }

    return false;
};

const getStudentContext = async (req, res) => { // Funcion para obtener el contexto del estudiante
    const { studentId} = req.params;

    if (!studentId) { // Si no se proporciona el studentId, se devuelve un error 404
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
        const result = await pool.query(
            `
        SELECT
            u.id AS student_id,
            u.full_name AS student_full_name,
            cg.id AS group_id,
            cg.name AS group_name,
            cg.grade AS group_grade,
            cg.section AS group_section,
            cg.school_year AS group_school_year,
            cg.teacher_id AS group_teacher_id,
            cg.center_id AS group_center_id,
            el.id AS level_id,
            el.name AS level_name,
            el.code AS level_code,
            el.description AS level_description
        FROM users u
        JOIN student_group_enrollments sge ON sge.user_id = u.id
        JOIN class_groups cg ON cg.id = sge.group_id
        JOIN educational_levels el ON el.id = cg.level_id
        WHERE u.id = $1
            AND u.is_active = TRUE
            AND sge.is_active = TRUE
            AND cg.is_active = TRUE
        LIMIT 1;
        `,
        [studentId]
        ); // Ejecutar la consulta para obtener el contexto del estudiante       
        
        if (result.rows.length === 0){
            return res.status(404).json({
                message: "No se encontró el contexto del estudiante",
                status: "ERROR"
            });
        }

        const row = result.rows[0]; //Se almacena el contexto del estudiante en una variable

        if (!canViewStudent(req.user, row)) {
            return res.status(403).json({
                message: "No tenés permiso para ver este estudiante",
                status: "ERROR"
            });
        }

        return res.status(200).json({ // se devuelve el contexto del estudiante en formato JSON
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
    
    } catch (error) { // Si ocurre un error durante la consulta, se devuelve un error 500
        console.error("Hubo un error al obtener el contexto del estudiante", error);
        return res.status(500).json({
            message: "Hubo un error al obtener el contexto del estutiante",
            status: "ERROR",
            error: error.message
        });
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
        const permResult = await pool.query(
            `
        SELECT
            u.id AS student_id,
            u.full_name AS student_full_name,
            cg.teacher_id AS group_teacher_id,
            cg.center_id AS group_center_id
        FROM users u
        JOIN student_group_enrollments sge ON sge.user_id = u.id
        JOIN class_groups cg ON cg.id = sge.group_id
        WHERE u.id = $1
            AND u.is_active = TRUE
            AND sge.is_active = TRUE
            AND cg.is_active = TRUE
        LIMIT 1;
        `,
            [studentId]
        );

        const row = permResult.rows[0];

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

        const totalsResult = await pool.query(
            `
        SELECT
            COALESCE(SUM(points_earned), 0) AS total_points,
            COUNT(*) FILTER (WHERE status = 'completed' AND is_review = FALSE) AS missions_completed
        FROM mission_attempts
        WHERE user_id = $1;
        `,
            [studentId]
        );

        const levelsResult = await pool.query(
            `
        SELECT
            el.id, el.name, el.code, el.order_index,
            COALESCE(lp.progress_percentage, 0) AS progress_percentage,
            COALESCE(lp.status, 'locked') AS status
        FROM educational_levels el
        LEFT JOIN level_progress lp ON lp.level_id = el.id AND lp.user_id = $1
        ORDER BY el.order_index ASC;
        `,
            [studentId]
        );

        const totals = totalsResult.rows[0];

        return res.status(200).json({
            message: "Progreso del estudiante obtenido exitosamente",
            status: "OK",
            student: {
                id: row.student_id,
                fullName: row.student_full_name,
                totalPoints: Number(totals.total_points),
                missionsCompleted: Number(totals.missions_completed),
                levels: levelsResult.rows.map((level) => ({
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
        return res.status(500).json({
            message: "Error al obtener el progreso del estudiante",
            status: "ERROR",
            error: error.message
        });
    }
};

module.exports = {
    getStudentContext,
    getStudentProgress
};