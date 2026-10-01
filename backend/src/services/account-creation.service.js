// Reglas para crear cuentas con ID y contraseña. Son reglas puras: reciben a
// quien crea la cuenta y lo que pidió, sin tocar la base.

// Un coordinador no puede crear coordinadores ni admins, para no poder
// escalar sus propios permisos.
const CREATABLE_ROLES_BY_ROLE = {
  coordinator: ["student", "teacher"],
  admin: ["student", "teacher", "coordinator", "admin", "validator"]
};

// Los estudiantes son niños y su contraseña la suele elegir el docente, así
// que alcanza con 4 caracteres. Las cuentas que ven datos de otros piden más.
const STUDENT_MIN_PASSWORD_LENGTH = 4;
const STAFF_MIN_PASSWORD_LENGTH = 8;

// Roles que solo tienen sentido dentro de un centro educativo.
const ROLES_THAT_NEED_A_CENTER = ["teacher", "coordinator"];

const canCreateRole = (requesterRole, role) => (CREATABLE_ROLES_BY_ROLE[requesterRole] || []).includes(role);

const minPasswordLength = (role) =>
  role === "student" ? STUDENT_MIN_PASSWORD_LENGTH : STAFF_MIN_PASSWORD_LENGTH;

/**
 * Centro en el que queda la cuenta nueva.
 *
 * El coordinador siempre crea cuentas en su propio centro. El admin no tiene
 * centro, así que lo indica en requestedCenterId: es obligatorio para docentes
 * y coordinadores, opcional para estudiantes (se ligan solos al usar el código
 * de su sala) y no aplica a las cuentas del equipo MindFlow.
 *
 * @returns {{centerId: number|null} | {error: string}}
 */
const resolveNewAccountCenter = ({ requester, role, requestedCenterId }) => {
  if (requester.role !== "admin") {
    return { centerId: requester.centerId || null };
  }

  if (!ROLES_THAT_NEED_A_CENTER.includes(role) && role !== "student") {
    return { centerId: null };
  }

  if (requestedCenterId === undefined || requestedCenterId === null) {
    return ROLES_THAT_NEED_A_CENTER.includes(role)
      ? { error: "centerId es obligatorio para crear docentes y coordinadores" }
      : { centerId: null };
  }

  const centerId = Number(requestedCenterId);

  if (!Number.isInteger(centerId) || centerId <= 0 || String(requestedCenterId).trim() !== String(centerId)) {
    return { error: "El centerId debe ser numérico" };
  }

  return { centerId };
};

module.exports = {
  canCreateRole,
  minPasswordLength,
  resolveNewAccountCenter
};
