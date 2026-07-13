# Test Results

## Smoke Test 1 - Milestone 1: Application ID Rename

**Data:** 13 Luglio 2026
**Dispositivo:** Pixel 9 (tokay_beta)
**OS:** Android API 37 Canary (arm64-v8a, PAGE_SIZE=4096)
**Build:** `app-debug.apk` (assembleDebug)
**Exit Code (Build):** 0
**Durata (Build):** 28s

### Procedure & Verification
1. Esecuzione `adb devices -l` e retrieval Device Props (OK, identificato Pixel 9 Canary).
2. Installazione `adb install -r app\build\outputs\apk\debug\app-debug.apk` (Success).
3. Avvio test monkey `adb shell monkey -p com.mihai.dailyhabit 1` (Events Injected: 1).
4. Ispezione logcat `adb logcat -d` (Nessuna eccezione o crash riconducibile al package `com.mihai.dailyhabit`).
5. Verifica data loss `adb shell run-as com.mihai.android17helloworld ls -l /data/user/0/...` (Il db `diet-plans.db` della precedente build è stato verificato ed è rimasto intatto sul dispositivo, isolato dal nuovo package).

### Esito
**SUPERATO**. L'app con il nuovo package `com.mihai.dailyhabit` si installa parallelamente alla vecchia e si avvia senza problemi architetturali immediati o conflitti di provider.
