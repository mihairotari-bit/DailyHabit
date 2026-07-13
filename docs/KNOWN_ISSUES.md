# DailyHabit V2 - Known Issues & Constraints

## 1. LiteRT-LM & ModelDownloadManager (Milestone 2 Pending)
- **Status:** SOSPESO. 
- L'integrazione del modello Gemma 4 E2B via LiteRT-LM (Milestone 2) è stata sospesa a causa di regressioni critiche identificate nella Milestone 1 (gestione dei piani legacy deterministici e giorni falsati).
- **Prossimo Step:** La Milestone 2 ripartirà non appena queste build verranno ampiamente verificate dagli sviluppatori e su field. Il download a runtime (1.3GB-2.5GB) è pronto in teoria ma non ancora collegato alla UI per evitare out-of-memory o ANR non tracciati.

## 2. Hilt / Dagger UI Testing
- **Status:** PENDING (`ProductionEngineBindingTest.kt`)
- Attualmente non c'è `hilt-android-testing` integrato nel file gradle. Il test di integrazione per verificare che il binding di produzione usi la classe corretta in androidTest è commentato (`@Ignore`) per non bloccare la CI/build. Va risolto aggiungendo le librerie Hilt appropriate.

## 3. UI Empty State Fallback per i profili
- **Status:** DA MIGLIORARE (UX)
- Abbiamo implementato un blocco che impedisce il crash se non viene trovato un `DailyMeals` valido con match semantico, restituendo l'utente alla schermata di review. L'UI di "Rivedi piano" ora gestisce parzialmente lo stato d'errore ma le card potrebbero richiedere ulteriori rifiniture in Compose per indicare visivamente i giorni in cui i pasti mancano.

## 4. OCR e Line-Breaks su PdfBox
- **Status:** KNOWN LIMITATION
- L'OCR base a volte unisce testi che sono su righe separate dipendendo dai margini. Il chunker documentale in `DietParser.kt` mitiga questo usando euristiche. Solo il passaggio a modelli `LiteRT` risolverà i disallineamenti di Layout complessi senza dipendere dal chunking deterministico.
