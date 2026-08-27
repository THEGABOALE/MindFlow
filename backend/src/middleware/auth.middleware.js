const { verifySessionToken } = require("../utils/jwt");

// Exige "Authorization: Bearer <token>" y deja el usuario en req.user.
const authenticate = (req, res, next) => {
  const authHeader = req.headers.authorization || "";
  const [scheme, token] = authHeader.split(" ");

  if (scheme !== "Bearer" || !token) {
    return res.status(401).json({
      message: "Falta el token de sesión",
      status: "ERROR"
    });
  }

  try {
    const payload = verifySessionToken(token);

    req.user = {
      id: payload.sub,
      role: payload.role
    };

    next();
  } catch (error) {
    return res.status(401).json({
      message: "Token de sesión inválido o expirado",
      status: "ERROR"
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
