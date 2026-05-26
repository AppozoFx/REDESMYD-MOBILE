package com.redes.app.network.dto

import com.redes.app.data.session.MobileSession
import org.json.JSONArray
import org.json.JSONObject

data class MobileSessionDto(
    val uid: String,
    val nombre: String,
    val nombreCorto: String?,
    val email: String?,
    val roles: List<String>,
    val areas: List<String>,
    val permissions: List<String>,
    val estadoAcceso: String,
    val isAdmin: Boolean,
) {
    fun toDomain(): MobileSession {
        return MobileSession(
            uid = uid,
            nombre = nombre,
            nombreCorto = nombreCorto,
            email = email,
            roles = roles,
            areas = areas,
            permissions = permissions,
            estadoAcceso = estadoAcceso,
            isAdmin = isAdmin,
        )
    }

    companion object {
        fun fromJson(json: JSONObject): MobileSessionDto {
            return MobileSessionDto(
                uid = json.optString("uid"),
                nombre = json.optString("nombre", json.optString("uid")),
                nombreCorto = json.optString("nombreCorto").ifBlank { null },
                email = json.optString("email").ifBlank { null },
                roles = json.optJSONArray("roles").toStringList(),
                areas = json.optJSONArray("areas").toStringList(),
                permissions = json.optJSONArray("permissions").toStringList(),
                estadoAcceso = json.optString("estadoAcceso", "INHABILITADO"),
                isAdmin = json.optBoolean("isAdmin", false),
            )
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList(length()) {
        for (index in 0 until length()) {
            val value = optString(index)
            if (value.isNotBlank()) add(value)
        }
    }
}
