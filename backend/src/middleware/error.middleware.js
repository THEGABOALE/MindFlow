const { respondServerError } = require("../utils/server-error");

// Cualquier ruta que no exista responde JSON, igual que el resto de la API,
// en vez de la página HTML de Express.
const notFound = (req, res) => {
  res.status(404).json({
    message: "Ruta no encontrada",
    status: "ERROR"
  });
};

// Último recurso para lo que no atrapó ningún controlador. Un cuerpo que no
// es JSON válido es culpa del cliente (400); todo lo demás es un 500 genérico.
// Express lo reconoce como manejador de errores por tener 4 parámetros, por
// eso se declara next aunque no se use.
const errorHandler = (err, req, res, next) => {
  if (err.type === "entity.parse.failed") {
    return res.status(400).json({
      message: "El cuerpo de la petición no es JSON válido",
      status: "ERROR"
    });
  }

  return respondServerError(res, "Error interno del servidor", err);
};

module.exports = { notFound, errorHandler };
