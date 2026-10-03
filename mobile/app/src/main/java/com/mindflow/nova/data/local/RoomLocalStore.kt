package com.mindflow.nova.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mindflow.nova.data.model.AnswerSubmission
import com.mindflow.nova.data.model.AttemptResult
import com.mindflow.nova.data.model.LevelResponse
import com.mindflow.nova.data.model.MissionContent
import com.mindflow.nova.data.model.SessionUser
import com.mindflow.nova.data.model.StudentProgress
import com.mindflow.nova.data.offline.PendingAttempt
import com.mindflow.nova.data.offline.RejectedNotice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** [LocalStore] sobre Room: cada cosa se guarda como el mismo JSON que manda el backend. */
class RoomLocalStore(
    private val dao: NovaDao,
    private val gson: Gson = Gson(),
    private val now: () -> Long = System::currentTimeMillis
) : LocalStore {

    private val levelsType = object : TypeToken<List<LevelResponse>>() {}.type
    private val answersType = object : TypeToken<List<AnswerSubmission>>() {}.type

    override suspend fun saveUser(user: SessionUser) =
        dao.upsertUser(CachedUserEntity(user.id, gson.toJson(user), now()))

    override suspend fun user(userId: Int): SessionUser? =
        dao.user(userId)?.let { gson.fromJson(it.json, SessionUser::class.java) }

    override suspend fun saveLevels(userId: Int, levels: List<LevelResponse>) =
        dao.upsertLevels(CachedLevelsEntity(userId, gson.toJson(levels), now()))

    override suspend fun levels(userId: Int): List<LevelResponse>? =
        dao.levels(userId)?.let { gson.fromJson(it.json, levelsType) }

    override suspend fun saveMission(content: MissionContent) =
        dao.upsertMission(CachedMissionEntity(content.id, gson.toJson(content), now()))

    override suspend fun mission(missionId: Int): MissionContent? =
        dao.mission(missionId)?.let { gson.fromJson(it.json, MissionContent::class.java) }

    override suspend fun missionSavedAt(missionId: Int): Long? = dao.mission(missionId)?.updatedAt

    override suspend fun saveProgress(userId: Int, progress: StudentProgress) =
        dao.upsertProgress(CachedProgressEntity(userId, gson.toJson(progress), now()))

    override suspend fun progress(userId: Int): StudentProgress? =
        dao.progress(userId)?.let { gson.fromJson(it.json, StudentProgress::class.java) }

    override fun observeProgress(userId: Int): Flow<StudentProgress?> =
        dao.observeProgress(userId).map { entity ->
            entity?.let { gson.fromJson(it.json, StudentProgress::class.java) }
        }

    override suspend fun addPending(attempt: PendingAttempt) =
        dao.upsertPending(
            PendingAttemptEntity(
                clientAttemptId = attempt.clientAttemptId,
                userId = attempt.userId,
                missionId = attempt.missionId,
                startedAt = attempt.startedAt,
                finishedAt = attempt.finishedAt,
                tzOffsetMinutes = attempt.tzOffsetMinutes,
                timedOut = attempt.timedOut,
                answersJson = gson.toJson(attempt.answers),
                provisionalJson = gson.toJson(attempt.provisional),
                createdAt = now(),
                pausedSeconds = attempt.pausedSeconds,
                usedExtraTime = attempt.usedExtraTime
            )
        )

    override suspend fun pending(userId: Int): List<PendingAttempt> = dao.pending(userId).map(::toPending)

    override fun observePending(userId: Int): Flow<List<PendingAttempt>> =
        dao.observePending(userId).map { list -> list.map(::toPending) }

    override suspend fun settlePending(userId: Int, doneIds: List<String>, notices: List<RejectedNotice>) =
        dao.settlePending(doneIds, notices.map { RejectedNoticeEntity(it.clientAttemptId, userId, it.message, now()) })

    override fun observeNotices(userId: Int): Flow<List<RejectedNotice>> =
        dao.observeNotices(userId).map { list -> list.map { RejectedNotice(it.clientAttemptId, it.message) } }

    override suspend fun clearNotices(userId: Int) = dao.deleteNotices(userId)

    override suspend fun removeNotice(clientAttemptId: String) = dao.deleteNotice(clientAttemptId)

    override suspend fun clearAccount(userId: Int) {
        dao.deleteUser(userId)
        dao.deleteLevels(userId)
        dao.deleteProgress(userId)
    }

    private fun toPending(entity: PendingAttemptEntity) = PendingAttempt(
        clientAttemptId = entity.clientAttemptId,
        userId = entity.userId,
        missionId = entity.missionId,
        startedAt = entity.startedAt,
        finishedAt = entity.finishedAt,
        tzOffsetMinutes = entity.tzOffsetMinutes,
        timedOut = entity.timedOut,
        answers = gson.fromJson(entity.answersJson, answersType),
        provisional = gson.fromJson(entity.provisionalJson, AttemptResult::class.java),
        pausedSeconds = entity.pausedSeconds,
        usedExtraTime = entity.usedExtraTime
    )
}
