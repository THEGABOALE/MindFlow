const crypto = require("crypto");

// Responde un 500 sin contarle al cliente qué falló por dentro: el detalle
// (que puede nombrar tablas, columnas o consultas) queda solo en el log del
// servidor, junto a un id corto que sí viaja en la respuesta para poder
// encontrarlo si alguien reporta el error.
const respondServerError = (res, message, error) => {
  const errorId = crypto.randomBytes(4).toString("hex");

  console.error(`[${errorId}] ${message}`, error);

  return res.status(500).json({
    message,
    status: "ERROR",
    errorId
  });
};

module.exports = { respondServerError };
