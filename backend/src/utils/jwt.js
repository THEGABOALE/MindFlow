const jwt = require("jsonwebtoken");
const env = require("../config/env");

// El token solo lleva el id. El rol y el centro se leen siempre de la base
// en cada peticion (ver auth.middleware.js), para que un cambio de rol o
// de centro se refleje al instante y no haya que esperar a que expire.
const signSessionToken = (userId) => {
  return jwt.sign(
    { sub: userId },
    env.auth.jwtSecret,
    { expiresIn: env.auth.jwtExpiresIn }
  );
};

const verifySessionToken = (token) => {
  return jwt.verify(token, env.auth.jwtSecret);
};

module.exports = {
  signSessionToken,
  verifySessionToken
};
