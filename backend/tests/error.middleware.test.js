const { errorHandler, notFound } = require("../src/middleware/error.middleware");

const fakeResponse = () => {
  const res = {};
  res.status = jest.fn(() => res);
  res.json = jest.fn(() => res);
  return res;
};

describe("errorHandler", () => {
  beforeEach(() => jest.spyOn(console, "error").mockImplementation(() => {}));
  afterEach(() => jest.restoreAllMocks());

  test("un cuerpo que no es JSON es un 400", () => {
    const res = fakeResponse();

    errorHandler({ type: "entity.parse.failed" }, {}, res, () => {});

    expect(res.status).toHaveBeenCalledWith(400);
    expect(res.json).toHaveBeenCalledWith({ message: "El cuerpo de la petición no es JSON válido", status: "ERROR" });
  });

  test("cualquier otro error es un 500 que no cuenta el detalle interno", () => {
    const res = fakeResponse();

    errorHandler(new Error('relation "users" does not exist'), {}, res, () => {});

    expect(res.status).toHaveBeenCalledWith(500);
    const body = res.json.mock.calls[0][0];
    expect(body).toEqual({ message: "Error interno del servidor", status: "ERROR", errorId: expect.any(String) });
    expect(JSON.stringify(body)).not.toContain("users");
  });

  test("el detalle queda en el log del servidor con el mismo id", () => {
    const res = fakeResponse();
    const error = new Error("detalle interno");

    errorHandler(error, {}, res, () => {});

    const { errorId } = res.json.mock.calls[0][0];
    expect(console.error).toHaveBeenCalledWith(`[${errorId}] Error interno del servidor`, error);
  });
});

describe("notFound", () => {
  test("una ruta que no existe responde JSON", () => {
    const res = fakeResponse();

    notFound({}, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith({ message: "Ruta no encontrada", status: "ERROR" });
  });
});
