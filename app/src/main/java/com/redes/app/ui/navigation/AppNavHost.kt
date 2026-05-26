package com.redes.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.redes.app.ui.auth.AuthUiState
import com.redes.app.ui.home.HomeUiState
import com.redes.app.ui.home.needsRoleSelection
import com.redes.app.ui.screens.ComunicadosScreen
import com.redes.app.ui.screens.HomeScreen
import com.redes.app.ui.screens.LoginScreen
import com.redes.app.ui.screens.NotificationsScreen
import com.redes.app.ui.screens.ProfileScreen
import com.redes.app.ui.screens.RoleSelectionScreen
import com.redes.app.ui.screens.SettingsScreen
import com.redes.app.ui.screens.SplashScreen
import com.redes.app.ui.screens.TecnicoOrderDetailScreen
import com.redes.app.ui.screens.TecnicoShellScreen
import com.redes.app.ui.tecnico.TecnicoTab
import com.redes.app.ui.tecnico.TecnicoUiState

@Composable
fun AppNavHost(
    uiState: AuthUiState,
    homeUiState: HomeUiState,
    tecnicoUiState: TecnicoUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRefreshHomeClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onComunicadoSeen: (String) -> Unit,
    onRoleSelected: (String) -> Unit,
    onChangeRoleClick: () -> Unit,
    onChangeRoleFromSettingsClick: () -> Unit,
    onTecnicoTabSelected: (TecnicoTab) -> Unit,
    onTecnicoRefreshClick: () -> Unit,
    onTecnicoPreviousDayClick: () -> Unit,
    onTecnicoNextDayClick: () -> Unit,
    onTecnicoDateSelected: (String) -> Unit,
    onTecnicoOrderClick: (String) -> Unit,
    onTecnicoDetailBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!uiState.isAuthResolved || !homeUiState.isStartupReady) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SplashScreen()
        }
        return
    }

    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val targetRoute = when {
        uiState.currentUser == null -> AppDestination.Login.route
        homeUiState.requiresComunicadosGate -> AppDestination.Comunicados.route
        homeUiState.needsRoleSelection -> AppDestination.RoleSelection.route
        else -> AppDestination.Home.route
    }

    LaunchedEffect(targetRoute) {
        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Login.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Login.route) {
            LoginScreen(
                uiState = uiState,
                onEmailChanged = onEmailChanged,
                onPasswordChanged = onPasswordChanged,
                onLoginClick = onLoginClick,
            )
        }
        composable(AppDestination.Comunicados.route) {
            ComunicadosScreen(
                uiState = homeUiState,
                onMarkSeenClick = onComunicadoSeen,
                onLogoutClick = onLogoutClick,
            )
        }
        composable(AppDestination.RoleSelection.route) {
            RoleSelectionScreen(
                uiState = homeUiState,
                onRoleSelected = onRoleSelected,
                onLogoutClick = onLogoutClick,
            )
        }
        composable(AppDestination.Home.route) {
            if (homeUiState.selectedRole == "TECNICO") {
                TecnicoShellScreen(
                    uiState = tecnicoUiState,
                    onTabSelected = onTecnicoTabSelected,
                    onRefreshClick = onTecnicoRefreshClick,
                    onPreviousDayClick = onTecnicoPreviousDayClick,
                    onNextDayClick = onTecnicoNextDayClick,
                    onDateSelected = onTecnicoDateSelected,
                    onOrderClick = { orderId ->
                        onTecnicoOrderClick(orderId)
                        navController.navigate(AppDestination.TecnicoOrderDetail.createRoute(orderId))
                    },
                    onAlertsClick = { navController.navigate(AppDestination.Notifications.route) },
                    onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                    onLogoutClick = onLogoutClick,
                )
            } else {
                HomeScreen(
                    uiState = homeUiState,
                    onProfileClick = { navController.navigate(AppDestination.Profile.route) },
                    onAlertsClick = { navController.navigate(AppDestination.Notifications.route) },
                    onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                    onRefreshClick = onRefreshHomeClick,
                    onLogoutClick = onLogoutClick,
                    onChangeRoleClick = onChangeRoleClick,
                )
            }
        }
        composable(AppDestination.Profile.route) {
            ProfileScreen(
                uiState = homeUiState,
                onAlertsClick = { navController.navigate(AppDestination.Notifications.route) },
                onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Settings.route) {
            SettingsScreen(
                uiState = homeUiState,
                onProfileClick = { navController.navigate(AppDestination.Profile.route) },
                onChangeRoleClick = {
                    onChangeRoleFromSettingsClick()
                    navController.navigate(AppDestination.RoleSelection.route) {
                        popUpTo(AppDestination.Home.route)
                    }
                },
                onLogoutClick = onLogoutClick,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(AppDestination.Notifications.route) {
            NotificationsScreen(
                uiState = homeUiState,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(
            route = AppDestination.TecnicoOrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            TecnicoOrderDetailScreen(
                uiState = tecnicoUiState,
                onAlertsClick = { navController.navigate(AppDestination.Notifications.route) },
                onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                onBackClick = {
                    onTecnicoDetailBack()
                    navController.popBackStack()
                },
            )
        }
    }
}
