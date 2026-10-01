package com.mindflow.nova.data.local

import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.PendingAttempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** [LocalStore] en memoria para los tests: los observadores emiten al cambiar algo. */
class FakeLocalStore(private val now: () -> Long = { 0L }) : LocalStore {

    private val users = MutableStateFlow<Map<Int, SessionUser>>(emptyMap())
    private val levels = MutableStateFlow<Map<Int, List<LevelResponse>>>(emptyMap())
    private val missions = MutableStateFlow<Map<Int, Pair<MissionContent, Long>>>(emptyMap())
    private val progress = MutableStateFlow<Map<Int, StudentProgress>>(emptyMap())
    private val pending = MutableStateFlow<Map<String, PendingAttempt>>(emptyMap())

    val clearedAccounts = mutableListOf<Int>()

    fun pendingNow(userId: Int): List<PendingAttempt> =
        pending.value.values.filter { it.userId == userId }.sortedBy { it.finishedAt }

    override suspend fun saveUser(user: SessionUser) = users.update { it + (user.id to user) }
    override suspend fun user(userId: Int): SessionUser? = users.value[userId]

    override suspend fun saveLevels(userId: Int, levels: List<LevelResponse>) =
        this.levels.update { it + (userId to levels) }
    override suspend fun levels(userId: Int): List<LevelResponse>? = levels.value[userId]

    override suspend fun saveMission(content: MissionContent) =
        missions.update { it + (content.id to (content to now())) }
    override suspend fun mission(missionId: Int): MissionContent? = missions.value[missionId]?.first
    override suspend fun missionSavedAt(missionId: Int): Long? = missions.value[missionId]?.second

    override suspend fun saveProgress(userId: Int, progress: StudentProgress) =
        this.progress.update { it + (userId to progress) }
    override suspend fun progress(userId: Int): StudentProgress? = progress.value[userId]
    override fun observeProgress(userId: Int): Flow<StudentProgress?> = progress.map { it[userId] }

    override suspend fun addPending(attempt: PendingAttempt) =
        pending.update { it + (attempt.clientAttemptId to attempt) }
    override suspend fun pending(userId: Int): List<PendingAttempt> = pendingNow(userId)
    override fun observePending(userId: Int): Flow<List<PendingAttempt>> =
        pending.map { all -> all.values.filter { it.userId == userId }.sortedBy { it.finishedAt } }
    override suspend fun removePending(clientAttemptIds: List<String>) =
        pending.update { it - clientAttemptIds.toSet() }

    override suspend fun clearAccount(userId: Int) {
        clearedAccounts += userId
        users.update { it - userId }
        levels.update { it - userId }
        progress.update { it - userId }
    }
}
