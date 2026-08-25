package com.mindflow.nova.data.model

data class JoinGroupRequest(
    val code: String,
    val studentName: String? = null
)

data class JoinGroupResponse(
    val message: String,
    val status: String,
    val student: StudentResponse?,
    val group: GroupResponse?,
    val level: JoinedLevelResponse?
)

data class StudentResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val roleId: Int
)

data class GroupResponse(
    val id: Int,
    val name: String,
    val grade: String,
    val section: String,
    val SchoolYear: Int
)

data class JoinedLevelResponse(
    val id: Int,
    val name: String,
    val code: String,
    val description: String?
)