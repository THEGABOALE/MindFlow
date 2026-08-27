const jwt = require("jsonwebtoken"); // Library used to sign and verify session tokens
const env = require("../config/env");

// Crea el token de sesión que la app mobile va a mandar en cada request
// autenticado (header Authorization: Bearer <token>).
const signSessionToken = (user) => {
  return jwt.sign(
    {
      sub: user.id,
      role: user.roleName
    },
    env.auth.jwtSecret,
    { expiresIn: env.auth.jwtExpiresIn }
  );
};

// Verifica y decodifica un token de sesión. Lanza si el token es inválido o
// expiró; quien llama es responsable de capturar el error.
const verifySessionToken = (token) => {
  return jwt.verify(token, env.auth.jwtSecret);
};

module.exports = {
  signSessionToken,
  verifySessionToken
};
