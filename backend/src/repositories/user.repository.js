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

// Liga al usuario con un centro solo si todavia no tenia uno.
const assignCenterIfMissing = async (db, userId, centerId) => {
  await db.query("UPDATE users SET center_id = $1 WHERE id = $2 AND center_id IS NULL;", [centerId, userId]);
};

module.exports = {
  findUserForLogin,
  findUserById,
  loginIdExists,
  findRoleId,
  createIdAccount,
  assignCenterIfMissing
};
