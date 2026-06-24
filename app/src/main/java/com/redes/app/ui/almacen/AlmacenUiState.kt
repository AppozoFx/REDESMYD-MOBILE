package com.redes.app.ui.almacen

import com.redes.app.data.almacen.*
import com.redes.app.data.coordinador.CoordinadorMapItem
import com.redes.app.data.tecnico.CuadrillaMapa
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class AlmacenTab { STOCK, LIQUIDACION, INSTALACIONES, MAPA }

data class LiquidacionForm(
    val snOnt: String = "",
    val snMeshes: List<String> = emptyList(),
    val snBoxes: List<String> = emptyList(),
    val snFono: String = "",
    val rotuloNapCto: String = "",
    val planGamer: Boolean = false,
    val kitWifiPro: Boolean = false,
    val servicioCableadoMesh: Boolean = false,
    val cat5e: Int = 0,
    val cat6: Int = 0,
    val observacion: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false,
    val snLookupResults: Map<String, AlmacenSnLookup> = emptyMap(),
    val meshExtraEnabled: Boolean = false,
    val boxExtraEnabled: Boolean = false,
    val fonoExtraEnabled: Boolean = false,
)

data class AlmacenUiState(
    val isEnabled: Boolean = false,
    val selectedTab: AlmacenTab = AlmacenTab.STOCK,
    val errorMessage: String? = null,
    val selectedYm: String = todayAlmacenYm(),

    // Stock
    val stock: List<AlmacenStockCuadrilla> = emptyList(),
    val isStockLoading: Boolean = false,
    val expandedStockCuadrillaId: String? = null,

    // Liquidación
    val liquidacion: AlmacenLiquidacionData? = null,
    val isLiquidacionLoading: Boolean = false,
    val expandedLiquidacionId: String? = null,
    val liquidacionForms: Map<String, LiquidacionForm> = emptyMap(),
    val preliquidaciones: Map<String, AlmacenPreliquidacion> = emptyMap(),
    val isPreliqLoadingId: String? = null,

    // Instalaciones
    val instalaciones: List<AlmacenInstalacion> = emptyList(),
    val isInstalacionesLoading: Boolean = false,
    val expandedInstalacionId: String? = null,

    // Mapa cuadrillas
    val cuadrillasMapa: List<CuadrillaMapa> = emptyList(),
    val isCuadrillasMapaLoading: Boolean = false,

    // Mapa órdenes
    val selectedYmd: String = todayAlmacenYmd(),
    val mapaItems: List<CoordinadorMapItem> = emptyList(),
    val isMapaItemsLoading: Boolean = false,

    val isRefreshing: Boolean = false,
)

fun todayAlmacenYm(): String = ZonedDateTime.now(ZoneId.of("America/Lima"))
    .format(DateTimeFormatter.ofPattern("yyyy-MM"))

fun todayAlmacenYmd(): String = ZonedDateTime.now(ZoneId.of("America/Lima"))
    .format(DateTimeFormatter.ISO_LOCAL_DATE)
