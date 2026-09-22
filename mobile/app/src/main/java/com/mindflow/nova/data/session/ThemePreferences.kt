package com.mindflow.nova.data.session

import android.content.Context
import android.content.SharedPreferences

/**
 * Guarda si la persona eligió el modo oscuro. Va aparte de [SessionStorage]
 * porque no es un dato sensible y tiene que sobrevivir al cierre de sesión.
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    private companion object {
        const val PREFS_NAME = "nova_preferences"
        const val KEY_DARK_MODE = "dark_mode"
    }
}
