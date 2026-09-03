package com.mindflow.nova

import android.app.Application
import com.mindflow.nova.data.session.SessionRepository
import com.mindflow.nova.data.session.SessionStorage

/**
 * Arma la sesión al arrancar la app. Crear el [SessionRepository] acá es lo
 * que deja el interceptor listo para firmar las peticiones con el token
 * guardado, antes de que se dibuje cualquier pantalla.
 */
class NovaApplication : Application() {

    lateinit var session: SessionRepository
        private set

    override fun onCreate() {
        super.onCreate()
        session = SessionRepository(SessionStorage(this))
    }
}
