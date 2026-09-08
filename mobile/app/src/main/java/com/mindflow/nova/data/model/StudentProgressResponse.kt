package com.mindflow.nova.data.model

/** Respuesta de GET /api/students/:studentId/progress. */
data class StudentProgressResponse(
    val message: String?,
    val status: String?,
    val student: StudentProgress
)

data class StudentProgress(
    val id: Int,
    val fullName: String,
    /** Semillas ganadas en total, sumando repasos. */
    val totalPoints: Int,
    /** Misiones completadas (sin contar repasos) en toda la ruta, no solo un nivel. */
    val missionsCompleted: Int,
    val levels: List<StudentLevelProgress>
)

data class StudentLevelProgress(
    val id: Int,
    val name: String,
    val code: String,
    val orderIndex: Int,
    val progressPercentage: Double,
    /** "locked", "in_progress" o "completed". */
    val status: String
)
