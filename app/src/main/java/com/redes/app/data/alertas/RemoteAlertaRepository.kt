package com.redes.app.data.alertas

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.redes.app.data.tecnico.NotifTecnicoItem
import com.redes.app.network.RedesApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteAlertaRepository(
    private val context: Context,
    private val apiClient: RedesApiClient,
) : AlertaRepository {

    override suspend fun postAlertaCerrarRuta(): Result<String> {
        return try {
            val alertaId = withContext(Dispatchers.IO) {
                apiClient.postAlertaApp(tipo = "CERRAR_RUTA")
            }
            Result.success(alertaId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun postRequiereAtencion(): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                apiClient.postAlertaApp(tipo = "REQUIERE_ATENCION")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun listenAlertaEstado(alertaId: String, onEstadoChange: (String) -> Unit): () -> Unit {
        val registration = FirebaseFirestore.getInstance()
            .collection("alertas_app")
            .document(alertaId)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) return@addSnapshotListener
                val estado = snap.getString("estado") ?: return@addSnapshotListener
                onEstadoChange(estado)
            }
        return { registration.remove() }
    }

    override fun markNotificacionesLeidas(cuadrillaId: String, ids: List<String>) {
        if (ids.isEmpty()) return
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        ids.forEach { id ->
            val ref = db.collection("notificaciones_tecnico")
                .document(cuadrillaId)
                .collection("items")
                .document(id)
            batch.update(ref, "leido", true)
        }
        batch.commit()
    }

    override fun listenNotificaciones(
        cuadrillaId: String,
        onUpdate: (List<NotifTecnicoItem>) -> Unit,
    ): () -> Unit {
        val registration = FirebaseFirestore.getInstance()
            .collection("notificaciones_tecnico")
            .document(cuadrillaId)
            .collection("items")
            .orderBy("creadoAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, error ->
                if (error != null || snap == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val items = snap.documents.mapNotNull { doc ->
                    val tipo = doc.getString("tipo") ?: return@mapNotNull null
                    val titulo = doc.getString("titulo") ?: return@mapNotNull null
                    val mensaje = doc.getString("mensaje") ?: ""
                    val leido = doc.getBoolean("leido") ?: false
                    val creadoAt = doc.getTimestamp("creadoAt")?.toDate()?.time
                    NotifTecnicoItem(
                        id = doc.id,
                        tipo = tipo,
                        titulo = titulo,
                        mensaje = mensaje,
                        leido = leido,
                        creadoAt = creadoAt,
                    )
                }
                onUpdate(items)
            }
        return { registration.remove() }
    }
}
