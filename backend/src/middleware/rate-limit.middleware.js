const { rateLimit, ipKeyGenerator } = require("express-rate-limit");

const FIFTEEN_MINUTES = 15 * 60 * 1000;

const tooManyAttempts = (req, res) => {
  res.status(429).json({
    message: "Demasiados intentos. Espera unos minutos y vuelve a intentarlo.",
    status: "ERROR"
  });
};

// Solo cuentan los intentos fallidos: entrar bien o usar un código válido no
// gasta el cupo.
const failedAttemptsLimiter = (options) =>
  rateLimit({
    windowMs: FIFTEEN_MINUTES,
    skipSuccessfulRequests: true,
    standardHeaders: "draft-8",
    legacyHeaders: false,
    handler: tooManyAttempts,
    ...options
  });

// Contraseñas probadas contra una misma cuenta. Va por cuenta y no solo por
// IP porque un aula entera sale a internet con la misma IP del colegio.
const loginAttemptsPerAccount = failedAttemptsLimiter({
  limit: 10,
  keyGenerator: (req) => {
    const loginId = req.body && typeof req.body.loginId === "string" ? req.body.loginId.trim().toLowerCase() : "";

    return `${ipKeyGenerator(req.ip)}:${loginId}`;
  }
});

// Tope general por IP para quien prueba muchas cuentas distintas. Es amplio
// para no frenar a un aula que entra a la vez y se equivoca de contraseña.
const loginAttemptsPerIp = failedAttemptsLimiter({ limit: 60 });

// Códigos de sala probados por un mismo estudiante. Va después de
// authenticate, así que req.user ya existe.
const joinCodeAttempts = failedAttemptsLimiter({
  limit: 10,
  keyGenerator: (req) => `user:${req.user.id}`
});

module.exports = {
  loginAttemptsPerAccount,
  loginAttemptsPerIp,
  joinCodeAttempts
};
