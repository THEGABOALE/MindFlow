const { calculateStreak, localDay, normalizeTzOffset } = require("../src/services/streak.service");

const TODAY = "2026-09-18";

describe("calculateStreak", () => {
  test("sin actividad no hay racha", () => {
    expect(calculateStreak([], TODAY)).toEqual({ days: 0, isActive: false, lastActivityDate: null });
  });

  test("actividad solo hoy: 1 dia y activa", () => {
    expect(calculateStreak([TODAY], TODAY)).toEqual({
      days: 1,
      isActive: true,
      lastActivityDate: TODAY
    });
  });

  test("dias seguidos hasta hoy suman y la racha esta activa", () => {
    const result = calculateStreak(["2026-09-16", "2026-09-17", "2026-09-18"], TODAY);

    expect(result.days).toBe(3);
    expect(result.isActive).toBe(true);
  });

  test("si la ultima fue ayer, se conserva el conteo pero queda congelada", () => {
    const result = calculateStreak(["2026-09-16", "2026-09-17"], TODAY);

    expect(result).toEqual({ days: 2, isActive: false, lastActivityDate: "2026-09-17" });
  });

  test("si la ultima fue hace 2 dias o mas, la racha se perdio", () => {
    const result = calculateStreak(["2026-09-14", "2026-09-15", "2026-09-16"], TODAY);

    expect(result).toEqual({ days: 0, isActive: false, lastActivityDate: "2026-09-16" });
  });

  test("un hueco corta la racha: solo cuentan los dias seguidos hasta hoy", () => {
    const result = calculateStreak(["2026-09-14", "2026-09-17", "2026-09-18"], TODAY);

    expect(result.days).toBe(2);
    expect(result.isActive).toBe(true);
  });

  test("ignora repetidos y no importa el orden", () => {
    const result = calculateStreak(["2026-09-18", "2026-09-17", "2026-09-18", "2026-09-17"], TODAY);

    expect(result.days).toBe(2);
  });

  test("los dias futuros no cuentan", () => {
    const result = calculateStreak(["2026-09-19", "2026-09-20"], TODAY);

    expect(result).toEqual({ days: 0, isActive: false, lastActivityDate: null });
  });

  test("cruza el cambio de mes", () => {
    const result = calculateStreak(["2026-08-30", "2026-08-31", "2026-09-01"], "2026-09-01");

    expect(result.days).toBe(3);
    expect(result.isActive).toBe(true);
  });

  test("cruza el cambio de anio", () => {
    const result = calculateStreak(["2026-12-31", "2027-01-01"], "2027-01-01");

    expect(result.days).toBe(2);
    expect(result.isActive).toBe(true);
  });
});

describe("localDay", () => {
  // 03:30 UTC del 18 es todavia las 21:30 del 17 en Costa Rica (UTC-6).
  const instant = new Date("2026-09-18T03:30:00Z");

  test("con desfase 0 es la fecha UTC", () => {
    expect(localDay(instant, 0)).toBe("2026-09-18");
  });

  test("un huso al oeste de UTC retrocede el dia", () => {
    expect(localDay(instant, -360)).toBe("2026-09-17");
  });

  test("un huso al este de UTC lo adelanta", () => {
    expect(localDay(new Date("2026-09-18T22:00:00Z"), 120)).toBe("2026-09-19");
  });
});

describe("normalizeTzOffset", () => {
  test("acepta enteros dentro de los husos reales, como texto o numero", () => {
    expect(normalizeTzOffset("-360")).toBe(-360);
    expect(normalizeTzOffset(330)).toBe(330);
  });

  test("todo lo demas cae a UTC", () => {
    expect(normalizeTzOffset(undefined)).toBe(0);
    expect(normalizeTzOffset("abc")).toBe(0);
    expect(normalizeTzOffset("12.5")).toBe(0);
    expect(normalizeTzOffset("99999")).toBe(0);
  });
});
