# DailyHabit Agent Coordination & Guidelines

Questo documento stabilisce le regole d'ingaggio per Codex e Antigravity all'interno del progetto DailyHabit.

## Regole di Base
1. **Leggere Prima di Agire**: Prima di modificare il codice o proporre architetture, consulta sempre `docs/HANDOFF.md` e `docs/PROJECT_STATUS.md`.
2. **Singolo Agente Writer**: Un solo agente può scrivere e committare su un branch alla volta. Evitare race conditions.
3. **Branching**: Non si lavora mai direttamente su `main`. Tutto il lavoro deve avvenire su branch tematici (es. `antigravity/litert-parser-v2`).
4. **Validazione Obbligatoria**: Mai dichiarare "successo", "completato" o "funzionante" senza aver compilato, eseguito i test, validato il 16KB page size e testato possibilmente l'APK finale su Pixel 9.
5. **No Credenziali/Privacy**: Mai committare file PDF personali, token Hugging Face, chiavi API o dati sensibili nei log e nei file di test. Usa sempre le fixture sanitizzate.
6. **Integrità Dipendenze**: Non sostituire dipendenze note funzionanti senza documentarne il motivo in `docs/DECISIONS.md`.
7. **Tracciabilità degli Errori**: Non nascondere gli errori nei catch block vuoti. Ogni errore deve essere analizzato o propagato opportunamente all'interfaccia utente in caso di fallimenti runtime.
8. **Commits Logici e Piccoli**: Usa commit chiari, prefissati (es. `feat:`, `fix:`, `docs:`) e granulari.

## Iterazioni di Lavoro
Al termine di ogni milestone o handoff tra agenti:
1. Esegui i test unitari e strumentali pertinenti.
2. Aggiorna `docs/PROJECT_STATUS.md`.
3. Aggiorna `docs/HANDOFF.md`.
4. Effettua il commit locale.
5. Fai il push del branch remoto.
