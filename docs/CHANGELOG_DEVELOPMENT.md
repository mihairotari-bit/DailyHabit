# Changelog Development

## [Unreleased]
### Added
- Pipeline di scansione nativa ibrida per PDF con OCR di fallback su base per pagina (`PdfNativeTextExtractor`).
- Modello tipizzato a blocchi (`DietStructureTokenizer`) protetto dai riferimenti ambigui del pranzo / alternative.
- Rigoroso `NativeTextQualityEvaluator` per definire il punteggio qualitativo del testo nativo con Enum motivazionali.
- UI Review con corretta renderizzazione di tutti i pasti differenziati.
- Numerosi test automatici per assicurare la robustezza architetturale.

### Changed
- Refactoring `DietParser` per iterare a "blocchi" invece di utilizzare espressioni regolari con lookbehind fragili.
- Rimozione del finto profilo "Piano Singolo" come fallback quando le intestazioni corrette non ci sono. L'errore viene mostrato in modo consono nella UI.
- Log testuali sanitizzati per aderire alla conformità della privacy.

### Fixed
- Pranzo Rest isolato e scorporato con successo da "Pranzo Training".

## [v1.9] Milestone 1.9
Validata su Pixel 9 reale:
- Parsing strutturale isolato
- Corretta migrazione da Regex Fragili a Blocchi
- Test superati in locale ed in remoto
