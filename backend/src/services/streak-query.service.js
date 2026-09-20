// Arma la racha de un estudiante leyendo sus intentos terminados. Recibe la
// conexion (pool o client de una transaccion) para poder usarse dentro del
// cierre de un intento y ver el intento recien cerrado.
const { calculateStreak, localDay } = require("./streak.service");

/**
 * Un dia cuenta si se termino al menos un intento (pasado o no), en la fecha
 * local del estudiante: tzOffsetMinutes es su desfase respecto a UTC.
 *
 * finished_at es un timestamp sin huso que se escribe con CURRENT_TIMESTAMP,
 * o sea, en la hora del huso de la base (UTC en Neon/Railway, pero la base
 * local usa el de la maquina). Por eso primero se le devuelve el huso de la
 * base para tener el instante real, luego se pasa a UTC y recien ahi se suma
 * el desfase del estudiante: asi el resultado no depende de como este
 * configurada la base.
 *
 * @returns {Promise<{streak: {days:number, isActive:boolean, lastActivityDate:string|null}, attemptsToday:number}>}
 *   attemptsToday es cuantos intentos termino hoy; si es 1, el ultimo fue el
 *   primero del dia, o sea, el que acaba de encender (o descongelar) la racha.
 */
const loadStreak = async (db, userId, tzOffsetMinutes, now = new Date()) => {
  const result = await db.query(
    `
    SELECT to_char(
             ((finished_at AT TIME ZONE current_setting('TimeZone')) AT TIME ZONE 'UTC')
               + make_interval(mins => $2),
             'YYYY-MM-DD'
           ) AS day,
           COUNT(*) AS attempts
    FROM mission_attempts
    WHERE user_id = $1 AND finished_at IS NOT NULL
    GROUP BY 1;
    `,
    [userId, tzOffsetMinutes]
  );

  const today = localDay(now, tzOffsetMinutes);
  const todayRow = result.rows.find((row) => row.day === today);

  return {
    streak: calculateStreak(
      result.rows.map((row) => row.day),
      today
    ),
    attemptsToday: todayRow ? Number(todayRow.attempts) : 0
  };
};

module.exports = { loadStreak };
