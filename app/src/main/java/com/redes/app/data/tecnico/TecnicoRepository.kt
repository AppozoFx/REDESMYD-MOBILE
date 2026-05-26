package com.redes.app.data.tecnico

interface TecnicoRepository {
    suspend fun fetchHome(): Result<TecnicoHomeData>
    suspend fun fetchOrders(ymd: String): Result<TecnicoOrdersData>
    suspend fun fetchOrderDetail(orderId: String): Result<TecnicoOrderDetail>
    suspend fun fetchStock(): Result<TecnicoStockData>
    suspend fun fetchMap(ymd: String): Result<TecnicoMapData>
}
