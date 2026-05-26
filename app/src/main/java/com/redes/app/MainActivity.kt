package com.redes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.redes.app.ui.theme.REDESTheme
import com.redes.app.ui.auth.AuthViewModel
import com.redes.app.ui.auth.AuthViewModelFactory
import com.redes.app.ui.home.HomeViewModel
import com.redes.app.ui.home.HomeViewModelFactory
import com.redes.app.ui.navigation.AppNavHost
import com.redes.app.ui.tecnico.TecnicoViewModel
import com.redes.app.ui.tecnico.TecnicoViewModelFactory

class MainActivity : ComponentActivity() {
    private val appContainer by lazy {
        (application as REDESApplication).appContainer
    }

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
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val tecnicoUiState by tecnicoViewModel.uiState.collectAsStateWithLifecycle()
            REDESTheme {
                AppNavHost(
                    uiState = uiState,
                    homeUiState = homeUiState,
                    tecnicoUiState = tecnicoUiState,
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
                    onTecnicoDetailBack = tecnicoViewModel::clearOrderDetail,
                )
            }
        }
    }

    private fun handleLogout() {
        appContainer.presenceManager.onSignedOut()
        viewModel.signOut()
    }
}
