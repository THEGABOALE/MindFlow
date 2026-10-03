// Reglas para aceptar un lote de intentos jugados en el teléfono (modo
// offline). Son puras: validan la forma del lote y que las horas tengan
// sentido, sin tocar la base.
const { normalizeTzOffset } = require("./streak.service");

const MAX_SYNC_BATCH = 50;
const FUTURE_TOLERANCE_MS = 5 * 60 * 1000;
const MAX_AGE_MS = 30 * 24 * 60 * 60 * 1000;
const INVALID_TIMES = "Las horas del intento no son válidas";

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
// Fecha ISO con huso explícito (Z o ±hh:mm): sin huso la hora es ambigua.
const ISO_WITH_ZONE = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?(Z|[+-]\d{2}:\d{2})$/;

const parseTime = (value) => {
  if (typeof value !== "string" || !ISO_WITH_ZONE.test(value)) return null;
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
};

const isAnswerList = (answers) =>
  Array.isArray(answers) && answers.every((a) => a !== null && typeof a === "object" && !Array.isArray(a));

/**
 * Valida la forma del lote. Si un intento está mal armado se rechaza el lote
 * entero (400): es un error de la app, no algo que se arregle reintentando.
 */
const parseSyncBatch = (body) => {
  const { attempts, tzOffsetMinutes } = body || {};

  if (!Array.isArray(attempts) || attempts.length === 0 || attempts.length > MAX_SYNC_BATCH) {
    return { error: `attempts debe ser una lista de 1 a ${MAX_SYNC_BATCH} intentos` };
  }

  const parsed = [];

  for (const raw of attempts) {
    const item = raw || {};
    const startedAt = parseTime(item.startedAt);
    const finishedAt = parseTime(item.finishedAt);

    if (
      typeof item.clientAttemptId !== "string" || !UUID_PATTERN.test(item.clientAttemptId) ||
      !Number.isInteger(item.missionId) ||
      !startedAt || !finishedAt ||
      !isAnswerList(item.answers) ||
      (item.timedOut !== undefined && typeof item.timedOut !== "boolean") ||
      (item.pausedSeconds !== undefined && !(Number.isInteger(item.pausedSeconds) && item.pausedSeconds >= 0)) ||
      (item.usedExtraTime !== undefined && typeof item.usedExtraTime !== "boolean")
    ) {
      return { error: "Hay un intento mal armado en el lote" };
    }

    parsed.push({
      clientAttemptId: item.clientAttemptId.toLowerCase(),
      missionId: item.missionId,
      startedAt,
      finishedAt,
      timedOut: item.timedOut === true,
      // Segundos con el reloj en pausa (la app en segundo plano). El tope se
      // aplica al calificar.
      pausedSeconds: item.pausedSeconds ?? 0,
      // Si compró el potenciador "+30 s"; se cobra solo si le alcanza el saldo.
      usedExtraTime: item.usedExtraTime === true,
      answers: item.answers
    });
  }

  // En el orden en que se jugaron: así una misión completada en este mismo
  // lote ya cuenta para desbloquear la siguiente.
  parsed.sort((a, b) => a.finishedAt - b.finishedAt);

  return { tzOffsetMinutes: normalizeTzOffset(tzOffsetMinutes), attempts: parsed };
};

/**
 * Las horas vienen del reloj del teléfono: se aceptan si tienen sentido. Un
 * reloj algo adelantado se tolera; uno muy adelantado, un intento de hace más
 * de 30 días o un fin anterior al inicio, no.
 */
const checkAttemptTimes = ({ startedAt, finishedAt, now }) => {
  if (finishedAt.getTime() - now.getTime() > FUTURE_TOLERANCE_MS) return INVALID_TIMES;
  if (now.getTime() - finishedAt.getTime() > MAX_AGE_MS) return INVALID_TIMES;
  if (startedAt.getTime() > finishedAt.getTime()) return INVALID_TIMES;
  return null;
};

module.exports = { MAX_SYNC_BATCH, parseSyncBatch, checkAttemptTimes };
