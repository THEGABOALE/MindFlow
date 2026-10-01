const { missionStartBlock } = require("../src/services/mission-access.service");

// Sala de Primaria alta (nivel 1).
const studentGroup = { level_id: 1 };

describe("missionStartBlock", () => {
  test("una mision de su nivel con la anterior completada se puede abrir", () => {
    expect(missionStartBlock({ studentGroup, mission: { level_id: 1 }, previousMissionCompleted: true })).toBeNull();
  });

  test("sin sala no puede abrir ninguna mision", () => {
    expect(missionStartBlock({ studentGroup: null, mission: { level_id: 1 }, previousMissionCompleted: true })).toBe(
      "Primero únete a una sala con el código de tu docente"
    );
  });

  test("una mision de otro nivel no se puede abrir", () => {
    expect(missionStartBlock({ studentGroup, mission: { level_id: 3 }, previousMissionCompleted: true })).toBe(
      "Esta misión no es de tu nivel"
    );
  });

  test("sin completar la anterior no se puede abrir", () => {
    expect(missionStartBlock({ studentGroup, mission: { level_id: 1 }, previousMissionCompleted: false })).toBe(
      "Primero completa la misión anterior"
    );
  });

  test("el nivel pesa mas que el orden: otro nivel se rechaza aunque no haya anterior", () => {
    expect(missionStartBlock({ studentGroup, mission: { level_id: 2 }, previousMissionCompleted: false })).toBe(
      "Esta misión no es de tu nivel"
    );
  });
});
