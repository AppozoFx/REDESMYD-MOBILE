package com.redes.app.data.coordinador

import android.content.Context
import android.net.Uri
import com.redes.app.network.RedesApiClient
import com.redes.app.network.RedesApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteCoordinadorRepository(
    private val context: Context,
    private val apiClient: RedesApiClient,
) : CoordinadorRepository {

    override suspend fun fetchResumen(ym: String): Result<CoordinadorResumen> = call { apiClient.fetchCoordinadorResumen(ym) }
    override suspend fun fetchCuadrillas(ymd: String): Result<CoordinadorCuadrillaData> = call { apiClient.fetchCoordinadorCuadrillas(ymd) }
    override suspend fun fetchMapa(ymd: String): Result<List<CoordinadorMapItem>> = call { apiClient.fetchCoordinadorMapa(ymd) }
    override suspend fun fetchCuadrillasMapa(): Result<List<com.redes.app.data.tecnico.CuadrillaMapa>> = call { apiClient.fetchCoordinadorCuadrillasMapa() }
    override suspend fun fetchStock(): Result<List<CoordinadorStockCuadrilla>> = call { apiClient.fetchCoordinadorStock() }
    override suspend fun fetchAuditoria(): Result<List<CoordinadorAuditoriaCuadrilla>> = call { apiClient.fetchCoordinadorAuditoria() }
    override suspend fun fetchPredespacho(ymd: String): Result<CoordinadorPredespacho> = call { apiClient.fetchCoordinadorPredespacho(ymd) }
    override suspend fun fetchOrdenDetail(id: String): Result<CoordinadorOrdenDetail> = call { apiClient.fetchCoordinadorOrdenDetail(id) }
    override suspend fun fetchVentas(year: Int?, month: Int?): Result<List<CoordinadorVenta>> = call { apiClient.fetchCoordinadorVentas(year, month) }
    override suspend fun fetchPlantillas(ym: String): Result<List<CoordinadorPlantillasCuadrilla>> = call { apiClient.fetchCoordinadorPlantillas(ym) }
    override suspend fun fetchLiquidacion(ym: String): Result<CoordinadorLiquidacionData> = call { apiClient.fetchCoordinadorLiquidacion(ym) }
    override suspend fun fetchPreliquidacion(ordenId: String): Result<CoordinadorPreliquidacion> = call { apiClient.fetchCoordinadorPreliquidacion(ordenId) }
    override suspend fun liquidarOrden(request: CoordinadorLiquidarRequest): Result<Unit> = call { apiClient.postCoordinadorLiquidar(request) }

    override suspend fun sustainEquipo(cuadrillaId: String, sn: String, photoUri: Uri): Result<CoordinadorEquipoAuditoria> = call {
        val cleanSn = sn.trim().uppercase()
        val cleanCuadrillaId = cuadrillaId.trim()
        val resolver = context.contentResolver
        val mimeType = resolver.getType(photoUri).orEmpty().ifBlank { "image/jpeg" }
        val bytes = resolver.openInputStream(photoUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("PHOTO_READ_FAILED")
        val ext = when {
            mimeType.contains("png", ignoreCase = true) -> "png"
            mimeType.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        apiClient.sustainCoordinadorEquipo(cleanCuadrillaId, cleanSn, bytes, "auditoria_${cleanSn}.$ext", mimeType)
    }

    private suspend inline fun <T> call(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(withContext(Dispatchers.IO) { block() })
        } catch (e: RedesApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
