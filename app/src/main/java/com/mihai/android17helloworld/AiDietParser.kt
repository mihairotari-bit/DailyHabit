package com.mihai.android17helloworld

import android.content.Context
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.GenerationConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiDietParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun parse(ocrText: String): DietPlan = withContext(Dispatchers.IO) {
        // Initialize Gemini Nano via AICore
        val config = GenerationConfig.builder().apply {
            context = this@AiDietParser.context
            temperature = 0.1f
        }.build()
        val model = GenerativeModel(config)

        val prompt = """
            Sei un esperto nutrizionista e un parser dati infallibile.
            Leggi attentamente il seguente testo estratto da un piano alimentare tramite OCR.
            Il tuo compito è estrarre le informazioni e restituirle ESATTAMENTE nel seguente formato JSON strutturato, senza alcun altro commento, testo o formattazione markdown. Solo puro JSON valido.

            SCHEMA JSON:
            {
              "title": "Titolo del piano",
              "type": "WEEKLY", // Usa WEEKLY se è diviso per giorni della settimana, GENERAL_CHOICE se è una singola lista da cui scegliere, UNKNOWN se non chiaro
              "days": [
                {
                  "day": "Nome del giorno (es. LUNEDÌ, oppure GIORNO CON ALLENAMENTO)",
                  "meals": [
                    {
                      "type": "BREAKFAST", // Uno tra: PRE_WORKOUT, POST_WORKOUT, BREAKFAST, LUNCH, SNACK, DINNER
                      "hasLunchAlternatives": false,
                      "groups": [
                        {
                          "alternatives": [
                            {
                              "name": "Nome alimento pulito",
                              "quantity": "Quantità e unità (es. 100g, 1 cucchiaio, a piacere, etc.)"
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            
            REGOLE IMPORTANTI:
            - Un "group" rappresenta un elemento obbligatorio del pasto.
            - Se ci sono opzioni ("oppure", "in alternativa"), mettile come multipli elementi dentro l'array "alternatives" dello stesso "group".
            - Se il pasto dice: "100g riso + 50g pollo", sono DUE "groups" separati, ciascuno con una singola "alternative".
            - Cerca di mappare `MealType` nel modo migliore possibile: Colazione -> BREAKFAST, Pranzo -> LUNCH, Cena -> DINNER, Spuntino/Merenda -> SNACK.
            - Non omettere nessun cibo o condimento.
            - Il JSON deve essere valido e non deve finire con una virgola. Non includere backtick (```).

            TESTO OCR DEL PIANO ALIMENTARE:
            $ocrText
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            var responseText = response.text ?: ""
            
            // Clean up possible markdown code blocks from Gemini
            if (responseText.startsWith("```json")) {
                responseText = responseText.removePrefix("```json")
            } else if (responseText.startsWith("```")) {
                responseText = responseText.removePrefix("```")
            }
            if (responseText.endsWith("```")) {
                responseText = responseText.removeSuffix("```")
            }
            responseText = responseText.trim()

            jsonParser.decodeFromString<DietPlan>(responseText)
        } finally {
            model.close()
        }
    }
}
