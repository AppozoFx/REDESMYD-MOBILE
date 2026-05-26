package com.redes.app.data.auth

import kotlinx.coroutines.flow.Flow

data class AuthUser(
    val uid: String,
    val email: String?,
)

interface AuthRepository {
    val currentUser: Flow<AuthUser?>

    suspend fun signIn(email: String, password: String): Result<AuthUser>

    fun signOut()
}
