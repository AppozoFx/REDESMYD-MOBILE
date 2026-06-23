package com.redes.app.data.alertas

data class AlertaApp(
    val id: String,
    val tipo: String,
    val estado: String,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val emisorUid: String,
    val creadoAt: Long?,
    val respondidoPorNombre: String?,
)
