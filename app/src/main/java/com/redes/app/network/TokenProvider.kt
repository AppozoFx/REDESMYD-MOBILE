package com.redes.app.network

interface TokenProvider {
    suspend fun getIdToken(forceRefresh: Boolean = false): String?
}
