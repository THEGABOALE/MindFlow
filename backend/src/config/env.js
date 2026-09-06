require("dotenv").config();

const env = {
  port: process.env.PORT || 3000,
  nodeEnv: process.env.NODE_ENV || "development",

  db: {
    // Neon/Railway dan una sola cadena de conexion; el desarrollo local sigue
    // usando las variables sueltas (ver database/connection.js).
    connectionString: process.env.DATABASE_URL || null,
    host: process.env.DB_HOST || "localhost",
    port: process.env.DB_PORT || 5432,
    name: process.env.DB_NAME,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD
  },

  auth: {
    googleClientId: process.env.GOOGLE_CLIENT_ID,
    jwtSecret: process.env.JWT_SECRET || "dev-secret-cambiar-en-produccion",
    jwtExpiresIn: process.env.JWT_EXPIRES_IN || "7d"
  }
};

module.exports = env;