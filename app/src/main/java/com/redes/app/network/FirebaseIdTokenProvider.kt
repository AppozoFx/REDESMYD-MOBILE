package com.redes.app.network

import com.google.firebase.auth.FirebaseAuth
import com.redes.app.data.common.awaitResult

class FirebaseIdTokenProvider(
    private val firebaseAuth: FirebaseAuth,
) : TokenProvider {

    override suspend fun getIdToken(forceRefresh: Boolean): String? {
        val currentUser = firebaseAuth.currentUser ?: return null
        return currentUser.getIdToken(forceRefresh).awaitResult().token
    }
}
