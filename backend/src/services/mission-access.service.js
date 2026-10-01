// Quien puede abrir un intento de una mision. Es una regla pura: recibe la
// sala del estudiante, la mision y si ya completo la anterior, sin tocar la
// base. La app ya bloquea lo mismo en la ruta, pero la regla que vale es esta:
// sin ella, cualquiera con su token podria saltarse niveles y misiones.

/**
 * @param {object} args
 * @param {{level_id:number}|null} args.studentGroup sala activa del estudiante
 * @param {{level_id:number}} args.mission
 * @param {boolean} args.previousMissionCompleted true si ya completo la mision
 *   anterior del nivel, o si esta es la primera
 * @returns {string|null} por que no puede abrirla, o null si puede
 */
const missionStartBlock = ({ studentGroup, mission, previousMissionCompleted }) => {
  if (!studentGroup) {
    return "Primero únete a una sala con el código de tu docente";
  }

  if (mission.level_id !== studentGroup.level_id) {
    return "Esta misión no es de tu nivel";
  }

  if (!previousMissionCompleted) {
    return "Primero completa la misión anterior";
  }

  return null;
};

module.exports = { missionStartBlock };
