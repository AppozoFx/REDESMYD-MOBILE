package com.redes.app.data.tecnico

import com.redes.app.network.RedesApiClient
import com.redes.app.network.RedesApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteTecnicoRepository(
    private val apiClient: RedesApiClient,
) : TecnicoRepository {
    override suspend fun fetchHome(): Result<TecnicoHomeData> = call { apiClient.fetchTecnicoHome() }

    override suspend fun fetchOrders(ymd: String): Result<TecnicoOrdersData> =
        call { apiClient.fetchTecnicoOrders(ymd) }

    override suspend fun fetchOrderDetail(orderId: String): Result<TecnicoOrderDetail> =
        call { apiClient.fetchTecnicoOrderDetail(orderId) }

    override suspend fun fetchStock(): Result<TecnicoStockData> = call { apiClient.fetchTecnicoStock() }

    override suspend fun fetchMap(ymd: String): Result<TecnicoMapData> =
        call { apiClient.fetchTecnicoMap(ymd) }

    private suspend inline fun <T> call(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(withContext(Dispatchers.IO) { block() })
        } catch (exception: RedesApiException) {
            Result.failure(exception)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
