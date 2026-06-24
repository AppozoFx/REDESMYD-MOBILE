package com.redes.app.network

import android.util.Log
import com.redes.app.data.almacen.AlmacenInstalacion
import com.redes.app.data.almacen.AlmacenLiquidacionData
import com.redes.app.data.almacen.AlmacenLiquidarRequest
import com.redes.app.data.almacen.AlmacenPreliquidacion
import com.redes.app.data.almacen.AlmacenSnLookup
import com.redes.app.data.almacen.AlmacenStockCuadrilla
import com.redes.app.network.dto.toAlmacenInstalacionesList
import com.redes.app.network.dto.toAlmacenLiquidacionData
import com.redes.app.network.dto.toAlmacenPreliquidacion
import com.redes.app.network.dto.toAlmacenSnLookup
import com.redes.app.network.dto.toAlmacenStockList
import com.redes.app.data.coordinador.CoordinadorAuditoriaCuadrilla
import com.redes.app.data.coordinador.CoordinadorCuadrillaData
import com.redes.app.data.coordinador.CoordinadorMapItem
import com.redes.app.data.coordinador.CoordinadorPredespacho
import com.redes.app.data.coordinador.CoordinadorPlantillasCuadrilla
import com.redes.app.data.coordinador.CoordinadorResumen
import com.redes.app.data.coordinador.CoordinadorStockCuadrilla
import com.redes.app.data.coordinador.CoordinadorVenta
import com.redes.app.data.session.MobileBootstrap
import com.redes.app.data.session.MobileSession
import com.redes.app.data.tecnico.CuadrillaMapa
import com.redes.app.data.tecnico.TecnicoHomeData
import com.redes.app.data.tecnico.TecnicoMapData
import com.redes.app.data.tecnico.TecnicoOrderDetail
import com.redes.app.data.tecnico.TecnicoOrdersData
import com.redes.app.data.tecnico.TecnicoStockEquipment
import com.redes.app.data.tecnico.TecnicoStockData
import com.redes.app.network.dto.MobileBootstrapDto
import com.redes.app.network.dto.MobileSessionDto
import com.redes.app.data.coordinador.CoordinadorEquipoAuditoria
import com.redes.app.data.coordinador.CoordinadorLiquidacionData
import com.redes.app.data.coordinador.CoordinadorLiquidarRequest
import com.redes.app.data.coordinador.CoordinadorOrdenDetail
import com.redes.app.data.coordinador.CoordinadorPreliquidacion
import com.redes.app.network.dto.toCoordinadorAuditoriaList
import com.redes.app.network.dto.toCoordinadorLiquidacionData
import com.redes.app.network.dto.toCoordinadorPreliquidacion
import com.redes.app.network.dto.toCoordinadorOrdenDetail
import com.redes.app.network.dto.toCoordinadorCuadrillaData
import com.redes.app.network.dto.toCoordinadorEquipoAuditoria
import com.redes.app.network.dto.toCoordinadorMapItems
import com.redes.app.network.dto.toCoordinadorPlantillasList
import com.redes.app.network.dto.toCoordinadorPredespacho
import com.redes.app.network.dto.toCoordinadorResumen
import com.redes.app.network.dto.toCoordinadorStockList
import com.redes.app.network.dto.toCoordinadorVentaList
import com.redes.app.network.dto.toTecnicoHomeData
import com.redes.app.network.dto.toCuadrillasMapa
import com.redes.app.network.dto.toTecnicoMapData
import com.redes.app.network.dto.toTecnicoOrderDetail
import com.redes.app.network.dto.toTecnicoOrdersData
import com.redes.app.network.dto.toTecnicoStockEquipment
import com.redes.app.network.dto.toTecnicoStockData
import com.redes.app.network.dto.toSupervisorHomeData
import com.redes.app.network.dto.toSupervisorOrdersData
import com.redes.app.network.dto.toSupervisorOrderDetail
import com.redes.app.network.dto.toSupervisorMapItems
import com.redes.app.network.dto.toJornadaData
import com.redes.app.network.dto.toSupervisorJornada
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException

