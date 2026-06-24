package com.redes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.redes.app.ui.theme.REDESTheme
import com.redes.app.ui.auth.AuthViewModel
import com.redes.app.ui.auth.AuthViewModelFactory
import com.redes.app.ui.home.HomeViewModel
import com.redes.app.ui.home.HomeViewModelFactory
import com.redes.app.ui.navigation.AppNavHost
import com.redes.app.ui.almacen.AlmacenViewModel
import com.redes.app.ui.almacen.AlmacenViewModelFactory
import com.redes.app.ui.coordinador.CoordinadorViewModel
import com.redes.app.ui.coordinador.CoordinadorViewModelFactory
import com.redes.app.ui.screens.ForceUpdateScreen
import com.redes.app.ui.tecnico.TecnicoViewModel
import com.redes.app.ui.tecnico.TecnicoViewModelFactory
import com.redes.app.ui.supervisor.SupervisorViewModel
import com.redes.app.ui.supervisor.SupervisorViewModelFactory
import com.redes.app.ui.update.ForceUpdateState
import com.redes.app.ui.update.ForceUpdateViewModel

class MainActivity : ComponentActivity() {
    private val appContainer by lazy {
        (application as REDESApplication).appContainer
    }

    private val forceUpdateViewModel: ForceUpdateViewModel by viewModels()

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(
            appContainer.authRepository
        )
    }

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            authRepository = appContainer.authRepository,
            sessionRepository = appContainer.sessionRepository,
        )
    }

    private val tecnicoViewModel: TecnicoViewModel by viewModels {
        TecnicoViewModelFactory(
            authRepository = appContainer.authRepository,
            sessionRepository = appContainer.sessionRepository,
            tecnicoRepository = appContainer.tecnicoRepository,
            trackingManager = appContainer.trackingManager,
            alertaRepository = appContainer.alertaRepository,
            context = this,
        )
    }

    private val supervisorViewModel: SupervisorViewModel by viewModels {
        SupervisorViewModelFactory(
            authRepository = appContainer.authRepository,
            sessionRepository = appContainer.sessionRepository,
            supervisorRepository = appContainer.supervisorRepository,
            trackingManager = appContainer.trackingManager,
            context = this,
        )
    }

    private val coordinadorViewModel: CoordinadorViewModel by viewModels {
        CoordinadorViewModelFactory(
            authRepository = appContainer.authRepository,
            sessionRepository = appContainer.sessionRepository,
            coordinadorRepository = appContainer.coordinadorRepository,
            tecnicoRepository = appContainer.tecnicoRepository,
        )
    }

    private val almacenViewModel: AlmacenViewModel by viewModels {
        AlmacenViewModelFactory(
            authRepository = appContainer.authRepository,
            sessionRepository = appContainer.sessionRepository,
            almacenRepository = appContainer.almacenRepository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Mantener el splash activo mientras se verifica la versión
        splashScreen.setKeepOnScreenCondition {
            forceUpdateViewModel.state.value is ForceUpdateState.Checking
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val updateState by forceUpdateViewModel.state.collectAsStateWithLifecycle()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val tecnicoUiState by tecnicoViewModel.uiState.collectAsStateWithLifecycle()
            val supervisorUiState by supervisorViewModel.uiState.collectAsStateWithLifecycle()
            val coordinadorUiState by coordinadorViewModel.uiState.collectAsStateWithLifecycle()
            val almacenUiState by almacenViewModel.uiState.collectAsStateWithLifecycle()
            REDESTheme {
                val required = updateState as? ForceUpdateState.UpdateRequired
                if (required != null) {
                    ForceUpdateScreen(
                        versionNominal = required.versionNominal,
                        mensaje = required.mensaje,
                    )
                    return@REDESTheme
                }
                // Estado Checking ya lo cubre el splash; UpToDate → flujo normal
                AppNavHost(
                    uiState = uiState,
                    homeUiState = homeUiState,
                    tecnicoUiState = tecnicoUiState,
                    supervisorUiState = supervisorUiState,
                    coordinadorUiState = coordinadorUiState,
                    almacenUiState = almacenUiState,
                    onEmailChanged = viewModel::onEmailChanged,
                    onPasswordChanged = viewModel::onPasswordChanged,
                    onLoginClick = viewModel::signIn,
                    onRefreshHomeClick = homeViewModel::refreshBackendSession,
                    onLogoutClick = ::handleLogout,
                    onComunicadoSeen = homeViewModel::markComunicadoSeen,
                    onRoleSelected = homeViewModel::selectRole,
                    onChangeRoleClick = homeViewModel::clearSelectedRole,
                    onChangeRoleFromSettingsClick = homeViewModel::clearSelectedRole,
                    onTecnicoTabSelected = tecnicoViewModel::selectTab,
                    onTecnicoRefreshClick = tecnicoViewModel::refreshAll,
                    onTecnicoPreviousDayClick = tecnicoViewModel::previousDay,
                    onTecnicoNextDayClick = tecnicoViewModel::nextDay,
                    onTecnicoDateSelected = tecnicoViewModel::selectDate,
                    onTecnicoOrderClick = tecnicoViewModel::loadOrderDetail,
                    onTecnicoStockSustainClick = tecnicoViewModel::sustainStockEquipment,
                    onTecnicoDetailBack = tecnicoViewModel::clearOrderDetail,
                    onTecnicoToggleMapMode = tecnicoViewModel::toggleMapMode,
                    onTecnicoCloseRoute = tecnicoViewModel::cerrarRuta,
                    onTecnicoRequiereAtencion = tecnicoViewModel::requerirAtencion,
                    onTecnicoNotificationsOpened = tecnicoViewModel::onNotificationsOpened,
                    onTecnicoRefreshCuadrillasMapa = tecnicoViewModel::refreshCuadrillasMapa,
                    onStartTracking = appContainer.trackingManager::startIfNeeded,
                    // Supervisor
                    onSupervisorTabSelected = supervisorViewModel::selectTab,
                    onSupervisorRefreshClick = supervisorViewModel::refreshAll,
                    onSupervisorOrderClick = supervisorViewModel::loadOrderDetail,
                    onSupervisorDetailBack = supervisorViewModel::clearOrderDetail,
                    onSupervisorToggleGarantias = supervisorViewModel::toggleGarantias,
                    onSupervisorPreviousDay = supervisorViewModel::previousDay,
                    onSupervisorNextDay = supervisorViewModel::nextDay,
                    onSupervisorSetMapMode = supervisorViewModel::setMapMode,
                    onSupervisorSelectCuadrilla = supervisorViewModel::selectCuadrilla,
                    onSupervisorRefreshCuadrillasMapa = supervisorViewModel::refreshCuadrillasMapa,
                    onSupervisorCloseRoute = supervisorViewModel::showCerrarRutaConfirm,
                    onSupervisorIniciarRuta = supervisorViewModel::iniciarRuta,
                    onSupervisorConfirmarCerrarRuta = supervisorViewModel::confirmarCerrarRuta,
                    onSupervisorDismissCerrarRuta = supervisorViewModel::dismissCerrarRutaConfirm,
                    onSupervisorIniciarRefrigerio = supervisorViewModel::iniciarRefrigerio,
                    onSupervisorFinRefrigerio = supervisorViewModel::finRefrigerio,
                    onSupervisorShowCuadrillasModal = supervisorViewModel::showCuadrillasModal,
                    onSupervisorHideCuadrillasModal = supervisorViewModel::hideCuadrillasModal,
                    onSupervisorDismissJornadaError = supervisorViewModel::dismissJornadaError,
                    onSupervisorSaveSupervision = supervisorViewModel::saveSupervision,
                    onSupervisorUpdateGarantia = supervisorViewModel::updateGarantia,
                    onStartSupervisorTracking = appContainer.trackingManager::startIfNeeded,
                    onSupervisorAlertsClick = supervisorViewModel::showAlertas,
                    onSupervisorDismissAlertas = supervisorViewModel::hideAlertas,
                    onCoordinadorTabSelected = coordinadorViewModel::selectTab,
                    onCoordinadorRefreshClick = coordinadorViewModel::refreshAll,
                    onCoordinadorAlmacenSubTab = coordinadorViewModel::selectAlmacenSubTab,
                    onCoordinadorGestionSubTab = coordinadorViewModel::selectGestionSubTab,
                    onCoordinadorToggleCuadrillasView = coordinadorViewModel::toggleCuadrillasView,
                    onCoordinadorToggleMapaMode = coordinadorViewModel::toggleMapaMode,
                    onCoordinadorSelectCuadrilla = coordinadorViewModel::selectCuadrilla,
                    onCoordinadorPreviousMonth = coordinadorViewModel::previousMonth,
                    onCoordinadorNextMonth = coordinadorViewModel::nextMonth,
                    onCoordinadorToggleCuadrillaExpanded = coordinadorViewModel::toggleCuadrillaExpanded,
                    onCoordinadorToggleStockExpanded = coordinadorViewModel::toggleStockExpanded,
                    onCoordinadorToggleAuditoriaExpanded = coordinadorViewModel::toggleAuditoriaExpanded,
                    onCoordinadorSustainEquipo = coordinadorViewModel::sustainAuditoriaEquipo,
                    onCoordinadorPreviousDay = coordinadorViewModel::previousDay,
                    onCoordinadorNextDay = coordinadorViewModel::nextDay,
                    onCoordinadorDateSelected = coordinadorViewModel::selectCuadrillasDate,
                    onCoordinadorDismissError = coordinadorViewModel::dismissError,
                    onCoordinadorOrdenClick = coordinadorViewModel::loadOrdenDetail,
                    onCoordinadorOrdenDetailBack = coordinadorViewModel::clearOrdenDetail,
                    onAlmacenTabSelected = almacenViewModel::selectTab,
                    onAlmacenRefreshClick = almacenViewModel::refreshAll,
                    onAlmacenPreviousMonth = almacenViewModel::previousMonth,
                    onAlmacenNextMonth = almacenViewModel::nextMonth,
                    onAlmacenToggleStockExpanded = almacenViewModel::toggleStockExpanded,
                    onAlmacenToggleInstalacionExpanded = almacenViewModel::toggleInstalacionExpanded,
                    onAlmacenToggleLiquidacionExpanded = almacenViewModel::toggleLiquidacionExpanded,
                    onAlmacenUpdateLiquidacionForm = almacenViewModel::updateLiquidacionForm,
                    onAlmacenLiquidarOrden = almacenViewModel::liquidarOrden,
                    onAlmacenDismissError = almacenViewModel::dismissError,
                )
            }
        }
    }

    private fun handleLogout() {
        appContainer.trackingManager.stop()
        appContainer.presenceManager.onSignedOut()
        viewModel.signOut()
    }
}
