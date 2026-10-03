const {
  countPaidReviewsNear,
  findSeedBalance,
  findStudentTotals,
  insertSettledAttempt
} = require("../src/repositories/attempt.repository");

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

describe("countPaidReviewsNear", () => {
  test("un intento del teléfono se compara contra la hora en que se terminó", async () => {
    const db = fakeDb([{ paid: 0 }]);
    const finishedAt = new Date("2026-09-28T20:01:00.000Z");

    await countPaidReviewsNear(db, 2, 1, 24, finishedAt);

    expect(db.calls[0].params).toEqual([2, 1, 24, "2026-09-28T20:01:00.000Z"]);
  });

  test("sin hora del intento se usa la actual (cierre en línea)", async () => {
    const db = fakeDb([{ paid: 0 }]);

    await countPaidReviewsNear(db, 2, 1, 24);

    expect(db.calls[0].params[3]).toBeNull();
    expect(db.calls[0].sql).toContain("LOCALTIMESTAMP");
  });

  test("la ventana mira hacia antes y hacia después de esa hora", async () => {
    const db = fakeDb([{ paid: 0 }]);

    await countPaidReviewsNear(db, 2, 1, 24);

    expect(db.calls[0].sql).toContain("finished_at > ref.t - make_interval");
    expect(db.calls[0].sql).toContain("finished_at < ref.t + make_interval");
  });

  test("devuelve cuántos repasos cobrados encontró", async () => {
    expect(await countPaidReviewsNear(fakeDb([{ paid: 3 }]), 2, 1, 24)).toBe(3);
    expect(await countPaidReviewsNear(fakeDb([{ paid: 0 }]), 2, 1, 24)).toBe(0);
  });
});

describe("findSeedBalance", () => {
  test("es lo ganado menos lo gastado del estudiante", async () => {
    const db = fakeDb([{ balance: "650" }]);

    expect(await findSeedBalance(db, 7)).toBe(650);
    expect(db.calls[0].sql).toContain("SUM(points_earned - seeds_spent)");
    expect(db.calls[0].params).toEqual([7, null]);
  });

  test("con una hora, cuenta solo lo terminado hasta ese momento", async () => {
    const db = fakeDb([{ balance: "450" }]);

    await findSeedBalance(db, 7, new Date("2026-10-02T15:00:00.000Z"));

    expect(db.calls[0].params).toEqual([7, "2026-10-02T15:00:00.000Z"]);
    expect(db.calls[0].sql).toContain("finished_at <= $2::timestamptz AT TIME ZONE current_setting('TimeZone')");
  });
});

describe("insertSettledAttempt", () => {
  test("guarda las semillas gastadas del intento", async () => {
    const db = fakeDb([{ id: 31 }]);

    const id = await insertSettledAttempt(db, {
      userId: 7, missionId: 2, clientAttemptId: "3f2b8c1e-5d4a-4b6f-9c2d-1a2b3c4d5e6f",
      startedAt: new Date("2026-10-02T15:00:00.000Z"), finishedAt: new Date("2026-10-02T15:01:00.000Z"),
      score: 100, correctAnswers: 9, wrongAnswers: 0, pointsEarned: 150, isReview: false,
      status: "completed", seedsSpent: 500
    });

    expect(id).toBe(31);
    expect(db.calls[0].sql).toContain("seeds_spent");
    expect(db.calls[0].params[11]).toBe(500);
  });

  test("sin potenciador no gasta nada", async () => {
    const db = fakeDb([{ id: 32 }]);

    await insertSettledAttempt(db, {
      userId: 7, missionId: 2, clientAttemptId: "3f2b8c1e-5d4a-4b6f-9c2d-1a2b3c4d5e6f",
      startedAt: new Date("2026-10-02T15:00:00.000Z"), finishedAt: new Date("2026-10-02T15:01:00.000Z"),
      score: 100, correctAnswers: 9, wrongAnswers: 0, pointsEarned: 150, isReview: false, status: "completed"
    });

    expect(db.calls[0].params[11]).toBe(0);
  });
});

describe("findStudentTotals", () => {
  test("las semillas totales descuentan lo gastado", async () => {
    const db = fakeDb([{ total_points: "150", missions_completed: "1" }]);

    expect(await findStudentTotals(db, 7)).toEqual({ totalPoints: 150, missionsCompleted: 1 });
    expect(db.calls[0].sql).toContain("SUM(points_earned - seeds_spent)");
  });
});
