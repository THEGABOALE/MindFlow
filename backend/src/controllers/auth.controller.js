const bcrypt = require("bcryptjs");
const { OAuth2Client } = require("google-auth-library");
const pool = require("../database/connection");
const env = require("../config/env");
const { signSessionToken } = require("../utils/jwt");

const googleClient = new OAuth2Client(env.auth.googleClientId);

const SALT_ROUNDS = 10;

const findUserForLogin = async ({ email, loginId }) => {
  const result = await pool.query(
    `
    SELECT
      u.id,
      u.full_name,
      u.email,
      u.login_id,
      u.password_hash,
      u.center_id,
      u.is_active,
      r.name AS role_name
    FROM users u
    JOIN roles r ON r.id = u.role_id
    WHERE ($1::VARCHAR IS NOT NULL AND u.email = $1)
       OR ($2::VARCHAR IS NOT NULL AND u.login_id = $2)
    LIMIT 1;
    `,
    [email || null, loginId || null]
  );

  return result.rows[0] || null;
};

const findUserById = async (id) => {
  const result = await pool.query(
    `
    SELECT
      u.id,
      u.full_name,
      u.email,
      u.login_id,
      u.center_id,
      u.is_active,
      r.name AS role_name
    FROM users u
    JOIN roles r ON r.id = u.role_id
    WHERE u.id = $1
    LIMIT 1;
    `,
    [id]
  );

  return result.rows[0] || null;
};

