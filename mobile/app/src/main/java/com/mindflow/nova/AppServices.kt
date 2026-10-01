package com.mindflow.nova

import android.content.Context
import com.mindflow.nova.data.local.LocalStore
import com.mindflow.nova.data.local.NovaDatabase
import com.mindflow.nova.data.local.RoomLocalStore
import com.mindflow.nova.data.offline.SyncRepository
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.session.SessionRepository
import com.mindflow.nova.data.session.SessionStorage

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

    fun init(context: Context) {
        localStore = RoomLocalStore(NovaDatabase.create(context).dao())
        // Crear la sesión deja el interceptor listo para firmar las peticiones.
        session = SessionRepository(SessionStorage(context), localStore)
        sync = SyncRepository(api = { RetrofitClient.api }, store = localStore)
    }
}
