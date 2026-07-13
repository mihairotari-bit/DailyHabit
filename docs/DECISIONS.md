# Architectural Decisions Record (ADR)

Questo documento registra tutte le decisioni architetturali importanti e vincolanti del progetto.

## ADR-001: LiteRT-LM invece di AICore
**Data:** 13 Luglio 2026
**Stato:** Approvato
**Decisione:** Abbandonare il servizio di sistema sperimentale `com.google.ai.edge.aicore:aicore` a favore della libreria in space-user `com.google.ai.edge.litertlm:litertlm-android`.
**Motivazione:** L'implementazione AICore attuale invia tutto il raw text al sistema operativo senza vincoli stringenti. LiteRT-LM offre una distribuzione esplicita del modello runtime tramite `context.getExternalFilesDir()`, non dipende dagli aggiornamenti OS OTA, supporta GPU nativamente tramite OpenCL/Vulkan e ci svincola dall'AOSP system layer.

## ADR-002: Testo PDF nativo prima dell'OCR
**Data:** 13 Luglio 2026
**Stato:** Approvato
**Decisione:** Usare `PdfBox-Android` come primo strato di estrazione.
**Motivazione:** Se un piano alimentare non è scannerizzato, estrarre il testo vettoriale previene errori di bounding box fusion tipici di ML Kit. Solo se la pagina fallisce, faremo fallback al rendering raster + ML Kit.

## ADR-003: Posticipo Rinominazione ApplicationID
**Data:** 13 Luglio 2026
**Stato:** Approvato
**Decisione:** L'app conserverà il pacchetto `com.mihai.android17helloworld` per la durata di questa milestone. 
**Motivazione:** Una ispezione ADB al dispositivo fisico (Pixel 9) ha rilevato che esistono già dati utente (`diet-plans.db`) associati all'applicationId `com.mihai.android17helloworld`. Rinominare l'applicationId in `com.mihai.dailyhabit` in questo momento provocherebbe una nuova installazione Android vuota, con conseguente perdita irreversibile di tutti i vecchi piani alimentari locali salvati. La rinominazione viene posticipata al momento in cui introdurremo una feature di Data Export/Import.

## ADR-004: Modello Gemma 4 E2B scaricato al primo avvio
**Data:** 13 Luglio 2026
**Stato:** Approvato
**Decisione:** Il file `.litertlm` (circa 1.3GB) non verrà mai buildato dentro l'APK.
**Motivazione:** Ridurre il tempo di loop sviluppo (assembleDebug non deve spostare 1.3GB), rispettare le policy massime di Play Store e consentire upgrade OTA (Over The Air) dei pesi LLM svincolati dal release engine dell'applicazione.

## ADR-005: Nessun server HTTP interno (no `litert-lm serve`)
**Data:** 13 Luglio 2026
**Stato:** Approvato
**Decisione:** Invokeremo l'Engine via API Kotlin nativa.
**Motivazione:** Eseguire un webserver C++ per creare un bridge HTTP su localhost all'interno di un'app Android è semanticamente non sicuro e un antipattern per il lifecycle OS (il server potrebbe venir killato dalla Low Memory Killer daemon in background in maniera invisibile). Le chiamate saranno fatte tramite `LlmInference`.
