package com.mindflow.nova.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda el token de sesión en el dispositivo, cifrado.
 *
 * El token es lo único que se persiste: el rol y el centro NO se guardan acá
 * porque el backend los resuelve contra la base en cada petición, así que
 * tenerlos cacheados solo daría datos viejos si a la persona la cambian de
 * rol o de sala.
 */
class SessionStorage(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "nova_session"
        const val KEY_TOKEN = "session_token"
    }
}
