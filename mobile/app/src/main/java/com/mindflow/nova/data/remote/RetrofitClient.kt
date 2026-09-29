package com.mindflow.nova.data.remote

import com.mindflow.nova.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Debug apunta al emulador local, release al backend de Railway.
    // Ver app/build.gradle.kts.
    private val BASE_URL = BuildConfig.BASE_URL

    /**
     * De dónde sale el token en cada petición. Lo setea NovaApplication al
     * arrancar; mientras sea null la app solo puede usar rutas públicas.
     */
    @Volatile
    var tokenProvider: () -> String? = { null }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            // El default de OkHttp (10 s) no alcanza cuando Railway y la base
            // de Neon están dormidos: la primera petición tiene que esperar a
            // que arranquen antes de responder.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: NovaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NovaApiService::class.java)
    }

    private val warmUpScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Despierta el backend y la base apenas abre la app, sin esperar respuesta:
     * mientras la persona ve el splash o escribe su usuario, el arranque en frío
     * ya va corriendo, y el login no tiene que cargar con esa espera.
     */
    fun warmUp() {
        warmUpScope.launch {
            runCatching { api.getDatabaseHealth() }
        }
    }
}
