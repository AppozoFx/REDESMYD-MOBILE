package com.redes.app.data.presence

data class PresencePayload(
    val uid: String,
    val roles: List<String> = emptyList(),
    val areas: List<String> = emptyList(),
)
