package com.redes.app.ui.supervisor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.session.SessionRepository
import com.redes.app.data.supervisor.JornadaEstado
import com.redes.app.data.supervisor.SupervisorMapMode
import com.redes.app.data.supervisor.SupervisorRepository
import com.redes.app.data.tracking.TrackingManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class SupervisorViewModel(
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    private val supervisorRepository: SupervisorRepository,
    private val trackingManager: TrackingManager,
    private val prefs: android.content.SharedPreferences,
    private val appContext: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupervisorUiState())
    val uiState: StateFlow<SupervisorUiState> = _uiState.asStateFlow()

    private var prevGarantias: Map<String, String> = emptyMap() // id → estado
    private var lastTramoAlertKey: String? = null

    init {
        SupervisorNotificationHelper.createChannel(appContext)
        viewModelScope.launch {
            combine(authRepository.currentUser, sessionRepository.selectedRole) { user, role ->
                user to role
            }.collect { (user, role) ->
                val enabled = user != null && role == "SUPERVISOR"
                _uiState.update { if (!enabled) SupervisorUiState() else it.copy(isEnabled = true) }
                if (enabled) {
                    refreshAll()
                    loadJornada()
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000L)
                checkTramoAlertas()
            }
        }
    }

    // ─── Alertas ──────────────────────────────────────────────────────────────

    private fun addAlerta(tipo: String, titulo: String, mensaje: String) {
        val item = com.redes.app.data.supervisor.SupervisorAlertItem(
            id = "${tipo}_${System.currentTimeMillis()}",
            tipo = tipo,
            titulo = titulo,
            mensaje = mensaje,
            leido = false,
            creadoAt = System.currentTimeMillis(),
        )
        _uiState.update { state ->
            val updated = (listOf(item) + state.alertas).take(30)
            state.copy(alertas = updated, alertCount = updated.count { !it.leido })
        }
        SupervisorNotificationHelper.postAlerta(appContext, titulo, mensaje)
    }

    private fun checkGarantiasAlertas(orders: List<com.redes.app.data.supervisor.SupervisorOrderSummary>) {
        val garantias = orders.filter { it.isGarantia }
        garantias.filter { it.id !in prevGarantias }.forEach { g ->
            addAlerta("GARANTIA_NUEVA", "Nueva garantía asignada", "${g.cliente.take(40)} – ${g.cuadrillaNombre}")
        }
        garantias.filter { it.id in prevGarantias && prevGarantias[it.id] != it.estado }.forEach { g ->
            addAlerta("GARANTIA_ESTADO", "Garantía: ${g.estado}", "${g.cliente.take(40)} → ${g.estado}")
        }
        prevGarantias = garantias.associate { it.id to it.estado }
    }

    private fun checkTramoAlertas() {
        val orders = _uiState.value.orders
        val garantias = orders.filter { it.isGarantia }
        if (garantias.isEmpty()) return
        val tramoAlerts = mapOf("07:45" to "08:00", "11:45" to "12:00", "15:45" to "16:00")
        val nowHm = try {
            val now = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Lima"))
            "%02d:%02d".format(now.hour, now.minute)
        } catch (_: Exception) { return }
        val tramo = tramoAlerts[nowHm] ?: return
        val garantiasEnTramo = garantias.filter { it.fechaProgramadaHm.take(5) == tramo }
        if (garantiasEnTramo.isEmpty()) return
        val key = "${todayLimaYmd()}_$tramo"
        if (lastTramoAlertKey == key) return
        lastTramoAlertKey = key
        addAlerta("GARANTIA_TRAMO", "⚠️ Garantía en tramo $tramo",
            "Tienes ${garantiasEnTramo.size} garantía(s) asignada(s) para el tramo $tramo")
    }

    fun showAlertas() {
        _uiState.update { state ->
            val read = state.alertas.map { it.copy(leido = true) }
            state.copy(showAlertas = true, alertas = read, alertCount = 0)
        }
    }

    fun hideAlertas() {
        _uiState.update { it.copy(showAlertas = false) }
    }

    // ─── Jornada ──────────────────────────────────────────────────────────────

    fun loadJornada() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isJornadaLoading = true) }
            val result = supervisorRepository.fetchJornada(todayLimaYmd())
            result.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        isJornadaLoading = false,
                        jornada = data.jornada,
                        oficina = data.oficina,
                        estadoRuta = if (data.jornada.estado == JornadaEstado.FINALIZADA) "RUTA_CERRADA" else if (data.jornada.estado != JornadaEstado.SIN_INICIAR) "EN_CAMPO" else null,
                    )
                }
                // Si ya estaba en ruta, asegurar tracking activo
                if (data.jornada.estado == JornadaEstado.EN_RUTA || data.jornada.estado == JornadaEstado.EN_REFRIGERIO) {
                    trackingManager.startIfNeeded()
                }
            }.onFailure {
                _uiState.update { it.copy(isJornadaLoading = false) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun iniciarRuta() {
        if (!_uiState.value.isEnabled) return
        _uiState.update { it.copy(isGettingLocation = true, jornadaError = null) }
        viewModelScope.launch {
            val location = getCurrentLocation()
            if (location == null) {
                _uiState.update { it.copy(isGettingLocation = false, jornadaError = "No se pudo obtener tu ubicación. Activa el GPS.") }
                return@launch
            }
            _uiState.update { it.copy(isGettingLocation = false, isJornadaLoading = true) }
            val result = supervisorRepository.postJornadaEvento("INICIO_RUTA", location.first, location.second)
            result.onSuccess { jornada ->
                prefs.edit().putString(KEY_INICIO_YMD, todayLimaYmd()).apply()
                trackingManager.startIfNeeded()
                _uiState.update {
                    it.copy(
                        isJornadaLoading = false,
                        jornada = jornada,
                        estadoRuta = "EN_CAMPO",
                        jornadaError = null,
                    )
                }
            }.onFailure { e ->
                val msg = e.message ?: "Error al iniciar ruta"
                val userMsg = when {
                    msg.contains("FUERA_DE_RADIO") -> {
                        val dist = Regex("distanciaMetros=(\\d+)").find(msg)?.groupValues?.get(1)
                        if (dist != null) "Debes estar en la oficina para iniciar ruta. Estás a ${dist}m de distancia." else "No estás en el punto de inicio."
                    }
                    msg.contains("UBICACION_REQUERIDA") -> "Se requiere tu ubicación para iniciar ruta."
                    else -> msg
                }
                _uiState.update { it.copy(isJornadaLoading = false, jornadaError = userMsg) }
            }
        }
    }

    fun showCerrarRutaConfirm() {
        _uiState.update { it.copy(showCerrarRutaConfirm = true) }
    }

    fun dismissCerrarRutaConfirm() {
        _uiState.update { it.copy(showCerrarRutaConfirm = false) }
    }

    fun confirmarCerrarRuta() {
        if (!_uiState.value.isEnabled) return
        trackingManager.stop()
        _uiState.update { it.copy(showCerrarRutaConfirm = false, isGettingLocation = true) }
        viewModelScope.launch {
            val location = getCurrentLocation()
            _uiState.update { it.copy(isGettingLocation = false, isJornadaLoading = true) }
            supervisorRepository.postJornadaEvento("FIN_RUTA", location?.first, location?.second)
                .onSuccess { jornada ->
                    _uiState.update {
                        it.copy(
                            isJornadaLoading = false,
                            jornada = jornada,
                            estadoRuta = "RUTA_CERRADA",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isJornadaLoading = false, jornadaError = e.localizedMessage) }
                }
        }
    }

    fun iniciarRefrigerio() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isJornadaLoading = true) }
            supervisorRepository.postJornadaEvento("INICIO_REFRIGERIO", null, null)
                .onSuccess { jornada -> _uiState.update { it.copy(isJornadaLoading = false, jornada = jornada) } }
                .onFailure { e -> _uiState.update { it.copy(isJornadaLoading = false, jornadaError = e.localizedMessage) } }
        }
    }

    fun finRefrigerio() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isJornadaLoading = true) }
            supervisorRepository.postJornadaEvento("FIN_REFRIGERIO", null, null)
                .onSuccess { jornada -> _uiState.update { it.copy(isJornadaLoading = false, jornada = jornada) } }
                .onFailure { e -> _uiState.update { it.copy(isJornadaLoading = false, jornadaError = e.localizedMessage) } }
        }
    }

    fun dismissJornadaError() {
        _uiState.update { it.copy(jornadaError = null) }
    }

    fun showCuadrillasModal() { _uiState.update { it.copy(showCuadrillasModal = true) } }
    fun hideCuadrillasModal() { _uiState.update { it.copy(showCuadrillasModal = false) } }

    // ─── Tabs y datos ─────────────────────────────────────────────────────────

    fun selectTab(tab: SupervisorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        when (tab) {
            SupervisorTab.MAPA -> refreshMapa()
            SupervisorTab.ORDENES -> if (_uiState.value.orders.isEmpty()) refreshOrders()
            else -> Unit
        }
    }

    fun toggleGarantias() {
        val newVal = !_uiState.value.showGarantias
        _uiState.update { it.copy(showGarantias = newVal, orders = emptyList()) }
        refreshOrders()
    }

    fun setMapMode(mode: SupervisorMapMode) {
        _uiState.update { it.copy(mapMode = mode, mapItems = emptyList(), allMapItems = emptyList(), cuadrillasMapa = emptyList()) }
        if (mode == SupervisorMapMode.CUADRILLAS) {
            refreshCuadrillasMapa()
            refreshAllMapItems()
        } else {
            refreshMapa()
        }
    }

    fun selectCuadrilla(id: String?) { _uiState.update { it.copy(selectedCuadrillaId = id) } }

    fun refreshCuadrillasMapa() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCuadrillasMapaLoading = true) }
            val result = supervisorRepository.fetchCuadrillasMapa()
            _uiState.update {
                it.copy(isCuadrillasMapaLoading = false, cuadrillasMapa = result.getOrNull() ?: it.cuadrillasMapa)
            }
        }
    }

    private fun refreshAllMapItems() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            val result = supervisorRepository.fetchMapa(_uiState.value.selectedYmd, SupervisorMapMode.CUADRILLAS)
            _uiState.update { it.copy(allMapItems = result.getOrNull() ?: it.allMapItems) }
        }
    }

    fun refreshAll() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, isInitialLoading = true, isHomeLoading = true, isOrdersLoading = true, isMapLoading = true, errorMessage = null) }
            val ymd = _uiState.value.selectedYmd
            val soloGarantias = _uiState.value.showGarantias

            val homeDeferred = async { supervisorRepository.fetchHome() }
            val ordersDeferred = async { supervisorRepository.fetchOrders(ymd, soloGarantias) }
            val mapaDeferred = async { supervisorRepository.fetchMapa(ymd, _uiState.value.mapMode) }

            val home = homeDeferred.await()
            val orders = ordersDeferred.await()
            val mapa = mapaDeferred.await()

            val error = listOf(home, orders).firstNotNullOfOrNull { it.exceptionOrNull()?.localizedMessage }
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    isInitialLoading = false, isHomeLoading = false, isOrdersLoading = false, isMapLoading = false,
                    home = home.getOrNull() ?: it.home,
                    ordersData = orders.getOrNull() ?: it.ordersData,
                    orders = orders.getOrNull()?.items ?: it.orders,
                    mapItems = mapa.getOrNull() ?: it.mapItems,
                    errorMessage = error,
                )
            }
            if (_uiState.value.mapMode == SupervisorMapMode.CUADRILLAS) {
                refreshAllMapItems()
                refreshCuadrillasMapa()
            }
        }
    }

    fun refreshOrders() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdersLoading = true, errorMessage = null) }
            val result = supervisorRepository.fetchOrders(_uiState.value.selectedYmd, _uiState.value.showGarantias)
            _uiState.update {
                it.copy(
                    isOrdersLoading = false,
                    ordersData = result.getOrNull() ?: it.ordersData,
                    orders = result.getOrNull()?.items ?: it.orders,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
            result.getOrNull()?.items?.let { checkGarantiasAlertas(it) }
            checkTramoAlertas()
        }
    }

    private fun refreshMapa() {
        if (!_uiState.value.isEnabled) return
        val mode = _uiState.value.mapMode
        if (mode == SupervisorMapMode.CUADRILLAS) { refreshCuadrillasMapa(); return }
        viewModelScope.launch {
            _uiState.update { it.copy(isMapLoading = true, errorMessage = null) }
            val result = supervisorRepository.fetchMapa(_uiState.value.selectedYmd, mode)
            _uiState.update {
                it.copy(isMapLoading = false, mapItems = result.getOrNull() ?: it.mapItems, errorMessage = result.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun previousDay() {
        val newYmd = java.time.LocalDate.parse(_uiState.value.selectedYmd).minusDays(1).toString()
        _uiState.update { it.copy(selectedYmd = newYmd, orders = emptyList()) }
        viewModelScope.launch { refreshOrders() }
    }

    fun nextDay() {
        val newYmd = java.time.LocalDate.parse(_uiState.value.selectedYmd).plusDays(1).toString()
        _uiState.update { it.copy(selectedYmd = newYmd, orders = emptyList()) }
        viewModelScope.launch { refreshOrders() }
    }

    fun loadOrderDetail(orderId: String) {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDetailLoading = true, selectedOrderDetail = null, errorMessage = null) }
            val result = supervisorRepository.fetchOrderDetail(orderId)
            _uiState.update {
                it.copy(isDetailLoading = false, selectedOrderDetail = result.getOrNull(), errorMessage = result.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun clearOrderDetail() {
        _uiState.update { it.copy(selectedOrderDetail = null, isDetailLoading = false, supervisionSavedOrderId = null) }
    }

    fun saveSupervision(orderId: String, notas: String, observaciones: String) {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSupervision = true, errorMessage = null) }
            supervisorRepository.saveSupervision(orderId, notas, observaciones)
                .onSuccess {
                    _uiState.update { it.copy(isSavingSupervision = false, supervisionSavedOrderId = orderId) }
                    loadOrderDetail(orderId)
                }
                .onFailure { e -> _uiState.update { it.copy(isSavingSupervision = false, errorMessage = e.localizedMessage) } }
        }
    }

    fun updateGarantia(
        ordenId: String,
        motivoGarantia: String,
        diagnosticoGarantia: String,
        solucionGarantia: String,
        responsableGarantia: String,
        casoGarantia: String,
        imputadoGarantia: String,
    ) {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingGarantia = true, garantiaSaved = false, errorMessage = null) }
            supervisorRepository.updateGarantia(
                ordenId, motivoGarantia, diagnosticoGarantia, solucionGarantia, responsableGarantia, casoGarantia, imputadoGarantia
            ).onSuccess {
                _uiState.update { it.copy(isSavingGarantia = false, garantiaSaved = true) }
                loadOrderDetail(ordenId)
            }.onFailure { e ->
                _uiState.update { it.copy(isSavingGarantia = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    fun clearGarantiaSaved() {
        _uiState.update { it.copy(garantiaSaved = false) }
    }

    fun dismissError() { _uiState.update { it.copy(errorMessage = null, jornadaError = null) } }

    // ─── GPS ──────────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    private suspend fun getCurrentLocation(): Pair<Double, Double>? =
        suspendCancellableCoroutine { cont ->
            try {
                LocationServices.getFusedLocationProviderClient(appContext).lastLocation
                    .addOnSuccessListener { loc -> cont.resume(if (loc != null) loc.latitude to loc.longitude else null) }
                    .addOnFailureListener { cont.resume(null) }
            } catch (e: Exception) {
                Log.w(TAG, "getCurrentLocation failed", e)
                cont.resume(null)
            }
        }
}

class SupervisorViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val supervisorRepository: SupervisorRepository,
    private val trackingManager: TrackingManager,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupervisorViewModel::class.java)) {
            val prefs = context.getSharedPreferences("redes_supervisor_jornada", Context.MODE_PRIVATE)
            return SupervisorViewModel(
                authRepository, sessionRepository, supervisorRepository,
                trackingManager, prefs, context.applicationContext,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private const val KEY_INICIO_YMD = "sup_inicio_ymd"
private const val TAG = "SupervisorViewModel"
