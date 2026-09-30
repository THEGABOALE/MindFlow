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

module.exports = {
  findActiveGroup,
  findUsableAccessCode,
  findEnrollmentForYear,
  enrollStudent,
  incrementCodeUses
};
