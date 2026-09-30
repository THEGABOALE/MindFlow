// Quien puede ver los datos de un estudiante. Es una regla pura: recibe a
// quien pregunta y los datos de la sala del estudiante, sin tocar la base.
//
// El estudiante solo se ve a si mismo, el profesor solo a su sala y el
// coordinador solo a su centro. El admin (equipo MindFlow) ve todo.
const canViewStudent = (requester, student) => {
  if (requester.role === "admin") {
    return true;
  }

  if (requester.role === "student") {
    return Number(requester.id) === student.student_id;
  }

  if (requester.role === "teacher") {
    return Number(requester.id) === student.group_teacher_id;
  }

  if (requester.role === "coordinator") {
    return requester.centerId !== null && Number(requester.centerId) === student.group_center_id;
  }

  return false;
};

module.exports = { canViewStudent };
