package com.redes.app.data.presence

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.session.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MobilePresenceManager(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val presenceRepository: PresenceRepository,
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var heartbeatJob: Job? = null

    override fun onStart(owner: LifecycleOwner) {
        startPresenceLoop()
    }

    override fun onStop(owner: LifecycleOwner) {
        stopPresenceLoop(markOffline = true)
    }

    fun onSignedOut() {
        stopPresenceLoop(markOffline = true)
    }

    private fun startPresenceLoop() {
        if (heartbeatJob?.isActive == true) return
        heartbeatJob = scope.launch {
            while (true) {
                syncOnline()
                delay(60_000)
            }
        }
    }

    private fun stopPresenceLoop(markOffline: Boolean) {
        heartbeatJob?.cancel()
        heartbeatJob = null
        if (markOffline) {
            scope.launch { syncOffline() }
        }
    }

    private suspend fun syncOnline() {
        val user = authRepository.currentUser.first() ?: return
        val cachedSession = sessionRepository.cachedSession.first()
        runCatching {
            presenceRepository.markOnline(
                PresencePayload(
                    uid = user.uid,
                    roles = cachedSession?.roles ?: emptyList(),
                    areas = cachedSession?.areas ?: emptyList(),
                )
            )
        }.onFailure { error ->
            Log.w("MobilePresence", "No se pudo marcar presencia online", error)
        }
    }

    private suspend fun syncOffline() {
        val user = authRepository.currentUser.first() ?: return
        runCatching {
            presenceRepository.markOffline(user.uid)
        }.onFailure { error ->
            Log.w("MobilePresence", "No se pudo marcar presencia offline", error)
        }
    }
}
