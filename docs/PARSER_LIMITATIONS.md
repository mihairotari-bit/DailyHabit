# Limitazioni Attuali del Parser (Legacy Deterministic)

L'attuale `DietParser` utilizza espressioni regolari e una state-machine esplicita (`DietLineClassifier`). Nonostante i miglioramenti della Milestone 1.9 che azzerano molti falsi positivi (email, recapiti, metadati), ci sono limiti strutturali.

## Limiti Strutturali
1. **Mancanza di Comprensione Semantica**: Il parser non "legge" il testo, ma cerca corrispondenze esatte e landmark strutturali. Se un medico usa una formulazione non prevista (es. "Al mattino consumare:"), il blocco fallisce.
2. **Grammatica Rigida (FoodItem)**: Riconosce solo `<Quantità> <Unità> <Nome>` o `<Nome> <Quantità> <Unità>`. Formati non convenzionali come "Un cucchiaio di olio" sfuggono senza una mappatura LLM.
3. **Impossibilità di Relazione Complessa**: Le alternative composte (es. "Opzione 1: A e B, oppure Opzione 2: C") vengono a volte appiattite se il marker non è esplicito.

## Soluzione Prevista (Milestone 2)
Il passaggio a **Gemma 4 (LiteRT-LM)** sostituirà la state machine con un prompt engineerizzato, delegando la comprensione del testo a un modello Edge capace di interpretare "semanticamente" le associazioni, pur restituendo uno schema JSON deterministico.
