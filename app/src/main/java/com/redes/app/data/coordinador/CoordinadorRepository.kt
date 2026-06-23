package com.redes.app.data.coordinador

interface CoordinadorRepository {
    suspend fun fetchResumen(ym: String): Result<CoordinadorResumen>
    suspend fun fetchCuadrillas(ymd: String): Result<CoordinadorCuadrillaData>
    suspend fun fetchMapa(ymd: String): Result<List<CoordinadorMapItem>>
    suspend fun fetchCuadrillasMapa(): Result<List<com.redes.app.data.tecnico.CuadrillaMapa>>
    suspend fun fetchStock(): Result<List<CoordinadorStockCuadrilla>>
    suspend fun fetchAuditoria(): Result<List<CoordinadorAuditoriaCuadrilla>>
    suspend fun fetchPredespacho(ymd: String): Result<CoordinadorPredespacho>
    suspend fun fetchVentas(year: Int?, month: Int?): Result<List<CoordinadorVenta>>
    suspend fun fetchPlantillas(ym: String): Result<List<CoordinadorPlantillasCuadrilla>>
    suspend fun fetchOrdenDetail(id: String): Result<CoordinadorOrdenDetail>
    suspend fun sustainEquipo(cuadrillaId: String, sn: String, photoUri: android.net.Uri): Result<CoordinadorEquipoAuditoria>
    suspend fun fetchLiquidacion(ym: String): Result<CoordinadorLiquidacionData>
    suspend fun fetchPreliquidacion(ordenId: String): Result<CoordinadorPreliquidacion>
    suspend fun liquidarOrden(request: CoordinadorLiquidarRequest): Result<Unit>
}
