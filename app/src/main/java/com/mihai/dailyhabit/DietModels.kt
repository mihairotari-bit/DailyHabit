package com.mihai.dailyhabit

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class PlanType { WEEKLY, GENERAL_CHOICE, UNKNOWN }

@Serializable
enum class ParserEngine { LEGACY_DETERMINISTIC, LITERT_GEMMA4_E2B, FAKE_TEST }

@Serializable
enum class DayProfileType { TRAINING, REST, WEEKDAY, CUSTOM, UNKNOWN }

@Serializable
data class DietPlan(
    val title: String = "Piano alimentare",
    val type: PlanType = PlanType.UNKNOWN,
    val days: List<DailyMeals>,
    val parserEngine: ParserEngine = ParserEngine.LEGACY_DETERMINISTIC,
    val parserVersion: String = "1.0",
    val extractionMethod: String = "Unknown",
    val isTestData: Boolean = false,
    val sourceFileName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class DailyMeals(
    val day: String,
    val meals: List<Meal>,
    val profileType: DayProfileType = DayProfileType.UNKNOWN
)

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
    MORNING_SNACK("Spuntino mattutino"), LUNCH("Pranzo"), SNACK("Merenda"), DINNER("Cena");
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
