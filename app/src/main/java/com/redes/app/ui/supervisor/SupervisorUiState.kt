package com.redes.app.ui.supervisor


import com.redes.app.data.supervisor.JornadaEstado
import com.redes.app.data.supervisor.SupervisorAlertItem
import com.redes.app.data.supervisor.OficinaConfig
import com.redes.app.data.supervisor.SupervisorHomeData
import com.redes.app.data.supervisor.SupervisorJornada
import com.redes.app.data.supervisor.SupervisorMapItem
import com.redes.app.data.supervisor.SupervisorMapMode
import com.redes.app.data.supervisor.SupervisorOrderDetail
import com.redes.app.data.supervisor.SupervisorOrderSummary
import com.redes.app.data.supervisor.SupervisorOrdersData
import com.redes.app.data.tecnico.CuadrillaMapa

enum class SupervisorTab {
    INICIO,
    ORDENES,
    STOCK,
    MAPA,
}

data class SupervisorUiState(
    val isEnabled: Boolean = false,
    val isInitialLoading: Boolean = false,
    val isHomeLoading: Boolean = false,
    val isOrdersLoading: Boolean = false,
    val isMapLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val isSavingSupervision: Boolean = false,
    val isSavingGarantia: Boolean = false,
    val garantiaSaved: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: SupervisorTab = SupervisorTab.INICIO,
    val selectedYmd: String = todayLimaYmd(),
    val showGarantias: Boolean = false,
    val home: SupervisorHomeData? = null,
    val ordersData: SupervisorOrdersData? = null,
    val orders: List<SupervisorOrderSummary> = emptyList(),
    val selectedOrderDetail: SupervisorOrderDetail? = null,
    val supervisionSavedOrderId: String? = null,
    val mapMode: SupervisorMapMode = SupervisorMapMode.MIS_ORDENES,
    val mapItems: List<SupervisorMapItem> = emptyList(),         // mis órdenes / garantías
    val allMapItems: List<SupervisorMapItem> = emptyList(),      // todas las órdenes (modo CUADRILLAS)
    val cuadrillasMapa: List<CuadrillaMapa> = emptyList(),       // todas las cuadrillas
    val isCuadrillasMapaLoading: Boolean = false,
    val selectedCuadrillaId: String? = null,
    // Jornada
    val jornada: SupervisorJornada? = null,
    val oficina: OficinaConfig? = null,
    val isJornadaLoading: Boolean = false,
    val jornadaError: String? = null,
    val isGettingLocation: Boolean = false,
    val showCerrarRutaConfirm: Boolean = false,
    val showCuadrillasModal: Boolean = false,
    val estadoRuta: String? = null,
    val alertas: List<SupervisorAlertItem> = emptyList(),
    val alertCount: Int = 0,
    val showAlertas: Boolean = false,
    val isRefreshing: Boolean = false,
)

fun todayLimaYmd(): String {
    val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
    return java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima")).toLocalDate().format(formatter)
}
