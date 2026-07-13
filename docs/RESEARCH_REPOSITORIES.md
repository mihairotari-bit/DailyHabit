# Ricerca nei Repository di Riferimento (WSL2)

Tutta l'analisi iniziale è stata svolta all'interno dell'ambiente di ricerca in WSL2 Ubuntu (`/home/mihai/android-references/`), usando git clone e ripgrep. 

Tutti i repository esaminati si basano sul comando di clonazione limitata (`git clone --depth 1`).

## Repository Analizzati

### 1. LiteRT
* **URL:** `https://github.com/google-ai-edge/LiteRT.git`
* **Commit HEAD:** `11ac3899271055585b373005202dcc2c93bebd30`
* **Licenza:** Apache 2.0
* **Conclusioni:** Contiene i sorgenti C++ e le interfacce JNI basilari per l'ambiente runtime unificato.

### 2. LiteRT-LM
* **URL:** `https://github.com/google-ai-edge/LiteRT-LM.git`
* **Commit HEAD:** `da33e30069cb391b80b9b8631cdc07e3a4715f95`
* **Licenza:** Apache 2.0
* **Conclusioni:** È il modulo chiave per l'inferenza di modelli Large Language. Ha sostituito ufficialmente MediaPipe LLM Inference e supporta nativamente Gemma. Contiene la specifica architetturale per le LlmInference API su Android (`com.google.ai.edge.litertlm:litertlm-android`).

### 3. Google AI Edge Gallery
* **URL:** `https://github.com/google-ai-edge/gallery.git`
* **Commit HEAD:** `ac4f6865820c20267a77d0dd67d753110d018fc1`
* **Conclusioni:** Analizzato specificatamente il pattern per scaricare in modo resiliente modelli molto grandi e istanziare il `LlmChatModelHelper`.

### 4. Android Compose Samples
* **URL:** `https://github.com/android/compose-samples.git`
* **Commit HEAD:** `bc182640f7903aa5ec77025b9fc3a015f40a6a13`
* **Conclusioni:** Riferimento assoluto per architetture Material 3 e adaptive layouts (per desktop mode e foldable).

### 5. Now In Android (NiA)
* **URL:** `https://github.com/android/nowinandroid.git`
* **Commit HEAD:** `7d45eae4f8720a0c77f507712ba2437ff974b6ed`
* **Conclusioni:** Il framework di standardizzazione Clean Architecture consigliato da Google. Lo utilizzeremo come blueprint per il refactoring della navigazione, dei viewmodel e dei flussi asincroni per DailyHabit.

### 6. Material Components Android
* **URL:** `https://github.com/material-components/material-components-android.git`
* **Commit HEAD:** `ac7e18efeefb331850c561faf9ab8bf81d27ba68`
* **Conclusioni:** Contiene i draft per le spec Material 3 Expressive (che replicheremo idiomaticamente in Compose tramite Shapes e Tonal Elevations).

### 7. PdfBox-Android
* **URL:** `https://github.com/TomRoush/PdfBox-Android.git`
* **Commit HEAD:** `acf64258dd2ce575fea8ac4e51f57cca7f4945d7`
* **Conclusioni:** Riferimento fondamentale per l'estrazione nativa di testo dai PDF aggirando l'OCR in caso di testo nativamente embeddato. Consente alta fedeltà architetturale pre-layouting.

### 8. LiteRT Samples
* **URL:** `https://github.com/google-ai-edge/litert-samples.git`
* **Commit HEAD:** `64fc05c48366fca8247a4cc020f69600c6b95bac`
* **Conclusioni:** Visione rapida degli usi specializzati. Per gli LLM ci affideremo alle classi estratte in LiteRT-LM.
