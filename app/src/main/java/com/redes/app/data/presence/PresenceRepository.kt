package com.redes.app.data.presence

interface PresenceRepository {
    suspend fun markOnline(payload: PresencePayload)

    suspend fun markOffline(uid: String)
}
