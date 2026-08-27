const { verifySessionToken } = require("../utils/jwt");

// Exige un token de sesión válido (header "Authorization: Bearer <token>") y
// deja el usuario decodificado en req.user para los controladores siguientes.
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

// Exige que req.user (ya autenticado con `authenticate`) tenga uno de los
// roles permitidos. Uso: router.post("/ruta", authenticate, requireRole("admin"), controller)
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
