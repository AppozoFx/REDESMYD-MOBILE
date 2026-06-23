package com.redes.app.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import com.redes.app.ui.coordinador.AlmacenSubTab
import com.redes.app.ui.coordinador.CoordinadorTab
import com.redes.app.ui.coordinador.CoordinadorUiState
import com.redes.app.ui.coordinador.GestionSubTab
import com.redes.app.ui.screens.ComunicadosScreen
import com.redes.app.ui.almacen.AlmacenTab
import com.redes.app.ui.almacen.AlmacenUiState
import com.redes.app.ui.screens.AlmacenShellScreen
import com.redes.app.ui.screens.CoordinadorShellScreen
import com.redes.app.ui.screens.CoordinadorOrderDetailScreen
import com.redes.app.ui.screens.HomeScreen
import com.redes.app.ui.screens.LoginScreen
import com.redes.app.ui.screens.NotificationsScreen
import com.redes.app.ui.screens.ProfileScreen
import com.redes.app.ui.screens.RoleSelectionScreen
import com.redes.app.ui.screens.SettingsScreen
import com.redes.app.ui.screens.SplashScreen
import com.redes.app.ui.screens.TecnicoOrderDetailScreen
import com.redes.app.ui.screens.TecnicoShellScreen
import com.redes.app.ui.screens.SupervisorShellScreen
import com.redes.app.ui.screens.SupervisorOrderDetailScreen
import com.redes.app.ui.tecnico.TecnicoTab
import com.redes.app.ui.tecnico.TecnicoUiState
import com.redes.app.ui.supervisor.SupervisorUiState
import com.redes.app.ui.supervisor.SupervisorTab
import com.redes.app.data.supervisor.SupervisorMapMode

