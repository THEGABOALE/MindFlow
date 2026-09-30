const { canViewStudent } = require("../src/services/student-access.service");

// Estudiante 2, en una sala del docente 4 que pertenece al centro 1.
const student = { student_id: 2, group_teacher_id: 4, group_center_id: 1 };

describe("canViewStudent", () => {
  test("el admin ve a cualquier estudiante", () => {
    expect(canViewStudent({ id: 5, role: "admin", centerId: null }, student)).toBe(true);
  });

  test("el estudiante solo se ve a si mismo", () => {
    expect(canViewStudent({ id: 2, role: "student", centerId: 1 }, student)).toBe(true);
    expect(canViewStudent({ id: 9, role: "student", centerId: 1 }, student)).toBe(false);
  });

  test("el docente solo ve a los de su sala", () => {
    expect(canViewStudent({ id: 4, role: "teacher", centerId: 1 }, student)).toBe(true);
    expect(canViewStudent({ id: 8, role: "teacher", centerId: 1 }, student)).toBe(false);
  });

  test("una sala sin docente no la ve ningun docente", () => {
    const sinDocente = { ...student, group_teacher_id: null };

    expect(canViewStudent({ id: 4, role: "teacher", centerId: 1 }, sinDocente)).toBe(false);
  });

  test("el coordinador solo ve a los de su centro", () => {
    expect(canViewStudent({ id: 3, role: "coordinator", centerId: 1 }, student)).toBe(true);
    expect(canViewStudent({ id: 3, role: "coordinator", centerId: 7 }, student)).toBe(false);
  });

  test("un coordinador sin centro no ve a nadie, ni a salas sin centro", () => {
    const sinCentro = { ...student, group_center_id: null };

    expect(canViewStudent({ id: 3, role: "coordinator", centerId: null }, student)).toBe(false);
    expect(canViewStudent({ id: 3, role: "coordinator", centerId: null }, sinCentro)).toBe(false);
  });

  test("cualquier otro rol no ve a nadie", () => {
    expect(canViewStudent({ id: 6, role: "validator", centerId: 1 }, student)).toBe(false);
  });

  test("compara el id aunque venga como texto", () => {
    expect(canViewStudent({ id: "2", role: "student", centerId: 1 }, student)).toBe(true);
  });
});
