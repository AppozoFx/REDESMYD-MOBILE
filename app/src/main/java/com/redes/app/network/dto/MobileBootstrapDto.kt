package com.redes.app.network.dto

import com.redes.app.data.session.MobileBootstrap
import com.redes.app.data.session.MobileComunicado
import org.json.JSONArray
import org.json.JSONObject

data class MobileBootstrapDto(
    val session: MobileSessionDto,
    val comunicados: List<MobileComunicadoDto>,
    val requiresComunicadosGate: Boolean,
    val roleSelectionRequired: Boolean,
    val defaultRole: String?,
) {
    fun toDomain(): MobileBootstrap {
        return MobileBootstrap(
            session = session.toDomain(),
            comunicados = comunicados.map { it.toDomain() },
            requiresComunicadosGate = requiresComunicadosGate,
            roleSelectionRequired = roleSelectionRequired,
            defaultRole = defaultRole,
        )
    }

    companion object {
        fun fromJson(json: JSONObject): MobileBootstrapDto {
            return MobileBootstrapDto(
                session = MobileSessionDto.fromJson(json.optJSONObject("session") ?: JSONObject()),
                comunicados = json.optJSONArray("comunicados").toComunicados(),
                requiresComunicadosGate = json.optBoolean("requiresComunicadosGate", false),
                roleSelectionRequired = json.optBoolean("roleSelectionRequired", false),
                defaultRole = json.optString("defaultRole").ifBlank { null },
            )
        }
    }
}

data class MobileComunicadoDto(
    val id: String,
    val titulo: String,
    val cuerpo: String,
    val obligatorio: Boolean,
    val persistencia: String,
    val placement: String,
    val target: String,
    val imageUrl: String?,
    val linkUrl: String?,
    val linkLabel: String?,
    val visibleDesde: String?,
    val visibleHasta: String?,
) {
    fun toDomain(): MobileComunicado {
        return MobileComunicado(
            id = id,
            titulo = titulo,
            cuerpo = cuerpo,
            obligatorio = obligatorio,
            persistencia = persistencia,
            placement = placement,
            target = target,
            imageUrl = imageUrl,
            linkUrl = linkUrl,
            linkLabel = linkLabel,
            visibleDesde = visibleDesde,
            visibleHasta = visibleHasta,
        )
    }
}

private fun JSONArray?.toComunicados(): List<MobileComunicadoDto> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val json = optJSONObject(index) ?: continue
            add(
                MobileComunicadoDto(
                    id = json.optString("id"),
                    titulo = json.optString("titulo"),
                    cuerpo = json.optString("cuerpo"),
                    obligatorio = json.optBoolean("obligatorio", false),
                    persistencia = json.optString("persistencia", "ONCE"),
                    placement = json.optString("placement", "PAGE"),
                    target = json.optString("target", "ALL"),
                    imageUrl = json.optString("imageUrl").ifBlank { null },
                    linkUrl = json.optString("linkUrl").ifBlank { null },
                    linkLabel = json.optString("linkLabel").ifBlank { null },
                    visibleDesde = json.optString("visibleDesde").ifBlank { null },
                    visibleHasta = json.optString("visibleHasta").ifBlank { null },
                )
            )
        }
    }
}
