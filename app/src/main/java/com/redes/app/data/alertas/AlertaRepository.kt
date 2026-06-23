package com.redes.app.data.alertas

import com.redes.app.data.tecnico.NotifTecnicoItem

interface AlertaRepository {
    suspend fun postAlertaCerrarRuta(): Result<String>
    suspend fun postRequiereAtencion(): Result<Unit>
    fun listenAlertaEstado(alertaId: String, onEstadoChange: (String) -> Unit): () -> Unit
    fun listenNotificaciones(cuadrillaId: String, onUpdate: (List<NotifTecnicoItem>) -> Unit): () -> Unit
    fun markNotificacionesLeidas(cuadrillaId: String, ids: List<String>)
}
