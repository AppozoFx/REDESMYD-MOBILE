package com.redes.app.network.dto

import com.redes.app.data.coordinador.*
import org.json.JSONArray
import org.json.JSONObject

internal fun JSONObject.toCoordinadorResumen(): CoordinadorResumen {
    val resumen = optJSONObject("resumen") ?: JSONObject()
    val totales = CoordinadorKpis(
        finalizadas = resumen.optInt("finalizadas"),
        garantias = resumen.optInt("garantias"),
        ventas = resumen.optInt("ventas"),
        cat6 = resumen.optInt("cat6"),
        cat5e = resumen.optInt("cat5e"),
    )
    val cuadrillas = optJSONArray("cuadrillas").toCoordinadorCuadrillaKpiList()
    return CoordinadorResumen(ym = optString("ym"), totales = totales, cuadrillas = cuadrillas)
}

private fun JSONArray?.toCoordinadorCuadrillaKpiList(): List<CoordinadorCuadrillaKpi> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorCuadrillaKpi(
                cuadrillaId = j.optString("cuadrillaId"),
                cuadrillaNombre = j.optString("cuadrillaNombre"),
                finalizadas = j.optInt("finalizadas"),
                garantias = j.optInt("garantias"),
                ventas = j.optInt("ventas"),
                cat6 = j.optInt("cat6"),
                cat5e = j.optInt("cat5e"),
                dias = j.optJSONArray("dias").toDiaKpiList(),
            ))
        }
    }
}

private fun JSONArray?.toDiaKpiList(): List<CoordinadorDiaKpi> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorDiaKpi(j.optString("ymd"), j.optInt("finalizadas"), j.optInt("garantias"), j.optInt("cat6"), j.optInt("cat5e")))
        }
    }
}

internal fun JSONObject.toCoordinadorCuadrillaData(): CoordinadorCuadrillaData {
    val ui = optJSONObject("updateInfo")
    return CoordinadorCuadrillaData(
        ymd = optString("ymd"),
        cuadrillas = optJSONArray("cuadrillas").toCoordinadorCuadrillaList(),
        updateInfo = ui?.let { CoordinadorUpdateInfo(at = it.optString("at").ifBlank { null }, byNombre = it.optString("byNombre")) },
    )
}

private fun JSONArray?.toCoordinadorCuadrillaList(): List<CoordinadorCuadrilla> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            val ordenes = j.optJSONObject("ordenes") ?: JSONObject()
            add(CoordinadorCuadrilla(
                id = j.optString("id"),
                nombre = j.optString("nombre"),
                categoria = j.optString("categoria"),
                estadoRuta = j.optString("estadoRuta").ifBlank { "OPERATIVA" },
                lat = j.optNullableDouble("lat"),
                lng = j.optNullableDouble("lng"),
                lastLocationAt = j.optTimestampMillis("lastLocationAt"),
                estadoActual = j.optString("estadoActual").ifBlank { "EN_RUTA" },
                ordenes = CoordinadorOrdenesResumen(
                    total = ordenes.optInt("total"),
                    agendadas = ordenes.optInt("agendadas"),
                    iniciadas = ordenes.optInt("iniciadas"),
                    finalizadas = ordenes.optInt("finalizadas"),
                    items = ordenes.optJSONArray("items").toOrdenItemList(),
                ),
            ))
        }
    }
}

private fun JSONArray?.toOrdenItemList(): List<CoordinadorOrdenItem> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorOrdenItem(
                id = j.optString("id"),
                ordenId = j.optString("ordenId"),
                cliente = j.optString("cliente"),
                estado = j.optString("estado"),
                motivoCancelacion = j.optString("motivoCancelacion"),
                hora = j.optString("hora"),
                tipo = j.optString("tipo"),
                direccion = j.optString("direccion"),
                cantMesh = j.optInt("cantMesh"),
                cantFono = j.optInt("cantFono"),
                cantBox = j.optInt("cantBox"),
            ))
        }
    }
}

internal fun JSONObject.toCoordinadorOrdenDetail(): CoordinadorOrdenDetail {
    val item = optJSONObject("item") ?: this
    return CoordinadorOrdenDetail(
        id = item.optString("id"),
        ordenId = item.optString("ordenId"),
        cliente = item.optString("cliente"),
        codigoCliente = item.optString("codigoCliente"),
        documento = item.optString("documento"),
        telefono = item.optString("telefono"),
        direccion = item.optString("direccion"),
        estado = item.optString("estado"),
        tipoTrabajo = item.optString("tipoTrabajo"),
        tipoServicio = item.optString("tipoServicio"),
        fechaProgramadaHm = item.optString("fechaProgramadaHm"),
        fechaProgramadaYmd = item.optString("fechaProgramadaYmd"),
        isGarantia = item.optBoolean("isGarantia"),
        region = item.optString("region"),
        cuadrillaId = item.optString("cuadrillaId"),
        cuadrillaNombre = item.optString("cuadrillaNombre"),
        lat = item.optNullableDouble("lat"),
        lng = item.optNullableDouble("lng"),
        cantMesh = item.optInt("cantMesh"),
        cantFono = item.optInt("cantFono"),
        cantBox = item.optInt("cantBox"),
    )
}

