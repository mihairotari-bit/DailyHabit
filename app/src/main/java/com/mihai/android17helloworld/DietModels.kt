package com.mihai.android17helloworld

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class PlanType { WEEKLY, GENERAL_CHOICE, UNKNOWN }

@Serializable
data class DietPlan(
    val title: String = "Piano alimentare",
    val type: PlanType = PlanType.UNKNOWN,
    val days: List<DailyMeals>,
)

@Serializable
data class DailyMeals(val day: String, val meals: List<Meal>)

@Serializable
data class Meal(
    val type: MealType,
    val groups: List<OptionGroup>,
    /** Cena may reference lunch choices without creating a fake food row. */
    val hasLunchAlternatives: Boolean = false,
)

@Serializable
data class OptionGroup(
    val id: String = UUID.randomUUID().toString(),
    val alternatives: List<FoodItem>
)

@Serializable
enum class MealType(val label: String) {
    PRE_WORKOUT("Pre-workout"), POST_WORKOUT("Post-allenamento"), BREAKFAST("Colazione"),
    LUNCH("Pranzo"), SNACK("Merenda"), DINNER("Cena");
}

@Serializable
data class FoodItem(
    val clientId: String = UUID.randomUUID().toString(),
    val name: String,
    val quantity: String = "",
    val calories: Int? = null,
    val proteinGrams: Float? = null,
    val carbsGrams: Float? = null,
    val fatGrams: Float? = null,
)
