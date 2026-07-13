# Project Status

* **Data:** 13 Luglio 2026
* **Branch Corrente:** `antigravity/litert-parser-v2`
* **Commit Base:** `338fd4ef6d7c8091eeeb40e92b993764c1bf1a88`
* **Build Status:** Compilazione completata con successo
* **Unit Test Status:** Pendente (FakeEngine parziale)
* **Pixel 9 Test Status:** Smoke test completato per rinomina pacchetto. Nessun crash.

## Ambiente Target
* **Android API:** 37.1 Canary (compileSdk 37)
* **Backend Inferenzia:** LiteRT-LM (Local)
* **Modello Installato:** Nessuno (Infrastruttura di runtime download in costruzione)
* **LLM Previsto:** Gemma 4 E2B IT

## Milestone Corrente: "Milestone 2 - Download Manager & SHA Verification"

### Funzionalità Completate (Milestone 0 & 1)
- [x] Inizializzazione Branching e Baseline.
- [x] Raccolta dati repository di ricerca e baseline architetturale.
- [x] Setup Governance (Documentazione, AGENTS.md, HANDOFF.md).
- [x] Rinomina `applicationId` da `com.mihai.android17helloworld` a `com.mihai.dailyhabit`.
- [x] Aggiornamento `libs.versions.toml` e sostituzione `aicore` con `litertlm`, aggiunta `pdfbox` e `workmanager`.
- [x] Rimozione `AiDietParser` originale in favore di `DietInferenceEngine` + `FakeDietInferenceEngine`.
- [x] Generazione Draft PR documentazione.
- [x] Esecuzione primo Build & Smoke Test su Pixel 9. La vecchia app e il vecchio DB (`diet-plans.db`) rimangono integri a fianco della nuova build.

### Funzionalità Incomplete (In lavorazione)
- [ ] Motore di Download HTTP Resumable per il modello (Milestone 2).
- [ ] Validazione SHA-256 e archiviazione nel SAF (Storage Access Framework).
- [ ] Pipeline multi-layer OCR (PdfBox -> MLKit).
- [ ] LiteRtInferenceEngine C++ API per Gemma 4 E2B IT.
- [ ] Refactoring UI.

### Blocchi
- Nessuno.

### Prossimo Passo Esatto
- Sviluppo del Download Manager (scaricamento modello `gemma-4-E2B-it.litertlm` ~2.5GB). Implementare logica resumable e validazione hash.