internal fun JSONObject.toCoordinadorMapItems(): List<CoordinadorMapItem> {
    return optJSONArray("items").toCoordinadorMapItemList()
}

private fun JSONArray?.toCoordinadorMapItemList(): List<CoordinadorMapItem> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            val lat = j.optNullableDouble("lat") ?: continue
            val lng = j.optNullableDouble("lng") ?: continue
            add(CoordinadorMapItem(
                id = j.optString("id"),
                ordenId = j.optString("ordenId"),
                cliente = j.optString("cliente"),
                codigoCliente = j.optString("codigoCliente"),
                direccion = j.optString("direccion"),
                estado = j.optString("estado"),
                tipoTrabajo = j.optString("tipoTrabajo"),
                fechaProgramadaHm = j.optString("fechaProgramadaHm"),
                cuadrillaId = j.optString("cuadrillaId"),
                cuadrillaNombre = j.optString("cuadrillaNombre"),
                lat = lat,
                lng = lng,
            ))
        }
    }
}

internal fun JSONObject.toCoordinadorStockList(): List<CoordinadorStockCuadrilla> {
    return optJSONArray("cuadrillas").toStockCuadrillaList()
}

private fun JSONArray?.toStockCuadrillaList(): List<CoordinadorStockCuadrilla> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorStockCuadrilla(
                cuadrillaId = j.optString("cuadrillaId"),
                cuadrillaNombre = j.optString("cuadrillaNombre"),
                ont = j.optInt("ont"),
                mesh = j.optInt("mesh"),
                fono = j.optInt("fono"),
                box = j.optInt("box"),
                total = j.optInt("total"),
                equipos = j.optJSONArray("equipos").toEquipoStockList(),
            ))
        }
    }
}

private fun JSONArray?.toEquipoStockList(): List<CoordinadorEquipoStock> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorEquipoStock(sn = j.optString("sn"), tipo = j.optString("tipo")))
        }
    }
}

internal fun JSONObject.toCoordinadorAuditoriaList(): List<CoordinadorAuditoriaCuadrilla> {
    return optJSONArray("cuadrillas").toAuditoriaCuadrillaList()
}

private fun JSONArray?.toAuditoriaCuadrillaList(): List<CoordinadorAuditoriaCuadrilla> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorAuditoriaCuadrilla(
                cuadrillaId = j.optString("cuadrillaId"),
                cuadrillaNombre = j.optString("cuadrillaNombre"),
                pendiente = j.optInt("pendiente"),
                sustentada = j.optInt("sustentada"),
                total = j.optInt("total"),
                items = j.optJSONArray("items").toEquipoAuditoriaList(),
            ))
        }
    }
}

private fun JSONArray?.toEquipoAuditoriaList(): List<CoordinadorEquipoAuditoria> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorEquipoAuditoria(
                sn = j.optString("sn"),
                tipo = j.optString("tipo"),
                estado = j.optString("estado"),
                fotoURL = j.optString("fotoURL").takeIf { it.isNotBlank() },
            ))
        }
    }
}

internal fun JSONObject.toCoordinadorEquipoAuditoria(): CoordinadorEquipoAuditoria {
    val item = optJSONObject("item") ?: this
    return CoordinadorEquipoAuditoria(
        sn = item.optString("sn"),
        tipo = item.optString("tipo"),
        estado = item.optString("estado").ifBlank { "sustentada" },
        fotoURL = item.optString("fotoURL").takeIf { it.isNotBlank() },
    )
}

internal fun JSONObject.toCoordinadorPredespacho(): CoordinadorPredespacho {
    val rows = optJSONArray("rows").toPredespachoRowList()
    return CoordinadorPredespacho(
        tienePredespacho = optBoolean("tienePredespacho") && rows.isNotEmpty(),
        ymd = optString("ymd"),
        rows = rows,
    )
}

