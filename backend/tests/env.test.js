// Sin esto, dotenv leeria el .env real del proyecto y siempre encontraria un
// JWT_SECRET, haciendo imposible simular el caso "falta la variable".
jest.mock("dotenv", () => ({ config: jest.fn() }));

describe("config/env: JWT_SECRET obligatorio en produccion", () => {
  const ORIGINAL_ENV = process.env;

  beforeEach(() => {
    jest.resetModules();
    process.env = { ...ORIGINAL_ENV };
    delete process.env.JWT_SECRET;
  });

  afterAll(() => {
    process.env = ORIGINAL_ENV;
  });

  test("en produccion sin JWT_SECRET, el server no arranca", () => {
    process.env.NODE_ENV = "production";

    expect(() => require("../src/config/env")).toThrow(/JWT_SECRET/);
  });

  test("en desarrollo sin JWT_SECRET, arranca con el secreto de prueba", () => {
    process.env.NODE_ENV = "development";

    const env = require("../src/config/env");

    expect(env.auth.jwtSecret).toBe("dev-secret-cambiar-en-produccion");
  });

  test("con JWT_SECRET presente, la usa tal cual (en cualquier entorno)", () => {
    process.env.NODE_ENV = "production";
    process.env.JWT_SECRET = "un-secreto-real";

    const env = require("../src/config/env");

    expect(env.auth.jwtSecret).toBe("un-secreto-real");
  });
});
