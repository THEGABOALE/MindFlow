const { findStudentsSummary } = require("../src/repositories/group.repository");

test("el resumen de la sala descuenta las semillas gastadas", async () => {
  const calls = [];
  const db = {
    query: async (sql, params) => {
      calls.push({ sql, params });
      return { rows: [] };
    }
  };

  await findStudentsSummary(db, 3);

  expect(calls[0].sql).toContain("SUM(a.points_earned - a.seeds_spent)");
});
