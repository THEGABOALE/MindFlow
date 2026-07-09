const pool = require("../database/connection");

const healthCheck = (req, res) => {
  res.json({
    message: "Servidor de MindFlow corriendo correctamente",
    status: "OK",
    service: "MindFlow"
  });
};

const databaseHealthCheck = async (req, res) => {
  try {
    const client = await pool.query("SELECT NOW()");
    res.json({
      message: "Conexión a la base de datos exitosa",
      status: "OK",
      databaseTime: client.rows[0].now
    });
  } catch (error) {
    res.status(500).json({
      message: "Error al conectar con la base de datos",
      status: "ERROR",
      error: error.message
    })
  }
};

module.exports = {
  healthCheck,
  databaseHealthCheck
}