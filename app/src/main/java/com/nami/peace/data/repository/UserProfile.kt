package com.nami.peace.data.repository

data class UserProfile(
    val name: String = "",
    val photoUri: String? = null,
    val bio: String = "",
    val occupation: String = "",
    val wakeTime: String = "",
    val bedTime: String = ""
)
