# Preparazione per LiteRT-LM (Milestone 2)

La Milestone 2 vedrà l'introduzione di LiteRT e Gemma 4 E2B in esecuzione totalmente on-device per ovviare ai limiti del parser deterministico.

## Moduli Architetturali da Sviluppare

1. **ModelDownloadManager**:
    - Download riprendibile e progress bar.
    - Verifica hash SHA-256 del binario.
    - Gestione dello spazio di storage.
2. **Inizializzazione Motore LiteRT-LM**:
    - Allocazione pesi in memoria e offload dinamico (GPU first con CPU fallback).
    - Lifecycle (non bloccare la UI, coroutines bound al ViewModel o ForegroundService per task lunghi).
3. **Structured Output**:
    - Estrazione JSON tipizzato dal prompt.
    - Mapping diretto al data class `DietPlan`.
4. **Fallback Deterministico Sicuro**:
    - Se LiteRT va in OOM o genera eccezioni, richiamare il Legacy Parser per evitare bricking dell'uso base dell'app.
5. **UI dedicata**:
    - `ModelDownloadScreen` per mostrare i progressi di estrazione pesi.
    - Indicatore del parser corrente utilizzato per l'inferenza.
