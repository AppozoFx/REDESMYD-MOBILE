package com.redes.app.ui.tecnico

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redes.app.data.alertas.AlertaRepository
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.session.SessionRepository
import com.redes.app.data.tecnico.MapMode
import com.redes.app.data.tecnico.TecnicoRepository
import com.redes.app.data.tracking.TrackingManager
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TecnicoViewModel(
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    private val tecnicoRepository: TecnicoRepository,
    private val trackingManager: TrackingManager,
    private val alertaRepository: AlertaRepository,
    private val alertaPrefs: android.content.SharedPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TecnicoUiState())
    val uiState: StateFlow<TecnicoUiState> = _uiState.asStateFlow()

    private var alertaListenerCleanup: (() -> Unit)? = null
    private var notifListenerCleanup: (() -> Unit)? = null
    private var activeCuadrillaId: String? = null

    init {
        viewModelScope.launch {
            combine(authRepository.currentUser, sessionRepository.selectedRole) { user, role ->
                user to role
            }.collect { (user, role) ->
                val enabled = user != null && role == "TECNICO"
                _uiState.update {
                    if (!enabled) {
                        TecnicoUiState()
                    } else {
                        it.copy(isEnabled = true)
                    }
                }
                if (enabled) {
                    refreshAll()
                    iniciarJornada()
                    restoreAlertaListener()
                }
            }
        }
    }

    private fun iniciarJornada() {
        if (!_uiState.value.isEnabled) return
        val today = todayLimaYmd()
        if (alertaPrefs.getString(KEY_INICIO_YMD, null) == today) return
        viewModelScope.launch {
            val result = tecnicoRepository.iniciarJornada()
            result.onSuccess { estadoRuta ->
                alertaPrefs.edit().putString(KEY_INICIO_YMD, today).apply()
                _uiState.update { it.copy(estadoRuta = estadoRuta) }
            }
        }
    }

    fun cerrarRuta() {
        if (!_uiState.value.isEnabled) return
        trackingManager.stopAndCloseRoute()
        _uiState.update { it.copy(isAlertaLoading = true, alertaEstado = "PENDIENTE") }
        viewModelScope.launch {
            val result = alertaRepository.postAlertaCerrarRuta()
            result.onSuccess { alertaId ->
                alertaPrefs.edit()
                    .putString(KEY_ALERTA_ID, alertaId)
                    .putString(KEY_ALERTA_YMD, todayLimaYmd())
                    .apply()
                _uiState.update { it.copy(isAlertaLoading = false, alertaPendienteId = alertaId) }
                startAlertaListener(alertaId)
            }.onFailure { e ->
                Log.e(TAG, "Error enviando alerta cerrar ruta", e)
                _uiState.update {
                    it.copy(
                        isAlertaLoading = false,
                        alertaEstado = null,
                        errorMessage = e.localizedMessage,
                    )
                }
            }
        }
    }

    private fun restoreAlertaListener() {
        val savedId = alertaPrefs.getString(KEY_ALERTA_ID, null) ?: return
        val savedYmd = alertaPrefs.getString(KEY_ALERTA_YMD, null)
        // Limpiar alerta de días anteriores al comenzar un nuevo día
        if (savedYmd != todayLimaYmd()) {
            alertaPrefs.edit().remove(KEY_ALERTA_ID).remove(KEY_ALERTA_YMD).apply()
            return
        }
        val currentEstado = _uiState.value.alertaEstado
        if (currentEstado == "ACEPTADA" || currentEstado == "RECHAZADA") return
        _uiState.update { it.copy(alertaPendienteId = savedId, alertaEstado = "PENDIENTE") }
        startAlertaListener(savedId)
    }

    private fun startAlertaListener(alertaId: String) {
        alertaListenerCleanup?.invoke()
        alertaListenerCleanup = alertaRepository.listenAlertaEstado(alertaId) { estado ->
            _uiState.update { it.copy(alertaEstado = estado) }
            if (estado == "ACEPTADA" || estado == "RECHAZADA") {
                alertaPrefs.edit().remove(KEY_ALERTA_ID).apply()
                alertaListenerCleanup?.invoke()
                alertaListenerCleanup = null
            }
        }
    }

    fun requerirAtencion() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            alertaRepository.postRequiereAtencion()
        }
    }

    fun onNotificationsOpened() {
        // Actualización optimista inmediata — el badge desaparece al instante
        val unreadIds = _uiState.value.notifItems.filter { !it.leido }.map { it.id }
        _uiState.update { it.copy(notifCount = 0) }
        // Persistir leido=true en Firestore para que el badge no reaparezca al relanzar la app
        val cuadrillaId = activeCuadrillaId ?: return
        if (unreadIds.isNotEmpty()) {
            alertaRepository.markNotificacionesLeidas(cuadrillaId, unreadIds)
        }
    }

    private fun startNotifListener(cuadrillaId: String) {
        activeCuadrillaId = cuadrillaId
        notifListenerCleanup?.invoke()
        notifListenerCleanup = alertaRepository.listenNotificaciones(cuadrillaId) { items ->
            val unread = items.count { !it.leido }
            _uiState.update { it.copy(notifItems = items, notifCount = unread) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        alertaListenerCleanup?.invoke()
        notifListenerCleanup?.invoke()
    }

    fun selectTab(tab: TecnicoTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == TecnicoTab.MAPA) refreshTodayMap()
    }

    fun toggleMapMode() {
        val newMode = if (_uiState.value.mapMode == MapMode.MIS_ORDENES) MapMode.CUADRILLAS else MapMode.MIS_ORDENES
        _uiState.update { it.copy(mapMode = newMode) }
        if (newMode == MapMode.CUADRILLAS) refreshCuadrillasMapa()
    }

    fun refreshCuadrillasMapa() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCuadrillasMapaLoading = true) }
            val result = tecnicoRepository.fetchCuadrillasMapa()
            _uiState.update {
                it.copy(
                    isCuadrillasMapaLoading = false,
                    cuadrillasMapa = result.getOrNull() ?: it.cuadrillasMapa,
                )
            }
        }
    }

    private fun refreshTodayMap() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMapLoading = true, errorMessage = null) }
            val result = tecnicoRepository.fetchMap(todayLimaYmd())
            _uiState.update {
                it.copy(
                    isMapLoading = false,
                    map = result.getOrNull() ?: it.map,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    fun previousDay() {
        updateYmd(changeDay(_uiState.value.selectedYmd, -1))
    }

    fun nextDay() {
        updateYmd(changeDay(_uiState.value.selectedYmd, 1))
    }

    fun selectDate(ymd: String) {
        updateYmd(ymd)
    }

    private fun updateYmd(ymd: String) {
        _uiState.update { it.copy(selectedYmd = ymd) }
        viewModelScope.launch {
            refreshOrders()
            refreshMap()
        }
    }

    fun refreshAll() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRefreshing = true,
                    isInitialLoading = true,
                    isHomeLoading = true,
                    isOrdersLoading = true,
                    isStockLoading = true,
                    isMapLoading = true,
                    errorMessage = null,
                )
            }
            val ymd = _uiState.value.selectedYmd
            val homeDeferred = async { tecnicoRepository.fetchHome() }
            val ordersDeferred = async { tecnicoRepository.fetchOrders(ymd) }
            val stockDeferred = async { tecnicoRepository.fetchStock() }
            val mapDeferred = async { tecnicoRepository.fetchMap(ymd) }

            val home = homeDeferred.await()
            val orders = ordersDeferred.await()
            val stock = stockDeferred.await()
            val map = mapDeferred.await()

            val error = listOf(home, orders, stock, map).firstNotNullOfOrNull { it.exceptionOrNull()?.localizedMessage }

            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    isInitialLoading = false,
                    isHomeLoading = false,
                    isOrdersLoading = false,
                    isStockLoading = false,
                    isMapLoading = false,
                    home = home.getOrNull() ?: it.home,
                    ordersData = orders.getOrNull() ?: it.ordersData,
                    orders = orders.getOrNull()?.items ?: it.orders,
                    stock = stock.getOrNull() ?: it.stock,
                    map = map.getOrNull() ?: it.map,
                    errorMessage = error,
                )
            }

            // Iniciar listener de notificaciones del técnico una vez que tenemos el cuadrillaId
            home.getOrNull()?.cuadrilla?.id?.takeIf { it.isNotBlank() }?.let { cuadrillaId ->
                startNotifListener(cuadrillaId)
            }
        }
    }

    fun refreshOrders() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdersLoading = true, errorMessage = null) }
            val result = tecnicoRepository.fetchOrders(_uiState.value.selectedYmd)
            _uiState.update {
                it.copy(
                    isOrdersLoading = false,
                    ordersData = result.getOrNull() ?: it.ordersData,
                    orders = result.getOrNull()?.items ?: it.orders,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    fun refreshMap() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMapLoading = true, errorMessage = null) }
            val result = tecnicoRepository.fetchMap(_uiState.value.selectedYmd)
            _uiState.update {
                it.copy(
                    isMapLoading = false,
                    map = result.getOrNull() ?: it.map,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    fun loadOrderDetail(orderId: String) {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isDetailLoading = true, errorMessage = null) }
            val result = tecnicoRepository.fetchOrderDetail(orderId)
            _uiState.update {
                it.copy(
                    isDetailLoading = false,
                    selectedOrderDetail = result.getOrNull(),
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    fun sustainStockEquipment(cuadrillaId: String, sn: String, photoUri: Uri) {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isStockLoading = true, errorMessage = null) }
            val result = tecnicoRepository.sustainStockEquipment(cuadrillaId, sn, photoUri)
            _uiState.update { current ->
                val currentStock = current.stock
                val updatedItem = result.getOrNull()
                val updatedStock = currentStock?.copy(
                    equipos = currentStock.equipos.map { item ->
                        if (item.sn.equals(sn.trim(), ignoreCase = true)) updatedItem ?: item else item
                    }
                )
                current.copy(
                    isStockLoading = false,
                    stock = updatedStock ?: current.stock,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    fun clearOrderDetail() {
        _uiState.update { it.copy(selectedOrderDetail = null, isDetailLoading = false) }
    }
}

class TecnicoViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val tecnicoRepository: TecnicoRepository,
    private val trackingManager: TrackingManager,
    private val alertaRepository: AlertaRepository,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TecnicoViewModel::class.java)) {
            val prefs = context.getSharedPreferences("redes_alertas", Context.MODE_PRIVATE)
            return TecnicoViewModel(
                authRepository, sessionRepository, tecnicoRepository,
                trackingManager, alertaRepository, prefs,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun changeDay(ymd: String, delta: Long): String {
    val date = java.time.LocalDate.parse(ymd)
    return date.plusDays(delta).toString()
}

private const val KEY_ALERTA_ID = "active_alerta_id"
private const val KEY_ALERTA_YMD = "active_alerta_ymd"
private const val KEY_INICIO_YMD = "inicio_jornada_ymd"
private const val TAG = "TecnicoViewModel"
