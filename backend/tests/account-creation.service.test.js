const {
  canCreateRole,
  minPasswordLength,
  resolveNewAccountCenter
} = require("../src/services/account-creation.service");

const admin = { id: 5, role: "admin", centerId: null };
const coordinator = { id: 3, role: "coordinator", centerId: 1 };

describe("canCreateRole", () => {
  test("el coordinador solo crea estudiantes y docentes", () => {
    expect(canCreateRole("coordinator", "student")).toBe(true);
    expect(canCreateRole("coordinator", "teacher")).toBe(true);
    expect(canCreateRole("coordinator", "coordinator")).toBe(false);
    expect(canCreateRole("coordinator", "admin")).toBe(false);
  });

  test("el admin crea cualquier rol", () => {
    ["student", "teacher", "coordinator", "admin", "validator"].forEach((role) => {
      expect(canCreateRole("admin", role)).toBe(true);
    });
  });

  test("los demas roles no crean cuentas", () => {
    expect(canCreateRole("teacher", "student")).toBe(false);
    expect(canCreateRole("student", "student")).toBe(false);
  });
});

describe("minPasswordLength", () => {
  test("estudiantes 4, el resto 8", () => {
    expect(minPasswordLength("student")).toBe(4);
    expect(minPasswordLength("teacher")).toBe(8);
    expect(minPasswordLength("coordinator")).toBe(8);
    expect(minPasswordLength("admin")).toBe(8);
  });
});

describe("resolveNewAccountCenter", () => {
  test("el coordinador crea en su centro e ignora el que mande", () => {
    expect(resolveNewAccountCenter({ requester: coordinator, role: "teacher", requestedCenterId: 9 })).toEqual({
      centerId: 1
    });
  });

  test("el admin tiene que indicar el centro de docentes y coordinadores", () => {
    expect(resolveNewAccountCenter({ requester: admin, role: "coordinator" })).toEqual({
      error: "centerId es obligatorio para crear docentes y coordinadores"
    });
    expect(resolveNewAccountCenter({ requester: admin, role: "teacher", requestedCenterId: 2 })).toEqual({
      centerId: 2
    });
  });

  test("el admin puede crear un estudiante con o sin centro", () => {
    expect(resolveNewAccountCenter({ requester: admin, role: "student" })).toEqual({ centerId: null });
    expect(resolveNewAccountCenter({ requester: admin, role: "student", requestedCenterId: "4" })).toEqual({
      centerId: 4
    });
  });

  test("las cuentas del equipo MindFlow no tienen centro", () => {
    expect(resolveNewAccountCenter({ requester: admin, role: "admin", requestedCenterId: 2 })).toEqual({
      centerId: null
    });
  });

  test("un centerId que no es entero positivo se rechaza", () => {
    ["abc", -1, 0, 1.5, "2x", {}].forEach((requestedCenterId) => {
      expect(resolveNewAccountCenter({ requester: admin, role: "teacher", requestedCenterId })).toEqual({
        error: "El centerId debe ser numérico"
      });
    });
  });
});
