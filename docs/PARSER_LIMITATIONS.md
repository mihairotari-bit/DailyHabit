# Limiti del Parser Deterministico (Legacy)

Attualmente il `DietParser` e `DietLineClassifier` supportano PDF nativi e falliscono verso l'OCR per le immagini. 
Sebbene la logica a blocchi per giorni e pasti risolva moltissime ambiguità, rimangono i seguenti limiti architetturali per i quali servirà l'uso di un LLM locale (Milestone 2):

1. **Variabilità Intestazioni**: Nutrizionisti diversi usano layout creativi e descrittivi impossibili da classificare in maniera esaustiva tramite Regular Expressions.
2. **Grammature complesse o composizionali**: Alcuni alimenti sono descritti con frazioni ("1/2 mela") o misure arbitrarie ("un filo d'olio", "una tazza") che il determinismo fa fatica a quantificare per i macros in maniera nativa.
3. **Reference Dinamici Complessi**: Testi come "mangia come al mattino ma togli 10g di carboidrati" non sono calcolabili facilmente in regex.
4. **Resilienza all'OCR Misto**: Pur essendoci l'OCR, la pipeline a volte sbaglia O e 0 (lettera / numero).

La Milestone 2 punterà a superare questi colli di bottiglia introducendo l'elaborazione NLP on-device con Gemma 4 E2B.
