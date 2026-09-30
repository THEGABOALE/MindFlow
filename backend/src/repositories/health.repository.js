// Hora de la base: sirve para comprobar que la conexion responde.
const getDatabaseTime = async (db) => {
  const result = await db.query("SELECT NOW()");

  return result.rows[0].now;
};

module.exports = {
  getDatabaseTime
};
