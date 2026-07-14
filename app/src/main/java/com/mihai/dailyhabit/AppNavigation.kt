package com.mihai.dailyhabit

sealed class AppDestination(val route: String) {
    object DataFlow : AppDestination("data_flow")
    object Home : AppDestination("home")
    object Journal : AppDestination("journal")
    object ModelManagement : AppDestination("model_management")
    object Settings : AppDestination("settings")
    object About : AppDestination("about")
}
