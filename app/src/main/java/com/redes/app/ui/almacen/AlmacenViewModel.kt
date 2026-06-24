package com.redes.app.ui.almacen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redes.app.data.almacen.AlmacenLiquidarRequest
import com.redes.app.data.almacen.AlmacenRepository
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.session.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "AlmacenViewModel"

class AlmacenViewModel(
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    private val almacenRepository: AlmacenRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlmacenUiState())
    val uiState: StateFlow<AlmacenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.currentUser, sessionRepository.selectedRole) { user, role ->
                user to role
            }.collect { (user, role) ->
                val enabled = user != null && role == "ALMACEN"
                _uiState.update { if (!enabled) AlmacenUiState() else it.copy(isEnabled = true) }
                if (enabled && _uiState.value.stock.isEmpty()) refreshStock()
            }
        }
    }

    fun refreshAll() {
        if (!_uiState.value.isEnabled) return
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        when (_uiState.value.selectedTab) {
            AlmacenTab.STOCK -> refreshStock()
            AlmacenTab.LIQUIDACION -> refreshLiquidacion()
            AlmacenTab.INSTALACIONES -> refreshInstalaciones()
            AlmacenTab.MAPA -> { refreshCuadrillasMapa(); refreshMapaItems() }
        }
        viewModelScope.launch { _uiState.update { it.copy(isRefreshing = false) } }
    }

    fun selectTab(tab: AlmacenTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
        when (tab) {
            AlmacenTab.STOCK -> if (_uiState.value.stock.isEmpty()) refreshStock()
            AlmacenTab.LIQUIDACION -> if (_uiState.value.liquidacion == null) refreshLiquidacion()
            AlmacenTab.INSTALACIONES -> if (_uiState.value.instalaciones.isEmpty()) refreshInstalaciones()
            AlmacenTab.MAPA -> {
                if (_uiState.value.cuadrillasMapa.isEmpty()) refreshCuadrillasMapa()
                if (_uiState.value.mapaItems.isEmpty()) refreshMapaItems()
            }
        }
    }

    fun previousMonth() {
        val ym = changeMonth(_uiState.value.selectedYm, -1)
        _uiState.update {
            it.copy(
                selectedYm = ym,
                liquidacion = null,
                instalaciones = emptyList(),
                expandedLiquidacionId = null,
                liquidacionForms = emptyMap(),
                preliquidaciones = emptyMap(),
            )
        }
        when (_uiState.value.selectedTab) {
            AlmacenTab.LIQUIDACION -> refreshLiquidacion()
            AlmacenTab.INSTALACIONES -> refreshInstalaciones()
            else -> {}
        }
    }

    fun nextMonth() {
        val ym = changeMonth(_uiState.value.selectedYm, 1)
        _uiState.update {
            it.copy(
                selectedYm = ym,
                liquidacion = null,
                instalaciones = emptyList(),
                expandedLiquidacionId = null,
                liquidacionForms = emptyMap(),
                preliquidaciones = emptyMap(),
            )
        }
        when (_uiState.value.selectedTab) {
            AlmacenTab.LIQUIDACION -> refreshLiquidacion()
            AlmacenTab.INSTALACIONES -> refreshInstalaciones()
            else -> {}
        }
    }

    fun toggleStockExpanded(cuadrillaId: String) {
        _uiState.update { it.copy(expandedStockCuadrillaId = if (it.expandedStockCuadrillaId == cuadrillaId) null else cuadrillaId) }
    }

    fun toggleInstalacionExpanded(id: String) {
        _uiState.update { it.copy(expandedInstalacionId = if (it.expandedInstalacionId == id) null else id) }
    }

    fun toggleLiquidacionExpanded(id: String) {
        val isExpanding = _uiState.value.expandedLiquidacionId != id
        _uiState.update {
            it.copy(
                expandedLiquidacionId = if (isExpanding) id else null,
                errorMessage = null,
            )
        }
        if (isExpanding && !_uiState.value.preliquidaciones.containsKey(id)) {
            fetchPreliquidacion(id)
        }
    }

    fun fetchPreliquidacion(ordenId: String) {
        if (!_uiState.value.isEnabled) return
        _uiState.update { it.copy(isPreliqLoadingId = ordenId) }
        viewModelScope.launch {
            val result = almacenRepository.fetchPreliquidacion(ordenId)
            result.getOrNull()?.let { preliq ->
                if (preliq.found) {
                    _uiState.update { state ->
                        val existingForm = state.liquidacionForms[ordenId] ?: LiquidacionForm()
                        val updated = existingForm.copy(
                            snOnt = existingForm.snOnt.ifBlank { preliq.snOnt },
                            snMeshes = existingForm.snMeshes.ifEmpty { preliq.snMeshes },
                            snBoxes = existingForm.snBoxes.ifEmpty { preliq.snBoxes },
                            snFono = existingForm.snFono.ifBlank { preliq.snFono },
                            rotuloNapCto = existingForm.rotuloNapCto.ifBlank { preliq.rotuloNapCto },
                        )
                        state.copy(
                            preliquidaciones = state.preliquidaciones + (ordenId to preliq),
                            liquidacionForms = state.liquidacionForms + (ordenId to updated),
                            isPreliqLoadingId = null,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isPreliqLoadingId = null) }
                }
            } ?: _uiState.update { it.copy(isPreliqLoadingId = null) }
            Log.d(TAG, "Preliquidacion $ordenId: ${result.getOrNull()?.found}")
        }
    }

    fun updateLiquidacionForm(ordenId: String, update: LiquidacionForm.() -> LiquidacionForm) {
        _uiState.update { state ->
            val current = state.liquidacionForms[ordenId] ?: LiquidacionForm()
            state.copy(liquidacionForms = state.liquidacionForms + (ordenId to current.update()))
        }
    }

    fun liquidarOrden(ordenId: String) {
        if (!_uiState.value.isEnabled) return
        val item = _uiState.value.liquidacion?.items?.find { it.id == ordenId } ?: return
        val form = _uiState.value.liquidacionForms[ordenId] ?: LiquidacionForm()

        _uiState.update { state ->
            state.copy(liquidacionForms = state.liquidacionForms + (ordenId to form.copy(isSubmitting = true, submitError = null)))
        }

        viewModelScope.launch {
            val request = AlmacenLiquidarRequest(
                ordenId = ordenId,
                snOnt = form.snOnt.trim().uppercase(),
                snMeshes = form.snMeshes.map { it.trim().uppercase() }.filter { it.isNotBlank() },
                snBoxes = form.snBoxes.map { it.trim().uppercase() }.filter { it.isNotBlank() },
                snFono = form.snFono.trim().uppercase(),
                rotuloNapCto = form.rotuloNapCto.trim(),
                planGamer = form.planGamer,
                kitWifiPro = form.kitWifiPro,
                servicioCableadoMesh = form.servicioCableadoMesh,
                cat5e = form.cat5e,
                cat6 = form.cat6,
                observacion = form.observacion.trim(),
            )
            val result = almacenRepository.liquidarOrden(request)
            if (result.isSuccess) {
                _uiState.update { state ->
                    val updatedItems = state.liquidacion?.items?.map {
                        if (it.id == ordenId) it.copy(liquidado = true, correccionPendiente = false) else it
                    } ?: emptyList()
                    val updatedLiq = state.liquidacion?.let { liq ->
                        val nuevasLiquidadas = updatedItems.count { it.liquidado }
                        liq.copy(
                            items = updatedItems,
                            kpi = liq.kpi.copy(
                                liquidadas = nuevasLiquidadas,
                                pendientes = liq.kpi.finalizadas - nuevasLiquidadas,
                            )
                        )
                    }
                    val updatedForm = (state.liquidacionForms[ordenId] ?: LiquidacionForm()).copy(
                        isSubmitting = false,
                        submitSuccess = true,
                        submitError = null,
                    )
                    state.copy(
                        liquidacion = updatedLiq,
                        liquidacionForms = state.liquidacionForms + (ordenId to updatedForm),
                        expandedLiquidacionId = null,
                    )
                }
                Log.d(TAG, "Liquidación OK: $ordenId")
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Error al liquidar"
                _uiState.update { state ->
                    val updatedForm = (state.liquidacionForms[ordenId] ?: LiquidacionForm()).copy(
                        isSubmitting = false,
                        submitError = msg,
                    )
                    state.copy(liquidacionForms = state.liquidacionForms + (ordenId to updatedForm))
                }
                Log.e(TAG, "Liquidación error $ordenId: $msg")
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun refreshStock() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isStockLoading = true) }
            val result = almacenRepository.fetchStock()
            _uiState.update {
                it.copy(
                    isStockLoading = false,
                    isRefreshing = false,
                    stock = result.getOrNull() ?: it.stock,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
            Log.d(TAG, "Stock cargado: ${result.getOrNull()?.size} cuadrillas")
        }
    }

    private fun refreshLiquidacion() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLiquidacionLoading = true) }
            val result = almacenRepository.fetchLiquidacion(_uiState.value.selectedYm)
            _uiState.update {
                it.copy(
                    isLiquidacionLoading = false,
                    isRefreshing = false,
                    liquidacion = result.getOrNull() ?: it.liquidacion,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    private fun refreshInstalaciones() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isInstalacionesLoading = true) }
            val result = almacenRepository.fetchInstalaciones(_uiState.value.selectedYm)
            _uiState.update {
                it.copy(
                    isInstalacionesLoading = false,
                    isRefreshing = false,
                    instalaciones = result.getOrNull() ?: it.instalaciones,
                    errorMessage = result.exceptionOrNull()?.localizedMessage,
                )
            }
        }
    }

    private fun refreshCuadrillasMapa() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCuadrillasMapaLoading = true) }
            val result = almacenRepository.fetchCuadrillasMapa()
            _uiState.update {
                it.copy(
                    isCuadrillasMapaLoading = false,
                    isRefreshing = false,
                    cuadrillasMapa = result.getOrNull() ?: it.cuadrillasMapa,
                )
            }
        }
    }

    private fun refreshMapaItems() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMapaItemsLoading = true) }
            val result = almacenRepository.fetchMapa(_uiState.value.selectedYmd)
            _uiState.update {
                it.copy(
                    isMapaItemsLoading = false,
                    mapaItems = result.getOrNull() ?: it.mapaItems,
                )
            }
        }
    }
}

class AlmacenViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val almacenRepository: AlmacenRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlmacenViewModel::class.java)) {
            return AlmacenViewModel(authRepository, sessionRepository, almacenRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun changeMonth(ym: String, delta: Int): String {
    val (y, m) = ym.split("-").map { it.toInt() }
    val date = java.time.YearMonth.of(y, m).plusMonths(delta.toLong())
    return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
}
