package com.mihai.dailyhabit

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietParser @Inject constructor(
    private val preprocessor: DietTextPreprocessor,
    private val classifier: DietLineClassifier,
    private val tokenizer: DietStructureTokenizer
) {
    fun parse(input: DietInferenceInput): DietPlan {
        val rawTokens = tokenizer.tokenize(input.rawText)
        val fullTextAfterTokenization = rawTokens.joinToString("\n")
        val sanitizedText = preprocessor.preprocess(fullTextAfterTokenization)
        val tokens = sanitizedText.split("\n").filter { it.isNotBlank() }
        
        var totalLines = tokens.size
        var discardedLines = 0
        var foodsExtracted = 0
        var unknownLines = 0
        
        val type = detectPlanType(tokens.joinToString("\n"))
        
        // Find Day Profiles
        val dayIndices = mutableListOf<Pair<String, Int>>()
        for ((i, token) in tokens.withIndex()) {
            val lower = token.lowercase()
            if (DAY_WITH_WORKOUT.matches(lower)) dayIndices.add("Giorno con allenamento" to i)
            else if (DAY_WITHOUT_WORKOUT.matches(lower)) dayIndices.add("Giorno senza allenamento" to i)
            else {
                val weekMatch = Regex("(?i)^(luned[iì]|marted[iì]|mercoled[iì]|gioved[iì]|venerd[iì]|sabato|domenica)$").find(token)
                if (weekMatch != null) dayIndices.add(weekMatch.groupValues[1].replaceFirstChar { it.uppercase() } to i)
            }
        }
        
        if (dayIndices.isEmpty()) {
            dayIndices.add("Piano Singolo" to 0) // Fallback if no day found
        }
        
        println("TOKENS: $tokens"); println("DAY INDICES: $dayIndices"); val plans = linkedMapOf<String, LinkedHashMap<MealType, MealDraft>>()
        
        for (d in 0 until dayIndices.size) {
            val (dayName, startIdx) = dayIndices[d]
            val endIdx = if (d + 1 < dayIndices.size) dayIndices[d + 1].second else tokens.size
            
            val dayTokens = tokens.subList(startIdx, endIdx)
            
            // Find Meal Headers within this Day
            val mealIndices = mutableListOf<Pair<MealType, Int>>()
            for ((i, token) in dayTokens.withIndex()) {
                val meal = mealFor(token)
                if (meal != null) {
                    mealIndices.add(meal to i)
                }
            }
            
            for (m in 0 until mealIndices.size) {
                val (mealType, mStart) = mealIndices[m]
                val mEnd = if (m + 1 < mealIndices.size) mealIndices[m + 1].second else dayTokens.size
                
                val mealTokens = dayTokens.subList(mStart + 1, mEnd) // Exclude the header itself
                
                val draft = plans.getOrPut(dayName) { linkedMapOf() }.getOrPut(mealType) { MealDraft() }
                
                var currentOption: MutableList<MutableList<FoodItem>>? = null
                var currentGroup: MutableList<FoodItem>? = null
                
                fun flushGroup() {
                    if (!currentGroup.isNullOrEmpty()) {
                        if (currentOption == null) {
                            currentOption = mutableListOf()
                            draft.options.add(currentOption!!)
                        }
                        currentOption!!.add(currentGroup!!.toMutableList())
                        currentGroup = null
                    }
                }
                
                fun startOption() {
                    flushGroup()
                    currentOption = mutableListOf()
                    draft.options.add(currentOption!!)
                }
                
                fun startGroup() {
                    flushGroup()
                    currentGroup = mutableListOf()
                }
                
                for (token in mealTokens) {
                    if (classifier.isLunchReference(token.lowercase())) {
                        draft.hasLunchAlternatives = true; println("LUNCH REF FOUND IN TOKEN: \$token for Meal: \$currentMealType")
                    }
                    val kind = classifier.classify(token)
                    if (kind == ParsedLineKind.UNKNOWN_NOISE) { unknownLines++; discardedLines++; continue }
                    
                    if (kind == ParsedLineKind.OPTION_MARKER || OPTION_MARKER.containsMatchIn(token)) {
                        startOption()
                        continue
                    }
                    if (kind == ParsedLineKind.GROUP_MARKER || token == "+") {
                        startGroup()
                        continue
                    }
                    if (kind == ParsedLineKind.LUNCH_REFERENCE) {
                        continue // Already set hasLunchAlternatives
                    }
                    
                    if (kind == ParsedLineKind.FOOD_CANDIDATE) {
                        val food = parseFood(token)
                        if (food != null) {
                            foodsExtracted++
                            if (currentGroup == null) currentGroup = mutableListOf()
                            currentGroup!!.add(food)
                        } else {
                            discardedLines++
                        }
                    } else {
                        discardedLines++
                    }
                }
                flushGroup()
            }
        }
        
        val restDay = plans.entries.find { it.key.contains("senza allenamento", ignoreCase = true) }?.value
        val restLunch = restDay?.get(MealType.LUNCH)
        val restLunchOptions = restLunch?.options?.size ?: 0
        val restLunchGroups = restLunch?.options?.sumOf { it.size } ?: 0
        val restLunchFoods = restLunch?.options?.sumOf { it.sumOf { g -> g.size } } ?: 0

        val report = ParseReport(
            extractionMethod = input.extractionMethod,
            nativeCharacterCount = input.pages.sumOf { it.text.length },
            totalLines = totalLines,
            discardedLines = discardedLines,
            foodsExtracted = foodsExtracted,
            unknownLines = unknownLines,
            daysFound = plans.size,
            mealsFound = plans.values.sumOf { it.size },
            restProfileDetected = restDay != null,
            restLunchHeaderDetected = restLunch != null,
            restLunchOptionCount = restLunchOptions,
            restLunchGroupCount = restLunchGroups,
            restLunchFoodCount = restLunchFoods,
            warnings = emptyList()
        )

        return DietPlan(
            type = type,
            parseReport = report,
            days = plans.map { (label, meals) ->
                val profile = when {
                    label.contains("con allenamento", ignoreCase = true) -> DayProfileType.TRAINING
                    label.contains("senza allenamento", ignoreCase = true) -> DayProfileType.REST
                    else -> DayProfileType.UNKNOWN
                }
                DailyMeals(label, STRICT_MEAL_ORDER.mapNotNull { t -> meals[t]?.toMeal(t) }, profileType = profile)
            }
        )
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
        if (line.isBlank() || classifier.classify(line) == ParsedLineKind.LUNCH_REFERENCE) return null
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

        line = line.trim().trim('-', ':', ',').trim()

        val hit = QUANTITY_FIRST.matchEntire(line) ?: QUANTITY_LAST.matchEntire(line)
        if (hit != null) {
            val name = hit.groups["name"]?.value?.trim()?.trim('-', ':', ',')?.trim() ?: return null
            val quantity = "${hit.groups["quantity"]?.value.orEmpty()} ${hit.groups["unit"]?.value.orEmpty()}".trim()
            return name.takeIf { it.length > 1 }?.let { FoodItem(name = it, quantity = quantity, calories = calories, proteinGrams = proteinGrams, carbsGrams = carbsGrams, fatGrams = fatGrams) }
        }

        val heuristicHit = QUANTITY_FIRST_FIND.find(line) ?: QUANTITY_LAST_FIND.find(line)
        if (heuristicHit != null) {
            val name = heuristicHit.groups["name"]?.value?.trim()?.trim('-', ':', ',')?.trim() ?: return null
            val quantity = "${heuristicHit.groups["quantity"]?.value.orEmpty()} ${heuristicHit.groups["unit"]?.value.orEmpty()}".trim()
            return name.takeIf { it.length > 1 }?.let { FoodItem(name = it, quantity = quantity, calories = calories, proteinGrams = proteinGrams, carbsGrams = carbsGrams, fatGrams = fatGrams) }
        }

        return null
    }

    private class MealDraft(var hasLunchAlternatives: Boolean = false) {
        val options: MutableList<MutableList<MutableList<FoodItem>>> = mutableListOf()
        fun toMeal(type: MealType): Meal {
            val mealOptions = options.map { opt ->
                MealOption(groups = opt.map { grp -> OptionGroup(alternatives = grp.toList()) })
            }.filter { it.groups.isNotEmpty() }
            return Meal(type, mealOptions, hasLunchAlternatives)
        }
    }

    private companion object {
        val STRICT_MEAL_ORDER = listOf(MealType.PRE_WORKOUT, MealType.POST_WORKOUT, MealType.BREAKFAST, MealType.MORNING_SNACK, MealType.LUNCH, MealType.SNACK, MealType.DINNER)
        val DAY_WITH_WORKOUT = Regex("(?i)^giorno\\s+con\\s+allenamento$")
        val DAY_WITHOUT_WORKOUT = Regex("(?i)^giorno\\s+senza\\s+allenamento$")
        val OPTION_MARKER = Regex("(?i)^opzione\\s*\\d+$")
        val QUANTITY_FIRST = Regex("(?i)^(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)\\s+(?<name>.+)$")
        val QUANTITY_LAST = Regex("(?i)^(?<name>.+?)[\\s:–-]+(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)$")
        val QUANTITY_FIRST_FIND = Regex("(?i)(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)\\s+(?<name>[a-zA-Z].*)")
        val QUANTITY_LAST_FIND = Regex("(?i)(?<name>[a-zA-Z].*?)[\\s:–-]+(?<quantity>\\d+(?:[,.]\\d+)?)\\s*(?<unit>g|gr|ml|pz|pezzi?)")
    }
}
