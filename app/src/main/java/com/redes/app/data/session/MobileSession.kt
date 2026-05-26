package com.redes.app.data.session

data class MobileSession(
    val uid: String,
    val nombre: String,
    val nombreCorto: String?,
    val email: String?,
    val roles: List<String>,
    val areas: List<String>,
    val permissions: List<String>,
    val estadoAcceso: String,
    val isAdmin: Boolean,
)
