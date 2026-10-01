const { hasPaidReviewNear } = require("../src/repositories/attempt.repository");

// Base falsa: guarda con qué parámetros se le preguntó y devuelve las filas dadas.
const fakeDb = (rows) => {
  const calls = [];

  return {
    calls,
    query: async (sql, params) => {
      calls.push({ sql, params });
      return { rows };
    }
  };
};

describe("hasPaidReviewNear", () => {
  test("un intento del teléfono se compara contra la hora en que se terminó", async () => {
    const db = fakeDb([]);
    const finishedAt = new Date("2026-09-28T20:01:00.000Z");

    await hasPaidReviewNear(db, 2, 1, 24, finishedAt);

    expect(db.calls[0].params).toEqual([2, 1, 24, "2026-09-28T20:01:00.000Z"]);
  });

  test("sin hora del intento se usa la actual (cierre en línea)", async () => {
    const db = fakeDb([]);

    await hasPaidReviewNear(db, 2, 1, 24);

    expect(db.calls[0].params[3]).toBeNull();
    expect(db.calls[0].sql).toContain("LOCALTIMESTAMP");
  });

  test("la ventana mira hacia antes y hacia después de esa hora", async () => {
    const db = fakeDb([]);

    await hasPaidReviewNear(db, 2, 1, 24);

    expect(db.calls[0].sql).toContain("finished_at > ref.t - make_interval");
    expect(db.calls[0].sql).toContain("finished_at < ref.t + make_interval");
  });

  test("devuelve si encontró un repaso cobrado", async () => {
    expect(await hasPaidReviewNear(fakeDb([{ "?column?": 1 }]), 2, 1, 24)).toBe(true);
    expect(await hasPaidReviewNear(fakeDb([]), 2, 1, 24)).toBe(false);
  });
});
