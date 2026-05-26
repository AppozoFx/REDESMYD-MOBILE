package com.redes.app.network.dto

import com.redes.app.data.tecnico.TecnicoConsumedMaterial
import com.redes.app.data.tecnico.TecnicoCuadrillaSummary
import com.redes.app.data.tecnico.TecnicoEquipmentSummary
import com.redes.app.data.tecnico.TecnicoHomeData
import com.redes.app.data.tecnico.TecnicoInstalledEquipment
import com.redes.app.data.tecnico.TecnicoKpis
import com.redes.app.data.tecnico.TecnicoMapData
import com.redes.app.data.tecnico.TecnicoMapItem
import com.redes.app.data.tecnico.TecnicoMember
import com.redes.app.data.tecnico.TecnicoOrderDetail
import com.redes.app.data.tecnico.TecnicoOrdersData
import com.redes.app.data.tecnico.TecnicoOrdersUpdateInfo
import com.redes.app.data.tecnico.TecnicoOrderSummary
import com.redes.app.data.tecnico.TecnicoStockData
import com.redes.app.data.tecnico.TecnicoStockEquipment
import com.redes.app.data.tecnico.TecnicoStockMaterial
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.toTecnicoCuadrillaSummary(): TecnicoCuadrillaSummary {
    return TecnicoCuadrillaSummary(
        id = optString("id"),
        nombre = optString("nombre"),
        categoria = optString("categoria"),
        area = optString("area"),
        coordinadorUid = optString("coordinadorUid"),
        coordinadorNombre = optString("coordinadorNombre"),
        gestorUid = optString("gestorUid"),
        gestorNombre = optString("gestorNombre"),
        integrantes = optJSONArray("integrantes").toMembers(),
    )
}

internal fun JSONObject.toTecnicoHomeData(): TecnicoHomeData {
    val tecnico = optJSONObject("tecnico") ?: JSONObject()
    val kpis = optJSONObject("kpis") ?: JSONObject()
    return TecnicoHomeData(
        fecha = optString("fecha"),
        tecnicoUid = tecnico.optString("uid"),
        tecnicoNombre = tecnico.optString("nombre"),
        cuadrilla = (optJSONObject("cuadrilla") ?: JSONObject()).toTecnicoCuadrillaSummary(),
        kpis = TecnicoKpis(
            instalacionesMes = kpis.optInt("instalacionesMes"),
            canceladasMes = kpis.optInt("canceladasMes"),
            anuladasMes = kpis.optInt("anuladasMes"),
            regestionMes = kpis.optInt("regestionMes"),
            garantiasMes = kpis.optInt("garantiasMes"),
            porcentajeGarantias = kpis.optDouble("porcentajeGarantias", 0.0),
        ),
        equipmentSummary = optJSONArray("equipmentSummary").toEquipmentSummary(),
    )
}

internal fun JSONObject.toTecnicoOrdersData(): TecnicoOrdersData {
    val update = optJSONObject("updateInfo")
    return TecnicoOrdersData(
        ymd = optString("ymd"),
        updateInfo = update?.let {
            TecnicoOrdersUpdateInfo(
                at = it.optString("at").ifBlank { null },
                byUid = it.optString("byUid"),
                byNombre = it.optString("byNombre"),
                sourceLabel = it.optString("sourceLabel"),
            )
        },
        items = optJSONArray("items").toTecnicoOrderSummaries(),
    )
}

internal fun JSONArray?.toTecnicoOrderSummaries(): List<TecnicoOrderSummary> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                TecnicoOrderSummary(
                    id = json.optString("id"),
                    ordenId = json.optString("ordenId"),
                    cliente = json.optString("cliente"),
                    codigoCliente = json.optString("codigoCliente"),
                    direccion = json.optString("direccion"),
                    estado = json.optString("estado"),
                    tipoTrabajo = json.optString("tipoTrabajo"),
                    tipoServicio = json.optString("tipoServicio"),
                    cuadrillaId = json.optString("cuadrillaId"),
                    cuadrillaNombre = json.optString("cuadrillaNombre"),
                    fechaProgramadaYmd = json.optString("fechaProgramadaYmd"),
                    fechaProgramadaHm = json.optString("fechaProgramadaHm"),
                    fechaFinVisiYmd = json.optString("fechaFinVisiYmd"),
                    fechaFinVisiHm = json.optString("fechaFinVisiHm"),
                    isGarantia = json.optBoolean("isGarantia"),
                    isFinalizada = json.optBoolean("isFinalizada"),
                    isLiquidated = json.optBoolean("isLiquidated"),
                    cantMesh = json.optInt("cantMesh"),
                    cantFono = json.optInt("cantFono"),
                    cantBox = json.optInt("cantBox"),
                    lat = json.optNullableDouble("lat"),
                    lng = json.optNullableDouble("lng"),
                )
            )
        }
    }
}

