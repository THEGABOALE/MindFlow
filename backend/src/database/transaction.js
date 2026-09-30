const pool = require("./connection");

/**
 * Corre `work` dentro de una transaccion y le pasa la conexion para que se la
 * de a los repositorios. Confirma si `work` termina y deshace todo si lanza.
 */
const withTransaction = async (work) => {
  const client = await pool.connect();

  try {
    await client.query("BEGIN");
    const result = await work(client);
    await client.query("COMMIT");

    return result;
  } catch (error) {
    await client.query("ROLLBACK");
    throw error;
  } finally {
    client.release();
  }
};

module.exports = { withTransaction };
