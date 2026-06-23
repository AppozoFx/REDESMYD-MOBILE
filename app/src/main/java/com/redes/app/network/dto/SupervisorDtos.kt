package com.redes.app.network.dto

import com.redes.app.data.supervisor.SupervisorCuadrillaSummary
import com.redes.app.data.supervisor.SupervisorHomeData
import com.redes.app.data.supervisor.SupervisorInfo
import com.redes.app.data.supervisor.SupervisorMapItem
import com.redes.app.data.supervisor.SupervisorOrderDetail
import com.redes.app.data.supervisor.SupervisorOrderSummary
import com.redes.app.data.supervisor.SupervisorOrdersData
import com.redes.app.data.supervisor.JornadaData
import com.redes.app.data.supervisor.JornadaEstado
import com.redes.app.data.supervisor.OficinaConfig
import com.redes.app.data.supervisor.SupervisorJornada
import com.redes.app.data.supervisor.SupervisorUpdateInfo
import com.redes.app.data.supervisor.SupervisorRegionResumen
import com.redes.app.data.supervisor.SupervisorTotales
import com.redes.app.data.supervisor.SupervisionData
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.toSupervisorHomeData(): SupervisorHomeData {
    val sup = optJSONObject("supervisor") ?: JSONObject()
    val tot = optJSONObject("totales") ?: JSONObject()
    return SupervisorHomeData(
        ymd = optString("ymd"),
        supervisor = SupervisorInfo(
            uid = sup.optString("uid"),
            nombre = sup.optString("nombre"),
            nombreCorto = sup.optString("nombreCorto"),
            vehiculoPlaca = sup.optString("vehiculoPlaca"),
        ),
        trackingHabilitado = optBoolean("trackingHabilitado", true),
        regionesHoy = (optJSONArray("regionesHoy") ?: JSONArray()).toStringList(),
        cuadrillasHoy = (optJSONArray("cuadrillasHoy") ?: JSONArray()).toSupervisorCuadrillaList(),
        ordenesPorRegion = (optJSONArray("ordenesPorRegion") ?: JSONArray()).toRegionResumenList(),
        totales = SupervisorTotales(
            ordenes = tot.optInt("ordenes"),
            garantias = tot.optInt("garantias"),
            finalizadas = tot.optInt("finalizadas"),
            pendientes = tot.optInt("pendientes"),
        ),
    )
}

private fun JSONArray.toSupervisorCuadrillaList(): List<SupervisorCuadrillaSummary> {
    return (0 until length()).map { i ->
        val o = optJSONObject(i) ?: JSONObject()
        SupervisorCuadrillaSummary(
            id = o.optString("id"),
            nombre = o.optString("nombre"),
            ordenesTotal = o.optInt("ordenesTotal"),
            garantiasTotal = o.optInt("garantiasTotal"),
            estadoActual = o.optString("estadoActual"),
        )
    }
}

private fun JSONArray.toRegionResumenList(): List<SupervisorRegionResumen> {
    return (0 until length()).map { i ->
        val o = optJSONObject(i) ?: JSONObject()
        SupervisorRegionResumen(
            regionId = o.optString("regionId"),
            regionNombre = o.optString("regionNombre"),
            total = o.optInt("total"),
            garantias = o.optInt("garantias"),
            finalizadas = o.optInt("finalizadas"),
            pendientes = o.optInt("pendientes"),
        )
    }
}

internal fun JSONObject.toSupervisorOrderSummary(): SupervisorOrderSummary {
    return SupervisorOrderSummary(
        id = optString("id"),
        ordenId = optString("ordenId"),
        cliente = optString("cliente"),
        codigoCliente = optString("codigoCliente"),
        direccion = optString("direccion"),
        estado = optString("estado"),
        tipoTrabajo = optString("tipoTrabajo"),
        tipoServicio = optString("tipoServicio"),
        fechaProgramadaHm = optString("fechaProgramadaHm"),
        fechaProgramadaYmd = optString("fechaProgramadaYmd"),
        isGarantia = optBoolean("isGarantia"),
        isFinalizada = optBoolean("isFinalizada"),
        region = optString("region"),
        cuadrillaId = optString("cuadrillaId"),
        cuadrillaNombre = optString("cuadrillaNombre"),
        hasSupervision = optBoolean("hasSupervision"),
        cantMesh = optInt("cantMesh"),
        cantFono = optInt("cantFono"),
        cantBox = optInt("cantBox"),
        lat = if (isNull("lat")) null else optDouble("lat"),
        lng = if (isNull("lng")) null else optDouble("lng"),
    )
}

internal fun JSONObject.toSupervisorOrdersData(): SupervisorOrdersData {
    val items = (optJSONArray("items") ?: JSONArray())
    val updateJson = optJSONObject("updateInfo")
    return SupervisorOrdersData(
        ymd = optString("ymd"),
        updateInfo = updateJson?.let {
            SupervisorUpdateInfo(
                at = if (it.isNull("at")) null else it.optString("at").takeIf { s -> s.isNotBlank() },
                byNombre = it.optString("byNombre").takeIf { s -> s.isNotBlank() },
            )
        },
        items = (0 until items.length()).map { i ->
            (items.optJSONObject(i) ?: JSONObject()).toSupervisorOrderSummary()
        },
    )
}

