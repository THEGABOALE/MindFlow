const pool = require("../database/connection");

const joinGroupByCode = async (req, res) => {
  const { code, studentName } = req.body || {};

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
        cg.is_active AS group_is_active,
        el.id AS level_id,
        el.name AS level_name,
        el.code AS level_code,
        el.description AS level_description
      FROM group_access_codes gac
      JOIN class_groups cg ON cg.id = gac.group_id
      JOIN educational_levels el ON el.id = cg.level_id
      WHERE gac.code = $1
      LIMIT 1;
      `,
      [code.trim().toUpperCase()]
    );

    if (codeResult.rows.length === 0) {
      await client.query("ROLLBACK");

      return res.status(404).json({
        message: "El código del grupo no existe",
        status: "ERROR"
      });
    }

    const accessCode = codeResult.rows[0];

    if (!accessCode.code_is_active || !accessCode.group_is_active) {
      await client.query("ROLLBACK");

      return res.status(400).json({
        message: "El código del grupo no está activo",
        status: "ERROR"
      });
    }

    if (accessCode.expires_at && new Date(accessCode.expires_at) < new Date()) {
      await client.query("ROLLBACK");

      return res.status(400).json({
        message: "El código del grupo ha expirado",
        status: "ERROR"
      });
    }

    if (
      accessCode.max_uses !== null &&
      accessCode.current_uses >= accessCode.max_uses
    ) {
      await client.query("ROLLBACK");

      return res.status(400).json({
        message: "El código del grupo alcanzó el número máximo de usos",
        status: "ERROR"
      });
    }

    const roleResult = await client.query(
      "SELECT id FROM roles WHERE name = 'student' LIMIT 1;"
    );

    if (roleResult.rows.length === 0) {
      await client.query("ROLLBACK");

      return res.status(500).json({
        message: "No existe el rol student en la base de datos",
        status: "ERROR"
      });
    }

    const studentRoleId = roleResult.rows[0].id;
    const finalStudentName = studentName?.trim() || "Estudiante Demo";

    const userResult = await client.query(
      `
      INSERT INTO users (full_name, email, password_hash, role_id)
      VALUES ($1, NULL, NULL, $2)
      RETURNING id, full_name, email, role_id;
      `,
      [finalStudentName, studentRoleId]
    );

    const student = userResult.rows[0];

    await client.query(
      `
      INSERT INTO student_group_enrollments (user_id, group_id)
      VALUES ($1, $2)
      ON CONFLICT (user_id, group_id) DO NOTHING;
      `,
      [student.id, accessCode.group_id]
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

    return res.status(201).json({
      message: "Estudiante agregado al grupo exitosamente",
      status: "OK",
      student: {
        id: student.id,
        fullName: student.full_name,
        email: student.email,
        roleId: student.role_id
      },
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