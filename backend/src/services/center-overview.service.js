const pool = require("../database/connection");
const groupRepository = require("../repositories/group.repository");

// Progreso general de todas las salas de un centro: cuantos alumnos tiene
// cada una, quien es su docente y el avance promedio en el nivel de esa sala.
// Lo usan tanto el coordinador (solo su propio centro) como el admin
// (cualquier centro), asi que vive en un solo lugar.
const getCenterRoomsOverview = async (centerId) => {
  const roomRows = await groupRepository.findCenterRooms(pool, centerId);

  const rooms = roomRows.map((room) => ({
    id: room.id,
    name: room.name,
    grade: room.grade,
    section: room.section,
    schoolYear: room.school_year,
    levelId: room.level_id,
    teacherName: room.teacher_name,
    studentCount: Number(room.student_count),
    averageProgressPercentage: Number(room.average_progress)
  }));

  return {
    totalStudents: rooms.reduce((sum, room) => sum + room.studentCount, 0),
    rooms
  };
};

module.exports = {
  getCenterRoomsOverview
};
