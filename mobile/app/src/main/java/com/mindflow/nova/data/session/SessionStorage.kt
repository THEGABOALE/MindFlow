package com.mindflow.nova.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda el token de sesión en el dispositivo, cifrado.
 *
 * Solo se guardan el token y el id de la persona: el rol y el centro NO van
 * acá porque el backend los resuelve contra la base en cada petición. Para
 * abrir sin conexión, la cuenta completa se guarda aparte (LocalStore) y se
 * busca con este id.
 */
class SessionStorage(context: Context) : TokenStore {

    private val prefs: SharedPreferences = try {
        openEncrypted(context)
    } catch (e: Exception) {
        // Si el archivo quedó cifrado con otra clave (por ejemplo, llegó en un
        // respaldo desde otro teléfono), no se puede leer y la app se cerraba al
        // abrir. Se borra y se empieza de cero: la persona vuelve a iniciar sesión.
        context.deleteSharedPreferences(PREFS_NAME)
        openEncrypted(context)
    }

    private fun openEncrypted(context: Context): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    override fun getUserId(): Int? = prefs.getInt(KEY_USER_ID, 0).takeIf { it > 0 }

    override fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "nova_session"
        const val KEY_TOKEN = "session_token"
        const val KEY_USER_ID = "session_user_id"
    }
}
