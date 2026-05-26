package com.redes.app.network

object MobileEndpoints {
    const val ME = "/api/mobile/me"
    const val BOOTSTRAP = "/api/mobile/bootstrap"
    const val PRESENCE = "/api/mobile/presencia"
    const val TECNICO_HOME = "/api/mobile/tecnico/home"
    const val TECNICO_ORDERS = "/api/mobile/tecnico/ordenes"
    const val TECNICO_STOCK = "/api/mobile/tecnico/stock"
    const val TECNICO_MAP = "/api/mobile/tecnico/mapa"

    fun comunicadoSeen(id: String): String = "/api/mobile/comunicados/$id/seen"
    fun tecnicoOrderDetail(id: String): String = "/api/mobile/tecnico/ordenes/$id"
}
