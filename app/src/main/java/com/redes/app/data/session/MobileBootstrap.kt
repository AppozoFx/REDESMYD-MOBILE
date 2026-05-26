package com.redes.app.data.session

data class MobileBootstrap(
    val session: MobileSession,
    val comunicados: List<MobileComunicado>,
    val requiresComunicadosGate: Boolean,
    val roleSelectionRequired: Boolean,
    val defaultRole: String?,
)

data class MobileComunicado(
    val id: String,
    val titulo: String,
    val cuerpo: String,
    val obligatorio: Boolean,
    val persistencia: String,
    val placement: String,
    val target: String,
    val imageUrl: String?,
    val linkUrl: String?,
    val linkLabel: String?,
    val visibleDesde: String?,
    val visibleHasta: String?,
) {
    val isBlocking: Boolean
        get() = obligatorio && persistencia.equals("ONCE", ignoreCase = true)
}
