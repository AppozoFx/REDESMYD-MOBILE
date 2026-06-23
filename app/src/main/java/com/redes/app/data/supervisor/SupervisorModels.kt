package com.redes.app.data.supervisor

import com.redes.app.data.tecnico.CuadrillaMapa

data class SupervisorInfo(
    val uid: String,
    val nombre: String,
    val nombreCorto: String,
    val vehiculoPlaca: String = "",
)

data class SupervisorCuadrillaSummary(
    val id: String,
    val nombre: String,
    val ordenesTotal: Int,
    val garantiasTotal: Int,
    val estadoActual: String,
)

data class SupervisorRegionResumen(
    val regionId: String,
    val regionNombre: String,
    val total: Int,
    val garantias: Int,
    val finalizadas: Int,
    val pendientes: Int,
)

data class SupervisorTotales(
    val ordenes: Int,
    val garantias: Int,
    val finalizadas: Int,
    val pendientes: Int,
)

data class SupervisorHomeData(
    val ymd: String,
    val supervisor: SupervisorInfo,
    val trackingHabilitado: Boolean,
    val regionesHoy: List<String>,
    val cuadrillasHoy: List<SupervisorCuadrillaSummary>,
    val ordenesPorRegion: List<SupervisorRegionResumen>,
    val totales: SupervisorTotales,
)

data class SupervisorOrderSummary(
    val id: String,
    val ordenId: String,
    val cliente: String,
    val codigoCliente: String,
    val direccion: String,
    val estado: String,
    val tipoTrabajo: String,
    val tipoServicio: String,
    val fechaProgramadaHm: String,
    val fechaProgramadaYmd: String,
    val isGarantia: Boolean,
    val isFinalizada: Boolean,
    val region: String,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val hasSupervision: Boolean,
    val cantMesh: Int,
    val cantFono: Int,
    val cantBox: Int,
    val lat: Double?,
    val lng: Double?,
)

data class SupervisorUpdateInfo(
    val at: String?,
    val byNombre: String?,
)

data class SupervisorOrdersData(
    val ymd: String,
    val updateInfo: SupervisorUpdateInfo?,
    val items: List<SupervisorOrderSummary>,
)

data class SupervisionData(
    val notas: String,
    val observaciones: String,
    val estadoSupervision: String,
    val supervisorUid: String,
    val supervisadoEn: String,
)

data class SupervisorOrderDetail(
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
    val plan: String,
    val supervision: SupervisionData?,
    val diagnosticoGarantia: String = "",
    val solucionGarantia: String = "",
    val responsableGarantia: String = "",
    val casoGarantia: String = "",
    val imputadoGarantia: String = "",
    val motivoGarantia: String = "",
)

data class SupervisorMapItem(
    val id: String,
    val ordenId: String,
    val cliente: String,
    val direccion: String,
    val estado: String,
    val isGarantia: Boolean,
    val cuadrillaId: String,
    val cuadrillaNombre: String,
    val region: String,
    val hasSupervision: Boolean,
    val lat: Double,
    val lng: Double,
)

enum class SupervisorMapMode {
    MIS_ORDENES,
    GARANTIAS,
    CUADRILLAS,
}

// ─── Jornada ──────────────────────────────────────────────────────────────────

enum class JornadaEstado { SIN_INICIAR, EN_RUTA, EN_REFRIGERIO, FINALIZADA }

data class SupervisorJornada(
    val uid: String,
    val ymd: String,
    val estado: JornadaEstado,
    val horaInicio: String?,
    val horaFin: String?,
    val horaInicioRefrigerio: String?,
    val horaFinRefrigerio: String?,
    val latInicio: Double?,
    val lngInicio: Double?,
    val latFin: Double?,
    val lngFin: Double?,
)

data class OficinaConfig(
    val lat: Double,
    val lng: Double,
    val radioMetros: Double,
)

data class JornadaData(
    val jornada: SupervisorJornada,
    val oficina: OficinaConfig?,
)

data class SupervisorAlertItem(
    val id: String,
    val tipo: String,        // GARANTIA_NUEVA, GARANTIA_ESTADO, GARANTIA_TRAMO
    val titulo: String,
    val mensaje: String,
    val leido: Boolean = false,
    val creadoAt: Long = System.currentTimeMillis(),
)
