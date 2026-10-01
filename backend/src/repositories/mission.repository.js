// Lectura del contenido de las misiones. Todas las funciones reciben la
// conexion (el pool o el client de una transaccion) para poder usarse dentro
// del cierre de un intento.

const findPublishedMission = async (db, missionId) => {
  const result = await db.query(
    `
    SELECT id, level_id, title, description, topic, order_index,
           points_reward, mechanic, time_limit_seconds, max_plumas
    FROM missions
    WHERE id = $1 AND is_published = TRUE
    LIMIT 1;
    `,
    [missionId]
  );

  return result.rows[0] || null;
};

// La mision publicada que va justo antes en el mismo nivel, o null si esta es
// la primera.
const findPreviousMission = async (db, levelId, orderIndex) => {
  const result = await db.query(
    `
    SELECT id
    FROM missions
    WHERE level_id = $1 AND is_published = TRUE AND order_index < $2
    ORDER BY order_index DESC
    LIMIT 1;
    `,
    [levelId, orderIndex]
  );

  return result.rows[0] || null;
};

const findQuestions = async (db, missionId) => {
  const result = await db.query(
    `
    SELECT id, question_text, question_type, feedback, order_index, points
    FROM questions
    WHERE mission_id = $1
    ORDER BY order_index ASC;
    `,
    [missionId]
  );

  return result.rows;
};

// Opciones de todas las preguntas de la mision (opcion multiple y verdadero/falso).
const findOptions = async (db, missionId) => {
  const result = await db.query(
    `
    SELECT ao.id, ao.question_id, ao.option_text, ao.is_correct, ao.feedback, ao.order_index
    FROM answer_options ao
    JOIN questions q ON q.id = ao.question_id
    WHERE q.mission_id = $1
    ORDER BY ao.question_id ASC, ao.order_index ASC;
    `,
    [missionId]
  );

  return result.rows;
};

// Pares de todas las preguntas de la mision (relacion de conceptos).
const findPairs = async (db, missionId) => {
  const result = await db.query(
    `
    SELECT qp.id, qp.question_id, qp.term, qp.match_text, qp.order_index
    FROM question_pairs qp
    JOIN questions q ON q.id = qp.question_id
    WHERE q.mission_id = $1
    ORDER BY qp.question_id ASC, qp.order_index ASC;
    `,
    [missionId]
  );

  return result.rows;
};

module.exports = {
  findPublishedMission,
  findPreviousMission,
  findQuestions,
  findOptions,
  findPairs
};
