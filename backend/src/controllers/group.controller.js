const pool = require("../database/connection"); // Import the database connection pool

const joinGroupByCode = async (req, res) => { // Function to handle joining a group by the access code
  const { code, studentName } = req.body || {}; // Destructure the code and studentName from the request body

  if (!code) { // Check if the code is provided
    return res.status(400).json({ // Return a 400 error if the code is missing
      message: "El código del grupo es obligatorio",
      status: "ERROR"
    });
  }

  const client = await pool.connect(); // Get a client from the connection pool

  try { // Start the transaction
    await client.query("BEGIN"); // Begin a transaction

    const codeResult = await client.query( // Query the database for the access code and related group and level information
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
      AND gac.is_active = TRUE
      AND cg.is_active = TRUE
      AND (gac.expires_at IS NULL OR gac.expires_at > CURRENT_TIMESTAMP)
      AND (gac.max_uses IS NULL OR gac.current_uses < gac.max_uses)
    LIMIT 1;
    `,
      [code.trim().toUpperCase()] // Use the provided code, trimmed and converted to uppercase, as a parameter for the query
    );

    if (codeResult.rows.length === 0) { // Check if the access code exists in the database and return a 404 error if the code does not exist
      await client.query("ROLLBACK");

      return res.status(404).json({
        message: "El código no es válido, expiró o alcanzó su límite de usos",
        status: "ERROR"
      });
    }

    const accessCode = codeResult.rows[0]; // Store the access code information in a variable

    if (!accessCode.code_is_active || !accessCode.group_is_active) { // if the access code or the group is not active, return a 404 error
      await client.query("ROLLBACK");

      return res.status(400).json({
        message: "El código del grupo no está activo",
        status: "ERROR"
      });
    }

    if (accessCode.expires_at && new Date(accessCode.expires_at) < new Date()) { // If the access code has an expiration date and it has passed, return a 404 error
      await client.query("ROLLBACK");

      return res.status(400).json({
        message: "El código del grupo ha expirado",
        status: "ERROR"
      });
    }

    if (
      accessCode.max_uses !== null &&
      accessCode.current_uses >= accessCode.max_uses
    ) { // If the access code has a maximum number of uses, and the current number of uses has reached that maximum, return a 404 error
      await client.query("ROLLBACK");

      return res.status(400).json({
        message: "El código del grupo alcanzó el número máximo de usos",
        status: "ERROR"
      });
    }

    const roleResult = await client.query( // Query the database for the role ID of the student role, which is needed to create a new student user
      "SELECT id FROM roles WHERE name = 'student' LIMIT 1;"
    );

    if (roleResult.rows.length === 0) { // If the student role does not exist in the database, return a 500 error
      await client.query("ROLLBACK");

      return res.status(500).json({
        message: "No existe el rol student en la base de datos",
        status: "ERROR"
      }); 
    }

    const studentRoleId = roleResult.rows[0].id; // Store the role ID of the student role in a variable
    const finalStudentName = studentName?.trim() || "Estudiante Demo"; // Use the provided student name, trimmed, or a default name if not provided

    const userResult = await client.query( // Insert a new 
      `
      INSERT INTO users (full_name, email, password_hash, role_id)
      VALUES ($1, NULL, NULL, $2)
      RETURNING id, full_name, email, role_id;
      `,
      [finalStudentName, studentRoleId] // Use the final student name and the role ID of the student role as parameters for the query
    );

    const student = userResult.rows[0]; // Store the newly created student user information in a variable

    await client.query( // Insert a new record into the student_group_enrollments table to associate the student with the group
      `
      INSERT INTO student_group_enrollments (user_id, group_id)
      VALUES ($1, $2)
      ON CONFLICT (user_id, group_id) DO NOTHING;
      `,
      [student.id, accessCode.group_id] // Use the student user ID and the group ID from the access code as parameters for the query
    );

    await client.query( // Update the current uses of the access code in the group_access_codes table to reflect that a new student has joined the group
      `
      UPDATE group_access_codes
      SET current_uses = current_uses + 1
      WHERE id = $1;
      `,
      [accessCode.code_id] 
    );

    await client.query("COMMIT"); // Commit the transaction to save the changes to the database

    return res.status(201).json({ // Return a 201 status code and a success message along with the student, group, and level information
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
  } catch (error) { // If any error occurs during the transaction, rollback the transaction and return a 500 error with the error message
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

module.exports = { // Export the JoinGroupByCode function to be used in other parts of the application
  joinGroupByCode
};