// Sala activa del estudiante. Si viene null, la app le muestra la pantalla
// del código; si ya tiene sala, entra directo al home aunque haya cerrado
// sesión antes, porque la matrícula vive en la base y no en el teléfono.
const findActiveGroup = async (userId) => {
  const result = await pool.query(
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

const buildSessionUser = (user, group) => ({
  id: user.id,
  fullName: user.full_name,
  email: user.email,
  loginId: user.login_id,
  role: user.role_name,
  centerId: user.center_id,
  group: group && {
    id: group.id,
    name: group.name,
    grade: group.grade,
    section: group.section,
    schoolYear: group.school_year,
    levelId: group.level_id
  }
});

const buildSessionResponse = async (user) => {
  const group = user.role_name === "student"
    ? await findActiveGroup(user.id)
    : null;

  return {
    message: "Sesión iniciada correctamente",
    status: "OK",
    token: signSessionToken(user.id),
    user: buildSessionUser(user, group)
  };
};

const loginWithGoogle = async (req, res) => {
  const { idToken } = req.body || {};

  if (!idToken) {
    return res.status(400).json({
      message: "El idToken de Google es obligatorio",
      status: "ERROR"
    });
  }

  if (!env.auth.googleClientId) {
    return res.status(500).json({
      message: "El servidor no tiene configurado GOOGLE_CLIENT_ID",
      status: "ERROR"
    });
  }

  let payload;

  try {
    const ticket = await googleClient.verifyIdToken({
      idToken,
      audience: env.auth.googleClientId
    });

    payload = ticket.getPayload();
  } catch (error) {
    return res.status(401).json({
      message: "El token de Google no es válido",
      status: "ERROR"
    });
  }

  if (!payload || !payload.email) {
    return res.status(401).json({
      message: "El token de Google no es válido",
      status: "ERROR"
    });
  }

  if (!payload.email_verified) {
    return res.status(401).json({
      message: "El correo de Google no está verificado",
      status: "ERROR"
    });
  }

  try {
    const user = await findUserForLogin({ email: payload.email });

    if (!user) {
      // La institución tiene que haber registrado el correo de antemano; el
      // rol lo define siempre la base de datos, nunca el cliente.
      return res.status(404).json({
        message: "Ese correo no está registrado en ninguna institución. Contactá a tu coordinador.",
        status: "ERROR"
      });
    }

    if (!user.is_active) {
      return res.status(403).json({
        message: "Esta cuenta está desactivada",
        status: "ERROR"
      });
    }

    return res.status(200).json(await buildSessionResponse(user));
  } catch (error) {
    return res.status(500).json({
      message: "Error al iniciar sesión con Google",
      status: "ERROR",
      error: error.message
    });
  }
};

const loginWithId = async (req, res) => {
  const { loginId, password } = req.body || {};

  if (!loginId || !password) {
    return res.status(400).json({
      message: "El ID y la contraseña son obligatorios",
      status: "ERROR"
    });
  }

  try {
    // El ID se guarda en minúsculas al crear la cuenta, así que se normaliza igual acá.
    const user = await findUserForLogin({ loginId: loginId.trim().toLowerCase() });

    // Mismo mensaje genérico si el ID no existe o la contraseña no coincide.
    if (!user || !user.password_hash) {
      return res.status(401).json({
        message: "ID o contraseña incorrectos",
        status: "ERROR"
      });
    }

    const passwordMatches = await bcrypt.compare(password, user.password_hash);

    if (!passwordMatches) {
      return res.status(401).json({
        message: "ID o contraseña incorrectos",
        status: "ERROR"
      });
    }

    if (!user.is_active) {
      return res.status(403).json({
        message: "Esta cuenta está desactivada",
        status: "ERROR"
      });
    }

    return res.status(200).json(await buildSessionResponse(user));
  } catch (error) {
    return res.status(500).json({
      message: "Error al iniciar sesión",
      status: "ERROR",
      error: error.message
    });
  }
};

// Le permite a la app validar el token guardado y saber a que home
// mandar al usuario (estudiante, docente, coordinador o admin).
const getMe = async (req, res) => {
  try {
    const user = await findUserById(req.user.id);

    if (!user || !user.is_active) {
      return res.status(401).json({
        message: "La sesión ya no es válida",
        status: "ERROR"
      });
    }

    const group = user.role_name === "student"
      ? await findActiveGroup(user.id)
      : null;

    return res.status(200).json({
      message: "OK",
      status: "OK",
      user: buildSessionUser(user, group)
    });
  } catch (error) {
    return res.status(500).json({
      message: "Error al obtener la sesión",
      status: "ERROR",
      error: error.message
    });
  }
};

// Un coordinador no puede crear coordinadores ni admins, para no poder
// escalar sus propios permisos.
const CREATABLE_ROLES_BY_ROLE = {
  coordinator: ["student", "teacher"],
  admin: ["student", "teacher", "coordinator", "admin", "validator"]
};

const createIdAccount = async (req, res) => {
  const { fullName, loginId, password, roleName } = req.body || {};

  if (!fullName || !loginId || !password) {
    return res.status(400).json({
      message: "fullName, loginId y password son obligatorios",
      status: "ERROR"
    });
  }

  if (password.length < 4) {
    return res.status(400).json({
      message: "La contraseña debe tener al menos 4 caracteres",
      status: "ERROR"
    });
  }

  const requestedRole = roleName || "student";
  const allowedRoles = CREATABLE_ROLES_BY_ROLE[req.user.role] || [];

  if (!allowedRoles.includes(requestedRole)) {
    return res.status(403).json({
      message: `No podés crear cuentas con el rol "${requestedRole}"`,
      status: "ERROR"
    });
  }

  const client = await pool.connect();

  try {
    await client.query("BEGIN");

    const normalizedLoginId = loginId.trim().toLowerCase();

    const existing = await client.query(
      "SELECT id FROM users WHERE login_id = $1 LIMIT 1;",
      [normalizedLoginId]
    );

    if (existing.rows.length > 0) {
      await client.query("ROLLBACK");

      return res.status(409).json({
        message: "Ya existe una cuenta con ese ID",
        status: "ERROR"
      });
    }

    const roleResult = await client.query(
      "SELECT id FROM roles WHERE name = $1 LIMIT 1;",
      [requestedRole]
    );

    if (roleResult.rows.length === 0) {
      await client.query("ROLLBACK");

      return res.status(400).json({
        message: `No existe el rol "${requestedRole}"`,
        status: "ERROR"
      });
    }

    // La cuenta nueva queda en el mismo centro que quien la crea (ya viene
    // fresco de la base gracias a authenticate).
    const centerId = req.user.centerId || null;

    const passwordHash = await bcrypt.hash(password, SALT_ROUNDS);

    const insertResult = await client.query(
      `
      INSERT INTO users (full_name, login_id, password_hash, role_id, center_id, created_by)
      VALUES ($1, $2, $3, $4, $5, $6)
      RETURNING id, full_name, login_id, center_id;
      `,
      [
        fullName.trim(),
        normalizedLoginId,
        passwordHash,
        roleResult.rows[0].id,
        centerId,
        req.user.id
      ]
    );

    await client.query("COMMIT");

    const created = insertResult.rows[0];

    return res.status(201).json({
      message: "Cuenta creada correctamente",
      status: "OK",
      user: {
        id: created.id,
        fullName: created.full_name,
        loginId: created.login_id,
        role: requestedRole,
        centerId: created.center_id
      }
    });
  } catch (error) {
    await client.query("ROLLBACK");

    return res.status(500).json({
      message: "Error al crear la cuenta",
      status: "ERROR",
      error: error.message
    });
  } finally {
    client.release();
  }
};

module.exports = {
  loginWithGoogle,
  loginWithId,
  getMe,
  createIdAccount
};
