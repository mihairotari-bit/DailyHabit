# Changelog Development

## [Unreleased]
### Added
- Documentazione architetturale inziale (`PROJECT_STATUS.md`, `HANDOFF.md`, `TEST_RESULTS.md`, `PULL_REQUEST.md`).
- Interfaccia `DietInferenceEngine` e `FakeDietInferenceEngine` per isolare il vecchio parsing da quello nuovo.
- `pdfbox.android`, `work.runtime.ktx`, `litertlm` (version catalog aggiornato).

### Changed
- **ApplicationId**: Cambiato pacchetto da `com.mihai.android17helloworld` a `com.mihai.dailyhabit`. Tutte le classi, import, package statement e manifest sono stati rinominati.
- **Dependency**: Sostituita la libreria sperimentale `aicore` con `litertlm-android:0.14.0`.
- **UI**: Rimosso riferimento ad `AiDietParser` fallace.

### Removed
- `AiDietParser.kt` e il mock obsoleto precedentemente accoppiato ad `aicore`.

## [Baseline] - 2026-07-13
- Tag: `baseline-aicore-parser` (Commit: 338fd4e)
- Stato di partenza del progetto (Applicazione con package android17helloworld, uso di Gemini Nano sperimentale, parse tramite Regex rigide).
