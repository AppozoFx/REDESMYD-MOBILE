package com.redes.app.data.tecnico

data class TecnicoCuadrillaSummary(
    val id: String,
    val nombre: String,
    val categoria: String,
    val area: String,
    val coordinadorUid: String,
    val coordinadorNombre: String,
    val gestorUid: String,
    val gestorNombre: String,
    val gestorWhatsapp: String = "",
    val integrantes: List<TecnicoMember>,
)

data class NotifTecnicoItem(
    val id: String,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val leido: Boolean,
    val creadoAt: Long?,
)

data class TecnicoMember(
    val uid: String,
    val nombre: String,
)

data class TecnicoHomeData(
    val fecha: String,
    val tecnicoUid: String,
    val tecnicoNombre: String,
    val cuadrilla: TecnicoCuadrillaSummary,
    val kpis: TecnicoKpis,
    val equipmentSummary: List<TecnicoEquipmentSummary>,
    val cableado: TecnicoCableadoSummary = TecnicoCableadoSummary(0, 0),
    val plantillasPendientes: List<TecnicoPlantillaPendiente> = emptyList(),
)

data class TecnicoCableadoSummary(
    val puntosCat5e: Int,
    val puntosCat6: Int,
)

data class TecnicoPlantillaPendiente(
    val ordenId: String,
    val pedido: String,
    val codigoCliente: String,
    val cliente: String,
    val ymd: String,
)

data class TecnicoKpis(
    val instalacionesMes: Int,
    val canceladasMes: Int,
    val anuladasMes: Int,
    val regestionMes: Int,
    val garantiasMes: Int,
    val porcentajeGarantias: Double,
)

data class TecnicoEquipmentSummary(
    val tipo: String,
    val cantidad: Int,
)

data class TecnicoOrderSummary(
    val id: String,
    val ordenId: String,
    val cliente: String,
    val codigoCliente: String,
    val direccion: String,
    val estado: String,
    val tipoTrabajo: String,
    val tipoServicio: String,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val fechaProgramadaYmd: String,
    val fechaProgramadaHm: String,
    val fechaFinVisiYmd: String,
    val fechaFinVisiHm: String,
    val isGarantia: Boolean,
    val isFinalizada: Boolean,
    val isLiquidated: Boolean,
    val plantillaStatus: String,
    val cantMesh: Int,
    val cantFono: Int,
    val cantBox: Int,
    val motivoCancelacion: String,
    val lat: Double?,
    val lng: Double?,
)

data class TecnicoOrdersUpdateInfo(
    val at: String?,
    val byUid: String,
    val byNombre: String,
    val sourceLabel: String,
)

data class TecnicoOrdersData(
    val ymd: String,
    val updateInfo: TecnicoOrdersUpdateInfo?,
    val items: List<TecnicoOrderSummary>,
)

data class TecnicoOrderDetail(
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
    val plan: String,
    val fechaProgramadaYmd: String,
    val fechaProgramadaHm: String,
    val fechaFinVisiYmd: String,
    val fechaFinVisiHm: String,
    val isGarantia: Boolean,
    val isFinalizada: Boolean,
    val isLiquidated: Boolean,
    val plantillaStatus: String,
    val liquidacionEstado: String,
    val liquidadoAt: String?,
    val observacion: String,
    val cantMesh: Int,
    val cantFono: Int,
    val cantBox: Int,
    val lat: Double?,
    val lng: Double?,
    val acta: String,
    val servicios: List<String>,
    val materiales: List<TecnicoConsumedMaterial>,
    val equipos: List<TecnicoInstalledEquipment>,
)

data class TecnicoConsumedMaterial(
    val materialId: String,
    val nombre: String,
    val cantidad: Double,
    val metros: Double,
    val status: String,
)

data class TecnicoInstalledEquipment(
    val sn: String,
    val tipo: String,
    val proid: String,
    val descripcion: String,
)

data class TecnicoStockData(
    val cuadrilla: TecnicoCuadrillaSummary,
    val equipos: List<TecnicoStockEquipment>,
    val materiales: List<TecnicoStockMaterial>,
    val bobinas: List<TecnicoStockBobina> = emptyList(),
)

data class TecnicoStockBobina(
    val id: String,
    val codigo: String,
    val metrosRestantes: Double,
    val metrosIniciales: Double,
    val fDespachoYmd: String = "",
)

data class TecnicoStockAuditoria(
    val requiere: Boolean,
    val estado: String,
    val fotoPath: String,
    val fotoURL: String,
    val actualizadoEn: Long?,
    val actualizadoPor: String,
    val marcadoPor: String,
    val marcadoPorNombre: String,
)

data class TecnicoStockEquipment(
    val id: String,
    val sn: String,
    val tipo: String,
    val proid: String,
    val fDespachoYmd: String = "",
    val guiaDespacho: String = "",
    val observacion: String = "",
    val auditoria: TecnicoStockAuditoria? = null,
)

data class TecnicoStockMaterial(
    val id: String,
    val nombre: String,
    val unidadTipo: String,
    val stockUnd: Double,
    val stockCm: Double,
)

data class TecnicoMapData(
    val ymd: String,
    val cuadrilla: TecnicoCuadrillaSummary,
    val items: List<TecnicoMapItem>,
)

data class TecnicoMapItem(
    val id: String,
    val ordenId: String,
    val cliente: String,
    val codigoCliente: String,
    val direccion: String,
    val estado: String,
    val tipoTrabajo: String,
    val fechaProgramadaHm: String,
    val lat: Double,
    val lng: Double,
)

enum class MapMode { MIS_ORDENES, CUADRILLAS }

data class CuadrillaMapa(
    val id: String,
    val nombre: String,
    val categoria: String,
    val vehiculo: String,
    val lat: Double,
    val lng: Double,
    val lastLocationAt: Long?,
    val estadoActual: String = "EN_RUTA", // "EN_RUTA" | "EN_ORDEN"
)
