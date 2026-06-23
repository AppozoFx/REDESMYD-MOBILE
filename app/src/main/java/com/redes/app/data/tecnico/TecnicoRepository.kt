package com.redes.app.data.tecnico

import android.net.Uri

interface TecnicoRepository {
    suspend fun fetchHome(): Result<TecnicoHomeData>
    suspend fun fetchOrders(ymd: String): Result<TecnicoOrdersData>
    suspend fun fetchOrderDetail(orderId: String): Result<TecnicoOrderDetail>
    suspend fun fetchStock(): Result<TecnicoStockData>
    suspend fun sustainStockEquipment(cuadrillaId: String, sn: String, photoUri: Uri): Result<TecnicoStockEquipment>
    suspend fun fetchMap(ymd: String): Result<TecnicoMapData>
    suspend fun fetchCuadrillasMapa(): Result<List<CuadrillaMapa>>
    suspend fun iniciarJornada(): Result<String>
}
