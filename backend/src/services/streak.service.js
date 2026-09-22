// Logica de la racha del estudiante, separada del controlador para poder
// testearla sin base de datos: recibe los dias en que hizo misiones y el dia de
// hoy, y devuelve cuantos dias seguidos lleva y si la racha esta activa.
//
// Un dia cuenta si el estudiante termino al menos un intento ese dia (pasado o
// no: lo que se premia es sentarse a hacer la leccion, no acertar). Todos los
// dias son fechas locales "YYYY-MM-DD" segun el huso del estudiante, no las
// del servidor, para que el dia no cambie a las 7 de la tarde por usar UTC.

const DAY_MS = 24 * 60 * 60 * 1000;
const MAX_TZ_OFFSET_MINUTES = 14 * 60;

// Desfase del huso en minutos respecto a UTC, positivo al este (Costa Rica = -360).
// Cualquier cosa que no sea un entero razonable se toma como UTC.
const normalizeTzOffset = (raw) => {
  const offset = Number(raw);

  if (!Number.isInteger(offset) || Math.abs(offset) > MAX_TZ_OFFSET_MINUTES) {
    return 0;
  }

  return offset;
};

// Fecha local "YYYY-MM-DD" de un instante para un huso dado.
const localDay = (date, tzOffsetMinutes) =>
  new Date(date.getTime() + tzOffsetMinutes * 60 * 1000).toISOString().slice(0, 10);

const dayNumber = (isoDay) => Math.floor(Date.parse(`${isoDay}T00:00:00Z`) / DAY_MS);

/**
 * - activa: hizo una mision hoy. Los dias son los seguidos que terminan hoy.
 * - congelada: la ultima fue ayer. Se conserva el conteo y hoy puede
 *   descongelarla y seguir sumando.
 * - perdida: la ultima fue hace 2 dias o mas. Vuelve a 0 y queda congelada
 *   hasta que haga una mision.
 *
 * @param {string[]} activityDays fechas "YYYY-MM-DD" con actividad (cualquier orden, con repetidas)
 * @param {string} today fecha local de hoy "YYYY-MM-DD"
 * @returns {{days:number, isActive:boolean, lastActivityDate:string|null}}
 */
const calculateStreak = (activityDays, today) => {
  const todayNumber = dayNumber(today);

  // Los dias futuros solo pueden venir de un desfase de huso mal informado: no cuentan.
  const days = new Set(
    activityDays.map(dayNumber).filter((day) => !Number.isNaN(day) && day <= todayNumber)
  );

  if (days.size === 0) {
    return { days: 0, isActive: false, lastActivityDate: null };
  }

  const last = Math.max(...days);
  const lastActivityDate = new Date(last * DAY_MS).toISOString().slice(0, 10);
  const gap = todayNumber - last;

  if (gap >= 2) {
    return { days: 0, isActive: false, lastActivityDate };
  }

  let run = 0;

  while (days.has(last - run)) {
    run += 1;
  }

  return { days: run, isActive: gap === 0, lastActivityDate };
};

module.exports = {
  calculateStreak,
  localDay,
  normalizeTzOffset
};
