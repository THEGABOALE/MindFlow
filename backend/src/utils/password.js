const bcrypt = require("bcryptjs");

const SALT_ROUNDS = 10;

const hashPassword = (password) => bcrypt.hash(password, SALT_ROUNDS);

const passwordMatches = (password, passwordHash) => bcrypt.compare(password, passwordHash);

module.exports = {
  hashPassword,
  passwordMatches
};
