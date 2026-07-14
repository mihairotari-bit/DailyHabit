package com.mihai.dailyhabit

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietParser @Inject constructor(
    private val preprocessor: DietTextPreprocessor
) {
    fun parse(ocrText: String): DietPlan {
        val sanitizedText = preprocessor.preprocess(ocrText)
        val type = detectPlanType(sanitizedText)
        val plans = linkedMapOf<String, LinkedHashMap<MealType, MealDraft>>()
        
        // Find boundaries for days
        val dayMatches = Regex("(?i).*\\bgiorno\\s+(con|senza)\\s+allenamento\\b.*").findAll(sanitizedText).toList()
        
        val blocks = mutableListOf<String>()
        if (dayMatches.isEmpty()) {
            blocks.add(sanitizedText)
        } else {
            for (i in dayMatches.indices) {
                val start = dayMatches[i].range.first
                val end = if (i + 1 < dayMatches.size) dayMatches[i + 1].range.first else sanitizedText.length
                blocks.add(sanitizedText.substring(start, end))
            }
            // Add anything before the first day as an implicit block (or discard if it's noise)
            val preamble = sanitizedText.substring(0, dayMatches[0].range.first).trim()
            if (preamble.isNotBlank()) blocks.add(0, preamble)
        }

        for (block in blocks) {
            var currentDay: String? = null
            var currentMeal: MealType? = null
            var currentGroup: MutableList<FoodItem>? = null
            var dinnerUsesLunch = false

            fun mealDraft(): MealDraft? {
                val day = currentDay ?: return null
                val meal = currentMeal ?: return null
                return plans.getOrPut(day) { linkedMapOf() }.getOrPut(meal) { MealDraft() }
            }
            fun flushGroup() {
                val group = currentGroup
                if (!group.isNullOrEmpty()) mealDraft()?.groups?.add(OptionGroup(alternatives = group.toList()))
                currentGroup = null
            }
            fun startGroup() { flushGroup(); currentGroup = mutableListOf() }

            val lines = chunkOcrLines(block)
            lines.forEachIndexed { index, line ->
                dayFor("$line ${lines.getOrNull(index + 1).orEmpty()}")?.let { day ->
                    flushGroup(); currentDay = day; currentMeal = null; dinnerUsesLunch = false; return@forEachIndexed
                }
                
                // Heuristic: Auto-assign day if missing
                if (currentDay == null) currentDay = "Giorno con allenamento"

                mealFor(line)?.let { meal ->
                    flushGroup(); currentMeal = meal; dinnerUsesLunch = false; return@forEachIndexed
                }
                
                // Heuristic: Auto-assign meal to BREAKFAST if parsing started without one
                if (currentMeal == null) currentMeal = MealType.BREAKFAST

                if (OPTION_MARKER.containsMatchIn(line)) { startGroup(); return@forEachIndexed }
                if (line == "+") return@forEachIndexed
                if (LUNCH_REFERENCE.containsMatchIn(line)) {
                    if (currentMeal == MealType.DINNER) {
                        dinnerUsesLunch = true
                        mealDraft()?.hasLunchAlternatives = true
                    }
                    return@forEachIndexed
                }
                
                parseFood(line)?.let { food ->
                    if (currentGroup == null) currentGroup = mutableListOf()
                    currentGroup?.add(food)
                }
                if (dinnerUsesLunch) mealDraft()?.hasLunchAlternatives = true
            }
            flushGroup()
        }

        return DietPlan(type = type, days = plans.map { (label, meals) ->
            val profile = when {
                label.contains("con allenamento", ignoreCase = true) -> DayProfileType.TRAINING
                label.contains("senza allenamento", ignoreCase = true) -> DayProfileType.REST
                else -> DayProfileType.UNKNOWN
            }
            DailyMeals(label, STRICT_MEAL_ORDER.mapNotNull { t -> meals[t]?.toMeal(t) }, profileType = profile)
        })
    }

    private fun detectPlanType(text: String): PlanType {
        val weekRegex = Regex("(?i)(?:^|\\s)(luned[iì]|marted[iì]|mercoled[iì]|gioved[iì]|venerd[iì]|sabato|domenica)(?:\\s|$)")
        val generalRegex = Regex("(?i)\\b(giorno\\s+con\\s+allenamento|giorno\\s+senza\\s+allenamento)\\b")
        var hasWeekly = false
        var hasGeneral = false
        text.lineSequence().forEach { line ->
            if (weekRegex.containsMatchIn(line)) hasWeekly = true
            if (generalRegex.containsMatchIn(line)) hasGeneral = true
        }
        return when {
            hasWeekly -> PlanType.WEEKLY
            hasGeneral -> PlanType.GENERAL_CHOICE
            else -> PlanType.UNKNOWN
        }
    }

    private fun normalize(input: String): String = input.replace(Regex("\\s+"), " ").trim()
    /**
     * OCR may split one food across visual rows.
     */
    private fun chunkOcrLines(text: String): List<String> {
        val source = text.lineSequence().map(::normalize).filter(String::isNotEmpty).toList()
        val chunks = mutableListOf<String>()
        var pending = ""
        fun flush() { if (pending.isNotBlank()) { chunks += pending; pending = "" } }
        source.forEach { raw ->
            val structural = dayFor(raw) != null || mealFor(raw) != null || OPTION_MARKER.containsMatchIn(raw) || raw == "+" || LUNCH_REFERENCE.containsMatchIn(raw)
            if (structural) { flush(); chunks += raw; return@forEach }
            val candidate = listOf(pending, raw).filter(String::isNotBlank).joinToString(" ")
            if (parseFood(candidate) != null) { flush(); chunks += candidate } else pending = candidate
        }
        flush()
        return chunks
    }
    private fun dayFor(line: String): String? {
        val weekMatch = Regex("(?i)(?:^|\\s)(luned[iì]|marted[iì]|mercoled[iì]|gioved[iì]|venerd[iì]|sabato|domenica)(?:\\s|$)").find(line)
        if (weekMatch != null) return weekMatch.groupValues[1].trim().replaceFirstChar { it.uppercase() }
        if (DAY_WITH_WORKOUT.containsMatchIn(line)) return "Giorno con allenamento"
        if (DAY_WITHOUT_WORKOUT.containsMatchIn(line)) return "Giorno senza allenamento"
        return null
    }
    private fun mealFor(line: String): MealType? = when {
        Regex("(?i)^(pre[- ]?workout|pre[- ]?allenamento|prewout)\\b").containsMatchIn(line) -> MealType.PRE_WORKOUT
        Regex("(?i)^(post[- ]?workout|post[- ]?allenamento)\\b").containsMatchIn(line) -> MealType.POST_WORKOUT
        Regex("(?i)^colazione\\b").containsMatchIn(line) -> MealType.BREAKFAST
        Regex("(?i)^spuntino\\s+mattutino\\b").containsMatchIn(line) -> MealType.MORNING_SNACK
        Regex("(?i)^pranzo\\b").containsMatchIn(line) -> MealType.LUNCH
        Regex("(?i)^(merenda|spuntino)\\b").containsMatchIn(line) -> MealType.SNACK
        Regex("(?i)^cena\\b").containsMatchIn(line) -> MealType.DINNER
        else -> null
    }
    private fun parseFood(input: String): FoodItem? {
        var line = input.replace(Regex("(?i)^oppure\\s*"), "").trim()
        if (line.isBlank() || LUNCH_REFERENCE.containsMatchIn(line)) return null
        if (line.lowercase().startsWith("note:")) return null
        
        var calories: Int? = null
        var proteinGrams: Float? = null
        var carbsGrams: Float? = null
        var fatGrams: Float? = null

        val kcalMatch = Regex("(?i)(\\d+(?:[.,]\\d+)?)\\s*(kcal|calorie)").find(line)
        if (kcalMatch != null) {
            calories = kcalMatch.groupValues[1].replace(',', '.').toFloatOrNull()?.toInt()
            line = line.replace(kcalMatch.value, "").trim()
        }
        
        val macroRegex = Regex("(?i)(\\d+(?:[.,]\\d+)?)\\s*g\\s*(pro|carbo|cho|grassi|fat|proteine)")
        macroRegex.findAll(line).forEach { match ->
            val value = match.groupValues[1].replace(',', '.').toFloatOrNull()
            val type = match.groupValues[2].lowercase()
            when {
                type.contains("pro") -> proteinGrams = value
                type.contains("carbo") || type.contains("cho") -> carbsGrams = value
                type.contains("grassi") || type.contains("fat") -> fatGrams = value
            }
            line = line.replace(match.value, "").trim()
        }

        // Cleanup any trailing hyphens or commas
        line = line.trim().trim('-', ':', ',').trim()

        val hit = QUANTITY_FIRST.matchEntire(line) ?: QUANTITY_LAST.matchEntire(line)
        if (hit != null) {
            val name = hit.groups["name"]?.value?.trim()?.trim('-', ':', ',')?.trim() ?: return null
            val quantity = "${hit.groups["quantity"]?.value.orEmpty()} ${hit.groups["unit"]?.value.orEmpty()}".trim()
            return name.takeIf { it.length > 1 }?.let { FoodItem(name = it, quantity = quantity, calories = calories, proteinGrams = proteinGrams, carbsGrams = carbsGrams, fatGrams = fatGrams) }
        }

        // Heuristic Fallback
        val heuristicHit = QUANTITY_FIRST_FIND.find(line) ?: QUANTITY_LAST_FIND.find(line)
        if (heuristicHit != null) {
            val name = heuristicHit.groups["name"]?.value?.trim()?.trim('-', ':', ',')?.trim() ?: return null
            val quantity = "${heuristicHit.groups["quantity"]?.value.orEmpty()} ${heuristicHit.groups["unit"]?.value.orEmpty()}".trim()
            return name.takeIf { it.length > 1 }?.let { FoodItem(name = it, quantity = quantity, calories = calories, proteinGrams = proteinGrams, carbsGrams = carbsGrams, fatGrams = fatGrams) }
        }

        // Catch-All Strategy: Se non ci sono grammature esatte (es. "Una mela", "Verdure a piacere"), 
        // cattura l'intera riga come alimento invece di scartarlo silenziosamente.
        return line.takeIf { it.length > 2 }?.let { FoodItem(name = it, quantity = "", calories = calories, proteinGrams = proteinGrams, carbsGrams = carbsGrams, fatGrams = fatGrams) }
    }

    private class MealDraft(val groups: MutableList<OptionGroup> = mutableListOf(), var hasLunchAlternatives: Boolean = false) {
        fun toMeal(type: MealType) = Meal(type, listOf(MealOption(groups = groups)), hasLunchAlternatives)
    }

    private companion object {
        val STRICT_MEAL_ORDER = listOf(MealType.PRE_WORKOUT, MealType.POST_WORKOUT, MealType.BREAKFAST, MealType.MORNING_SNACK, MealType.LUNCH, MealType.SNACK, MealType.DINNER)
        val DAY_WITH_WORKOUT = Regex("(?i).*\\bgiorno\\s+con\\s+allenamento\\b.*")
        val DAY_WITHOUT_WORKOUT = Regex("(?i).*\\bgiorno\\s+senza\\s+allenamento\\b.*")
        val OPTION_MARKER = Regex("(?i)\\bopzione\\s*\\d+\\b")
        val LUNCH_REFERENCE = Regex("(?i)vedi\\s+(le\\s+)?alternative\\s+del\\s+pranzo")
        val QUANTITY_FIRST = Regex("(?i)^(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)\\s+(?<name>.+)$")
        val QUANTITY_LAST = Regex("(?i)^(?<name>.+?)[\\s:–-]+(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)$")
        val QUANTITY_FIRST_FIND = Regex("(?i)(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)\\s+(?<name>[a-zA-Z].*)")
        val QUANTITY_LAST_FIND = Regex("(?i)(?<name>[a-zA-Z].*?)[\\s:–-]+(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)")
    }
}
