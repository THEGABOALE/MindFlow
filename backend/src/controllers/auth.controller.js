const pool = require("../database/connection");
const { withTransaction } = require("../database/transaction");
const userRepository = require("../repositories/user.repository");
const groupRepository = require("../repositories/group.repository");
const { signSessionToken } = require("../utils/jwt");
const { hashPassword, passwordMatches } = require("../utils/password");
const { isGoogleLoginConfigured, verifyGoogleIdToken } = require("../utils/google-token");
const { respondServerError } = require("../utils/server-error");

// Sala activa del estudiante. Si viene null, la app le muestra la pantalla
// del código; si ya tiene sala, entra directo al home aunque haya cerrado
// sesión antes, porque la matrícula vive en la base y no en el teléfono.
const findSessionGroup = (user) =>
  user.role_name === "student" ? groupRepository.findActiveGroup(pool, user.id) : null;

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
  const group = await findSessionGroup(user);

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

  if (!isGoogleLoginConfigured()) {
    return res.status(500).json({
      message: "El servidor no tiene configurado GOOGLE_CLIENT_ID",
      status: "ERROR"
    });
  }

  let payload;

  try {
    payload = await verifyGoogleIdToken(idToken);
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
    const user = await userRepository.findUserForLogin(pool, { email: payload.email });

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
    return respondServerError(res, "Error al iniciar sesión con Google", error);
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
    const user = await userRepository.findUserForLogin(pool, { loginId: loginId.trim().toLowerCase() });

    // Mismo mensaje genérico si el ID no existe o la contraseña no coincide.
    if (!user || !user.password_hash) {
      return res.status(401).json({
        message: "ID o contraseña incorrectos",
        status: "ERROR"
      });
    }

    if (!(await passwordMatches(password, user.password_hash))) {
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
    return respondServerError(res, "Error al iniciar sesión", error);
  }
};

// Le permite a la app validar el token guardado y saber a que home
// mandar al usuario (estudiante, docente, coordinador o admin).
const getMe = async (req, res) => {
  try {
    const user = await userRepository.findUserById(pool, req.user.id);

    if (!user || !user.is_active) {
      return res.status(401).json({
        message: "La sesión ya no es válida",
        status: "ERROR"
      });
    }

    const group = await findSessionGroup(user);

    return res.status(200).json({
      message: "OK",
      status: "OK",
      user: buildSessionUser(user, group)
    });
  } catch (error) {
    return respondServerError(res, "Error al obtener la sesión", error);
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

  try {
    const outcome = await withTransaction(async (db) => {
      const normalizedLoginId = loginId.trim().toLowerCase();

      if (await userRepository.loginIdExists(db, normalizedLoginId)) {
        return { httpStatus: 409, body: { message: "Ya existe una cuenta con ese ID", status: "ERROR" } };
      }

      const roleId = await userRepository.findRoleId(db, requestedRole);

      if (roleId === null) {
        return { httpStatus: 400, body: { message: `No existe el rol "${requestedRole}"`, status: "ERROR" } };
      }

      const created = await userRepository.createIdAccount(db, {
        fullName: fullName.trim(),
        loginId: normalizedLoginId,
        passwordHash: await hashPassword(password),
        roleId,
        // La cuenta nueva queda en el mismo centro que quien la crea (ya viene
        // fresco de la base gracias a authenticate).
        centerId: req.user.centerId || null,
        createdBy: req.user.id
      });

      return {
        httpStatus: 201,
        body: {
          message: "Cuenta creada correctamente",
          status: "OK",
          user: {
            id: created.id,
            fullName: created.full_name,
            loginId: created.login_id,
            role: requestedRole,
            centerId: created.center_id
          }
        }
      };
    });

    return res.status(outcome.httpStatus).json(outcome.body);
  } catch (error) {
    return respondServerError(res, "Error al crear la cuenta", error);
  }
};

module.exports = {
  loginWithGoogle,
  loginWithId,
  getMe,
  createIdAccount
};
