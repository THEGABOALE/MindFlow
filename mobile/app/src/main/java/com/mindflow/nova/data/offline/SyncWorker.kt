package com.mindflow.nova.data.offline

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mindflow.nova.AppServices

/**
 * Sube en segundo plano los resultados pendientes de la cuenta con la sesión
 * abierta. Lo programa AppServices.scheduleSync, que espera a que haya red. Las
 * colas de otras cuentas del mismo teléfono esperan a que esa persona entre.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = AppServices.session.currentUserId() ?: return Result.success()
        val report = AppServices.sync.syncPending(userId)

        return if (shouldRetry(report.stoppedBy)) Result.retry() else Result.success()
    }
}

/**
 * Sin red o con el servidor fallando se vuelve a intentar más tarde. Con la
 * sesión vencida no: hasta que la persona vuelva a entrar fallaría siempre, y
 * al iniciar sesión se programa de nuevo.
 */
internal fun shouldRetry(stop: SyncStop?): Boolean = stop == SyncStop.NETWORK || stop == SyncStop.SERVER
