package com.mindflow.nova.data.model

/** El estudiante ya tiene que estar logueado; el código solo lo matricula en la sala. */
data class JoinGroupRequest(
    val code: String
)

data class JoinGroupResponse(
    val message: String,
    val status: String,
    /** true si ya estaba en esa sala (no se creó ni se movió nada). */
    val alreadyEnrolled: Boolean,
    val group: GroupResponse?,
    val level: JoinedLevelResponse?
)

data class GroupResponse(
    val id: Int,
    val name: String,
    val grade: String,
    val section: String?,
    val schoolYear: Int
)

data class JoinedLevelResponse(
    val id: Int,
    val name: String,
    val code: String,
    val description: String?
)
