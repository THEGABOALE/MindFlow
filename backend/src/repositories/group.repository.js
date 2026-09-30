// Salas, codigos de acceso y matriculas. Todas las funciones reciben la
// conexion (el pool o el client de una transaccion).

// Sala activa del estudiante; si tiene de varios años, la mas reciente.
const findActiveGroup = async (db, userId) => {
  const result = await db.query(
    `
    SELECT cg.id, cg.name, cg.grade, cg.section, cg.school_year, cg.level_id
    FROM student_group_enrollments sge
    JOIN class_groups cg ON cg.id = sge.group_id
    WHERE sge.user_id = $1
      AND sge.is_active = TRUE
      AND cg.is_active = TRUE
    ORDER BY cg.school_year DESC
    LIMIT 1;
    `,
    [userId]
  );

  return result.rows[0] || null;
};

// El codigo con su sala y su nivel, solo si todavia se puede usar: activo, de
// una sala activa, sin vencer y sin haber llegado a su limite de usos.
const findUsableAccessCode = async (db, code) => {
  const result = await db.query(
    `
    SELECT
      gac.id AS code_id,
      gac.code,
      gac.group_id,
      gac.expires_at,
      gac.max_uses,
      gac.current_uses,
      gac.is_active AS code_is_active,
      cg.name AS group_name,
      cg.grade,
      cg.section,
      cg.school_year,
      cg.center_id,
      cg.is_active AS group_is_active,
      el.id AS level_id,
      el.name AS level_name,
      el.code AS level_code,
      el.description AS level_description
    FROM group_access_codes gac
    JOIN class_groups cg ON cg.id = gac.group_id
    JOIN educational_levels el ON el.id = cg.level_id
    WHERE gac.code = $1
      AND gac.is_active = TRUE
      AND cg.is_active = TRUE
      AND (gac.expires_at IS NULL OR gac.expires_at > CURRENT_TIMESTAMP)
      AND (gac.max_uses IS NULL OR gac.current_uses < gac.max_uses)
    LIMIT 1;
    `,
    [code]
  );

  return result.rows[0] || null;
};

// Sala activa en la que el estudiante ya esta matriculado ese año lectivo.
const findEnrollmentForYear = async (db, userId, schoolYear) => {
  const result = await db.query(
    `
    SELECT sge.group_id, cg.name AS group_name
    FROM student_group_enrollments sge
    JOIN class_groups cg ON cg.id = sge.group_id
    WHERE sge.user_id = $1
      AND sge.is_active = TRUE
      AND cg.is_active = TRUE
      AND cg.school_year = $2
    LIMIT 1;
    `,
    [userId, schoolYear]
  );

  return result.rows[0] || null;
};

// Si ya habia una matricula inactiva en esa sala, se reactiva.
const enrollStudent = async (db, userId, groupId) => {
  await db.query(
    `
    INSERT INTO student_group_enrollments (user_id, group_id)
    VALUES ($1, $2)
    ON CONFLICT (user_id, group_id) DO UPDATE SET is_active = TRUE;
    `,
    [userId, groupId]
  );
};

const incrementCodeUses = async (db, codeId) => {
  await db.query(
    `
    UPDATE group_access_codes
    SET current_uses = current_uses + 1
    WHERE id = $1;
    `,
    [codeId]
  );
};

// El estudiante activo con su sala activa y el nivel de esa sala. Trae tambien
// el docente y el centro de la sala, que son los que deciden quien puede verlo.
const findStudentContext = async (db, studentId) => {
  const result = await db.query(
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
  );

  return result.rows[0] || null;
};

// Salas activas que tiene asignadas un docente.
const findGroupsByTeacher = async (db, teacherId) => {
  const result = await db.query(
    `
    SELECT id, name, grade, section, school_year, level_id
    FROM class_groups
    WHERE teacher_id = $1 AND is_active = TRUE
    ORDER BY school_year DESC, name ASC;
    `,
    [teacherId]
  );

  return result.rows;
};

// Estudiantes activos de una sala con sus semillas, misiones completadas y
// cuando y cuanto duro su ultimo intento.
const findStudentsSummary = async (db, groupId) => {
  const result = await db.query(
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
    [groupId]
  );

  return result.rows;
};

// Promedio de aciertos de la sala por mecanica, sin contar repasos.
const findAverageScoreByMechanic = async (db, groupId) => {
  const result = await db.query(
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
    [groupId]
  );

  return result.rows;
};

// Salas activas de un centro con su docente, cuantos alumnos tienen y el
// avance promedio en el nivel de la sala.
const findCenterRooms = async (db, centerId) => {
  const result = await db.query(
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

  return result.rows;
};

module.exports = {
  findStudentContext,
  findGroupsByTeacher,
  findStudentsSummary,
  findAverageScoreByMechanic,
  findCenterRooms,
  findActiveGroup,
  findUsableAccessCode,
  findEnrollmentForYear,
  enrollStudent,
  incrementCodeUses
};
