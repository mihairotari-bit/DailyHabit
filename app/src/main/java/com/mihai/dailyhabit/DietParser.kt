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
        val tokens = sanitizedText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        
        var totalLines = tokens.size
        var discardedLines = 0
        var foodsExtracted = 0
        var unknownLines = 0
        
        val type = detectPlanType(tokens.joinToString("\n"))
        
        val days = mutableListOf<DailyMeals>()
        
        // Split Document into Day Blocks
        val dayBlocks = splitIntoDayBlocks(tokens)
        
        var restProfileDetected = false
        var restLunchHeaderDetected = false
        var restLunchBlockStart = false
        var restLunchOptionCount = 0
        var restLunchGroupCount = 0
        var restLunchFoodCount = 0

        for ((dayName, dayTokens) in dayBlocks) {
            val isRest = dayName.contains("senza allenamento", ignoreCase = true)
            val isTraining = dayName.contains("con allenamento", ignoreCase = true)
            if (isRest) restProfileDetected = true
            
            val profile = when {
                isTraining -> DayProfileType.TRAINING
                isRest -> DayProfileType.REST
                else -> DayProfileType.UNKNOWN
            }
            
            // Split Day Block into Meal Blocks
            val mealBlocks = splitIntoMealBlocks(dayTokens)
            
            val parsedMeals = mutableListOf<Meal>()
            
            for ((mealType, mealTokens) in mealBlocks) {
                if (isRest && mealType == MealType.LUNCH) {
                    restLunchHeaderDetected = true
                    if (mealTokens.isNotEmpty()) restLunchBlockStart = true
                }
                
                var hasLunchAlternatives = false
                val options = mutableListOf<MealOption>()
                
                var currentOptionGroups = mutableListOf<OptionGroup>()
                var currentGroupFoods = mutableListOf<FoodItem>()
                
                fun flushGroup() {
                    if (currentGroupFoods.isNotEmpty()) {
                        currentOptionGroups.add(OptionGroup(alternatives = currentGroupFoods.toList()))
                        currentGroupFoods.clear()
                    }
                }
                
                fun flushOption() {
                    flushGroup()
                    if (currentOptionGroups.isNotEmpty()) {
                        options.add(MealOption(groups = currentOptionGroups.toList()))
                        currentOptionGroups.clear()
                    }
                }
                
                for (token in mealTokens) {
                    if (classifier.isLunchReference(token.lowercase())) {
                        hasLunchAlternatives = true
                    }
                    val kind = classifier.classify(token)
                    if (kind == ParsedLineKind.UNKNOWN_NOISE) { unknownLines++; discardedLines++; continue }
                    
                    if (kind == ParsedLineKind.OPTION_MARKER || OPTION_MARKER.containsMatchIn(token)) {
                        flushOption()
                        continue
                    }
                    if (kind == ParsedLineKind.GROUP_MARKER || token == "+") {
                        flushGroup()
                        continue
                    }
                    if (kind == ParsedLineKind.LUNCH_REFERENCE) {
                        continue
                    }
                    
                    if (kind == ParsedLineKind.FOOD_CANDIDATE) {
                        val food = parseFood(token)
                        if (food != null) {
                            foodsExtracted++
                            currentGroupFoods.add(food)
                        } else {
                            discardedLines++
                        }
                    } else {
                        discardedLines++
                    }
                }
                flushOption() // Flush any remaining items in the last meal
                
                if (options.isNotEmpty() || hasLunchAlternatives) {
                    if (isRest && mealType == MealType.LUNCH) {
                        restLunchOptionCount = options.size
                        restLunchGroupCount = options.sumOf { it.groups.size }
                        restLunchFoodCount = options.sumOf { it.groups.sumOf { g -> g.alternatives.size } }
                    }
                    parsedMeals.add(Meal(mealType, options, hasLunchAlternatives))
                }
            }
            days.add(DailyMeals(dayName, parsedMeals, profileType = profile))
        }

        val warningsList = mutableListOf<String>()
        if (days.isEmpty()) {
            warningsList.add("DAY_PROFILE_NOT_FOUND")
        }

        val report = ParseReport(
            extractionMethod = input.extractionMethod,
            nativeCharacterCount = input.pages.sumOf { it.text.length },
            totalLines = totalLines,
            discardedLines = discardedLines,
            foodsExtracted = foodsExtracted,
            unknownLines = unknownLines,
            daysFound = days.size,
            mealsFound = days.sumOf { it.meals.size },
            restProfileDetected = restProfileDetected,
            restLunchHeaderDetected = restLunchHeaderDetected,
            restLunchBlockStart = restLunchBlockStart,
            restLunchOptionCount = restLunchOptionCount,
            restLunchGroupCount = restLunchGroupCount,
            restLunchFoodCount = restLunchFoodCount,
            ocrFallbackPageCount = input.pages.count { !it.isNativeValid },
            warnings = warningsList
        )

        return DietPlan(
            type = type,
            parseReport = report,
            days = days
        )
    }
    
    private fun splitIntoDayBlocks(tokens: List<String>): List<Pair<String, List<String>>> {
        val blocks = mutableListOf<Pair<String, List<String>>>()
        var currentDay: String? = null
        var currentTokens = mutableListOf<String>()
        
        for (token in tokens) {
            val lower = token.lowercase()
            val weekMatch = Regex("(?i)^(luned[iì]|marted[iì]|mercoled[iì]|gioved[iì]|venerd[iì]|sabato|domenica)$").find(token)
            
            if (DAY_WITH_WORKOUT.matches(lower)) {
                if (currentDay != null) blocks.add(currentDay to currentTokens.toList())
                currentDay = "Giorno con allenamento"
                currentTokens.clear()
            } else if (DAY_WITHOUT_WORKOUT.matches(lower)) {
                if (currentDay != null) blocks.add(currentDay to currentTokens.toList())
                currentDay = "Giorno senza allenamento"
                currentTokens.clear()
            } else if (weekMatch != null) {
                if (currentDay != null) blocks.add(currentDay to currentTokens.toList())
                currentDay = weekMatch.groupValues[1].replaceFirstChar { it.uppercase() }
                currentTokens.clear()
            } else {
                if (currentDay != null) {
                    currentTokens.add(token)
                }
            }
        }
        if (currentDay != null) {
            blocks.add(currentDay to currentTokens.toList())
        }
        return blocks
    }
    
    private fun splitIntoMealBlocks(tokens: List<String>): List<Pair<MealType, List<String>>> {
        val blocks = mutableListOf<Pair<MealType, List<String>>>()
        var currentMeal: MealType? = null
        var currentTokens = mutableListOf<String>()
        
        for (token in tokens) {
            val meal = mealFor(token)
            if (meal != null) {
                if (currentMeal != null) {
                    blocks.add(currentMeal to currentTokens.toList())
                }
                currentMeal = meal
                currentTokens.clear()
            } else {
                if (currentMeal != null) {
                    currentTokens.add(token)
                }
            }
        }
        if (currentMeal != null) {
            blocks.add(currentMeal to currentTokens.toList())
        }
        return blocks.sortedBy { STRICT_MEAL_ORDER.indexOf(it.first) }
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
