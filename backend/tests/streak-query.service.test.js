const { loadStreak } = require("../src/services/streak-query.service");

// Base falsa: devuelve las filas ya agrupadas por dia y guarda con que
// parametros se le pregunto, para no necesitar un Postgres real.
const fakeDb = (rows) => {
  const calls = [];
  const queries = [];

  return {
    calls,
    queries,
    query: async (sql, params) => {
      queries.push(sql);
      calls.push(params);
      return { rows };
    }
  };
};

const NOW = new Date("2026-09-18T15:00:00Z");

describe("loadStreak", () => {
  test("le pasa a la consulta el usuario y el desfase del huso", async () => {
    const db = fakeDb([]);

    await loadStreak(db, 7, -360, NOW);

    expect(db.calls).toEqual([[7, -360]]);
  });

  test("no asume que la base corre en UTC: convierte finished_at con el huso de la sesion", async () => {
    const db = fakeDb([]);

    await loadStreak(db, 7, 0, NOW);

    expect(db.queries[0]).toContain("AT TIME ZONE current_setting('TimeZone')");
    expect(db.queries[0]).toContain("AT TIME ZONE 'UTC'");
  });

  test("sin intentos terminados: racha en 0 y ningun intento hoy", async () => {
    const result = await loadStreak(fakeDb([]), 7, 0, NOW);

    expect(result.streak).toEqual({ days: 0, isActive: false, lastActivityDate: null });
    expect(result.attemptsToday).toBe(0);
  });

  test("el primer intento del dia: racha activa y attemptsToday en 1", async () => {
    const db = fakeDb([
      { day: "2026-09-17", attempts: "2" },
      { day: "2026-09-18", attempts: "1" }
    ]);

    const result = await loadStreak(db, 7, 0, NOW);

    expect(result.streak.days).toBe(2);
    expect(result.streak.isActive).toBe(true);
    expect(result.attemptsToday).toBe(1);
  });

  test("varios intentos hoy: attemptsToday los cuenta", async () => {
    const result = await loadStreak(fakeDb([{ day: "2026-09-18", attempts: "3" }]), 7, 0, NOW);

    expect(result.attemptsToday).toBe(3);
  });

  test("el dia de hoy sale del huso del estudiante, no de UTC", async () => {
    // 03:30 UTC del 18 todavia es 17 en Costa Rica (UTC-6).
    const early = new Date("2026-09-18T03:30:00Z");
    const db = fakeDb([{ day: "2026-09-17", attempts: "1" }]);

    const result = await loadStreak(db, 7, -360, early);

    expect(result.streak.isActive).toBe(true);
    expect(result.attemptsToday).toBe(1);
  });

  test("actividad solo de ayer: congelada y ningun intento hoy", async () => {
    const result = await loadStreak(fakeDb([{ day: "2026-09-17", attempts: "1" }]), 7, 0, NOW);

    expect(result.streak).toEqual({ days: 1, isActive: false, lastActivityDate: "2026-09-17" });
    expect(result.attemptsToday).toBe(0);
  });
});
