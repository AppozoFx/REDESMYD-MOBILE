package com.redes.app.data.almacen

// ── Stock ─────────────────────────────────────────────────────────────────────

data class AlmacenStockCuadrilla(
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val ont: Int,
    val mesh: Int,
    val fono: Int,
    val box: Int,
    val total: Int,
    val equipos: List<AlmacenEquipo>,
)

data class AlmacenEquipo(
    val sn: String,
    val tipo: String,
)

// ── Liquidación ───────────────────────────────────────────────────────────────

data class AlmacenLiquidacionData(
    val ym: String,
    val items: List<AlmacenLiquidacionItem>,
    val kpi: AlmacenLiquidacionKpi,
)

data class AlmacenLiquidacionItem(
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

data class AlmacenLiquidacionKpi(
    val finalizadas: Int,
    val liquidadas: Int,
    val pendientes: Int,
)

data class AlmacenPreliquidacion(
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

data class AlmacenSnLookup(
    val sn: String,
    val found: Boolean,
    val equipo: String,
    val inTargetCuadrillaStock: Boolean,
    val isInstalado: Boolean,
    val reason: String,
    val actionHint: String,
)

data class AlmacenLiquidarRequest(
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

// ── Instalaciones ─────────────────────────────────────────────────────────────

data class AlmacenInstalacion(
    val id: String,
    val codigoCliente: String,
    val cliente: String,
    val cuadrillaNombre: String,
    val fechaYmd: String,
    val tipoOrden: String,
    val plan: String,
    val tipoCuadrilla: String,
    val coordinadorNombre: String,
    val acta: String,
    val snOnt: String,
    val snMesh: List<String>,
    val snBox: List<String>,
    val snFono: String,
    val precon: String,
    val bobinaMetros: Int,
    val estadoMateriales: String,
    val planGamer: Boolean,
    val kitWifiPro: Boolean,
    val servicioCableadoMesh: Boolean,
    val cat5e: Int,
    val cat6: Int,
    val puntosUTP: Int,
    val observacion: String,
)
