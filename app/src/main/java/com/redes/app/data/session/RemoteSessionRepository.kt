package com.redes.app.data.session

import com.redes.app.data.local.SessionCacheDataSource
import com.redes.app.network.RedesApiClient
import com.redes.app.network.RedesApiException
import com.redes.app.network.TokenProvider
import kotlinx.coroutines.flow.Flow

class RemoteSessionRepository(
    private val apiClient: RedesApiClient,
    private val tokenProvider: TokenProvider,
    private val cacheDataSource: SessionCacheDataSource,
) : SessionRepository {

    override val isConfigured: Boolean
        get() = apiClient.isConfigured

    override val cachedSession: Flow<MobileSession?> = cacheDataSource.cachedSession
    override val selectedRole: Flow<String?> = cacheDataSource.selectedRole

    override suspend fun fetchBootstrap(): Result<MobileBootstrap> {
        if (!isConfigured) {
            return Result.failure(
                BackendConfigurationException("Configura redes.apiBaseUrl para habilitar el backend movil.")
            )
        }

        return try {
            val idToken = tokenProvider.getIdToken()
                ?: return Result.failure(IllegalStateException("No hay sesion autenticada en Firebase."))
            val bootstrap = apiClient.fetchBootstrap(idToken)
            cacheDataSource.saveSession(bootstrap.session)
            Result.success(bootstrap)
        } catch (exception: RedesApiException) {
            Result.failure(exception)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun markComunicadoSeen(comunicadoId: String): Result<Unit> {
        if (!isConfigured) {
            return Result.failure(
                BackendConfigurationException("Configura redes.apiBaseUrl para habilitar el backend movil.")
            )
        }

        return try {
            apiClient.markComunicadoSeen(comunicadoId)
            Result.success(Unit)
        } catch (exception: RedesApiException) {
            Result.failure(exception)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun saveSelectedRole(role: String?) {
        cacheDataSource.saveSelectedRole(role)
    }

    override suspend fun clearCache() {
        cacheDataSource.clear()
    }
}
