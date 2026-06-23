package com.redes.app.data.supervisor

import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.network.RedesApiClient
import com.redes.app.network.RedesApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteSupervisorRepository(
    private val apiClient: RedesApiClient,
) : SupervisorRepository {

    private suspend inline fun <T> call(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(withContext(Dispatchers.IO) { block() })
        } catch (e: RedesApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchHome(): Result<SupervisorHomeData> =
        call { apiClient.fetchSupervisorHome() }

    override suspend fun fetchOrders(ymd: String, soloGarantias: Boolean): Result<SupervisorOrdersData> =
        call { apiClient.fetchSupervisorOrders(ymd, soloGarantias) }

    override suspend fun fetchOrderDetail(orderId: String): Result<SupervisorOrderDetail> =
        call { apiClient.fetchSupervisorOrderDetail(orderId) }

    override suspend fun saveSupervision(orderId: String, notas: String, observaciones: String): Result<Unit> =
        call { apiClient.postSupervisorSupervision(orderId, notas, observaciones) }

    override suspend fun updateGarantia(
        ordenId: String,
        motivoGarantia: String,
        diagnosticoGarantia: String,
        solucionGarantia: String,
        responsableGarantia: String,
        casoGarantia: String,
        imputadoGarantia: String,
    ): Result<Unit> = call {
        apiClient.postSupervisorGarantiaUpdate(
            ordenId, motivoGarantia, diagnosticoGarantia, solucionGarantia, responsableGarantia, casoGarantia, imputadoGarantia
        )
    }

    override suspend fun fetchMapa(ymd: String, modo: SupervisorMapMode): Result<List<SupervisorMapItem>> =
        call { apiClient.fetchSupervisorMapa(ymd, modo) }

    override suspend fun fetchCuadrillasMapa(): Result<List<CuadrillaMapa>> =
        call { apiClient.fetchSupervisorCuadrillasMapa() }

    override suspend fun iniciarJornada(): Result<String> =
        call { apiClient.postInicioJornada() }

    override suspend fun fetchJornada(ymd: String): Result<JornadaData> =
        call { apiClient.fetchSupervisorJornada(ymd) }

    override suspend fun postJornadaEvento(tipo: String, lat: Double?, lng: Double?): Result<SupervisorJornada> =
        call { apiClient.postSupervisorJornadaEvento(tipo, lat, lng) }
}
