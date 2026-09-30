// Arma la racha de un estudiante a partir de sus intentos terminados. Recibe
// la conexion (pool o client de una transaccion) para poder usarse dentro del
// cierre de un intento y ver el intento recien cerrado.
const attemptRepository = require("../repositories/attempt.repository");
const { calculateStreak, localDay } = require("./streak.service");

/**
 * Un dia cuenta si se termino al menos un intento (pasado o no), en la fecha
 * local del estudiante: tzOffsetMinutes es su desfase respecto a UTC.
 *
 * @returns {Promise<{streak: {days:number, isActive:boolean, lastActivityDate:string|null}, attemptsToday:number}>}
 *   attemptsToday es cuantos intentos termino hoy; si es 1, el ultimo fue el
 *   primero del dia, o sea, el que acaba de encender (o descongelar) la racha.
 */
const loadStreak = async (db, userId, tzOffsetMinutes, now = new Date()) => {
  const activityDays = await attemptRepository.findActivityDays(db, userId, tzOffsetMinutes);

  const today = localDay(now, tzOffsetMinutes);
  const todayRow = activityDays.find((row) => row.day === today);

  return {
    streak: calculateStreak(
      activityDays.map((row) => row.day),
      today
    ),
    attemptsToday: todayRow ? Number(todayRow.attempts) : 0
  };
};

module.exports = { loadStreak };
