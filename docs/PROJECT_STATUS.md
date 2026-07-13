# Project Status

* **Data:** 13 Luglio 2026
* **Branch Corrente:** `antigravity/litert-parser-v2`
* **Commit Base:** `338fd4ef6d7c8091eeeb40e92b993764c1bf1a88`
* **Build Status:** Non determinato (In fase di migrazione dipendenze)
* **Unit Test Status:** Pendente
* **Pixel 9 Test Status:** Pendente

## Ambiente Target
* **Android API:** 37.1 Canary (compileSdk 37)
* **Backend Inferenzia:** LiteRT-LM (Local)
* **Modello Installato:** Nessuno (Infrastruttura di runtime download in costruzione)
* **LLM Previsto:** Gemma 4 E2B IT

## Milestone Corrente: "LiteRT-LM & Architecture Overhaul"

### Funzionalità Completate
- [x] Inizializzazione Branching e Baseline.
- [x] Raccolta dati repository di ricerca e baseline architetturale.
- [x] Setup Governance (Documentazione, AGENTS.md, HANDOFF.md).

### Funzionalità Incomplete (In lavorazione)
- [ ] Aggiornamento `libs.versions.toml` con Google Maven LiteRT-LM.
- [ ] Motore di Download HTTP Resumable per il modello.
- [ ] Componenti Base Clean Architecture (Feature Modules o simili).
- [ ] Pipeline multi-layer OCR (PdfBox -> MLKit).
- [ ] LiteRtInferenceEngine.
- [ ] Refactoring UI.

### Blocchi
- Nessuno attualmente. Attendere di testare l'inizializzazione GPU/CPU con LiteRT-LM.

### Prossimo Passo Esatto
- Identificare la versione esatta di `com.google.ai.edge.litertlm:litertlm-android` e inserirla nel file TOML. Rimuovere i riferimenti obsoleti ad `aicore`. Configurare Gradle per Build ARM64 compatibile.
