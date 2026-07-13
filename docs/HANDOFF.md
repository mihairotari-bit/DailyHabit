# Handoff Document per Codex/Sviluppatori

## Stato Attuale
Abbiamo completato la **Milestone 1**. L'App è stata rinominata e compila senza errori con l'engine mockato (`FakeDietInferenceEngine`). 
Il parser AICore è stato definitivamente rimosso a favore di una interfaccia `DietInferenceEngine` più robusta e agnostica. 

Lo **Smoke Test** su Pixel 9 ha confermato che:
- La build gira regolarmente senza crash immediati.
- Il database SQLite della precedente incarnazione (`com.mihai.android17helloworld`) rimane al sicuro e non sovrascritto.

Siamo pronti per aggredire la **Milestone 2**.

## Modelli Target
- **Modello:** `litert-community/gemma-4-E2B-it-litert-lm`
- **Nome File Reale:** `gemma-4-E2B-it.litertlm`
- **SHA-256 (File LFS):** `181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c`
- **Dimensione Esatta:** `2588147712` byte (circa 2.47 GB effettivi).

## Direttive Immediatate per il prossimo Turno (Milestone 2)
Costruire il `ModelDownloadManager`.

1. **WorkManager vs Foreground Service**: Decidi la tecnologia (consigliato `WorkManager` con `ForegroundInfo` per notifiche e download resiliente).
2. **Hash Check**: Integrare nel downloader una funzione che legge in streaming il file salvato e ne verifica il SHA-256, rigettandolo se corrotto.
3. **Storage Access**: Salvare il file preferibilmente in `Context.getExternalFilesDir()` per permettere backup o visibilità se debug.

## Limitazioni / Regole d'oro in vigore
- Nessun download bloccante nel Main Thread.
- Non pacchettizzare modelli nell'APK.
- Non caricare il modello interamente in RAM byte-array per la verifica hash. Usa gli InputStream bufferizzati.
- Mai usare l'NPU per questo task sul Tensor G4/G5 finché il LiteRT-LM C++ wrapper non lo supporterà esplicitamente in modo stabile. Solo GPU/CPU.
