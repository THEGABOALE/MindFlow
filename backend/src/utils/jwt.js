const jwt = require("jsonwebtoken");
const env = require("../config/env");

const signSessionToken = (user) => {
  return jwt.sign(
    {
      sub: user.id,
      role: user.roleName,
      centerId: user.centerId || null
    },
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
