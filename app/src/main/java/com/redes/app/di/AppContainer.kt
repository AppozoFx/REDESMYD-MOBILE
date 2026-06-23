package com.redes.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.redes.app.BuildConfig
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.auth.FirebaseAuthRepository
import com.redes.app.data.local.SessionCacheDataSource
import com.redes.app.data.presence.BackendPresenceRepository
import com.redes.app.data.presence.MobilePresenceManager
import com.redes.app.data.presence.PresenceRepository
import com.redes.app.data.session.RemoteSessionRepository
import com.redes.app.data.session.SessionRepository
import com.redes.app.data.tecnico.RemoteTecnicoRepository
import com.redes.app.data.tecnico.TecnicoRepository
import com.redes.app.data.alertas.AlertaRepository
import com.redes.app.data.alertas.RemoteAlertaRepository
import com.redes.app.data.almacen.AlmacenRepository
import com.redes.app.data.almacen.RemoteAlmacenRepository
import com.redes.app.data.coordinador.CoordinadorRepository
import com.redes.app.data.coordinador.RemoteCoordinadorRepository
import com.redes.app.data.supervisor.SupervisorRepository
import com.redes.app.data.supervisor.RemoteSupervisorRepository
import com.redes.app.data.tracking.TrackingManager
import com.redes.app.data.tracking.TrackingRepository
import com.redes.app.network.AuthTokenInterceptor
import com.redes.app.network.FirebaseIdTokenProvider
import com.redes.app.network.RedesApiClient
import com.redes.app.network.TokenProvider
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface AppContainer {
    val authRepository: AuthRepository
    val tokenProvider: TokenProvider
    val sessionRepository: SessionRepository
    val presenceManager: MobilePresenceManager
    val tecnicoRepository: TecnicoRepository
    val trackingRepository: TrackingRepository
    val trackingManager: TrackingManager
    val alertaRepository: AlertaRepository
    val coordinadorRepository: CoordinadorRepository
    val supervisorRepository: SupervisorRepository
    val almacenRepository: AlmacenRepository
}

class DefaultAppContainer(
    private val context: Context,
) : AppContainer {
    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val sessionCacheDataSource: SessionCacheDataSource by lazy {
        SessionCacheDataSource(context)
    }

    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository(firebaseAuth)
    }

    override val tokenProvider: TokenProvider by lazy {
        FirebaseIdTokenProvider(firebaseAuth)
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthTokenInterceptor(tokenProvider))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    override val sessionRepository: SessionRepository by lazy {
        RemoteSessionRepository(
            apiClient = apiClient,
            tokenProvider = tokenProvider,
            cacheDataSource = sessionCacheDataSource,
        )
    }

    private val apiClient: RedesApiClient by lazy {
        RedesApiClient(
            baseUrl = BuildConfig.API_BASE_URL,
            httpClient = httpClient,
        )
    }

    private val presenceRepository: PresenceRepository by lazy {
        BackendPresenceRepository(
            apiClient = apiClient
        )
    }

    override val tecnicoRepository: TecnicoRepository by lazy {
        RemoteTecnicoRepository(
            context = context,
            apiClient = apiClient,
        )
    }

    override val presenceManager: MobilePresenceManager by lazy {
        MobilePresenceManager(
            authRepository = authRepository,
            sessionRepository = sessionRepository,
            presenceRepository = presenceRepository,
        )
    }

    override val trackingRepository: TrackingRepository by lazy {
        TrackingRepository(apiClient = apiClient)
    }

    override val trackingManager: TrackingManager by lazy {
        TrackingManager(context = context)
    }

    override val alertaRepository: AlertaRepository by lazy {
        RemoteAlertaRepository(
            context = context,
            apiClient = apiClient,
        )
    }

    override val coordinadorRepository: CoordinadorRepository by lazy {
        RemoteCoordinadorRepository(context = context, apiClient = apiClient)
    }

    override val supervisorRepository: SupervisorRepository by lazy {
        RemoteSupervisorRepository(apiClient = apiClient)
    }

    override val almacenRepository: AlmacenRepository by lazy {
        RemoteAlmacenRepository(apiClient = apiClient)
    }
}
