# Draft: DailyHabit V2 — LiteRT-LM local-first parser architecture

## Obiettivo
Migrare il core del parser dell'app Android da AICore (sperimentale, non vincolato) a **LiteRT-LM** (Google AI Edge) garantendo un'architettura 100% on-device, privacy-first e robusta per processare piani alimentari PDF tramite Gemma 4 E2B IT.

## Baseline
- **ApplicationId Iniziale:** `com.mihai.android17helloworld`
- **Motore di inferenza precedente:** `com.google.ai.edge.aicore`
- **UI:** Compose-first minimale.
- Il pacchetto originale è stato conservato sul device per evitare la cancellazione dei database Room locali (`diet-plans.db`).

## Architettura Proposta
1. **Model Management**: Download asincrono via `WorkManager` (o similare) in background, validazione SHA-256 e storage in `app-specific storage`. Il modello NON è pacchettizzato nell'APK per rientrare nei limiti del Play Store.
2. **Text Extraction**: Layer vettoriale `PdfBox-Android` (testo nativo) con fallback su raster OCR `ML Kit Text Recognition` (bundled offline).
3. **Inference**: Esecuzione locale via `LiteRT-LM` C++ JNI (`LlmInference`), con primary backend **GPU** (OpenCL/Vulkan) e fallback **CPU** (XNNPack).
4. **Data Reconciliation**: Validatore JSON che converte l'output generativo di Gemma nelle strutture Room (`DietPlan`, `DailyMeals`, ecc.).

## Milestone e Stato
- [x] **Milestone 0:** Inizializzazione Repository, Baseline Tagging e Architettura Documentale (`AGENTS.md`, `HANDOFF.md`, ADRs).
- [x] **Milestone 1:** Refactoring `ApplicationId` (`com.mihai.dailyhabit`), aggiornamento librerie (`pdfbox`, `mlkit bundled`, `litertlm`) e smoke test fisico sul device.
- [ ] **Milestone 2:** Implementazione Download Manager (Resumable HTTP) e validazione SHA-256 per `gemma-4-E2B-it.litertlm` (2.58 GB).
- [ ] **Milestone 3:** Integrazione LiteRT-LM e configurazione C++ JNI per GPU delegate.
- [ ] **Milestone 4:** Sviluppo della Pipeline di estrazione Testo (PdfBox + OCR).
- [ ] **Milestone 5:** Esecuzione reale con FakeDietEngine (Unit Testing).
- [ ] **Milestone 6:** Validazione sul Device Fisico (Pixel 9) con Gemma 4 E2B reale.

## Commit Completati
- `338fd4e` Initial commit with Gemini Nano LLM parser
- `f3d7700` docs: audit baseline before LiteRT migration
- [IN CORSO] refactor: rename application package to com.mihai.dailyhabit

## Test Eseguiti
- [x] Compilazione con Android API 37 Canary.
- [x] Smoke test e Application ID rename validation su Pixel 9.

## Test Ancora Mancanti
- Download Manager Resilience Test.
- Parsing deterministico da JSON a Oggetti Room.
- LiteRT-LM LLM Inference accuracy & latency tests su GPU.

## Rischi e Known Issues
- **Memoria:** L'istanza di Gemma 4 E2B richiede ~2.5GB di RAM bloccata per i pesi. Rischio di evizione da parte del Low Memory Killer (LMK).
- **Page Size:** L'app deve supportare la paginazione 16KB per poter girare sulle ultime Android 15/16/17 QPR.
- **Data Migration:** Da implementare export/import dati per trasferire `diet-plans.db` dalla vecchia app.

## Checklist Pre-Merge
- [ ] L'app non crasha al caricamento del modello.
- [ ] Il test fisico sul Pixel 9 mostra output parsato corretto al 100%.
- [ ] Tutto il codice è coperto da test `FakeEngine`.
- [ ] Memoria allocata stabile (< 3.5GB heap runtime).

## Istruzioni per Codex (Handoff)
1. Fai sempre riferimento a `docs/HANDOFF.md` per lo stato preciso.
2. Questa PR rimarrà in DRAFT per tutta la durata del lavoro. Aggiornala incrementalmente alla fine di ogni Milestone tramite `gh pr edit`.
3. Non rimuovere i file di documentazione architetturale (`DECISIONS.md`, `AGENTS.md`).

---
_Ultimo SHA del branch:_ `(aggiornato via script)`
