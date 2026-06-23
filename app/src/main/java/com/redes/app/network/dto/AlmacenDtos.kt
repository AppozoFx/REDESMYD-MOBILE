package com.redes.app.network.dto

import com.redes.app.data.almacen.*
import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.toAlmacenStockList(): List<AlmacenStockCuadrilla> {
    val arr = optJSONArray("cuadrillas") ?: return emptyList()
    return (0 until arr.length()).mapNotNull { i ->
        val obj = arr.optJSONObject(i) ?: return@mapNotNull null
        AlmacenStockCuadrilla(
            cuadrillaId = obj.optString("cuadrillaId"),
            cuadrillaNombre = obj.optString("cuadrillaNombre"),
            ont = obj.optInt("ont"),
            mesh = obj.optInt("mesh"),
            fono = obj.optInt("fono"),
            box = obj.optInt("box"),
            total = obj.optInt("total"),
            equipos = run {
                val eArr = obj.optJSONArray("equipos") ?: return@run emptyList()
                (0 until eArr.length()).mapNotNull { j ->
                    val eq = eArr.optJSONObject(j) ?: return@mapNotNull null
                    AlmacenEquipo(sn = eq.optString("sn"), tipo = eq.optString("tipo"))
                }
            },
        )
    }
}

fun JSONObject.toAlmacenLiquidacionData(): AlmacenLiquidacionData {
    val ym = optString("ym")
    val arr = optJSONArray("items")
    val items = if (arr != null) {
        (0 until arr.length()).mapNotNull { i ->
            val obj = arr.optJSONObject(i) ?: return@mapNotNull null
            AlmacenLiquidacionItem(
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
    val kpi = AlmacenLiquidacionKpi(
        finalizadas = kpiObj?.optInt("finalizadas") ?: 0,
        liquidadas = kpiObj?.optInt("liquidadas") ?: 0,
        pendientes = kpiObj?.optInt("pendientes") ?: 0,
    )
    return AlmacenLiquidacionData(ym = ym, items = items, kpi = kpi)
}

fun JSONObject.toAlmacenPreliquidacion(ordenId: String): AlmacenPreliquidacion {
    val found = optBoolean("found")
    val item = optJSONObject("item")
    return AlmacenPreliquidacion(
        ordenId = ordenId,
        found = found,
        codigoCliente = item?.optString("codigoCliente") ?: "",
        fechaYmd = item?.optString("fechaYmd") ?: "",
        snOnt = item?.optString("snOnt") ?: "",
        snMeshes = item?.optJSONArray("snMeshes")?.toStringList() ?: emptyList(),
        snBoxes = item?.optJSONArray("snBoxes")?.toStringList() ?: emptyList(),
        snFono = item?.optString("snFono") ?: "",
        rotuloNapCto = item?.optString("rotuloNapCto") ?: "",
    )
}

fun JSONObject.toAlmacenSnLookup(sn: String): AlmacenSnLookup = AlmacenSnLookup(
    sn = optString("sn", sn),
    found = optBoolean("found"),
    equipo = optString("equipo"),
    inTargetCuadrillaStock = optBoolean("inTargetCuadrillaStock"),
    isInstalado = optBoolean("isInstalado"),
    reason = optString("reason"),
    actionHint = optString("actionHint"),
)

fun JSONObject.toAlmacenInstalacionesList(): List<AlmacenInstalacion> {
    val arr = optJSONArray("items") ?: return emptyList()
    return (0 until arr.length()).mapNotNull { i ->
        val obj = arr.optJSONObject(i) ?: return@mapNotNull null
        AlmacenInstalacion(
            id = obj.optString("id"),
            codigoCliente = obj.optString("codigoCliente"),
            cliente = obj.optString("cliente"),
            cuadrillaNombre = obj.optString("cuadrillaNombre"),
            fechaYmd = obj.optString("fechaYmd"),
            tipoOrden = obj.optString("tipoOrden"),
            plan = obj.optString("plan"),
            tipoCuadrilla = obj.optString("tipoCuadrilla"),
            coordinadorNombre = obj.optString("coordinadorNombre"),
            acta = obj.optString("acta"),
            snOnt = obj.optString("snOnt"),
            snMesh = obj.optJSONArray("snMesh")?.toStringList() ?: emptyList(),
            snBox = obj.optJSONArray("snBox")?.toStringList() ?: emptyList(),
            snFono = obj.optString("snFono"),
            precon = obj.optString("precon"),
            bobinaMetros = obj.optInt("bobinaMetros"),
            estadoMateriales = obj.optString("estadoMateriales", "pendiente"),
            planGamer = obj.optBoolean("planGamer"),
            kitWifiPro = obj.optBoolean("kitWifiPro"),
            servicioCableadoMesh = obj.optBoolean("servicioCableadoMesh"),
            cat5e = obj.optInt("cat5e"),
            cat6 = obj.optInt("cat6"),
            puntosUTP = obj.optInt("puntosUTP"),
            observacion = obj.optString("observacion"),
        )
    }
}

private fun JSONArray.toStringList(): List<String> =
    (0 until length()).map { optString(it) }.filter { it.isNotBlank() }
