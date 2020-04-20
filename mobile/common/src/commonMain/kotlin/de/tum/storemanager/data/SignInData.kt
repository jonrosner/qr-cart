package de.tum.storemanager.data

import kotlinx.serialization.Serializable

@Serializable
data class SignInData(
    val username: String,
    val password: String
)
