package com.mindflow.nova.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/"

    /**
     * De dónde sale el token en cada petición. Lo setea NovaApplication al
     * arrancar; mientras sea null la app solo puede usar rutas públicas.
     */
    @Volatile
    var tokenProvider: () -> String? = { null }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
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
}
