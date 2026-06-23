package com.redes.app.ui.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.redes.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ForceUpdateViewModel : ViewModel() {

    private val _state = MutableStateFlow<ForceUpdateState>(ForceUpdateState.Checking)
    val state: StateFlow<ForceUpdateState> = _state.asStateFlow()

    init {
        checkVersion()
    }

    private fun checkVersion() {
        viewModelScope.launch {
            try {
                val snapshot = fetchAppConfig()
                val versionMinima = snapshot.getLong("versionMinima") ?: 0L
                if (BuildConfig.VERSION_CODE < versionMinima) {
                    val nominal = snapshot.getString("versionNominalMinima") ?: ""
                    val mensaje = snapshot.getString("mensaje")
                        ?: "Esta versión ya no es compatible. Actualiza la app para continuar."
                    _state.value = ForceUpdateState.UpdateRequired(nominal, mensaje)
                } else {
                    _state.value = ForceUpdateState.UpToDate
                }
            } catch (_: Exception) {
                // Ante error de red no bloqueamos — el usuario podrá entrar
                _state.value = ForceUpdateState.UpToDate
            }
        }
    }

    private suspend fun fetchAppConfig(): DocumentSnapshot =
        suspendCancellableCoroutine { cont ->
            FirebaseFirestore.getInstance()
                .collection("app_config")
                .document("android")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (cont.isActive) cont.resume(snapshot)
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }
}