private fun JSONArray?.toPredespachoRowList(): List<CoordinadorPredespachoRow> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            val pc = j.optJSONObject("precon")
            add(CoordinadorPredespachoRow(
                cuadrillaId    = j.optString("cuadrillaId"),
                cuadrillaNombre = j.optString("cuadrillaNombre"),
                ont            = j.optInt("ont"),
                mesh           = j.optInt("mesh"),
                fono           = j.optInt("fono"),
                box            = j.optInt("box"),
                bobinaResi     = j.optInt("bobinaResi"),
                rolloCondo     = j.optBoolean("rolloCondo"),
                updatedByName  = j.optString("updatedByName"),
                precon50       = pc?.optInt("PRECON_50")  ?: 0,
                precon100      = pc?.optInt("PRECON_100") ?: 0,
                precon150      = pc?.optInt("PRECON_150") ?: 0,
                precon200      = pc?.optInt("PRECON_200") ?: 0,
            ))
        }
    }
}

internal fun JSONObject.toCoordinadorVentaList(): List<CoordinadorVenta> {
    return optJSONArray("items").toVentaList()
}

private fun JSONArray?.toVentaList(): List<CoordinadorVenta> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorVenta(j.optString("id"), j.optString("cuadrillaId"), j.optString("cuadrillaNombre"), j.optInt("totalCents"), j.optInt("saldoPendienteCents"), j.optInt("cuotasTotal"), j.optInt("cuotasPagadas"), j.optString("estado"), j.optString("area"), j.optString("creadoAtStr").takeIf { it.isNotBlank() }))
        }
    }
}

internal fun JSONObject.toCoordinadorPlantillasList(): List<CoordinadorPlantillasCuadrilla> {
    return optJSONArray("pendientesByCuadrilla").toPlantillasCuadrillaList()
}

private fun JSONArray?.toPlantillasCuadrillaList(): List<CoordinadorPlantillasCuadrilla> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            val pedidos = j.optJSONArray("pedidos").toPedidoList()
            add(CoordinadorPlantillasCuadrilla(j.optString("cuadrillaId"), j.optString("cuadrillaNombre"), j.optInt("total"), pedidos))
        }
    }
}

private fun JSONArray?.toPedidoList(): List<CoordinadorPedidoPendiente> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) {
            val j = optJSONObject(i) ?: continue
            add(CoordinadorPedidoPendiente(j.optString("pedido"), j.optString("cliente"), j.optString("ymd")))
        }
    }
}

internal fun JSONObject.toCoordinadorLiquidacionData(): CoordinadorLiquidacionData {
    val arr = optJSONArray("items")
    val items = if (arr != null) {
        (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            CoordinadorLiquidacionItem(
                id = obj.optString("id"),
                ordenId = obj.optString("ordenId"),
                codigoCliente = obj.optString("codigoCliente"),
                cliente = obj.optString("cliente"),
                cuadrillaId = obj.optString("cuadrillaId"),
                cuadrillaNombre = obj.optString("cuadrillaNombre"),
                fechaYmd = obj.optString("fechaYmd"),
                fechaHm = obj.optString("fechaHm"),
                tipo = obj.optString("tipo"),
                plan = obj.optString("plan"),
                liquidado = obj.optBoolean("liquidado"),
                correccionPendiente = obj.optBoolean("correccionPendiente"),
                cantMesh = obj.optString("cantMesh", "0"),
                cantFono = obj.optString("cantFono", "0"),
                cantBox = obj.optString("cantBox", "0"),
            )
        }
    } else emptyList()
    val kpiObj = optJSONObject("kpi")
    val kpi = CoordinadorLiquidacionKpi(
        finalizadas = kpiObj?.optInt("finalizadas") ?: 0,
        liquidadas = kpiObj?.optInt("liquidadas") ?: 0,
        pendientes = kpiObj?.optInt("pendientes") ?: 0,
    )
    return CoordinadorLiquidacionData(ym = optString("ym"), items = items, kpi = kpi)
}

internal fun JSONObject.toCoordinadorPreliquidacion(ordenId: String): CoordinadorPreliquidacion {
    val found = optBoolean("found")
    val item = optJSONObject("item")
    return CoordinadorPreliquidacion(
        ordenId = ordenId,
        found = found,
        codigoCliente = item?.optString("codigoCliente") ?: "",
        fechaYmd = item?.optString("fechaYmd") ?: "",
        snOnt = item?.optString("snOnt") ?: "",
        snMeshes = item?.optJSONArray("snMeshes")?.toCoordStringList() ?: emptyList(),
        snBoxes = item?.optJSONArray("snBoxes")?.toCoordStringList() ?: emptyList(),
        snFono = item?.optString("snFono") ?: "",
        rotuloNapCto = item?.optString("rotuloNapCto") ?: "",
    )
}

private fun JSONArray.toCoordStringList(): List<String> =
    (0 until length()).map { optString(it) }.filter { it.isNotBlank() }
