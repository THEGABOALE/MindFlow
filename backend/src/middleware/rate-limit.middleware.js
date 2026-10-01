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

// Clave del límite por cuenta: solo el ID, sin la IP. Si llevara la IP, quien
// prueba contraseñas desde muchas IPs tendría 10 intentos nuevos en cada una
// contra la misma cuenta. Sin ID (petición mal armada) se cuenta por IP.
const loginAccountKey = (req) => {
  const loginId = req.body && typeof req.body.loginId === "string" ? req.body.loginId.trim().toLowerCase() : "";

  return loginId ? `account:${loginId}` : `ip:${ipKeyGenerator(req.ip)}`;
};

// Contraseñas probadas contra una misma cuenta, vengan de donde vengan. Va por
// cuenta y no solo por IP porque un aula entera sale a internet con la misma
// IP del colegio.
const loginAttemptsPerAccount = failedAttemptsLimiter({
  limit: 10,
  keyGenerator: loginAccountKey
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
  loginAccountKey,
  loginAttemptsPerAccount,
  loginAttemptsPerIp,
  joinCodeAttempts
};
