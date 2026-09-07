require("dotenv").config();

const nodeEnv = process.env.NODE_ENV || "development";

// El secreto con el que se firman los tokens de sesion. En produccion es
// obligatorio: si falta, el server no arranca en vez de firmar con un secreto
// que esta en el repo (cualquiera podria falsificar sesiones). En desarrollo
// se permite un fallback comodo, pero avisando por consola.
const DEV_JWT_SECRET = "dev-secret-cambiar-en-produccion";

const resolveJwtSecret = () => {
  if (process.env.JWT_SECRET) {
    return process.env.JWT_SECRET;
  }

  if (nodeEnv === "production") {
    throw new Error(
      "Falta la variable JWT_SECRET. Es obligatoria en produccion para firmar los tokens de sesion."
    );
  }

  console.warn(
    "[env] JWT_SECRET no esta definida; usando un secreto de desarrollo. NO usar asi en produccion."
  );

  return DEV_JWT_SECRET;
};

const env = {
  port: process.env.PORT || 3000,
  nodeEnv,

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
    jwtSecret: resolveJwtSecret(),
    jwtExpiresIn: process.env.JWT_EXPIRES_IN || "7d"
  }
};

module.exports = env;