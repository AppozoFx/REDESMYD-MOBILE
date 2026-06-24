package com.redes.app.ui.coordinador

import com.redes.app.data.coordinador.*
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class CoordinadorTab { INICIO, CUADRILLAS, ALMACEN, GESTION }
enum class AlmacenSubTab { STOCK, AUDITORIA, PREDESPACHO }
enum class GestionSubTab { VENTAS, PLANTILLAS }
enum class CuadrillasViewMode { LISTA, MAPA }
enum class MapaMode { MIS_ORDENES, CUADRILLAS }

data class CoordinadorLiquidacionForm(
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
    val meshExtraEnabled: Boolean = false,
    val boxExtraEnabled: Boolean = false,
    val fonoExtraEnabled: Boolean = false,
)

data class CoordinadorUiState(
    val isEnabled: Boolean = false,
    val selectedTab: CoordinadorTab = CoordinadorTab.INICIO,
    val errorMessage: String? = null,

    // Inicio
    val selectedYm: String = todayLimaYm(),
    val resumen: CoordinadorResumen? = null,
    val isResumenLoading: Boolean = false,

    // Cuadrillas
    val selectedYmd: String = todayLimaYmd(),
    val cuadrillaData: CoordinadorCuadrillaData? = null,
    val isCuadrillasLoading: Boolean = false,
    val cuadrillasViewMode: CuadrillasViewMode = CuadrillasViewMode.LISTA,
    val selectedCuadrillaId: String? = null,
    val expandedCuadrillaId: String? = null,

    // Mapa
    val mapaMode: MapaMode = MapaMode.MIS_ORDENES,
    val mapaItems: List<CoordinadorMapItem> = emptyList(),
    val isMapaLoading: Boolean = false,
    val cuadrillasMapa: List<com.redes.app.data.tecnico.CuadrillaMapa> = emptyList(),
    val isCuadrillasMapaLoading: Boolean = false,

    // Almacén
    val almacenSubTab: AlmacenSubTab = AlmacenSubTab.STOCK,
    val stock: List<CoordinadorStockCuadrilla> = emptyList(),
    val isStockLoading: Boolean = false,
    val expandedStockCuadrillaId: String? = null,
    val auditoria: List<CoordinadorAuditoriaCuadrilla> = emptyList(),
    val isAuditoriaLoading: Boolean = false,
    val expandedAuditoriaCuadrillaId: String? = null,
    val auditoriaSustainingSnId: String? = null,
    val predespacho: CoordinadorPredespacho? = null,
    val isPredespachoLoading: Boolean = false,

    // Liquidación
    val liquidacion: CoordinadorLiquidacionData? = null,
    val isLiquidacionLoading: Boolean = false,
    val expandedLiquidacionId: String? = null,
    val liquidacionForms: Map<String, CoordinadorLiquidacionForm> = emptyMap(),
    val preliquidaciones: Map<String, CoordinadorPreliquidacion> = emptyMap(),
    val isPreliqLoadingId: String? = null,

    // Detalle de orden
    val selectedOrdenDetail: CoordinadorOrdenDetail? = null,
    val isOrdenDetailLoading: Boolean = false,

    // Gestión
    val gestionSubTab: GestionSubTab = GestionSubTab.VENTAS,
    val ventas: List<CoordinadorVenta> = emptyList(),
    val isVentasLoading: Boolean = false,
    val plantillas: List<CoordinadorPlantillasCuadrilla> = emptyList(),
    val isPlantillasLoading: Boolean = false,
    val isRefreshing: Boolean = false,
)

fun todayLimaYm(): String = ZonedDateTime.now(ZoneId.of("America/Lima"))
    .format(DateTimeFormatter.ofPattern("yyyy-MM"))

fun todayLimaYmd(): String = ZonedDateTime.now(ZoneId.of("America/Lima"))
    .format(DateTimeFormatter.ISO_LOCAL_DATE)
