package com.redes.app.ui.tecnico

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
    val isLoading: Boolean = false,
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
)

fun todayLimaYmd(): String {
    val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
    return java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima")).toLocalDate().format(formatter)
}
