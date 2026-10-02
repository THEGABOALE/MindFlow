package com.mindflow.nova.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NovaDao {
    @Upsert suspend fun upsertUser(entity: CachedUserEntity)
    @Query("SELECT * FROM cached_user WHERE userId = :userId") suspend fun user(userId: Int): CachedUserEntity?

    @Upsert suspend fun upsertLevels(entity: CachedLevelsEntity)
    @Query("SELECT * FROM cached_levels WHERE userId = :userId") suspend fun levels(userId: Int): CachedLevelsEntity?

    @Upsert suspend fun upsertMission(entity: CachedMissionEntity)
    @Query("SELECT * FROM cached_mission WHERE missionId = :missionId") suspend fun mission(missionId: Int): CachedMissionEntity?

    @Upsert suspend fun upsertProgress(entity: CachedProgressEntity)
    @Query("SELECT * FROM cached_progress WHERE userId = :userId") suspend fun progress(userId: Int): CachedProgressEntity?
    @Query("SELECT * FROM cached_progress WHERE userId = :userId") fun observeProgress(userId: Int): Flow<CachedProgressEntity?>

    @Upsert suspend fun upsertPending(entity: PendingAttemptEntity)
    @Query("SELECT * FROM pending_attempt WHERE userId = :userId ORDER BY finishedAt")
    suspend fun pending(userId: Int): List<PendingAttemptEntity>
    @Query("SELECT * FROM pending_attempt WHERE userId = :userId ORDER BY finishedAt")
    fun observePending(userId: Int): Flow<List<PendingAttemptEntity>>
    @Query("DELETE FROM pending_attempt WHERE clientAttemptId IN (:ids)") suspend fun deletePending(ids: List<String>)

    @Upsert suspend fun upsertNotices(entities: List<RejectedNoticeEntity>)
    @Query("SELECT * FROM rejected_notice WHERE userId = :userId ORDER BY createdAt")
    fun observeNotices(userId: Int): Flow<List<RejectedNoticeEntity>>
    @Query("DELETE FROM rejected_notice WHERE userId = :userId") suspend fun deleteNotices(userId: Int)
    @Query("DELETE FROM rejected_notice WHERE clientAttemptId = :clientAttemptId") suspend fun deleteNotice(clientAttemptId: String)

    /** Saca de la cola lo que el servidor ya resolvió y guarda los avisos de rechazo, todo o nada. */
    @Transaction
    suspend fun settlePending(doneIds: List<String>, notices: List<RejectedNoticeEntity>) {
        if (notices.isNotEmpty()) upsertNotices(notices)
        if (doneIds.isNotEmpty()) deletePending(doneIds)
    }

    @Query("DELETE FROM cached_user WHERE userId = :userId") suspend fun deleteUser(userId: Int)
    @Query("DELETE FROM cached_levels WHERE userId = :userId") suspend fun deleteLevels(userId: Int)
    @Query("DELETE FROM cached_progress WHERE userId = :userId") suspend fun deleteProgress(userId: Int)
}
