package com.redes.app.ui.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Comunicados : AppDestination("comunicados")
    data object RoleSelection : AppDestination("role_selection")
    data object Home : AppDestination("home")
    data object Profile : AppDestination("profile")
    data object Settings : AppDestination("settings")
    data object Notifications : AppDestination("notifications")
    data object TecnicoOrderDetail : AppDestination("tecnico_order_detail/{orderId}") {
        fun createRoute(orderId: String): String = "tecnico_order_detail/$orderId"
    }
    data object SupervisorOrderDetail : AppDestination("supervisor_order_detail/{orderId}") {
        fun createRoute(orderId: String): String = "supervisor_order_detail/$orderId"
    }
    data object CoordinadorOrderDetail : AppDestination("coordinador_order_detail/{orderId}") {
        fun createRoute(orderId: String): String = "coordinador_order_detail/$orderId"
    }
}
