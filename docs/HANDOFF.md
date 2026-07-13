# Hand-off

Questo documento serve a mantenere la continuità operativa in caso di cambio agente o pause dello sviluppo, garantendo che lo stato esatto del progetto, i problemi pendenti e le dipendenze siano sempre chiari.

## Ultimo Agente (Antigravity)
* **Data Inizio Fase:** 13 Luglio 2026
* **Branch Corrente:** `antigravity/litert-parser-v2`
* **Commit Base:** `338fd4ef6d7c8091eeeb40e92b993764c1bf1a88` (baseline-aicore-parser)

## Riassunto Ultime Modifiche
- Inizializzata infrastruttura per le policy degli agenti (`AGENTS.md`).
- Generata base documentale iniziale per l'handoff (`docs/`).
- Documentato esito ricerca WSL2 (`docs/RESEARCH_REPOSITORIES.md`).
- Creato Tracker delle Task locali all'agente.

## Risultato Build / Test
*Nessuna modifica strutturale al codice Android ancora applicata. La build corrente in `main` (AICore parser) era funzionante all'ultimo commit di partenza.*

## Problemi Aperti
- Occorre aggiornare `build.gradle.kts` e `libs.versions.toml` rimuovendo `aicore` e inserendo la nuova suite `litertlm-android` e `pdfbox-android`.
- Bisogna implementare il downloader di background del modello per gestire 1.3GB di file Gemma.

## Istruzioni per Codex / Altri Agenti
- Per qualsiasi intervento sul parsing, visionare l'interfaccia `DietInferenceEngine` che verrà sviluppata.
- Non reinserire AICore (che è hard-deprecated per noi a favore di LiteRT-LM in spazio utente).
