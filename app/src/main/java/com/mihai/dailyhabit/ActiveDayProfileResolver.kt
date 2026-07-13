package com.mihai.dailyhabit

import javax.inject.Inject

class ActiveDayProfileResolver @Inject constructor() {
    /**
     * Resolves the appropriate DailyMeals profile based on semantic rules rather than exact string matching.
     */
    fun resolve(plan: DietPlan, requestedType: DayProfileType, fallbackLabel: String? = null): DailyMeals? {
        if (plan.days.isEmpty()) return null
        
        val semanticMatch = when (requestedType) {
            DayProfileType.TRAINING -> plan.days.find { it.profileType == DayProfileType.TRAINING || isTrainingDay(it.day) }
            DayProfileType.REST -> plan.days.find { it.profileType == DayProfileType.REST || isRestDay(it.day) }
            DayProfileType.WEEKDAY -> plan.days.find { it.profileType == DayProfileType.WEEKDAY || it.day.equals(fallbackLabel, ignoreCase = true) }
            DayProfileType.CUSTOM -> plan.days.find { it.profileType == DayProfileType.CUSTOM || it.day.equals(fallbackLabel, ignoreCase = true) }
            DayProfileType.UNKNOWN -> null
        }
        
        if (semanticMatch != null) return semanticMatch
        
        // Se non troviamo match semantico, chiediamo all'utente (restituiamo null) 
        // TRANNE se c'è un match esatto per etichetta o siamo in UNKNOWN/CUSTOM con size=1.
        val exactLabelMatch = plan.days.find { it.day.equals(fallbackLabel, ignoreCase = true) }
        if (exactLabelMatch != null) return exactLabelMatch
        
        return null
    }

    private fun isTrainingDay(dayLabel: String): Boolean {
        val lower = dayLabel.lowercase()
        return lower.contains("con allenamento") || lower.contains("training") || lower.contains("workout")
    }

    private fun isRestDay(dayLabel: String): Boolean {
        val lower = dayLabel.lowercase()
        return lower.contains("senza allenamento") || lower.contains("riposo") || lower.contains("rest")
    }
}
