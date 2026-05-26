package com.redes.app.data.session

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    val isConfigured: Boolean
    val cachedSession: Flow<MobileSession?>
    val selectedRole: Flow<String?>

    suspend fun fetchBootstrap(): Result<MobileBootstrap>
    suspend fun markComunicadoSeen(comunicadoId: String): Result<Unit>
    suspend fun saveSelectedRole(role: String?)
    suspend fun clearCache()
}
