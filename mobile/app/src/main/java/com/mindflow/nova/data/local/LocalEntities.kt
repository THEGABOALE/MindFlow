package com.mindflow.nova.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Lo que se guarda en el teléfono para funcionar sin conexión. El contenido va
// como JSON en una sola columna: siempre se lee entero, nunca por partes.

@Entity(tableName = "cached_user")
data class CachedUserEntity(@PrimaryKey val userId: Int, val json: String, val updatedAt: Long)

@Entity(tableName = "cached_levels")
data class CachedLevelsEntity(@PrimaryKey val userId: Int, val json: String, val updatedAt: Long)

@Entity(tableName = "cached_mission")
data class CachedMissionEntity(@PrimaryKey val missionId: Int, val json: String, val updatedAt: Long)

@Entity(tableName = "cached_progress")
data class CachedProgressEntity(@PrimaryKey val userId: Int, val json: String, val updatedAt: Long)

@Entity(tableName = "pending_attempt", indices = [Index("userId")])
data class PendingAttemptEntity(
    @PrimaryKey val clientAttemptId: String,
    val userId: Int,
    val missionId: Int,
    val startedAt: String,
    val finishedAt: String,
    val tzOffsetMinutes: Int,
    val timedOut: Boolean,
    val answersJson: String,
    val provisionalJson: String,
    val createdAt: Long
)