internal fun JSONObject.toSupervisorOrderDetail(): SupervisorOrderDetail {
    val supJson = optJSONObject("supervision")
    return SupervisorOrderDetail(
        id = optString("id"),
        ordenId = optString("ordenId"),
        cliente = optString("cliente"),
        codigoCliente = optString("codigoCliente"),
        documento = optString("documento"),
        telefono = optString("telefono"),
        direccion = optString("direccion"),
        estado = optString("estado"),
        tipoTrabajo = optString("tipoTrabajo"),
        tipoServicio = optString("tipoServicio"),
        fechaProgramadaHm = optString("fechaProgramadaHm"),
        fechaProgramadaYmd = optString("fechaProgramadaYmd"),
        isGarantia = optBoolean("isGarantia"),
        region = optString("region"),
        cuadrillaId = optString("cuadrillaId"),
        cuadrillaNombre = optString("cuadrillaNombre"),
        lat = if (isNull("lat")) null else optDouble("lat"),
        lng = if (isNull("lng")) null else optDouble("lng"),
        plan = optString("plan"),
        supervision = supJson?.let {
            SupervisionData(
                notas = it.optString("notas"),
                observaciones = it.optString("observaciones"),
                estadoSupervision = it.optString("estadoSupervision"),
                supervisorUid = it.optString("supervisorUid"),
                supervisadoEn = it.optString("supervisadoEn"),
            )
        },
        diagnosticoGarantia = optString("diagnosticoGarantia"),
        solucionGarantia = optString("solucionGarantia"),
        responsableGarantia = optString("responsableGarantia"),
        casoGarantia = optString("casoGarantia"),
        imputadoGarantia = optString("imputadoGarantia"),
        motivoGarantia = optString("motivoGarantia"),
    )
}

internal fun JSONObject.toSupervisorMapItems(): List<SupervisorMapItem> {
    val items = optJSONArray("items") ?: JSONArray()
    return (0 until items.length()).mapNotNull { i ->
        val o = items.optJSONObject(i) ?: return@mapNotNull null
        if (o.isNull("lat") || o.isNull("lng")) return@mapNotNull null
        SupervisorMapItem(
            id = o.optString("id"),
            ordenId = o.optString("ordenId"),
            cliente = o.optString("cliente"),
            direccion = o.optString("direccion"),
            estado = o.optString("estado"),
            isGarantia = o.optBoolean("isGarantia"),
            cuadrillaId = o.optString("cuadrillaId"),
            cuadrillaNombre = o.optString("cuadrillaNombre"),
            region = o.optString("region"),
            hasSupervision = o.optBoolean("hasSupervision"),
            lat = o.optDouble("lat"),
            lng = o.optDouble("lng"),
        )
    }
}

private fun JSONArray.toStringList(): List<String> =
    (0 until length()).map { i -> optString(i) }.filter { it.isNotBlank() }

internal fun JSONObject.toJornadaEstado(): JornadaEstado = when (optString("estado").uppercase()) {
    "EN_RUTA"          -> JornadaEstado.EN_RUTA
    "EN_REFRIGERIO"    -> JornadaEstado.EN_REFRIGERIO
    "FINALIZADA"       -> JornadaEstado.FINALIZADA
    else               -> JornadaEstado.SIN_INICIAR
}

internal fun JSONObject.toSupervisorJornada(): SupervisorJornada = SupervisorJornada(
    uid = optString("uid"),
    ymd = optString("ymd"),
    estado = toJornadaEstado(),
    horaInicio = optString("horaInicio").takeIf { it.isNotBlank() },
    horaFin = optString("horaFin").takeIf { it.isNotBlank() },
    horaInicioRefrigerio = optString("horaInicioRefrigerio").takeIf { it.isNotBlank() },
    horaFinRefrigerio = optString("horaFinRefrigerio").takeIf { it.isNotBlank() },
    latInicio = if (isNull("latInicio")) null else optDouble("latInicio"),
    lngInicio = if (isNull("lngInicio")) null else optDouble("lngInicio"),
    latFin = if (isNull("latFin")) null else optDouble("latFin"),
    lngFin = if (isNull("lngFin")) null else optDouble("lngFin"),
)

internal fun JSONObject.toJornadaData(): JornadaData {
    val jornadaJson = optJSONObject("jornada") ?: JSONObject()
    val oficinaJson = optJSONObject("oficina")
    return JornadaData(
        jornada = jornadaJson.toSupervisorJornada(),
        oficina = oficinaJson?.let {
            OficinaConfig(
                lat = it.optDouble("lat"),
                lng = it.optDouble("lng"),
                radioMetros = it.optDouble("radioMetros", 500.0),
            )
        },
    )
}
