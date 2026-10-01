package com.mindflow.nova.data.session

/** Dónde queda la sesión entre aperturas de la app: el token y de quién es. */
interface TokenStore {
    fun getToken(): String?
    fun saveToken(token: String)

    /** Id de la persona dueña del token, para abrir su cuenta guardada sin conexión. */
    fun getUserId(): Int?
    fun saveUserId(userId: Int)

    fun clear()
}
