package com.redes.app.data.presence

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.redes.app.data.common.awaitResult

class FirestorePresenceRepository(
    private val firestore: FirebaseFirestore,
) : PresenceRepository {

    override suspend fun markOnline(payload: PresencePayload) {
        firestore.collection("usuarios_presencia")
            .document(payload.uid)
            .set(
                mapOf(
                    "uid" to payload.uid,
                    "online" to true,
                    "source" to "MOBILE",
                    "roles" to payload.roles,
                    "areas" to payload.areas,
                    "lastSeenAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
                com.google.firebase.firestore.SetOptions.merge(),
            )
            .awaitResult()
    }

    override suspend fun markOffline(uid: String) {
        firestore.collection("usuarios_presencia")
            .document(uid)
            .set(
                mapOf(
                    "uid" to uid,
                    "online" to false,
                    "source" to "MOBILE",
                    "lastSeenAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                    "clientClosedAt" to Timestamp.now(),
                ),
                com.google.firebase.firestore.SetOptions.merge(),
            )
            .awaitResult()
    }
}
