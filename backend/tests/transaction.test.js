// La conexion real se reemplaza por una falsa que anota lo que se le pide,
// para probar la transaccion sin un Postgres.
const mockClient = { query: jest.fn(), release: jest.fn() };

jest.mock("../src/database/connection", () => ({
  connect: jest.fn(async () => mockClient)
}));

const { withTransaction } = require("../src/database/transaction");

const statements = () => mockClient.query.mock.calls.map((call) => call[0]);

describe("withTransaction", () => {
  beforeEach(() => {
    mockClient.query.mockReset();
    mockClient.release.mockReset();
  });

  test("confirma los cambios y devuelve lo que devuelve el trabajo", async () => {
    const result = await withTransaction(async (db) => {
      await db.query("UPDATE algo");
      return "listo";
    });

    expect(result).toBe("listo");
    expect(statements()).toEqual(["BEGIN", "UPDATE algo", "COMMIT"]);
    expect(mockClient.release).toHaveBeenCalledTimes(1);
  });

  test("si el trabajo falla deshace todo y propaga el error", async () => {
    const work = async (db) => {
      await db.query("UPDATE algo");
      throw new Error("falló a la mitad");
    };

    await expect(withTransaction(work)).rejects.toThrow("falló a la mitad");
    expect(statements()).toEqual(["BEGIN", "UPDATE algo", "ROLLBACK"]);
  });

  test("devuelve la conexion al pool aunque falle", async () => {
    await expect(withTransaction(async () => { throw new Error("x"); })).rejects.toThrow();

    expect(mockClient.release).toHaveBeenCalledTimes(1);
  });
});
