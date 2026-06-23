package com.redes.app.data.coordinador

data class CoordinadorKpis(
    val finalizadas: Int,
    val garantias: Int,
    val ventas: Int,
    val cat6: Int,
    val cat5e: Int,
)

data class CoordinadorDiaKpi(val ymd: String, val finalizadas: Int, val garantias: Int, val cat6: Int, val cat5e: Int)

data class CoordinadorCuadrillaKpi(
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val finalizadas: Int,
    val garantias: Int,
    val ventas: Int,
    val cat6: Int,
    val cat5e: Int,
    val dias: List<CoordinadorDiaKpi>,
)

data class CoordinadorResumen(val ym: String, val totales: CoordinadorKpis, val cuadrillas: List<CoordinadorCuadrillaKpi>)

// --- Cuadrillas del coordinador ---
data class CoordinadorOrdenItem(
    val id: String,
    val ordenId: String,
    val cliente: String,
    val estado: String,
    val motivoCancelacion: String,
    val hora: String,
    val tipo: String,
    val direccion: String,
    val cantMesh: Int = 0,
    val cantFono: Int = 0,
    val cantBox: Int = 0,
)

data class CoordinadorOrdenDetail(
    val id: String,
    val ordenId: String,
    val cliente: String,
    val codigoCliente: String,
    val documento: String,
    val telefono: String,
    val direccion: String,
    val estado: String,
    val tipoTrabajo: String,
    val tipoServicio: String,
    val fechaProgramadaHm: String,
    val fechaProgramadaYmd: String,
    val isGarantia: Boolean,
    val region: String,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val lat: Double?,
    val lng: Double?,
    val cantMesh: Int = 0,
    val cantFono: Int = 0,
    val cantBox: Int = 0,
)

data class CoordinadorOrdenesResumen(
    val total: Int,
    val agendadas: Int,
    val iniciadas: Int,
    val finalizadas: Int,
    val items: List<CoordinadorOrdenItem> = emptyList(),
)

data class CoordinadorCuadrilla(
    val id: String,
    val nombre: String,
    val categoria: String,
    val estadoRuta: String,
    val lat: Double?,
    val lng: Double?,
    val lastLocationAt: Long?,
    val estadoActual: String, // "EN_RUTA" | "EN_ORDEN"
    val ordenes: CoordinadorOrdenesResumen,
)

data class CoordinadorUpdateInfo(val at: String?, val byNombre: String?)

data class CoordinadorCuadrillaData(
    val ymd: String,
    val cuadrillas: List<CoordinadorCuadrilla>,
    val updateInfo: CoordinadorUpdateInfo? = null,
)

// --- Mapa ---
data class CoordinadorMapItem(
    val id: String,
    val ordenId: String,
    val cliente: String,
    val codigoCliente: String,
    val direccion: String,
    val estado: String,
    val tipoTrabajo: String,
    val fechaProgramadaHm: String,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val lat: Double,
    val lng: Double,
)

// --- Almacén: Stock ---
data class CoordinadorEquipoStock(val sn: String, val tipo: String)

data class CoordinadorStockCuadrilla(
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val ont: Int,
    val mesh: Int,
    val fono: Int,
    val box: Int,
    val total: Int,
    val equipos: List<CoordinadorEquipoStock> = emptyList(),
)

// --- Almacén: Auditoría ---
data class CoordinadorEquipoAuditoria(
    val sn: String,
    val tipo: String,
    val estado: String,
    val fotoURL: String?,
)

data class CoordinadorAuditoriaCuadrilla(
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val pendiente: Int,
    val sustentada: Int,
    val total: Int,
    val items: List<CoordinadorEquipoAuditoria> = emptyList(),
)

// --- Almacén: Predespacho ---
data class CoordinadorPredespachoRow(
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val ont: Int,
    val mesh: Int,
    val fono: Int,
    val box: Int,
    val bobinaResi: Int,
    val rolloCondo: Boolean,
    val updatedByName: String,
    val precon50: Int = 0,
    val precon100: Int = 0,
    val precon150: Int = 0,
    val precon200: Int = 0,
)

data class CoordinadorPredespacho(val tienePredespacho: Boolean, val ymd: String, val rows: List<CoordinadorPredespachoRow>)

// --- Gestión: Ventas ---
data class CoordinadorVenta(
    val id: String,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val totalCents: Int,
    val saldoPendienteCents: Int,
    val cuotasTotal: Int,
    val cuotasPagadas: Int,
    val estado: String,
    val area: String,
    val creadoAtStr: String?,
)

// --- Gestión: Plantillas ---
data class CoordinadorPedidoPendiente(val pedido: String, val cliente: String, val ymd: String)

data class CoordinadorPlantillasCuadrilla(
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val total: Int,
    val pedidos: List<CoordinadorPedidoPendiente>,
)

// --- Liquidación ---
data class CoordinadorLiquidacionItem(
    val id: String,
    val ordenId: String,
    val codigoCliente: String,
    val cliente: String,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val fechaYmd: String,
    val fechaHm: String,
    val tipo: String,
    val plan: String,
    val liquidado: Boolean,
    val correccionPendiente: Boolean,
    val cantMesh: String,
    val cantFono: String,
    val cantBox: String,
)

data class CoordinadorLiquidacionKpi(
    val finalizadas: Int,
    val liquidadas: Int,
    val pendientes: Int,
)

data class CoordinadorLiquidacionData(
    val ym: String,
    val items: List<CoordinadorLiquidacionItem>,
    val kpi: CoordinadorLiquidacionKpi,
)

data class CoordinadorPreliquidacion(
    val ordenId: String,
    val found: Boolean,
    val codigoCliente: String,
    val fechaYmd: String,
    val snOnt: String,
    val snMeshes: List<String>,
    val snBoxes: List<String>,
    val snFono: String,
    val rotuloNapCto: String,
)

data class CoordinadorLiquidarRequest(
    val ordenId: String,
    val snOnt: String,
    val snMeshes: List<String>,
    val snBoxes: List<String>,
    val snFono: String,
    val rotuloNapCto: String,
    val planGamer: Boolean,
    val kitWifiPro: Boolean,
    val servicioCableadoMesh: Boolean,
    val cat5e: Int,
    val cat6: Int,
    val observacion: String,
)
