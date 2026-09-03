package com.mindflow.nova.data.model

/** Login por ID + contraseña, para primaria donde no hay correo institucional. */
data class LoginIdRequest(
    val loginId: String,
    val password: String
)

/** Login con el correo institucional validado por Google. */
data class LoginGoogleRequest(
    val idToken: String
)

data class SessionUser(
    val id: Int,
    val fullName: String,
    val email: String?,
    val loginId: String?,
    /** student, teacher, coordinator, admin o validator. */
    val role: String,
    val centerId: Int?
)

/** Respuesta de los dos logins: trae el token de sesión y quién es la persona. */
data class LoginResponse(
    val message: String,
    val status: String,
    val token: String?,
    val user: SessionUser?
)

/** Respuesta de /api/auth/me: valida el token guardado, sin emitir uno nuevo. */
data class MeResponse(
    val message: String,
    val status: String,
    val user: SessionUser?
)
