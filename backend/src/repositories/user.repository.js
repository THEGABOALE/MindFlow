// Cuentas y roles. Todas las funciones reciben la conexion (el pool o el
// client de una transaccion).

// Busca por correo (login con Google) o por ID (login con contraseña). Es la
// unica consulta que devuelve el hash de la contraseña.
const findUserForLogin = async (db, { email, loginId }) => {
  const result = await db.query(
    `
    SELECT
      u.id,
      u.full_name,
      u.email,
      u.login_id,
      u.password_hash,
      u.center_id,
      u.is_active,
      r.name AS role_name
    FROM users u
    JOIN roles r ON r.id = u.role_id
    WHERE ($1::VARCHAR IS NOT NULL AND u.email = $1)
       OR ($2::VARCHAR IS NOT NULL AND u.login_id = $2)
    LIMIT 1;
    `,
    [email || null, loginId || null]
  );

  return result.rows[0] || null;
};

const findUserById = async (db, id) => {
  const result = await db.query(
    `
    SELECT
      u.id,
      u.full_name,
      u.email,
      u.login_id,
      u.center_id,
      u.is_active,
      r.name AS role_name
    FROM users u
    JOIN roles r ON r.id = u.role_id
    WHERE u.id = $1
    LIMIT 1;
    `,
    [id]
  );

  return result.rows[0] || null;
};

const loginIdExists = async (db, loginId) => {
  const result = await db.query("SELECT id FROM users WHERE login_id = $1 LIMIT 1;", [loginId]);

  return result.rows.length > 0;
};

const findRoleId = async (db, roleName) => {
  const result = await db.query("SELECT id FROM roles WHERE name = $1 LIMIT 1;", [roleName]);

  return result.rows.length > 0 ? result.rows[0].id : null;
};

const createIdAccount = async (db, { fullName, loginId, passwordHash, roleId, centerId, createdBy }) => {
  const result = await db.query(
    `
    INSERT INTO users (full_name, login_id, password_hash, role_id, center_id, created_by)
    VALUES ($1, $2, $3, $4, $5, $6)
    RETURNING id, full_name, login_id, center_id;
    `,
    [fullName, loginId, passwordHash, roleId, centerId, createdBy]
  );

  return result.rows[0];
};

// Bloquea la fila del usuario hasta que termine la transaccion. Sirve para
// que dos peticiones del mismo estudiante que cambian su progreso (cerrar
// intentos, unirse a una sala) se hagan una despues de la otra y no las dos
// a la vez sobre los mismos datos. Las lecturas normales no esperan.
const lockUser = async (db, userId) => {
  await db.query("SELECT id FROM users WHERE id = $1 FOR UPDATE;", [userId]);
};

// Liga al usuario con un centro solo si todavia no tenia uno.
const assignCenterIfMissing = async (db, userId, centerId) => {
  await db.query("UPDATE users SET center_id = $1 WHERE id = $2 AND center_id IS NULL;", [centerId, userId]);
};

// Cuantas cuentas activas hay por rol; los roles sin cuentas salen en 0.
const countActiveUsersByRole = async (db) => {
  const result = await db.query(
    `
    SELECT r.name AS role_name, COUNT(*) FILTER (WHERE u.is_active) AS active_count
    FROM roles r
    LEFT JOIN users u ON u.role_id = r.id
    GROUP BY r.name;
    `
  );

  return result.rows;
};

/**
 * Cuentas ordenadas por nombre, con filtros opcionales y paginacion. Cada fila
 * trae en total_count cuantas cuentas cumplen los filtros en total.
 *
 * @param {object} filters
 * @param {string} [filters.role] nombre exacto del rol
 * @param {string|number} [filters.centerId]
 * @param {string} [filters.search] texto que aparezca en el nombre, el ID o el correo
 * @param {boolean} [filters.isActive]
 * @param {number} filters.limit
 * @param {number} filters.offset
 */
const listUsers = async (db, { role, centerId, search, isActive, limit, offset }) => {
  const conditions = [];
  const params = [];

  if (role) {
    params.push(role);
    conditions.push(`r.name = $${params.length}`);
  }

  if (centerId) {
    params.push(centerId);
    conditions.push(`u.center_id = $${params.length}`);
  }

  if (search) {
    params.push(`%${search}%`);
    conditions.push(`(u.full_name ILIKE $${params.length} OR u.login_id ILIKE $${params.length} OR u.email ILIKE $${params.length})`);
  }

  if (isActive !== undefined) {
    params.push(isActive);
    conditions.push(`u.is_active = $${params.length}`);
  }

  const whereClause = conditions.length ? `WHERE ${conditions.join(" AND ")}` : "";

  params.push(limit);
  const limitParam = `$${params.length}`;
  params.push(offset);
  const offsetParam = `$${params.length}`;

  const result = await db.query(
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

  return result.rows;
};

module.exports = {
  countActiveUsersByRole,
  listUsers,
  findUserForLogin,
  findUserById,
  loginIdExists,
  findRoleId,
  createIdAccount,
  lockUser,
  assignCenterIfMissing
};
