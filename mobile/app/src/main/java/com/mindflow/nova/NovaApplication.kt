package com.mindflow.nova

import android.app.Application
import com.mindflow.nova.data.remote.RetrofitClient
import com.mindflow.nova.data.session.SessionRepository
import com.mindflow.nova.data.session.ThemePreferences

/**
 * Arma la sesión al arrancar la app. Crear el [SessionRepository] acá es lo
 * que deja el interceptor listo para firmar las peticiones con el token
 * guardado, antes de que se dibuje cualquier pantalla.
 */
class NovaApplication : Application() {

    lateinit var session: SessionRepository
        private set

    lateinit var themePreferences: ThemePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        AppServices.init(this)
        session = AppServices.session
        themePreferences = ThemePreferences(this)
        RetrofitClient.warmUp()
        // Lo que quedó sin subir la última vez se sube apenas haya red.
        if (session.hasStoredToken()) AppServices.scheduleSync()
    }
}
