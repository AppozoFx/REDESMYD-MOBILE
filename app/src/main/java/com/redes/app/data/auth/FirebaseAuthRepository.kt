package com.redes.app.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.redes.app.data.common.awaitResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth,
) : AuthRepository {

    override val currentUser: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }

        firebaseAuth.addAuthStateListener(listener)
        trySend(firebaseAuth.currentUser?.toAuthUser())

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).awaitResult()
            val user = authResult.user?.toAuthUser()
                ?: return Result.failure(IllegalStateException("No se pudo obtener el usuario autenticado."))
            Result.success(user)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}

private fun FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email,
    )
}
