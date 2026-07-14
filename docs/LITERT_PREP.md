# LiteRT-LM Migration Preparation (Milestone 1.9)

Questo documento traccia i prerequisiti completati in vista della Milestone 2 (integrazione di Gemma 4 via LiteRT).

## Stato Attuale
1. **Parser Engine Isolation**: L'interfaccia `DietInferenceEngine` è il contratto esclusivo.
2. **Parser Scaffolding**: È stato creato il placeholder `LiteRtDietInferenceEngine`.
3. **Data Model**: `ParserEngine.LITERT_GEMMA4_E2B` è già definito e supportato dal database e serializzatore.
4. **App Core**: La logica legacy usa `DietParser` e `LegacyDeterministicDietInferenceEngine`, ma è strutturalmente intercambiabile.

## Prossimi Passi (Milestone 2)
1. **Model Download Manager**: Creare il servizio di download (Foreground Service) per Gemma 4 (circa 1.5GB/2GB per la versione 2B).
2. **Task Inizializzazione**: Utilizzare le API Edge/LiteRT per caricare i pesi e creare la pipeline di prompt.
3. **Prompt Engineering**: Progettare un prompt robusto a singola passata che estragga la medesima struttura canonica del DietPlan da un testo documentale OCR.
