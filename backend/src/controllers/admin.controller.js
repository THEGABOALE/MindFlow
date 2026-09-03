const pool = require("../database/connection");
const { getCenterRoomsOverview } = require("../services/center-overview.service");

const MAX_USERS_LIMIT = 200;
const DEFAULT_USERS_LIMIT = 50;

// El admin (equipo MindFlow) es el unico rol que ve todos los centros a la
// vez. requireRole ya exige rol "admin" en la ruta.
const getGlobalOverview = async (req, res) => {
  try {
    const totalsResult = await pool.query(
      `
      SELECT r.name AS role_name, COUNT(*) FILTER (WHERE u.is_active) AS active_count
      FROM roles r
      LEFT JOIN users u ON u.role_id = r.id
      GROUP BY r.name;
      `
    );

    const centersResult = await pool.query(
      `
      SELECT
        ec.id,
        ec.name,
        it.name AS institution_type,
        ec.department,
        ec.municipality,
        COUNT(DISTINCT cg.id) AS room_count,
        COUNT(DISTINCT sge.user_id) AS student_count,
        COALESCE(AVG(lp.progress_percentage), 0) AS average_progress
      FROM educational_centers ec
      LEFT JOIN institution_types it ON it.id = ec.institution_type_id
      LEFT JOIN class_groups cg ON cg.center_id = ec.id AND cg.is_active = TRUE
      LEFT JOIN student_group_enrollments sge ON sge.group_id = cg.id AND sge.is_active = TRUE
      LEFT JOIN level_progress lp ON lp.user_id = sge.user_id AND lp.level_id = cg.level_id
      WHERE ec.is_active = TRUE
      GROUP BY ec.id, it.name
      ORDER BY ec.name ASC;
      `
    );

    const usersByRole = {};
    totalsResult.rows.forEach((row) => {
      usersByRole[row.role_name] = Number(row.active_count);
    });

    const centers = centersResult.rows.map((center) => ({
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
    const centerResult = await pool.query(
      "SELECT id, name FROM educational_centers WHERE id = $1 AND is_active = TRUE LIMIT 1;",
      [centerId]
    );

    const center = centerResult.rows[0];

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

  const conditions = [];
  const params = [];

  if (role) {
    params.push(role);
    conditions.push(`r.name = $${params.length}`);
  }

  if (centerId) {
    if (!/^\d+$/.test(centerId)) {
      return res.status(400).json({
        message: "El centerId debe ser numérico",
        status: "ERROR"
      });
    }

    params.push(centerId);
    conditions.push(`u.center_id = $${params.length}`);
  }

  if (search) {
    params.push(`%${search}%`);
    conditions.push(`(u.full_name ILIKE $${params.length} OR u.login_id ILIKE $${params.length} OR u.email ILIKE $${params.length})`);
  }

  if (isActive === "true" || isActive === "false") {
    params.push(isActive === "true");
    conditions.push(`u.is_active = $${params.length}`);
  }

  const whereClause = conditions.length ? `WHERE ${conditions.join(" AND ")}` : "";

  params.push(limit);
  const limitParam = `$${params.length}`;
  params.push(offset);
  const offsetParam = `$${params.length}`;

  try {
    const result = await pool.query(
      `
      SELECT
        u.id, u.full_name, u.email, u.login_id, u.center_id, u.is_active, u.created_at,
        r.name AS role_name, ec.name AS center_name,
        COUNT(*) OVER() AS total_count
      FROM users u
      JOIN roles r ON r.id = u.role_id
      LEFT JOIN educational_centers ec ON ec.id = u.center_id
      ${whereClause}
      ORDER BY u.full_name ASC
      LIMIT ${limitParam} OFFSET ${offsetParam};
      `,
      params
    );

    const total = result.rows[0] ? Number(result.rows[0].total_count) : 0;

    return res.status(200).json({
      message: "Usuarios obtenidos exitosamente",
      status: "OK",
      total,
      limit,
      offset,
      users: result.rows.map((user) => ({
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
