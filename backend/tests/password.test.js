const { hashPassword, passwordMatches } = require("../src/utils/password");

describe("utils/password", () => {
  test("el hash no es la contraseña y coincide solo con la correcta", async () => {
    const hash = await hashPassword("clave-secreta");

    expect(hash).not.toBe("clave-secreta");
    expect(await passwordMatches("clave-secreta", hash)).toBe(true);
    expect(await passwordMatches("otra-clave", hash)).toBe(false);
  });

  test("dos hashes de la misma contraseña son distintos (sal aleatoria)", async () => {
    expect(await hashPassword("1234")).not.toBe(await hashPassword("1234"));
  });

  test("reconoce los hashes que ya estan guardados en la base", async () => {
    // Hash de "1234" tomado de data.sql (cuenta de prueba garciaga).
    const stored = "$2b$10$pP0jxzQU/ztVr6XUSwPOCusjAw.s6KC9pSW1slQ9BB0G2DnLQ3rU6";

    expect(await passwordMatches("1234", stored)).toBe(true);
  });
});
