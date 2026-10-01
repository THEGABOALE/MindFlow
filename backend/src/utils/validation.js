// Chequeos de tipo para lo que llega del cliente. Sin ellos, un número donde
// se esperaba texto (o un objeto donde se esperaba un número) revienta más
// adelante con un 500 en vez de responder un 400 claro.

// Texto con al menos un carácter que no sea espacio.
const isFilledString = (value) => typeof value === "string" && value.trim().length > 0;

/**
 * Lee un entero no negativo de un parámetro de la URL.
 *
 * @returns {number|undefined|null} el número; undefined si no vino (para usar
 *   el valor por defecto), o null si vino pero no es un entero >= 0.
 */
const parseNonNegativeInt = (raw) => {
  if (raw === undefined || raw === "") {
    return undefined;
  }

  return typeof raw === "string" && /^\d+$/.test(raw) ? Number(raw) : null;
};

module.exports = { isFilledString, parseNonNegativeInt };
