const { parseSyncBatch, checkAttemptTimes, MAX_SYNC_BATCH } = require("../src/services/attempt-sync.service");

const UUID_A = "3f2b8c1e-5d4a-4b6f-9c2d-1a2b3c4d5e6f";
const UUID_B = "9a8b7c6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d";

const item = (over = {}) => ({
  clientAttemptId: UUID_A,
  missionId: 1,
  startedAt: "2026-10-01T14:00:00.000-06:00",
  finishedAt: "2026-10-01T14:01:00.000-06:00",
  timedOut: false,
  answers: [{ questionId: 1, selectedOptionId: 2 }],
  ...over
});

describe("parseSyncBatch", () => {
  test("acepta un lote válido, convierte las horas y ordena por fin", () => {
    const parsed = parseSyncBatch({
      tzOffsetMinutes: -360,
      attempts: [
        item({ clientAttemptId: UUID_B, finishedAt: "2026-10-01T14:05:00.000-06:00" }),
        item()
      ]
    });

    expect(parsed.tzOffsetMinutes).toBe(-360);
    expect(parsed.attempts.map((a) => a.clientAttemptId)).toEqual([UUID_A, UUID_B]);
    expect(parsed.attempts[0].finishedAt).toEqual(new Date("2026-10-01T20:01:00.000Z"));
  });

  test("rechaza un lote vacío, que no es lista o que pasa del máximo", () => {
    expect(parseSyncBatch({ attempts: [] }).error).toBeDefined();
    expect(parseSyncBatch({ attempts: "x" }).error).toBeDefined();
    expect(parseSyncBatch(undefined).error).toBeDefined();
    const many = Array.from({ length: MAX_SYNC_BATCH + 1 }, () => item());
    expect(parseSyncBatch({ attempts: many }).error).toBeDefined();
  });

  test("rechaza el lote entero si un intento está mal armado", () => {
    expect(parseSyncBatch({ attempts: [item({ clientAttemptId: "no-es-uuid" })] }).error).toBeDefined();
    expect(parseSyncBatch({ attempts: [item({ missionId: "2" })] }).error).toBeDefined();
    expect(parseSyncBatch({ attempts: [item({ startedAt: "ayer" })] }).error).toBeDefined();
    // Sin huso horario la hora es ambigua.
    expect(parseSyncBatch({ attempts: [item({ finishedAt: "2026-10-01T14:01:00" })] }).error).toBeDefined();
    expect(parseSyncBatch({ attempts: [item({ answers: [null] })] }).error).toBeDefined();
    expect(parseSyncBatch({ attempts: [item({ answers: {} })] }).error).toBeDefined();
    expect(parseSyncBatch({ attempts: [item({ timedOut: "no" })] }).error).toBeDefined();
    expect(parseSyncBatch({ attempts: [null] }).error).toBeDefined();
  });

  test("timedOut es opcional y el huso inválido cae en UTC", () => {
    const parsed = parseSyncBatch({ tzOffsetMinutes: 9999, attempts: [item({ timedOut: undefined })] });
    expect(parsed.attempts[0].timedOut).toBe(false);
    expect(parsed.tzOffsetMinutes).toBe(0);
  });

  test("el id del intento se guarda en minúsculas para compararlo siempre igual", () => {
    const parsed = parseSyncBatch({ attempts: [item({ clientAttemptId: UUID_A.toUpperCase() })] });
    expect(parsed.attempts[0].clientAttemptId).toBe(UUID_A);
  });
});

describe("checkAttemptTimes", () => {
  const now = new Date("2026-10-01T20:00:00Z");

  test("horas normales pasan", () => {
    expect(checkAttemptTimes({
      startedAt: new Date("2026-10-01T19:58:00Z"), finishedAt: new Date("2026-10-01T19:59:00Z"), now
    })).toBeNull();
  });

  test("un reloj adelantado hasta 5 minutos se tolera", () => {
    expect(checkAttemptTimes({
      startedAt: new Date("2026-10-01T20:03:00Z"), finishedAt: new Date("2026-10-01T20:04:59Z"), now
    })).toBeNull();
  });

  test("más de 5 minutos en el futuro, más de 30 días o el fin antes del inicio se rechazan", () => {
    expect(checkAttemptTimes({
      startedAt: new Date("2026-10-01T20:05:00Z"), finishedAt: new Date("2026-10-01T20:06:00Z"), now
    })).toBe("Las horas del intento no son válidas");
    expect(checkAttemptTimes({
      startedAt: new Date("2026-08-30T19:00:00Z"), finishedAt: new Date("2026-08-31T19:00:00Z"), now
    })).toBe("Las horas del intento no son válidas");
    expect(checkAttemptTimes({
      startedAt: new Date("2026-10-01T19:59:00Z"), finishedAt: new Date("2026-10-01T19:58:00Z"), now
    })).toBe("Las horas del intento no son válidas");
  });
});
