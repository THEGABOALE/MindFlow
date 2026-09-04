const pool = require("../database/connection");

// Matricula en una sala al estudiante que ya inició sesión. El código NO crea
// cuentas: primero la persona se loguea (Google o ID) y recién ahí usa el
// código que le dio su colegio. Así, si cierra sesión y vuelve a entrar, la
// matrícula sigue guardada y no tiene que poner el código de nuevo.
const joinGroupByCode = async (req, res) => {
  const { code } = req.body || {};

  if (!code) {
    return res.status(400).json({
      message: "El código del grupo es obligatorio",
      status: "ERROR"
    });
  }

  const client = await pool.connect();

  try {
    await client.query("BEGIN");

    const codeResult = await client.query(
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
      [code.trim().toUpperCase()]
    );

    if (codeResult.rows.length === 0) {
      await client.query("ROLLBACK");

      return res.status(404).json({
        message: "El código no es válido, expiró o alcanzó su límite de usos",
        status: "ERROR"
      });
    }

    const accessCode = codeResult.rows[0];

    const buildResponse = (alreadyEnrolled) => ({
      message: alreadyEnrolled
        ? "Ya estabas en esta sala"
        : "Te uniste a la sala exitosamente",
      status: "OK",
      alreadyEnrolled,
      group: {
        id: accessCode.group_id,
        name: accessCode.group_name,
        grade: accessCode.grade,
        section: accessCode.section,
        schoolYear: accessCode.school_year
      },
      level: {
        id: accessCode.level_id,
        name: accessCode.level_name,
        code: accessCode.level_code,
        description: accessCode.level_description
      }
    });

    // Si ya está en otra sala del mismo año lectivo, no se cambia solo: un
    // traslado de sección lo tiene que hacer el docente o el coordinador.
    const enrollmentResult = await client.query(
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
      [req.user.id, accessCode.school_year]
    );

    const currentEnrollment = enrollmentResult.rows[0];

    if (currentEnrollment) {
      await client.query("COMMIT");

      if (currentEnrollment.group_id === accessCode.group_id) {
        return res.status(200).json(buildResponse(true));
      }

      return res.status(409).json({
        message: `Ya pertenecés a la sala "${currentEnrollment.group_name}" este año. Pedile a tu docente que te traslade.`,
        status: "ERROR"
      });
    }

    await client.query(
      `
      INSERT INTO student_group_enrollments (user_id, group_id)
      VALUES ($1, $2)
      ON CONFLICT (user_id, group_id) DO UPDATE SET is_active = TRUE;
      `,
      [req.user.id, accessCode.group_id]
    );

    // El estudiante queda ligado al centro de su sala si todavía no lo estaba.
    await client.query(
      "UPDATE users SET center_id = $1 WHERE id = $2 AND center_id IS NULL;",
      [accessCode.center_id, req.user.id]
    );

    await client.query(
      `
      UPDATE group_access_codes
      SET current_uses = current_uses + 1
      WHERE id = $1;
      `,
      [accessCode.code_id]
    );

    await client.query("COMMIT");

    return res.status(201).json(buildResponse(false));
  } catch (error) {
    await client.query("ROLLBACK");

    return res.status(500).json({
      message: "Error al unir estudiante al grupo",
      status: "ERROR",
      error: error.message
    });
  } finally {
    client.release();
  }
};

module.exports = {
  joinGroupByCode
};
