package com.mindflow.nova.ui.screens.teacher

import androidx.compose.ui.graphics.Color

data class TeacherStudent(
    val name: String,
    val elapsedTime: String?
)

data class MinigameResult(
    val name: String,
    val percentage: Int
)

data class TeacherRoom(
    val id: Int,
    val name: String,
    val accentColor: Color,
    val levelLabel: String,
    val gradeLabels: List<String>,
    val teacherName: String,
    val studentCount: Int,
    val roomsAssigned: Int,
    val minigamesScheduled: Int,
    val students: List<TeacherStudent>,
    val minigameResults: List<MinigameResult>
)

/**
 * Datos de ejemplo para el panel docente ("Salas" / perfil de maestro). No
 * existe todavía backend para salones, alumnos ni resultados por minijuego;
 * esta pantalla es de solo lectura sobre datos mock, siguiendo el wireframe.
 */
object TeacherMockData {

    private val demoStudents = listOf(
        TeacherStudent("Miguel Ángel Castillo Ruiz", "2:30s"),
        TeacherStudent("Mateo Alejandro Rojas Benítez", null),
        TeacherStudent("Hugo Arce Ríos", "1:12s"),
        TeacherStudent("Juana María Sánchez Berríos", "1:55s"),
        TeacherStudent("Magda Francela Berríos Martínez", null),
        TeacherStudent("Jorge Román Torres Ramírez", null)
    )

    private val demoResults = listOf(
        MinigameResult("Cuestionario verdadero o falso", 75),
        MinigameResult("Cuestionario opción múltiple", 75),
        MinigameResult("Sopa de letras", 70)
    )

    val rooms = listOf(
        TeacherRoom(
            id = 1,
            name = "Primaria alta",
            accentColor = Color(0xFFCFE0FF),
            levelLabel = "Primaria",
            gradeLabels = listOf("Primer grado", "Segundo Grado", "Tercer Grado"),
            teacherName = "José Martínez",
            studentCount = 130,
            roomsAssigned = 10,
            minigamesScheduled = 5,
            students = demoStudents,
            minigameResults = demoResults
        ),
        TeacherRoom(
            id = 2,
            name = "Secundaria baja",
            accentColor = Color(0xFFBFEAE0),
            levelLabel = "Secundario",
            gradeLabels = listOf("Primer año", "Segundo año", "Tercer año"),
            teacherName = "José Martínez",
            studentCount = 13,
            roomsAssigned = 10,
            minigamesScheduled = 5,
            students = demoStudents,
            minigameResults = demoResults
        ),
        TeacherRoom(
            id = 3,
            name = "Secundaria alta",
            accentColor = Color(0xFFC7CDFB),
            levelLabel = "Secundario",
            gradeLabels = listOf("Cuarto año", "Quinto año"),
            teacherName = "José Martínez",
            studentCount = 13,
            roomsAssigned = 10,
            minigamesScheduled = 5,
            students = demoStudents,
            minigameResults = demoResults
        )
    )
}
