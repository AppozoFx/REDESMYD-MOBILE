package com.redes.app.ui.tecnico

import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.data.tecnico.MapMode
import com.redes.app.data.tecnico.NotifTecnicoItem
import com.redes.app.data.tecnico.TecnicoHomeData
import com.redes.app.data.tecnico.TecnicoMapData
import com.redes.app.data.tecnico.TecnicoOrderDetail
import com.redes.app.data.tecnico.TecnicoOrdersData
import com.redes.app.data.tecnico.TecnicoOrderSummary
import com.redes.app.data.tecnico.TecnicoStockData

enum class TecnicoTab {
    INICIO,
    ORDENES,
    STOCK,
    MAPA,
}

data class TecnicoUiState(
    val isEnabled: Boolean = false,
    val isInitialLoading: Boolean = false,
    val isHomeLoading: Boolean = false,
    val isOrdersLoading: Boolean = false,
    val isStockLoading: Boolean = false,
    val isMapLoading: Boolean = false,
    val isDetailLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: TecnicoTab = TecnicoTab.INICIO,
    val selectedYmd: String = todayLimaYmd(),
    val home: TecnicoHomeData? = null,
    val ordersData: TecnicoOrdersData? = null,
    val orders: List<TecnicoOrderSummary> = emptyList(),
    val stock: TecnicoStockData? = null,
    val map: TecnicoMapData? = null,
    val selectedOrderDetail: TecnicoOrderDetail? = null,
    val mapMode: MapMode = MapMode.MIS_ORDENES,
    val cuadrillasMapa: List<CuadrillaMapa> = emptyList(),
    val isCuadrillasMapaLoading: Boolean = false,
    val alertaPendienteId: String? = null,
    val alertaEstado: String? = null,
    val isAlertaLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val estadoRuta: String? = null,
    val notifItems: List<NotifTecnicoItem> = emptyList(),
    val notifCount: Int = 0,
)

fun todayLimaYmd(): String {
    val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
    return java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima")).toLocalDate().format(formatter)
}
