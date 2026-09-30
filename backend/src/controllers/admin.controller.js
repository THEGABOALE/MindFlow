const pool = require("../database/connection");
const userRepository = require("../repositories/user.repository");
const centerRepository = require("../repositories/center.repository");
const { getCenterRoomsOverview } = require("../services/center-overview.service");

const MAX_USERS_LIMIT = 200;
const DEFAULT_USERS_LIMIT = 50;

// El admin (equipo MindFlow) es el unico rol que ve todos los centros a la
// vez. requireRole ya exige rol "admin" en la ruta.
const getGlobalOverview = async (req, res) => {
  try {
    const totals = await userRepository.countActiveUsersByRole(pool);
    const centerRows = await centerRepository.findCentersOverview(pool);

    const usersByRole = {};
    totals.forEach((row) => {
      usersByRole[row.role_name] = Number(row.active_count);
    });

    const centers = centerRows.map((center) => ({
      id: center.id,
      name: center.name,
      institutionType: center.institution_type,
      department: center.department,
      municipality: center.municipality,
      roomCount: Number(center.room_count),
      studentCount: Number(center.student_count),
      averageProgressPercentage: Number(center.average_progress)
    }));

    return res.status(200).json({
      message: "Resumen global obtenido exitosamente",
      status: "OK",
      usersByRole,
      totalCenters: centers.length,
      centers
    });
  } catch (error) {
    return res.status(500).json({
      message: "Error al obtener el resumen global",
      status: "ERROR",
      error: error.message
    });
  }
};

// Mismo detalle que ve el coordinador de su propio centro, pero el admin
// puede pedirlo de cualquier centro.
const getCenterOverview = async (req, res) => {
  const { centerId } = req.params;

  if (!/^\d+$/.test(centerId)) {
    return res.status(400).json({
      message: "El ID del centro debe ser numérico",
      status: "ERROR"
    });
  }

  try {
    const center = await centerRepository.findActiveCenter(pool, centerId);

    if (!center) {
      return res.status(404).json({
        message: "No se encontró el centro educativo",
        status: "ERROR"
      });
    }

    const overview = await getCenterRoomsOverview(centerId);

    return res.status(200).json({
      message: "Resumen del centro obtenido exitosamente",
      status: "OK",
      center: {
        id: center.id,
        name: center.name
      },
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

// Listado de cuentas con filtros, para que el admin pueda auditar quien
// existe en la plataforma sin entrar directo a la base de datos.
const listUsers = async (req, res) => {
  const { role, centerId, search, isActive } = req.query;
  const limit = Math.min(Number(req.query.limit) || DEFAULT_USERS_LIMIT, MAX_USERS_LIMIT);
  const offset = Number(req.query.offset) || 0;

  if (centerId && !/^\d+$/.test(centerId)) {
    return res.status(400).json({
      message: "El centerId debe ser numérico",
      status: "ERROR"
    });
  }

  try {
    const rows = await userRepository.listUsers(pool, {
      role,
      centerId,
      search,
      isActive: isActive === "true" || isActive === "false" ? isActive === "true" : undefined,
      limit,
      offset
    });

    const total = rows[0] ? Number(rows[0].total_count) : 0;

    return res.status(200).json({
      message: "Usuarios obtenidos exitosamente",
      status: "OK",
      total,
      limit,
      offset,
      users: rows.map((user) => ({
        id: user.id,
        fullName: user.full_name,
        email: user.email,
        loginId: user.login_id,
        role: user.role_name,
        centerId: user.center_id,
        centerName: user.center_name,
        isActive: user.is_active,
        createdAt: user.created_at
      }))
    });
  } catch (error) {
    return res.status(500).json({
      message: "Error al obtener los usuarios",
      status: "ERROR",
      error: error.message
    });
  }
};

module.exports = {
  getGlobalOverview,
  getCenterOverview,
  listUsers
};
