package com.mihai.dailyhabit

import javax.inject.Inject

class FakeDietInferenceEngine @Inject constructor() : DietInferenceEngine {
    override suspend fun parse(input: DietInferenceInput): DietPlan {
        return DietPlan(
            title = "Piano Fake",
            type = PlanType.WEEKLY,
            days = listOf(
                DailyMeals(
                    day = "Lunedì",
                    meals = listOf(
                        Meal(
                            type = MealType.BREAKFAST,
                            options = listOf(
                                MealOption(
                                    groups = listOf(
                                        OptionGroup(
                                            alternatives = listOf(
                                                FoodItem(name = "Latte", quantity = "200ml"),
                                                FoodItem(name = "Fette biscottate", quantity = "30g")
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
