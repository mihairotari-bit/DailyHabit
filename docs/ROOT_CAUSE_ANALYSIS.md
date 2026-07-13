# Root Cause Analysis: Parser Noise and Missing Rest Lunch

## Symptom 1: Non-Food Items Parsed as Food
**Symptom:**
In the diet plan UI, contents such as nutritionist name, patient name, email, addresses, website, general notes, and narrative instructions (like cooking methods) are parsed and shown as `FoodItem` objects inside meals like Breakfast or Lunch.

**Technical Cause:**
The `DietParser` contains a `Catch-All Strategy` in `parseFood()`:
```kotlin
return line.takeIf { it.length > 2 }?.let { FoodItem(...) }
```
This heuristic assumes that any line longer than 2 characters that isn't a meal header, day header, or option marker is a food item with an empty quantity. Because the ML Kit OCR extracts the entire document line-by-line, including page headers/footers (e.g. "ELISABETTA MORONI", "PIANO NUTRIZIONALE"), and contact information (e.g. "email@example.com"), these lines bypass the strict quantity regexes and fall into the catch-all, turning into phantom food items.

**Selected Fix:**
Introduce a `DietTextPreprocessor` stage before `DietParser.parse`. The preprocessor will sanitize the input OCR string by identifying and stripping out lines that match non-food patterns:
- Contact info (emails, URLs, phone numbers)
- Specific keywords (e.g., "PIANO NUTRIZIONALE", "NOTE:", "CONDIMENTO:")
- Professional titles and generic descriptive paragraphs that lack any macro/food patterns.
We will also modify the `parseFood` method to explicitly ignore lines that don't look like food or contain known exclusion keywords.

## Symptom 2: Missing Lunch in "Giorno senza allenamento"
**Symptom:**
In the "Review Plan" screen, the Rest Day ("Giorno senza allenamento") fails to show "Pranzo".

**Technical Cause:**
The issue stems from two interacting factors:
1. **Noisy Text in Boundaries:** The real PDF OCR may insert headers, footers, or contact info right before or inside the "GIORNO SENZA ALLENAMENTO" block, or immediately after it.
2. **Greedy Regex / Split Error:** The split logic using `Regex("(?i)(.*giorno\\s+senza\\s+allenamento.*)")` uses `substring` at the `match.range.first`. If there are anomalies or if the meal type detection is missed due to missing newlines, the parser might fail to transition to `MealType.LUNCH`. 
3. **Implicit Meal Continuation:** Without strict boundaries, if "PRANZO" is missed or skipped because it's glued to another word, the items inside Lunch get swallowed by the preceding meal (BREAKFAST or MORNING_SNACK). If "PRANZO" is found but the foods fail to parse due to noise, the meal is empty.

**Selected Fix:**
- Improve the segment boundary logic. Instead of a single split, we will use robust text chunking based on Day Headers.
- `DietTextPreprocessor` will clean up headers/footers that interrupt the natural flow of Meals, ensuring that "PRANZO" sits on its own line clearly recognizable by `DietParser`.

**Residual Risks:**
- Highly aggressive filtering in `DietTextPreprocessor` might accidentally strip out a valid food item if its name triggers a heuristic (e.g., a food brand that resembles a URL or professional name). We must use relatively targeted patterns (e.g., `@` for emails, `http` for URLs, and exact keyword matches for known headers).
