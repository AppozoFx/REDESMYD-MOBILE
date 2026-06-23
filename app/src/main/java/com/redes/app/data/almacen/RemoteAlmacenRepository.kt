package com.redes.app.data.almacen

import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.network.RedesApiClient
import com.redes.app.network.RedesApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteAlmacenRepository(
    private val apiClient: RedesApiClient,
) : AlmacenRepository {

    override suspend fun fetchStock(): Result<List<AlmacenStockCuadrilla>> = call { apiClient.fetchAlmacenStock() }

    override suspend fun fetchLiquidacion(ym: String): Result<AlmacenLiquidacionData> = call { apiClient.fetchAlmacenLiquidacion(ym) }

    override suspend fun fetchInstalaciones(ym: String): Result<List<AlmacenInstalacion>> = call { apiClient.fetchAlmacenInstalaciones(ym) }

    override suspend fun fetchCuadrillasMapa(): Result<List<CuadrillaMapa>> = call { apiClient.fetchAlmacenCuadrillasMapa() }

    override suspend fun fetchPreliquidacion(ordenId: String): Result<AlmacenPreliquidacion> = call { apiClient.fetchAlmacenPreliquidacion(ordenId) }

    override suspend fun lookupSn(sn: String, cuadrillaId: String): Result<AlmacenSnLookup> = call { apiClient.lookupAlmacenSn(sn, cuadrillaId) }

    override suspend fun liquidarOrden(request: AlmacenLiquidarRequest): Result<Unit> = call { apiClient.postAlmacenLiquidar(request) }

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
