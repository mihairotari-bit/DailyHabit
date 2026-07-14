# DailyHabit UI Theme Guide

Il design language di DailyHabit punta a trasmettere "benessere, professionalità, e premium feel".

## Light Mode (Default)
L'app abbandona lo sfondo bianco puro e i colori flat, in favore di una palette morbida.

* **Background**: `Crema (#F7F5F0)` - Riposa la vista e dona un aspetto organico.
* **Surface**: `Bianco Caldo (#FDFCF8)` - Eleva le card e gli elementi rispetto allo sfondo crema.
* **SurfaceContainer**: `Crema Leggera (#F0EEE8)` - Usata per menù a tendina, card in secondo piano, blocchi di opzioni.
* **Primary**: `Verde Fresco (#2B8A4D)` - Utilizzato per azioni call-to-action primarie e highlight. Non fluorescente.
* **PrimaryContainer**: `Verde Salvia Molto Chiaro (#E6F4EA)` - Ideale per badge e sfondi attivi (es: toggle allenamento).
* **Typography**: Antracite (`#1A1C1A`) e grigio morbido (`#434844`) per leggibilità, evitando il nero puro (`#000000`).

## Interazioni
- I menù (es: Hamburger Dropdown) usano forme pill-shaped (`RoundedCornerShape(24.dp)`) per seguire le linee curve naturali.
- Gli state-change (selezione cibi, toggle) devono avere un'animazione di entrata/uscita visibile (es. `scaleIn` per i checkmark).
