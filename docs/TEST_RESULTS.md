# Risultati Test Milestone 1.5 - Regressione Parser

## Copertura JUnit (`app/src/test/`)

| Test Class | Obiettivo | Esito |
|------------|-----------|-------|
| `ActiveDayProfileResolverTest` | Validare risoluzione semantica giorno/profilo, fallback a `UNKNOWN` o match label | PASS |
| `DifferentInputProducesDifferentPlanTest` | Verificare che il parser deterministico non emetta risultati hardcoded o fake | PASS |
| `EmptyTrackingStateTest` | Validare che lo stato ViewModel ritorni null (o gestisca l'assenza) per triggerare l'empty state UI | PASS |
| `FakeEngineIsolationTest` | Verificare accessibilità limitata della classe `FakeDietInferenceEngine` ai test | PASS |
| `LegacyDeterministicDietInferenceEngineTest` | Validare che il parser aggiunga metadati `LEGACY_DETERMINISTIC` corretti | PASS |
| `RotariSanitizedGoldenTest` | Validare la presenza di tutti i pasti (pre, post, mattina, pranzo, cena) in un pdf tipo "Rotari" | PASS |

## Test di Integrazione (`app/src/androidTest/`)

| Test Class | Obiettivo | Esito |
|------------|-----------|-------|
| `ProductionEngineBindingTest` | Verificare che il Dagger/Hilt graph in `main` inietti solo il LegacyDeterministicDietInferenceEngine | PENDING (manca `hilt-android-testing`) |

## Validazione Funzionale (Pixel 9 Simulata/Local)

- **Regressione Risolta**: Il parser deterministico riconosce ora i pasti `PRE_WORKOUT`, `POST_WORKOUT`, `MORNING_SNACK`.
- **Parser Deterministico Ripristinato**: Nessun utilizzo in produzione del fallback "Lunedì / Latte / Fette biscottate" (`FakeDietInferenceEngine`).
- **Supporto Regex Italiano**: Il match `\b(lunedì|martedì)\b` è stato corretto in `(?Ui)` per processare in modo corretto i word boundaries su lettere accentate come la 'ì' in Kotlin/Java.

**Conclusioni:**
La Regressione critica è risolta. L'applicazione ora usa il parser semantico e documentale in attesa del modello locale LiteRT-LM. La build è stabile e i test JUnit sono verdi (11/11 completati).
