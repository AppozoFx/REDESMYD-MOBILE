package com.redes.app.data.tecnico

import android.content.Context
import android.net.Uri
import com.redes.app.network.RedesApiClient
import com.redes.app.network.RedesApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteTecnicoRepository(
    private val context: Context,
    private val apiClient: RedesApiClient,
) : TecnicoRepository {
    override suspend fun fetchHome(): Result<TecnicoHomeData> = call { apiClient.fetchTecnicoHome() }

    override suspend fun fetchOrders(ymd: String): Result<TecnicoOrdersData> =
        call { apiClient.fetchTecnicoOrders(ymd) }

    override suspend fun fetchOrderDetail(orderId: String): Result<TecnicoOrderDetail> =
        call { apiClient.fetchTecnicoOrderDetail(orderId) }

    override suspend fun fetchStock(): Result<TecnicoStockData> = call {
        apiClient.fetchTecnicoStock()
    }

    override suspend fun sustainStockEquipment(cuadrillaId: String, sn: String, photoUri: Uri): Result<TecnicoStockEquipment> = call {
        sustainEquipment(cuadrillaId, sn, photoUri)
    }

    override suspend fun fetchMap(ymd: String): Result<TecnicoMapData> =
        call { apiClient.fetchTecnicoMap(ymd) }

    override suspend fun fetchCuadrillasMapa(): Result<List<CuadrillaMapa>> =
        call { apiClient.fetchTecnicoCuadrillasMapa() }

    override suspend fun iniciarJornada(): Result<String> =
        call { apiClient.postInicioJornada() }

    private suspend inline fun <T> call(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(withContext(Dispatchers.IO) { block() })
        } catch (exception: RedesApiException) {
            Result.failure(exception)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private suspend fun sustainEquipment(cuadrillaId: String, sn: String, photoUri: Uri): TecnicoStockEquipment {
        val cleanSn = sn.trim().uppercase()
        val cleanCuadrillaId = cuadrillaId.trim()
        if (cleanSn.isBlank()) throw IllegalArgumentException("SN_REQUIRED")
        if (cleanCuadrillaId.isBlank()) throw IllegalArgumentException("CUADRILLA_REQUIRED")

        val resolver = context.contentResolver
        val mimeType = resolver.getType(photoUri).orEmpty().ifBlank { "image/jpeg" }
        val bytes = resolver.openInputStream(photoUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("PHOTO_READ_FAILED")
        val ext = when {
            mimeType.contains("png", ignoreCase = true) -> "png"
            mimeType.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }

        return apiClient.sustainTecnicoStockEquipment(
            cuadrillaId = cleanCuadrillaId,
            sn = cleanSn,
            photoBytes = bytes,
            fileName = "auditoria_${cleanSn}.$ext",
            mimeType = mimeType,
        )
    }
}