class RedesApiClient(
    baseUrl: String,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    private val tag = "RedesApiClient"

    val isConfigured: Boolean
        get() = normalizedBaseUrl.isNotBlank()

    suspend fun fetchCurrentSession(idToken: String): MobileSession {
        if (!isConfigured) {
            throw RedesApiException("Base URL del backend no configurada.")
        }

        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.ME}")
            .get()
            .header("Authorization", "Bearer $idToken")
            .build()

        Log.d(tag, "GET ${request.url}")

        return executeJson(request) { json ->
            MobileSessionDto.fromJson(json).toDomain()
        }
    }

    suspend fun fetchBootstrap(idToken: String): MobileBootstrap {
        if (!isConfigured) {
            throw RedesApiException("Base URL del backend no configurada.")
        }

        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.BOOTSTRAP}")
            .get()
            .header("Authorization", "Bearer $idToken")
            .build()

        Log.d(tag, "GET ${request.url}")

        return executeJson(request) { json ->
            MobileBootstrapDto.fromJson(json).toDomain()
        }
    }

    suspend fun markPresenceOnline() {
        if (!isConfigured) return
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.PRESENCE}")
            .post("{}".toRequestBody())
            .build()
        Log.d(tag, "POST ${request.url}")
        executeWithoutBody(request)
    }

    suspend fun markPresenceOffline() {
        if (!isConfigured) return
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.PRESENCE}")
            .delete()
            .build()
        Log.d(tag, "DELETE ${request.url}")
        executeWithoutBody(request)
    }

    suspend fun markComunicadoSeen(comunicadoId: String) {
        if (!isConfigured) return
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.comunicadoSeen(comunicadoId)}")
            .post("{}".toRequestBody())
            .build()
        Log.d(tag, "POST ${request.url}")
        executeWithoutBody(request)
    }

    suspend fun fetchTecnicoHome(): TecnicoHomeData {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.TECNICO_HOME}")
            .get()
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toTecnicoHomeData() }
    }

    suspend fun fetchTecnicoOrders(ymd: String): TecnicoOrdersData {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.TECNICO_ORDERS}?ymd=$ymd")
            .get()
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toTecnicoOrdersData() }
    }

    suspend fun fetchTecnicoOrderDetail(orderId: String): TecnicoOrderDetail {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.tecnicoOrderDetail(orderId)}")
            .get()
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> (json.optJSONObject("item") ?: JSONObject()).toTecnicoOrderDetail() }
    }

    suspend fun fetchTecnicoStock(): TecnicoStockData {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.TECNICO_STOCK}")
            .get()
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toTecnicoStockData() }
    }

    suspend fun sustainTecnicoStockEquipment(
        cuadrillaId: String,
        sn: String,
        photoBytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): TecnicoStockEquipment {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("cuadrillaId", cuadrillaId)
            .addFormDataPart("sn", sn)
            .addFormDataPart("marcarSustentado", "true")
            .addFormDataPart(
                "file",
                fileName,
                photoBytes.toRequestBody(mimeType.takeIf { it.isNotBlank() }?.toMediaTypeOrNull()),
            )
            .build()

        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.TECNICO_STOCK}")
            .post(body)
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "POST ${request.url}")
        return executeJson(request) { json ->
            (json.optJSONObject("item") ?: JSONObject()).toTecnicoStockEquipment()
        }
    }

    fun postTracking(lat: Double, lng: Double, accuracy: Float?, speed: Float?) {
        if (!isConfigured) return
        val body = org.json.JSONObject().apply {
            put("lat", lat)
            put("lng", lng)
            accuracy?.let { put("accuracy", it.toDouble()) }
            speed?.takeIf { it >= 0f }?.let { put("speed", it.toDouble()) }
        }
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.TRACKING}")
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "POST ${request.url}")
        executeWithoutBody(request)
    }

    suspend fun fetchTecnicoCuadrillasMapa(): List<CuadrillaMapa> {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.TECNICO_CUADRILLAS_MAPA}")
            .get()
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toCuadrillasMapa() }
    }

    suspend fun fetchCoordinadorResumen(ym: String): CoordinadorResumen {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_INICIO}?ym=$ym").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorResumen() }
    }
    suspend fun fetchCoordinadorCuadrillas(ymd: String): CoordinadorCuadrillaData {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_CUADRILLAS}?ymd=$ymd").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorCuadrillaData() }
    }
    suspend fun fetchCoordinadorCuadrillasMapa(): List<CuadrillaMapa> {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_CUADRILLAS_MAPA}")
            .get()
            .header("X-Mobile-Role", "COORDINADOR")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toCuadrillasMapa() }
    }

    suspend fun fetchCoordinadorMapa(ymd: String): List<CoordinadorMapItem> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_MAPA}?ymd=$ymd").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorMapItems() }
    }
    suspend fun fetchCoordinadorStock(): List<CoordinadorStockCuadrilla> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_STOCK}").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorStockList() }
    }
    suspend fun fetchCoordinadorAuditoria(): List<CoordinadorAuditoriaCuadrilla> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_AUDITORIA}").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorAuditoriaList() }
    }
    suspend fun fetchCoordinadorPredespacho(ymd: String): CoordinadorPredespacho {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_PREDESPACHO}?ymd=$ymd").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorPredespacho() }
    }
    suspend fun fetchCoordinadorOrdenDetail(id: String): CoordinadorOrdenDetail {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.coordinadorOrdenDetail(id)}").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorOrdenDetail() }
    }
    suspend fun fetchCoordinadorVentas(year: Int?, month: Int?): List<CoordinadorVenta> {
        val params = buildString { year?.let { append("?year=$it") }; month?.let { append("${if (year != null) "&" else "?"}month=$it") } }
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_VENTAS}$params").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorVentaList() }
    }
    suspend fun fetchCoordinadorPlantillas(ym: String): List<CoordinadorPlantillasCuadrilla> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_PLANTILLAS}?ym=$ym").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorPlantillasList() }
    }

    suspend fun sustainCoordinadorEquipo(cuadrillaId: String, sn: String, photoBytes: ByteArray, fileName: String, mimeType: String): CoordinadorEquipoAuditoria {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("cuadrillaId", cuadrillaId)
            .addFormDataPart("sn", sn)
            .addFormDataPart("file", fileName, photoBytes.toRequestBody(mimeType.takeIf { it.isNotBlank() }?.toMediaTypeOrNull()))
            .build()
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_AUDITORIA_SUSTENTAR}").post(body).header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "POST ${request.url}"); return executeJson(request) { it.toCoordinadorEquipoAuditoria() }
    }

    suspend fun postInicioJornada(): String {
        if (!isConfigured) throw RedesApiException("Base URL del backend no configurada.")
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.INICIO_JORNADA}")
            .post("{}".toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "POST ${request.url}")
        return executeJson(request) { json ->
            json.optString("estadoRuta").ifBlank { "EN_CAMPO" }
        }
    }

    suspend fun postAlertaApp(tipo: String): String {
        if (!isConfigured) throw RedesApiException("Base URL del backend no configurada.")
        val body = org.json.JSONObject().apply { put("tipo", tipo) }
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.ALERTAS_APP}")
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "POST ${request.url}")
        return executeJson(request) { json ->
            json.optString("alertaId").ifBlank { throw RedesApiException("alertaId ausente en respuesta.") }
        }
    }

    // ── SUPERVISOR ────────────────────────────────────────────────────────────

    // ── ALMACEN ───────────────────────────────────────────────────────────────

    suspend fun fetchAlmacenStock(): List<AlmacenStockCuadrilla> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_STOCK}").get().header("X-Mobile-Role", "ALMACEN").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toAlmacenStockList() }
    }

    suspend fun fetchCoordinadorLiquidacion(ym: String): CoordinadorLiquidacionData {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_LIQUIDACION}?ym=$ym").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorLiquidacionData() }
    }

    suspend fun fetchCoordinadorPreliquidacion(ordenId: String): CoordinadorPreliquidacion {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_LIQUIDACION_PRELIQUIDACION}?ordenId=$ordenId").get().header("X-Mobile-Role", "COORDINADOR").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorPreliquidacion(ordenId) }
    }

    suspend fun postCoordinadorLiquidar(request: CoordinadorLiquidarRequest) {
        val json = org.json.JSONObject().apply {
            put("ordenId", request.ordenId)
            put("snOnt", request.snOnt)
            put("snMeshes", org.json.JSONArray(request.snMeshes))
            put("snBoxes", org.json.JSONArray(request.snBoxes))
            put("snFono", request.snFono)
            put("rotuloNapCto", request.rotuloNapCto)
            put("planGamer", request.planGamer)
            put("kitWifiPro", request.kitWifiPro)
            put("servicioCableadoMesh", request.servicioCableadoMesh)
            put("cat5e", request.cat5e)
            put("cat6", request.cat6)
            put("observacion", request.observacion)
        }
        val httpRequest = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.COORDINADOR_LIQUIDACION_LIQUIDAR}")
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "COORDINADOR")
            .build()
        Log.d(tag, "POST ${httpRequest.url}")
        executeWithoutBody(httpRequest)
    }

    suspend fun fetchAlmacenLiquidacion(ym: String): AlmacenLiquidacionData {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_LIQUIDACION}?ym=$ym").get().header("X-Mobile-Role", "ALMACEN").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toAlmacenLiquidacionData() }
    }

    suspend fun fetchAlmacenInstalaciones(ym: String): List<AlmacenInstalacion> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_INSTALACIONES}?ym=$ym").get().header("X-Mobile-Role", "ALMACEN").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toAlmacenInstalacionesList() }
    }

    suspend fun fetchAlmacenPreliquidacion(ordenId: String): AlmacenPreliquidacion {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_LIQUIDACION_PRELIQUIDACION}?ordenId=$ordenId").get().header("X-Mobile-Role", "ALMACEN").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toAlmacenPreliquidacion(ordenId) }
    }

    suspend fun lookupAlmacenSn(sn: String, cuadrillaId: String): AlmacenSnLookup {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_LIQUIDACION_SN_LOOKUP}?sn=$sn&cuadrillaId=$cuadrillaId").get().header("X-Mobile-Role", "ALMACEN").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toAlmacenSnLookup(sn) }
    }

    suspend fun postAlmacenLiquidar(request: AlmacenLiquidarRequest) {
        val json = org.json.JSONObject().apply {
            put("ordenId", request.ordenId)
            put("snOnt", request.snOnt)
            put("snMeshes", org.json.JSONArray(request.snMeshes))
            put("snBoxes", org.json.JSONArray(request.snBoxes))
            put("snFono", request.snFono)
            put("rotuloNapCto", request.rotuloNapCto)
            put("planGamer", request.planGamer)
            put("kitWifiPro", request.kitWifiPro)
            put("servicioCableadoMesh", request.servicioCableadoMesh)
            put("cat5e", request.cat5e)
            put("cat6", request.cat6)
            put("observacion", request.observacion)
        }
        val httpRequest = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_LIQUIDACION_LIQUIDAR}")
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "ALMACEN")
            .build()
        Log.d(tag, "POST ${httpRequest.url}")
        executeWithoutBody(httpRequest)
    }

    suspend fun fetchAlmacenCuadrillasMapa(): List<CuadrillaMapa> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_CUADRILLAS_MAPA}").get().header("X-Mobile-Role", "ALMACEN").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { json -> json.toCuadrillasMapa() }
    }

    suspend fun fetchAlmacenMapa(ymd: String): List<CoordinadorMapItem> {
        val request = Request.Builder().url("$normalizedBaseUrl${MobileEndpoints.ALMACEN_MAPA}?ymd=$ymd").get().header("X-Mobile-Role", "ALMACEN").build()
        Log.d(tag, "GET ${request.url}"); return executeJson(request) { it.toCoordinadorMapItems() }
    }

    suspend fun fetchSupervisorHome(): com.redes.app.data.supervisor.SupervisorHomeData {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_HOME}")
            .get()
            .header("X-Mobile-Role", "SUPERVISOR")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toSupervisorHomeData() }
    }

    suspend fun fetchSupervisorOrders(ymd: String, soloGarantias: Boolean = false): com.redes.app.data.supervisor.SupervisorOrdersData {
        val params = "?ymd=$ymd${if (soloGarantias) "&garantias=true" else ""}"
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_ORDERS}$params")
            .get()
            .header("X-Mobile-Role", "SUPERVISOR")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toSupervisorOrdersData() }
    }

    suspend fun fetchSupervisorOrderDetail(orderId: String): com.redes.app.data.supervisor.SupervisorOrderDetail {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.supervisorOrderDetail(orderId)}")
            .get()
            .header("X-Mobile-Role", "SUPERVISOR")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> (json.optJSONObject("item") ?: JSONObject()).toSupervisorOrderDetail() }
    }

    suspend fun postSupervisorSupervision(orderId: String, notas: String, observaciones: String) {
        val body = org.json.JSONObject().apply {
            put("orderId", orderId)
            put("notas", notas)
            put("observaciones", observaciones)
        }
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_SUPERVISION}")
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "SUPERVISOR")
            .build()
        Log.d(tag, "POST ${request.url}")
        executeWithoutBody(request)
    }

    suspend fun postSupervisorGarantiaUpdate(
        ordenId: String,
        motivoGarantia: String,
        diagnosticoGarantia: String,
        solucionGarantia: String,
        responsableGarantia: String,
        casoGarantia: String,
        imputadoGarantia: String,
    ) {
        val body = org.json.JSONObject().apply {
            put("ordenId", ordenId)
            put("motivoGarantia", motivoGarantia)
            put("diagnosticoGarantia", diagnosticoGarantia)
            put("solucionGarantia", solucionGarantia)
            put("responsableGarantia", responsableGarantia)
            put("casoGarantia", casoGarantia)
            put("imputadoGarantia", imputadoGarantia)
        }
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_GARANTIAS_UPDATE}")
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "SUPERVISOR")
            .build()
        Log.d(tag, "POST ${request.url}")
        executeWithoutBody(request)
    }

    suspend fun fetchSupervisorMapa(ymd: String, modo: com.redes.app.data.supervisor.SupervisorMapMode): List<com.redes.app.data.supervisor.SupervisorMapItem> {
        val modoStr = modo.name
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_MAPA}?ymd=$ymd&modo=$modoStr")
            .get()
            .header("X-Mobile-Role", "SUPERVISOR")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toSupervisorMapItems() }
    }

    suspend fun fetchSupervisorCuadrillasMapa(): List<CuadrillaMapa> {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_CUADRILLAS_MAPA}")
            .get()
            .header("X-Mobile-Role", "SUPERVISOR")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toCuadrillasMapa() }
    }

    suspend fun fetchSupervisorJornada(ymd: String): com.redes.app.data.supervisor.JornadaData {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_JORNADA}?ymd=$ymd")
            .get().header("X-Mobile-Role", "SUPERVISOR").build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toJornadaData() }
    }

    suspend fun postSupervisorJornadaEvento(tipo: String, lat: Double?, lng: Double?): com.redes.app.data.supervisor.SupervisorJornada {
        val body = org.json.JSONObject().apply {
            put("tipo", tipo)
            lat?.let { put("lat", it) }
            lng?.let { put("lng", it) }
        }
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.SUPERVISOR_JORNADA}")
            .post(body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .header("X-Mobile-Role", "SUPERVISOR").build()
        Log.d(tag, "POST ${request.url}")
        return executeJson(request) { json -> (json.optJSONObject("jornada") ?: JSONObject()).toSupervisorJornada() }
    }

    // ── TECNICO MAP ───────────────────────────────────────────────────────────

    suspend fun fetchTecnicoMap(ymd: String): TecnicoMapData {
        val request = Request.Builder()
            .url("$normalizedBaseUrl${MobileEndpoints.TECNICO_MAP}?ymd=$ymd")
            .get()
            .header("X-Mobile-Role", "TECNICO")
            .build()
        Log.d(tag, "GET ${request.url}")
        return executeJson(request) { json -> json.toTecnicoMapData() }
    }

    private fun <T> executeJson(request: Request, parser: (JSONObject) -> T): T {
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                Log.d(
                    tag,
                    "HTTP ${response.code} ${request.method} ${request.url} contentType=${response.header("Content-Type")} body=${body.take(220)}"
                )
                if (!response.isSuccessful) {
                    throw RedesApiException(
                        message = buildErrorMessage(request, response.code, body),
                        statusCode = response.code,
                    )
                }

                val json = parseJsonResponse(response, body)
                return parser(json)
            }
        } catch (exception: RedesApiException) {
            Log.e(tag, "RedesApiException ${request.method} ${request.url}: ${exception.message}", exception)
            throw exception
        } catch (exception: IOException) {
            Log.e(tag, "IOException ${request.method} ${request.url}: ${exception.message}", exception)
            throw RedesApiException(buildConnectionErrorMessage(request, exception))
        } catch (exception: Exception) {
            Log.e(tag, "Unexpected exception ${request.method} ${request.url}: ${exception.message}", exception)
            throw RedesApiException("Respuesta invalida del backend: ${exception.message ?: "sin detalle"}.")
        }
    }

    private fun buildErrorMessage(request: Request, statusCode: Int, body: String): String {
        return when (statusCode) {
            401 -> "El backend rechazo el token de Firebase."
            403 -> "El backend no permitio el acceso del usuario."
            404 -> when {
                request.url.encodedPath.endsWith(MobileEndpoints.BOOTSTRAP) ->
                    "El endpoint /api/mobile/bootstrap aun no existe en el backend."
                request.url.encodedPath.endsWith(MobileEndpoints.ME) ->
                    "El endpoint /api/mobile/me aun no existe en el backend."
                request.url.encodedPath.endsWith(MobileEndpoints.TECNICO_STOCK) ->
                    "El endpoint /api/mobile/tecnico/stock aun no existe en el backend."
                else -> "El endpoint solicitado aun no existe en el backend."
            }
            else -> {
                val parsed = runCatching { JSONObject(body).optString("error") }.getOrNull().orEmpty()
                parsed.ifBlank { "Error backend ($statusCode)." }
            }
        }
    }

    private fun executeWithoutBody(request: Request) {
        try {
            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                Log.d(
                    tag,
                    "HTTP ${response.code} ${request.method} ${request.url} contentType=${response.header("Content-Type")} body=${body.take(220)}"
                )
                if (!response.isSuccessful) {
                    throw RedesApiException(
                        message = buildErrorMessage(request, response.code, body),
                        statusCode = response.code,
                    )
                }
            }
        } catch (exception: RedesApiException) {
            Log.e(tag, "RedesApiException ${request.method} ${request.url}: ${exception.message}", exception)
            throw exception
        } catch (exception: IOException) {
            Log.e(tag, "IOException ${request.method} ${request.url}: ${exception.message}", exception)
            throw RedesApiException(buildConnectionErrorMessage(request, exception))
        }
    }

    private fun buildConnectionErrorMessage(request: Request, exception: IOException): String {
        val host = request.url.host.lowercase()
        val isLocalDevHost = host == "127.0.0.1" || host == "localhost"
        val isRefused = exception is ConnectException || exception.message?.contains("ECONNREFUSED", ignoreCase = true) == true
        return if (isLocalDevHost && isRefused) {
            "No se pudo conectar con el backend local. Si usas telefono fisico, vuelve a ejecutar adb reverse tcp:3000 tcp:3000."
        } else {
            "No se pudo conectar con el backend."
        }
    }

    private fun parseJsonResponse(response: Response, body: String): JSONObject {
        val contentType = response.header("Content-Type").orEmpty()
        val trimmedBody = body.trim()
        if (!contentType.contains("application/json", ignoreCase = true)) {
            val preview = trimmedBody.take(180).replace('\n', ' ')
            throw RedesApiException(
                "El backend respondio con Content-Type '$contentType' en lugar de JSON. Vista previa: $preview"
            )
        }

        return try {
            JSONObject(trimmedBody)
        } catch (exception: Exception) {
            val preview = trimmedBody.take(180).replace('\n', ' ')
            throw RedesApiException("JSON invalido del backend. Vista previa: $preview")
        }
    }
}
