const { Pool } = require("pg");
const env = require("../config/env");

const pool = new Pool({
  host: env.db.host,
  port: env.db.port,
  database: env.db.name,
  user: env.db.user,
  password: env.db.password
});

module.exports = pool;