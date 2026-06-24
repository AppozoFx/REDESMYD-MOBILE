package com.redes.app.ui.coordinador

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redes.app.data.auth.AuthRepository
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
        refreshResumen()
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
        refreshResumen()
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
