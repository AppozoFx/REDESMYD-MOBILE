package com.redes.app.data.supervisor

import com.redes.app.data.tecnico.CuadrillaMapa

interface SupervisorRepository {
    suspend fun fetchHome(): Result<SupervisorHomeData>
    suspend fun fetchOrders(ymd: String, soloGarantias: Boolean = false): Result<SupervisorOrdersData>
    suspend fun fetchOrderDetail(orderId: String): Result<SupervisorOrderDetail>
    suspend fun saveSupervision(orderId: String, notas: String, observaciones: String): Result<Unit>
    suspend fun updateGarantia(
        ordenId: String,
        motivoGarantia: String,
        diagnosticoGarantia: String,
        solucionGarantia: String,
        responsableGarantia: String,
        casoGarantia: String,
        imputadoGarantia: String,
    ): Result<Unit>
    suspend fun fetchMapa(ymd: String, modo: SupervisorMapMode): Result<List<SupervisorMapItem>>
    suspend fun fetchCuadrillasMapa(): Result<List<CuadrillaMapa>>
    suspend fun iniciarJornada(): Result<String>
    suspend fun fetchJornada(ymd: String): Result<JornadaData>
    suspend fun postJornadaEvento(tipo: String, lat: Double?, lng: Double?): Result<SupervisorJornada>
}