internal fun JSONObject.toTecnicoOrderDetail(): TecnicoOrderDetail {
    return TecnicoOrderDetail(
        id = optString("id"),
        ordenId = optString("ordenId"),
        cliente = optString("cliente"),
        codigoCliente = optString("codigoCliente"),
        direccion = optString("direccion"),
        estado = optString("estado"),
        tipoTrabajo = optString("tipoTrabajo"),
        tipoServicio = optString("tipoServicio"),
        plan = optString("plan"),
        fechaProgramadaYmd = optString("fechaProgramadaYmd"),
        fechaProgramadaHm = optString("fechaProgramadaHm"),
        fechaFinVisiYmd = optString("fechaFinVisiYmd"),
        fechaFinVisiHm = optString("fechaFinVisiHm"),
        isGarantia = optBoolean("isGarantia"),
        isFinalizada = optBoolean("isFinalizada"),
        isLiquidated = optBoolean("isLiquidated"),
        liquidacionEstado = optString("liquidacionEstado"),
        liquidadoAt = optString("liquidadoAt").ifBlank { null },
        observacion = optString("observacion"),
        materiales = optJSONArray("materiales").toConsumedMaterials(),
        equipos = optJSONArray("equipos").toInstalledEquipments(),
    )
}

internal fun JSONObject.toTecnicoStockData(): TecnicoStockData {
    return TecnicoStockData(
        cuadrilla = (optJSONObject("cuadrilla") ?: JSONObject()).toTecnicoCuadrillaSummary(),
        equipos = optJSONArray("equipos").toStockEquipments(),
        materiales = optJSONArray("materiales").toStockMaterials(),
    )
}

internal fun JSONObject.toTecnicoMapData(): TecnicoMapData {
    return TecnicoMapData(
        ymd = optString("ymd"),
        cuadrilla = (optJSONObject("cuadrilla") ?: JSONObject()).toTecnicoCuadrillaSummary(),
        items = optJSONArray("items").toMapItems(),
    )
}

private fun JSONArray?.toMembers(): List<TecnicoMember> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                TecnicoMember(
                    uid = json.optString("uid"),
                    nombre = json.optString("nombre"),
                )
            )
        }
    }
}

private fun JSONArray?.toConsumedMaterials(): List<TecnicoConsumedMaterial> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                TecnicoConsumedMaterial(
                    materialId = json.optString("materialId"),
                    nombre = json.optString("nombre"),
                    cantidad = json.optDouble("cantidad", 0.0),
                    metros = json.optDouble("metros", 0.0),
                    status = json.optString("status"),
                )
            )
        }
    }
}

private fun JSONArray?.toInstalledEquipments(): List<TecnicoInstalledEquipment> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                TecnicoInstalledEquipment(
                    sn = json.optString("sn"),
                    tipo = json.optString("tipo"),
                    proid = json.optString("proid"),
                    descripcion = json.optString("descripcion"),
                )
            )
        }
    }
}

private fun JSONArray?.toStockEquipments(): List<TecnicoStockEquipment> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                TecnicoStockEquipment(
                    id = json.optString("id"),
                    sn = json.optString("sn"),
                    tipo = json.optString("tipo"),
                    proid = json.optString("proid"),
                )
            )
        }
    }
}

private fun JSONArray?.toStockMaterials(): List<TecnicoStockMaterial> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                TecnicoStockMaterial(
                    id = json.optString("id"),
                    nombre = json.optString("nombre"),
                    unidadTipo = json.optString("unidadTipo"),
                    stockUnd = json.optDouble("stockUnd", 0.0),
                    stockCm = json.optDouble("stockCm", 0.0),
                )
            )
        }
    }
}

private fun JSONArray?.toMapItems(): List<TecnicoMapItem> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            val lat = json.optNullableDouble("lat") ?: continue
            val lng = json.optNullableDouble("lng") ?: continue
            add(
                TecnicoMapItem(
                    id = json.optString("id"),
                    ordenId = json.optString("ordenId"),
                    cliente = json.optString("cliente"),
                    codigoCliente = json.optString("codigoCliente"),
                    direccion = json.optString("direccion"),
                    estado = json.optString("estado"),
                    tipoTrabajo = json.optString("tipoTrabajo"),
                    fechaProgramadaHm = json.optString("fechaProgramadaHm"),
                    lat = lat,
                    lng = lng,
                )
            )
        }
    }
}

private fun JSONArray?.toEquipmentSummary(): List<TecnicoEquipmentSummary> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                TecnicoEquipmentSummary(
                    tipo = json.optString("tipo"),
                    cantidad = json.optInt("cantidad"),
                )
            )
        }
    }
}

private fun JSONObject.optNullableDouble(name: String): Double? {
    if (isNull(name)) return null
    return when (val value = opt(name)) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull()
        else -> null
    }
}
