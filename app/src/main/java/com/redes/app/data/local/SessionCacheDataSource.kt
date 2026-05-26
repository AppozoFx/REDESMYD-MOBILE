package com.redes.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.redes.app.data.session.MobileSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "redes_session")

class SessionCacheDataSource(
    context: Context,
) {
    private val dataStore = context.sessionDataStore

    val cachedSession: Flow<MobileSession?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[SESSION_JSON]?.let(::fromJson)
        }

    val selectedRole: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[SELECTED_ROLE]?.ifBlank { null }
        }

    suspend fun saveSession(session: MobileSession) {
        dataStore.edit { preferences ->
            preferences[SESSION_JSON] = toJson(session)
        }
    }

    suspend fun saveSelectedRole(role: String?) {
        dataStore.edit { preferences ->
            if (role.isNullOrBlank()) {
                preferences.remove(SELECTED_ROLE)
            } else {
                preferences[SELECTED_ROLE] = role.trim().uppercase()
            }
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(SESSION_JSON)
            preferences.remove(SELECTED_ROLE)
        }
    }

    private fun toJson(session: MobileSession): String {
        return JSONObject()
            .put("uid", session.uid)
            .put("nombre", session.nombre)
            .put("nombreCorto", session.nombreCorto)
            .put("email", session.email)
            .put("roles", JSONArray(session.roles))
            .put("areas", JSONArray(session.areas))
            .put("permissions", JSONArray(session.permissions))
            .put("estadoAcceso", session.estadoAcceso)
            .put("isAdmin", session.isAdmin)
            .toString()
    }

    private fun fromJson(value: String): MobileSession? {
        return runCatching {
            val json = JSONObject(value)
            MobileSession(
                uid = json.optString("uid"),
                nombre = json.optString("nombre"),
                nombreCorto = json.optString("nombreCorto").ifBlank { null },
                email = json.optString("email").ifBlank { null },
                roles = json.optJSONArray("roles").toStringList(),
                areas = json.optJSONArray("areas").toStringList(),
                permissions = json.optJSONArray("permissions").toStringList(),
                estadoAcceso = json.optString("estadoAcceso", "INHABILITADO"),
                isAdmin = json.optBoolean("isAdmin", false),
            )
        }.getOrNull()
    }

    private companion object {
        val SESSION_JSON = stringPreferencesKey("session_json")
        val SELECTED_ROLE = stringPreferencesKey("selected_role")
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
