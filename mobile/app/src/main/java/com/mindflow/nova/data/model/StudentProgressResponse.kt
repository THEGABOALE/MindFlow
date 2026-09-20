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
    /** IDs de todas las misiones completadas, de cualquier nivel. */
    val completedMissionIds: List<Int> = emptyList(),
    /** Racha de días haciendo misiones. Null si el backend todavía no la manda. */
    val streak: StudentStreak? = null,
    val levels: List<StudentLevelProgress>
)

data class StudentStreak(
    /** Días seguidos haciendo al menos una misión. */
    val days: Int,
    /** True si ya hizo una misión hoy (llama); false si está congelada (hielo). */
    val isActive: Boolean,
    /** Último día con actividad, "YYYY-MM-DD", o null si nunca hizo una. */
    val lastActivityDate: String?
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