@Composable
fun AppNavHost(
    uiState: AuthUiState,
    homeUiState: HomeUiState,
    tecnicoUiState: TecnicoUiState,
    supervisorUiState: SupervisorUiState,
    coordinadorUiState: CoordinadorUiState,
    almacenUiState: AlmacenUiState,
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
    onTecnicoStockSustainClick: (String, String, android.net.Uri) -> Unit,
    onTecnicoDetailBack: () -> Unit,
    onTecnicoToggleMapMode: () -> Unit = {},
    onTecnicoCloseRoute: () -> Unit = {},
    onTecnicoRequiereAtencion: () -> Unit = {},
    onTecnicoNotificationsOpened: () -> Unit = {},
    onTecnicoRefreshCuadrillasMapa: () -> Unit = {},
    onStartTracking: () -> Unit = {},
    onCoordinadorTabSelected: (CoordinadorTab) -> Unit = {},
    onCoordinadorRefreshClick: () -> Unit = {},
    onCoordinadorAlmacenSubTab: (AlmacenSubTab) -> Unit = {},
    onCoordinadorGestionSubTab: (GestionSubTab) -> Unit = {},
    onCoordinadorToggleCuadrillasView: () -> Unit = {},
    onCoordinadorToggleMapaMode: () -> Unit = {},
    onCoordinadorSelectCuadrilla: (String?) -> Unit = {},
    onCoordinadorPreviousMonth: () -> Unit = {},
    onCoordinadorNextMonth: () -> Unit = {},
    onCoordinadorToggleCuadrillaExpanded: (String) -> Unit = {},
    onCoordinadorToggleStockExpanded: (String) -> Unit = {},
    onCoordinadorToggleAuditoriaExpanded: (String) -> Unit = {},
    onCoordinadorSustainEquipo: (String, String, android.net.Uri) -> Unit = { _, _, _ -> },
    onCoordinadorPreviousDay: () -> Unit = {},
    onCoordinadorNextDay: () -> Unit = {},
    onCoordinadorDateSelected: (String) -> Unit = {},
    onCoordinadorDismissError: () -> Unit = {},
    onCoordinadorOrdenClick: (String) -> Unit = {},
    onCoordinadorOrdenDetailBack: () -> Unit = {},
    onCoordinadorToggleLiquidacionExpanded: (String) -> Unit = {},
    onCoordinadorUpdateLiquidacionForm: (String, com.redes.app.ui.coordinador.CoordinadorLiquidacionForm.() -> com.redes.app.ui.coordinador.CoordinadorLiquidacionForm) -> Unit = { _, _ -> },
    onCoordinadorLiquidarOrden: (String) -> Unit = {},
    // Almacén
    onAlmacenTabSelected: (AlmacenTab) -> Unit = {},
    onAlmacenRefreshClick: () -> Unit = {},
    onAlmacenPreviousMonth: () -> Unit = {},
    onAlmacenNextMonth: () -> Unit = {},
    onAlmacenToggleStockExpanded: (String) -> Unit = {},
    onAlmacenToggleInstalacionExpanded: (String) -> Unit = {},
    onAlmacenToggleLiquidacionExpanded: (String) -> Unit = {},
    onAlmacenUpdateLiquidacionForm: (String, com.redes.app.ui.almacen.LiquidacionForm.() -> com.redes.app.ui.almacen.LiquidacionForm) -> Unit = { _, _ -> },
    onAlmacenLiquidarOrden: (String) -> Unit = {},
    onAlmacenDismissError: () -> Unit = {},
    // Supervisor
    onSupervisorTabSelected: (SupervisorTab) -> Unit = {},
    onSupervisorRefreshClick: () -> Unit = {},
    onSupervisorOrderClick: (String) -> Unit = {},
    onSupervisorDetailBack: () -> Unit = {},
    onSupervisorToggleGarantias: () -> Unit = {},
    onSupervisorPreviousDay: () -> Unit = {},
    onSupervisorNextDay: () -> Unit = {},
    onSupervisorSetMapMode: (SupervisorMapMode) -> Unit = {},
    onSupervisorSelectCuadrilla: (String?) -> Unit = {},
    onSupervisorRefreshCuadrillasMapa: () -> Unit = {},
    onSupervisorCloseRoute: () -> Unit = {},
    onSupervisorIniciarRuta: () -> Unit = {},
    onSupervisorConfirmarCerrarRuta: () -> Unit = {},
    onSupervisorDismissCerrarRuta: () -> Unit = {},
    onSupervisorIniciarRefrigerio: () -> Unit = {},
    onSupervisorFinRefrigerio: () -> Unit = {},
    onSupervisorShowCuadrillasModal: () -> Unit = {},
    onSupervisorHideCuadrillasModal: () -> Unit = {},
    onSupervisorDismissJornadaError: () -> Unit = {},
    onSupervisorSaveSupervision: (String, String, String) -> Unit = { _, _, _ -> },
    onSupervisorUpdateGarantia: (String, String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onStartSupervisorTracking: () -> Unit = {},
    onSupervisorAlertsClick: () -> Unit = {},
    onSupervisorDismissAlertas: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (!uiState.isAuthResolved || !homeUiState.isStartupReady) {
        SplashScreen(modifier = modifier)
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
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { grants ->
                    if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                        onStartTracking()
                    }
                }
                LaunchedEffect(Unit) {
                    val locationGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (locationGranted) {
                        onStartTracking()
                    } else {
                        val permissions = buildList {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }
                }
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
                    onStockSustainClick = onTecnicoStockSustainClick,
                    onToggleMapMode = onTecnicoToggleMapMode,
                    onCloseRouteClick = onTecnicoCloseRoute,
                    onRequiereAtencionClick = onTecnicoRequiereAtencion,
                    onRefreshCuadrillasMapa = onTecnicoRefreshCuadrillasMapa,
                    onAlertsClick = {
                        onTecnicoNotificationsOpened()
                        navController.navigate(AppDestination.Notifications.route)
                    },
                    onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                    onLogoutClick = onLogoutClick,
                )
            } else if (homeUiState.selectedRole == "SUPERVISOR") {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { grants ->
                    if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) onStartSupervisorTracking()
                }
                LaunchedEffect(Unit) {
                    val locationGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (locationGranted) onStartSupervisorTracking()
                    val permsToRequest = buildList {
                        if (!locationGranted) add(Manifest.permission.ACCESS_FINE_LOCATION)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val notifGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                            if (!notifGranted) add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    if (permsToRequest.isNotEmpty()) permissionLauncher.launch(permsToRequest.toTypedArray())
                }
                SupervisorShellScreen(
                    uiState = supervisorUiState,
                    onTabSelected = onSupervisorTabSelected,
                    onRefreshClick = onSupervisorRefreshClick,
                    onOrderClick = { orderId ->
                        onSupervisorOrderClick(orderId)
                        navController.navigate(AppDestination.SupervisorOrderDetail.createRoute(orderId))
                    },
                    onToggleGarantias = onSupervisorToggleGarantias,
                    onPreviousDay = onSupervisorPreviousDay,
                    onNextDay = onSupervisorNextDay,
                    onSetMapMode = onSupervisorSetMapMode,
                    onSelectCuadrilla = onSupervisorSelectCuadrilla,
                    onRefreshCuadrillasMapa = onSupervisorRefreshCuadrillasMapa,
                    onCloseRouteClick = onSupervisorCloseRoute,
                    onIniciarRuta = onSupervisorIniciarRuta,
                    onConfirmarCerrarRuta = onSupervisorConfirmarCerrarRuta,
                    onDismissCerrarRuta = onSupervisorDismissCerrarRuta,
                    onIniciarRefrigerio = onSupervisorIniciarRefrigerio,
                    onFinRefrigerio = onSupervisorFinRefrigerio,
                    onShowCuadrillasModal = onSupervisorShowCuadrillasModal,
                    onHideCuadrillasModal = onSupervisorHideCuadrillasModal,
                    onDismissJornadaError = onSupervisorDismissJornadaError,
                    onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                    onLogoutClick = onLogoutClick,
                    onAlertsClick = onSupervisorAlertsClick,
                    onDismissAlertas = onSupervisorDismissAlertas,
                )
            } else if (homeUiState.selectedRole == "ALMACEN") {
                AlmacenShellScreen(
                    uiState = almacenUiState,
                    onTabSelected = onAlmacenTabSelected,
                    onRefreshClick = onAlmacenRefreshClick,
                    onPreviousMonth = onAlmacenPreviousMonth,
                    onNextMonth = onAlmacenNextMonth,
                    onToggleStockExpanded = onAlmacenToggleStockExpanded,
                    onToggleInstalacionExpanded = onAlmacenToggleInstalacionExpanded,
                    onToggleLiquidacionExpanded = onAlmacenToggleLiquidacionExpanded,
                    onUpdateLiquidacionForm = onAlmacenUpdateLiquidacionForm,
                    onLiquidarOrden = onAlmacenLiquidarOrden,
                    onDismissError = onAlmacenDismissError,
                    onAlertsClick = { navController.navigate(AppDestination.Notifications.route) },
                    onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
                    onLogoutClick = onLogoutClick,
                )
            } else if (homeUiState.selectedRole == "COORDINADOR") {
                CoordinadorShellScreen(
                    uiState = coordinadorUiState,
                    onTabSelected = onCoordinadorTabSelected,
                    onRefreshClick = onCoordinadorRefreshClick,
                    onAlmacenSubTab = onCoordinadorAlmacenSubTab,
                    onGestionSubTab = onCoordinadorGestionSubTab,
                    onToggleCuadrillasView = onCoordinadorToggleCuadrillasView,
                    onToggleMapaMode = onCoordinadorToggleMapaMode,
                    onSelectCuadrilla = onCoordinadorSelectCuadrilla,
                    onPreviousMonth = onCoordinadorPreviousMonth,
                    onNextMonth = onCoordinadorNextMonth,
                    onToggleCuadrillaExpanded = onCoordinadorToggleCuadrillaExpanded,
                    onToggleStockExpanded = onCoordinadorToggleStockExpanded,
                    onToggleAuditoriaExpanded = onCoordinadorToggleAuditoriaExpanded,
                    onAuditoriaSustain = onCoordinadorSustainEquipo,
                    onOrdenClick = { ordenId ->
                        onCoordinadorOrdenClick(ordenId)
                        navController.navigate(AppDestination.CoordinadorOrderDetail.createRoute(ordenId))
                    },
                    onPreviousDay = onCoordinadorPreviousDay,
                    onNextDay = onCoordinadorNextDay,
                    onDateSelected = onCoordinadorDateSelected,
                    onDismissError = onCoordinadorDismissError,
                    onToggleLiquidacionExpanded = onCoordinadorToggleLiquidacionExpanded,
                    onUpdateLiquidacionForm = onCoordinadorUpdateLiquidacionForm,
                    onLiquidarOrden = onCoordinadorLiquidarOrden,
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
                tecnicoUiState = if (homeUiState.selectedRole == "TECNICO") tecnicoUiState else null,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(
            route = AppDestination.SupervisorOrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            SupervisorOrderDetailScreen(
                uiState = supervisorUiState,
                onBackClick = {
                    onSupervisorDetailBack()
                    navController.popBackStack()
                },
                onSaveSupervision = onSupervisorSaveSupervision,
                onUpdateGarantia = onSupervisorUpdateGarantia,
                onSettingsClick = { navController.navigate(AppDestination.Settings.route) },
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
        composable(
            route = AppDestination.CoordinadorOrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) {
            CoordinadorOrderDetailScreen(
                uiState = coordinadorUiState,
                onBackClick = {
                    onCoordinadorOrdenDetailBack()
                    navController.popBackStack()
                },
            )
        }
    }
}
