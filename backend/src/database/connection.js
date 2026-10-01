const { Pool } = require("pg");
const env = require("../config/env");

// Con DATABASE_URL (Neon, Railway) la conexion va cifrada y se verifica el
// certificado del servidor, para que nadie en el camino pueda hacerse pasar
// por la base. Sin ella asumimos Postgres local de desarrollo y no se fuerza
// SSL.
const pool = env.db.connectionString
  ? new Pool({
      connectionString: env.db.connectionString,
      ssl: { rejectUnauthorized: true }
    })
  : new Pool({
      host: env.db.host,
      port: env.db.port,
      database: env.db.name,
      user: env.db.user,
      password: env.db.password
    });

module.exports = pool;
