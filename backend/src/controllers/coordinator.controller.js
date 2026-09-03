const { getCenterRoomsOverview } = require("../services/center-overview.service");

// El coordinador ve el progreso general de todas las salas de SU centro
// (requireRole ya exige rol "coordinator"; acá se filtra por center_id).
const getCenterOverview = async (req, res) => {
  if (!req.user.centerId) {
    return res.status(400).json({
      message: "Tu cuenta no tiene un centro educativo asignado",
      status: "ERROR"
    });
  }

  try {
    const overview = await getCenterRoomsOverview(req.user.centerId);

    return res.status(200).json({
      message: "Resumen del centro obtenido exitosamente",
      status: "OK",
      ...overview
    });
  } catch (error) {
    return res.status(500).json({
      message: "Error al obtener el resumen del centro",
      status: "ERROR",
      error: error.message
    });
  }
};

module.exports = {
  getCenterOverview
};
