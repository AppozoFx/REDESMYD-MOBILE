package com.redes.app.network

import android.util.Log
import com.redes.app.data.session.MobileBootstrap
import com.redes.app.data.session.MobileSession
import com.redes.app.data.tecnico.TecnicoHomeData
import com.redes.app.data.tecnico.TecnicoMapData
import com.redes.app.data.tecnico.TecnicoOrderDetail
import com.redes.app.data.tecnico.TecnicoOrdersData
import com.redes.app.data.tecnico.TecnicoStockData
import com.redes.app.network.dto.MobileBootstrapDto
import com.redes.app.network.dto.MobileSessionDto
import com.redes.app.network.dto.toTecnicoHomeData
import com.redes.app.network.dto.toTecnicoMapData
import com.redes.app.network.dto.toTecnicoOrderDetail
import com.redes.app.network.dto.toTecnicoOrdersData
import com.redes.app.network.dto.toTecnicoStockData
import okhttp3.OkHttpClient
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
