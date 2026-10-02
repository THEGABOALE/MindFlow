package com.mindflow.nova

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.local.NovaDatabase
import com.mindflow.nova.data.local.RoomLocalStore
import com.mindflow.nova.data.offline.SyncRepository
import com.mindflow.nova.data.offline.SyncWorker
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.remote.networkAvailability
import com.mindflow.nova.data.session.SessionRepository
import com.mindflow.nova.data.session.SessionStorage
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/**
 * Lo que comparte toda la app y vive mientras vive el proceso. Lo arma
 * NovaApplication al arrancar; los ViewModels lo usan como valor por defecto.
 */
object AppServices {
    lateinit var localStore: LocalStore
        private set

    lateinit var session: SessionRepository
        private set

    /** Una sola para toda la app: el cierre de las lecciones y la subida en segundo plano comparten su candado. */
    lateinit var sync: SyncRepository
        private set

    private lateinit var appContext: Context

    /** True cuando hay red, false cuando se pierde. */
    val networkAvailable: Flow<Boolean> by lazy { networkAvailability(appContext) }

    fun init(context: Context) {
        appContext = context.applicationContext
        localStore = RoomLocalStore(NovaDatabase.create(context).dao())
        // Crear la sesión deja el interceptor listo para firmar las peticiones.
        session = SessionRepository(SessionStorage(context), localStore)
        sync = SyncRepository(api = { RetrofitClient.api }, store = localStore, tokenFor = session::tokenFor)
    }

    /**
     * Pide subir los resultados pendientes en segundo plano apenas haya red,
     * aunque la app esté cerrada. Si ya hay una subida programada no se agrega
     * otra; si falla, WorkManager la reintenta cada vez más espaciada.
     */
    fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    private const val SYNC_WORK_NAME = "sync-attempts"
}
