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
    val integrantes: List<TecnicoMember>,
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
    val cantMesh: Int,
    val cantFono: Int,
    val cantBox: Int,
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
    val liquidacionEstado: String,
    val liquidadoAt: String?,
    val observacion: String,
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
)

data class TecnicoStockEquipment(
    val id: String,
    val sn: String,
    val tipo: String,
    val proid: String,
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
