package com.redes.app.ui.tecnico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.redes.app.data.auth.AuthRepository
import com.redes.app.data.session.SessionRepository
import com.redes.app.data.tecnico.TecnicoRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(TecnicoUiState())
    val uiState: StateFlow<TecnicoUiState> = _uiState.asStateFlow()

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
                }
            }
        }
    }

    fun selectTab(tab: TecnicoTab) {
        _uiState.update { it.copy(selectedTab = tab) }
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
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
                    isLoading = false,
                    home = home.getOrNull() ?: it.home,
                    ordersData = orders.getOrNull() ?: it.ordersData,
                    orders = orders.getOrNull()?.items ?: it.orders,
                    stock = stock.getOrNull() ?: it.stock,
                    map = map.getOrNull() ?: it.map,
                    errorMessage = error,
                )
            }
        }
    }

    fun refreshOrders() {
        if (!_uiState.value.isEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = tecnicoRepository.fetchOrders(_uiState.value.selectedYmd)
            _uiState.update {
                it.copy(
                    isLoading = false,
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
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = tecnicoRepository.fetchMap(_uiState.value.selectedYmd)
            _uiState.update {
                it.copy(
                    isLoading = false,
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

    fun clearOrderDetail() {
        _uiState.update { it.copy(selectedOrderDetail = null, isDetailLoading = false) }
    }
}

class TecnicoViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository,
    private val tecnicoRepository: TecnicoRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TecnicoViewModel::class.java)) {
            return TecnicoViewModel(authRepository, sessionRepository, tecnicoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

private fun changeDay(ymd: String, delta: Long): String {
    val date = java.time.LocalDate.parse(ymd)
    return date.plusDays(delta).toString()
}
