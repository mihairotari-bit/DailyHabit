package com.mihai.dailyhabit

sealed class AppDestination(val route: String) {
    object DataFlow : AppDestination("data_flow")
    object SavedArea : AppDestination("saved_area")
    object ModelManagement : AppDestination("model_management")
    object Settings : AppDestination("settings")
    object About : AppDestination("about")
}

sealed class SavedDestination(val route: String) {
    object DaySelection : SavedDestination("day_selection")
    object MealSelection : SavedDestination("meal_selection")
    object MealDetail : SavedDestination("meal_detail")
    object Journal : SavedDestination("journal")
}
