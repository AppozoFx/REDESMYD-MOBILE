package com.redes.app.data.presence

import com.redes.app.network.RedesApiClient

class BackendPresenceRepository(
    private val apiClient: RedesApiClient,
) : PresenceRepository {
    override suspend fun markOnline(payload: PresencePayload) {
        apiClient.markPresenceOnline()
    }

    override suspend fun markOffline(uid: String) {
        apiClient.markPresenceOffline()
    }
}
