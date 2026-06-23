# Tasks — harden-recognition-thresholds

## 1. Endurecer o limiar
- [x] Elevar `THRESHOLD_CONFIDENT` de `0.82f` para `0.90f` em `RecognitionParams`
- [x] Atualizar o KDoc/constante explicando que é provisório até embedding (F4.3) e que a
      recalibração definitiva é a F4.5

## 2. Testes de controle determinísticos (permanentes, CI-safe)
- [x] Criar teste com fixtures medidas (Lab + aspect ratio) das imagens reais
- [x] Cenário **negativo**: `frente-druse` vs. remédio cadastrado (frente+verso) → NÃO confiante
- [x] Cenário **positivo**: verso (consulta) vs. frente cadastrada → confiante
- [x] Garantir que exercem `RecognitionScorer.score` + `RecognitionEngine.decide` (puros)

## 3. Ajustar testes existentes ao novo limiar
- [x] Revisar `RecognitionEngineTest` para o limiar 0.90 (casos que assumiam 0.82)

## 4. Validação
- [x] `./gradlew testDebugUnitTest` e `assembleDebug` verdes

## 5. Documentação e fechamento
- [x] Atualizar `CHANGELOG.md` (Não lançado)
- [x] `openspec validate harden-recognition-thresholds --strict`
- [x] `openspec archive harden-recognition-thresholds --yes`
