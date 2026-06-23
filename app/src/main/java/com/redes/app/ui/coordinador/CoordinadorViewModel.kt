package com.redes.app.ui.coordinador

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.coordinador.CoordinadorLiquidarRequest
import com.redes.app.data.coordinador.CoordinadorRepository
import com.redes.app.data.session.SessionRepository
import com.redes.app.data.tecnico.TecnicoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "CoordinadorViewModel"

class CoordinadorViewModel(
    authRepository: AuthRepository,
    sessionRepository: SessionRepository,
    private val coordinadorRepository: CoordinadorRepository,
    private val tecnicoRepository: TecnicoRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoordinadorUiState())
    val uiState: StateFlow<CoordinadorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(authRepository.currentUser, sessionRepository.selectedRole) { user, role ->
                user to role
            }.collect { (user, role) ->
                val enabled = user != null && role == "COORDINADOR"
                _uiState.update { if (!enabled) CoordinadorUiState() else it.copy(isEnabled = true) }
                if (enabled) refreshAll()
            }
        }
    }

    fun refreshAll() {
        if (!_uiState.value.isEnabled) return
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        when (_uiState.value.selectedTab) {
            CoordinadorTab.LIQUIDACION -> { refreshLiquidacion(); return }
            else -> {}
        }
        val ym = _uiState.value.selectedYm
        val ymd = _uiState.value.selectedYmd
        viewModelScope.launch {
            _uiState.update { it.copy(isResumenLoading = true, isCuadrillasLoading = true, isMapaLoading = true) }
            val resumenDeferred = async { coordinadorRepository.fetchResumen(ym) }
            val cuadrillasDeferred = async { coordinadorRepository.fetchCuadrillas(ymd) }
            val mapaDeferred = async { coordinadorRepository.fetchMapa(ymd) }
            val resumen = resumenDeferred.await()
            val cuadrillas = cuadrillasDeferred.await()
            val mapa = mapaDeferred.await()
            val error = listOf(resumen, cuadrillas).firstNotNullOfOrNull { it.exceptionOrNull()?.localizedMessage }
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    isResumenLoading = false,
                    isCuadrillasLoading = false,
                    isMapaLoading = false,
                    resumen = resumen.getOrNull() ?: it.resumen,
                    cuadrillaData = cuadrillas.getOrNull() ?: it.cuadrillaData,
                    mapaItems = mapa.getOrNull() ?: it.mapaItems,
                    errorMessage = error,
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun loadOrdenDetail(id: String) {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdenDetailLoading = true, selectedOrdenDetail = null) }
            val result = coordinadorRepository.fetchOrdenDetail(id)
            _uiState.update {
                it.copy(
                    isOrdenDetailLoading = false,
                    selectedOrdenDetail = result.getOrNull(),
                    errorMessage = if (result.isFailure) result.exceptionOrNull()?.localizedMessage else it.errorMessage,
                )
            }
        }
    }

    fun clearOrdenDetail() {
        _uiState.update { it.copy(selectedOrdenDetail = null, isOrdenDetailLoading = false) }
    }

    fun selectTab(tab: CoordinadorTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
        when (tab) {
            CoordinadorTab.INICIO -> if (_uiState.value.resumen == null) refreshResumen()
            CoordinadorTab.CUADRILLAS -> if (_uiState.value.cuadrillaData == null) refreshCuadrillas()
            CoordinadorTab.LIQUIDACION -> if (_uiState.value.liquidacion == null) refreshLiquidacion()
            CoordinadorTab.ALMACEN -> {
                if (_uiState.value.stock.isEmpty()) refreshStock()
            }
            CoordinadorTab.GESTION -> {
                if (_uiState.value.ventas.isEmpty()) refreshVentas()
            }
        }
    }

    fun selectAlmacenSubTab(subTab: AlmacenSubTab) {
        _uiState.update { it.copy(almacenSubTab = subTab) }
        when (subTab) {
            AlmacenSubTab.STOCK -> if (_uiState.value.stock.isEmpty()) refreshStock()
            AlmacenSubTab.AUDITORIA -> if (_uiState.value.auditoria.isEmpty()) refreshAuditoria()
            AlmacenSubTab.PREDESPACHO -> if (_uiState.value.predespacho == null) refreshPredespacho()
        }
    }

    fun selectGestionSubTab(subTab: GestionSubTab) {
        _uiState.update { it.copy(gestionSubTab = subTab) }
        when (subTab) {
            GestionSubTab.VENTAS -> if (_uiState.value.ventas.isEmpty()) refreshVentas()
            GestionSubTab.PLANTILLAS -> if (_uiState.value.plantillas.isEmpty()) refreshPlantillas()
        }
    }

    fun toggleCuadrillasView() {
        val newMode = if (_uiState.value.cuadrillasViewMode == CuadrillasViewMode.LISTA) CuadrillasViewMode.MAPA else CuadrillasViewMode.LISTA
        _uiState.update { it.copy(cuadrillasViewMode = newMode) }
    }

    fun toggleMapaMode() {
        val newMode = if (_uiState.value.mapaMode == MapaMode.MIS_ORDENES) MapaMode.CUADRILLAS else MapaMode.MIS_ORDENES
        _uiState.update { it.copy(mapaMode = newMode) }
        if (newMode == MapaMode.CUADRILLAS && _uiState.value.cuadrillasMapa.isEmpty()) refreshCuadrillasMapa()
        if (newMode == MapaMode.MIS_ORDENES && _uiState.value.mapaItems.isEmpty()) refreshMapaItems()
    }

    fun selectCuadrilla(cuadrillaId: String?) {
        _uiState.update { it.copy(selectedCuadrillaId = cuadrillaId) }
    }

    fun toggleCuadrillaExpanded(cuadrillaId: String) {
        _uiState.update { it.copy(expandedCuadrillaId = if (it.expandedCuadrillaId == cuadrillaId) null else cuadrillaId) }
    }

    fun toggleStockExpanded(cuadrillaId: String) {
        _uiState.update { it.copy(expandedStockCuadrillaId = if (it.expandedStockCuadrillaId == cuadrillaId) null else cuadrillaId) }
    }

    fun toggleAuditoriaExpanded(cuadrillaId: String) {
        _uiState.update { it.copy(expandedAuditoriaCuadrillaId = if (it.expandedAuditoriaCuadrillaId == cuadrillaId) null else cuadrillaId) }
    }

    fun sustainAuditoriaEquipo(cuadrillaId: String, sn: String, photoUri: Uri) {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(auditoriaSustainingSnId = sn) }
            val result = coordinadorRepository.sustainEquipo(cuadrillaId, sn, photoUri)
            _uiState.update { it.copy(auditoriaSustainingSnId = null) }
            if (result.isSuccess) {
                val updated = result.getOrNull()!!
                _uiState.update { st ->
                    st.copy(auditoria = st.auditoria.map { cuad ->
                        if (cuad.cuadrillaId != cuadrillaId) cuad
                        else cuad.copy(
                            items = cuad.items.map { if (it.sn == sn) updated else it },
                            pendiente = cuad.items.count { it.sn != sn && it.estado != "sustentada" },
                            sustentada = cuad.items.count { it.sn == sn || it.estado == "sustentada" },
                        )
                    })
                }
            } else {
                _uiState.update { it.copy(errorMessage = result.exceptionOrNull()?.localizedMessage) }
            }
        }
    }

    fun toggleLiquidacionExpanded(id: String) {
        val isExpanding = _uiState.value.expandedLiquidacionId != id
        _uiState.update {
            it.copy(expandedLiquidacionId = if (isExpanding) id else null, errorMessage = null)
        }
        if (isExpanding && !_uiState.value.preliquidaciones.containsKey(id)) {
            fetchLiquidacionPreliq(id)
        }
    }

    fun fetchLiquidacionPreliq(ordenId: String) {
        if (!_uiState.value.isEnabled) return
        _uiState.update { it.copy(isPreliqLoadingId = ordenId) }
        viewModelScope.launch {
            val result = coordinadorRepository.fetchPreliquidacion(ordenId)
            result.getOrNull()?.let { preliq ->
                if (preliq.found) {
                    _uiState.update { state ->
                        val existing = state.liquidacionForms[ordenId] ?: CoordinadorLiquidacionForm()
                        val updated = existing.copy(
                            snOnt = existing.snOnt.ifBlank { preliq.snOnt },
                            snMeshes = existing.snMeshes.ifEmpty { preliq.snMeshes },
                            snBoxes = existing.snBoxes.ifEmpty { preliq.snBoxes },
                            snFono = existing.snFono.ifBlank { preliq.snFono },
                            rotuloNapCto = existing.rotuloNapCto.ifBlank { preliq.rotuloNapCto },
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
            Log.d(TAG, "Preliquidacion coord $ordenId: ${result.getOrNull()?.found}")
        }
    }

    fun updateLiquidacionForm(ordenId: String, update: CoordinadorLiquidacionForm.() -> CoordinadorLiquidacionForm) {
        _uiState.update { state ->
            val current = state.liquidacionForms[ordenId] ?: CoordinadorLiquidacionForm()
            state.copy(liquidacionForms = state.liquidacionForms + (ordenId to current.update()))
        }
    }

    fun liquidarOrden(ordenId: String) {
        if (!_uiState.value.isEnabled) return
        val form = _uiState.value.liquidacionForms[ordenId] ?: CoordinadorLiquidacionForm()
        _uiState.update { state ->
            state.copy(liquidacionForms = state.liquidacionForms + (ordenId to form.copy(isSubmitting = true, submitError = null)))
        }
        viewModelScope.launch {
            val request = CoordinadorLiquidarRequest(
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
            val result = coordinadorRepository.liquidarOrden(request)
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
                    val updatedForm = (state.liquidacionForms[ordenId] ?: CoordinadorLiquidacionForm()).copy(
                        isSubmitting = false, submitSuccess = true, submitError = null,
                    )
                    state.copy(
                        liquidacion = updatedLiq,
                        liquidacionForms = state.liquidacionForms + (ordenId to updatedForm),
                        expandedLiquidacionId = null,
                    )
                }
                Log.d(TAG, "Liquidación coord OK: $ordenId")
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Error al liquidar"
                _uiState.update { state ->
                    val updatedForm = (state.liquidacionForms[ordenId] ?: CoordinadorLiquidacionForm()).copy(
                        isSubmitting = false, submitError = msg,
                    )
                    state.copy(liquidacionForms = state.liquidacionForms + (ordenId to updatedForm))
                }
                Log.e(TAG, "Liquidación coord error $ordenId: $msg")
            }
        }
    }

    fun previousMonth() {
        val ym = changeMonth(_uiState.value.selectedYm, -1)
        _uiState.update {
            it.copy(
                selectedYm = ym,
                resumen = null,
                ventas = emptyList(),
                plantillas = emptyList(),
                liquidacion = null,
                expandedLiquidacionId = null,
                liquidacionForms = emptyMap(),
                preliquidaciones = emptyMap(),
            )
        }
        when (_uiState.value.selectedTab) {
            CoordinadorTab.INICIO -> refreshResumen()
            CoordinadorTab.LIQUIDACION -> refreshLiquidacion()
            else -> refreshResumen()
        }
    }

    fun nextMonth() {
        val ym = changeMonth(_uiState.value.selectedYm, 1)
        _uiState.update {
            it.copy(
                selectedYm = ym,
                resumen = null,
                ventas = emptyList(),
                plantillas = emptyList(),
                liquidacion = null,
                expandedLiquidacionId = null,
                liquidacionForms = emptyMap(),
                preliquidaciones = emptyMap(),
            )
        }
        when (_uiState.value.selectedTab) {
            CoordinadorTab.INICIO -> refreshResumen()
            CoordinadorTab.LIQUIDACION -> refreshLiquidacion()
            else -> refreshResumen()
        }
    }

    fun previousDay() {
        if (!_uiState.value.isEnabled) return
        val prev = java.time.LocalDate.parse(_uiState.value.selectedYmd).minusDays(1).toString()
        _uiState.update { it.copy(selectedYmd = prev, cuadrillaData = null) }
        refreshCuadrillas()
    }

    fun nextDay() {
        if (!_uiState.value.isEnabled) return
        val today = java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")).toString()
        val current = _uiState.value.selectedYmd
        if (current >= today) return
        val next = java.time.LocalDate.parse(current).plusDays(1).toString()
        _uiState.update { it.copy(selectedYmd = next, cuadrillaData = null) }
        refreshCuadrillas()
    }

    fun selectCuadrillasDate(ymd: String) {
        if (!_uiState.value.isEnabled) return
        _uiState.update { it.copy(selectedYmd = ymd, cuadrillaData = null) }
        refreshCuadrillas()
    }

    private fun refreshLiquidacion() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLiquidacionLoading = true) }
            val result = coordinadorRepository.fetchLiquidacion(_uiState.value.selectedYm)
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

    private fun refreshResumen() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isResumenLoading = true) }
            val result = coordinadorRepository.fetchResumen(_uiState.value.selectedYm)
            _uiState.update { it.copy(isResumenLoading = false, resumen = result.getOrNull() ?: it.resumen, errorMessage = result.exceptionOrNull()?.localizedMessage) }
        }
    }

    private fun refreshCuadrillas() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCuadrillasLoading = true) }
            val result = coordinadorRepository.fetchCuadrillas(_uiState.value.selectedYmd)
            _uiState.update { it.copy(isCuadrillasLoading = false, cuadrillaData = result.getOrNull() ?: it.cuadrillaData, errorMessage = result.exceptionOrNull()?.localizedMessage) }
            // También cargar datos de mapa (órdenes)
            refreshMapaItems()
        }
    }

    private fun refreshMapaItems() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMapaLoading = true) }
            val result = coordinadorRepository.fetchMapa(_uiState.value.selectedYmd)
            _uiState.update { it.copy(isMapaLoading = false, mapaItems = result.getOrNull() ?: it.mapaItems) }
        }
    }

    private fun refreshCuadrillasMapa() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCuadrillasMapaLoading = true) }
            val result = coordinadorRepository.fetchCuadrillasMapa()
            _uiState.update { it.copy(isCuadrillasMapaLoading = false, cuadrillasMapa = result.getOrNull() ?: it.cuadrillasMapa) }
        }
    }

    private fun refreshStock() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isStockLoading = true) }
            val result = coordinadorRepository.fetchStock()
            _uiState.update { it.copy(isStockLoading = false, stock = result.getOrNull() ?: it.stock, errorMessage = result.exceptionOrNull()?.localizedMessage) }
        }
    }

    private fun refreshAuditoria() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAuditoriaLoading = true) }
            val result = coordinadorRepository.fetchAuditoria()
            _uiState.update { it.copy(isAuditoriaLoading = false, auditoria = result.getOrNull() ?: it.auditoria) }
        }
    }

    private fun refreshPredespacho() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPredespachoLoading = true) }
            val result = coordinadorRepository.fetchPredespacho(_uiState.value.selectedYmd)
            _uiState.update { it.copy(isPredespachoLoading = false, predespacho = result.getOrNull() ?: it.predespacho) }
        }
    }

    private fun refreshVentas() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isVentasLoading = true) }
            val parts = _uiState.value.selectedYm.split("-")
            val year = parts.getOrNull(0)?.toIntOrNull()
            val month = parts.getOrNull(1)?.toIntOrNull()
            val result = coordinadorRepository.fetchVentas(year, month)
            _uiState.update { it.copy(isVentasLoading = false, ventas = result.getOrNull() ?: it.ventas, errorMessage = result.exceptionOrNull()?.localizedMessage) }
        }
    }

    private fun refreshPlantillas() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPlantillasLoading = true) }
            val result = coordinadorRepository.fetchPlantillas(_uiState.value.selectedYm)
            _uiState.update { it.copy(isPlantillasLoading = false, plantillas = result.getOrNull() ?: it.plantillas, errorMessage = result.exceptionOrNull()?.localizedMessage) }
        }
    }
}

class CoordinadorViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val coordinadorRepository: CoordinadorRepository,
    private val tecnicoRepository: TecnicoRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoordinadorViewModel::class.java)) {
            return CoordinadorViewModel(authRepository, sessionRepository, coordinadorRepository, tecnicoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun changeMonth(ym: String, delta: Int): String {
    val (y, m) = ym.split("-").map { it.toInt() }
    val date = java.time.YearMonth.of(y, m).plusMonths(delta.toLong())
    return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"))
}
