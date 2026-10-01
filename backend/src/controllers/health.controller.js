const pool = require("../database/connection");
const healthRepository = require("../repositories/health.repository");
const { respondServerError } = require("../utils/server-error");

const healthCheck = (req, res) => {
  res.json({
    message: "Servidor de MindFlow corriendo correctamente",
    status: "OK",
    service: "MindFlow"
  });
};

const databaseHealthCheck = async (req, res) => {
  try {
    const databaseTime = await healthRepository.getDatabaseTime(pool);
    res.json({
      message: "Conexión a la base de datos exitosa",
      status: "OK",
      databaseTime
    });
  } catch (error) {
    return respondServerError(res, "Error al conectar con la base de datos", error);
  }
};

module.exports = {
  healthCheck,
  databaseHealthCheck
}