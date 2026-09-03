const { verifySessionToken } = require("../utils/jwt");
const pool = require("../database/connection");

// Exige "Authorization: Bearer <token>" y deja el usuario en req.user.
// El rol, el centro y si la cuenta sigue activa se leen de la base en cada
// peticion (el token solo prueba identidad, no lleva esos datos), asi un
// cambio de rol, de centro o una desactivacion aplican de inmediato.
const authenticate = async (req, res, next) => {
  const authHeader = req.headers.authorization || "";
  const [scheme, token] = authHeader.split(" ");

  if (scheme !== "Bearer" || !token) {
    return res.status(401).json({
      message: "Falta el token de sesión",
      status: "ERROR"
    });
  }

  let payload;

  try {
    payload = verifySessionToken(token);
  } catch (error) {
    return res.status(401).json({
      message: "Token de sesión inválido o expirado",
      status: "ERROR"
    });
  }

  try {
    const result = await pool.query(
      `
      SELECT u.id, u.center_id, u.is_active, r.name AS role_name
      FROM users u
      JOIN roles r ON r.id = u.role_id
      WHERE u.id = $1
      LIMIT 1;
      `,
      [payload.sub]
    );

    const user = result.rows[0];

    if (!user || !user.is_active) {
      return res.status(401).json({
        message: "La sesión ya no es válida",
        status: "ERROR"
      });
    }

    req.user = {
      id: user.id,
      role: user.role_name,
      centerId: user.center_id
    };

    next();
  } catch (error) {
    return res.status(500).json({
      message: "Error al validar la sesión",
      status: "ERROR",
      error: error.message
    });
  }
};

// Exige que req.user tenga uno de los roles permitidos.
const requireRole = (...allowedRoles) => {
  return (req, res, next) => {
    if (!req.user || !allowedRoles.includes(req.user.role)) {
      return res.status(403).json({
        message: "No tenés permiso para realizar esta acción",
        status: "ERROR"
      });
    }

    next();
  };
};

module.exports = {
  authenticate,
  requireRole
};
