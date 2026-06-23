package com.redes.app.data.almacen

interface AlmacenRepository {
    suspend fun fetchStock(): Result<List<AlmacenStockCuadrilla>>
    suspend fun fetchLiquidacion(ym: String): Result<AlmacenLiquidacionData>
    suspend fun fetchInstalaciones(ym: String): Result<List<AlmacenInstalacion>>
    suspend fun fetchCuadrillasMapa(): Result<List<com.redes.app.data.tecnico.CuadrillaMapa>>
    suspend fun fetchPreliquidacion(ordenId: String): Result<AlmacenPreliquidacion>
    suspend fun lookupSn(sn: String, cuadrillaId: String): Result<AlmacenSnLookup>
    suspend fun liquidarOrden(request: AlmacenLiquidarRequest): Result<Unit>
}
