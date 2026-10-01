package com.mindflow.nova.data.local

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.PendingAttempt
import kotlinx.coroutines.flow.Flow

/**
 * Lo que la app guarda en el teléfono. Todo lo de una persona va con su
 * userId, así que en una tablet compartida nadie ve los datos de otro.
 */
interface LocalStore {
    suspend fun saveUser(user: SessionUser)
    suspend fun user(userId: Int): SessionUser?

    suspend fun saveLevels(userId: Int, levels: List<LevelResponse>)
    suspend fun levels(userId: Int): List<LevelResponse>?

    suspend fun saveMission(content: MissionContent)
    suspend fun mission(missionId: Int): MissionContent?
    /** Cuándo se guardó la misión (epoch ms), o null si no está. */
    suspend fun missionSavedAt(missionId: Int): Long?

    suspend fun saveProgress(userId: Int, progress: StudentProgress)
    suspend fun progress(userId: Int): StudentProgress?
    fun observeProgress(userId: Int): Flow<StudentProgress?>

    suspend fun addPending(attempt: PendingAttempt)
    suspend fun pending(userId: Int): List<PendingAttempt>
    fun observePending(userId: Int): Flow<List<PendingAttempt>>
    suspend fun removePending(clientAttemptIds: List<String>)

    /** Borra la caché de la cuenta al cerrar sesión; los pendientes se quedan. */
    suspend fun clearAccount(userId: Int)
}
