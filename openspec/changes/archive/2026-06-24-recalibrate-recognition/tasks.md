## 0. Pré-requisito — fotos reais (antes de qualquer código)

> O usuário deve deixar as 6 fotos na raiz do projeto com os nomes abaixo antes da tarefa 0.1.
> `pill_a_frente.jpg`, `pill_a_verso.jpg`, `pill_b_frente.jpg`, `pill_b_verso.jpg`,
> `pill_c_frente.jpg`, `pill_c_verso.jpg`
> Os 3 comprimidos devem ser visualmente parecidos (mesma cor/formato aproximado).

- [ ] 0.1 Criar `app/src/androidTest/java/com/meusremedios/data/ml/RealPillFeatureExtractorTest.kt`:
         teste instrumentado que carrega cada foto de `app/src/androidTest/assets/`, executa
         `TfliteFeatureExtractor.extract()` em cada uma, e loga os vetores resultantes
         (embedding, colorLab, aspectRatio, imprintText) em formato Kotlin-friendly (pronto para
         copiar como constantes)
- [ ] 0.2 Mover as 6 fotos da raiz para `app/src/androidTest/assets/`
- [ ] 0.3 Rodar `make connected` no emulador para executar `RealPillFeatureExtractorTest`
         e capturar o output (vetores extraídos pelo modelo real)
- [ ] 0.4 Criar `app/src/test/java/com/meusremedios/data/ml/RealPillFixtures.kt` com os vetores
         extraídos hardcoded como constantes Kotlin:
         `PILL_A_FRENTE`, `PILL_A_VERSO`, `PILL_B_FRENTE`, `PILL_B_VERSO`, `PILL_C_FRENTE`,
         `PILL_C_VERSO` — cada uma um `FeatureSet` com embedding real, colorLab real, aspectRatio
         real e imprintText (se extraído)

## 1. Calcular e validar novo limiar

- [ ] 1.1 Calcular os scores reais para todos os pares relevantes usando `RealPillFixtures`:
         positivo (pill_A_frente vs pill_A_verso), negativo hard (pill_A vs pill_B,
         pill_A vs pill_C, pill_B vs pill_C) — documentar os scores no comentário de
         `THRESHOLD_CONFIDENT`
- [ ] 1.2 Escrever testes de controle em `RecognitionScorerTest` com fixtures reais: positivo
         full, positivo sem imprint (se algum comprimido não tiver imprint), todos os pares
         negativos hard; mais fixtures sintéticas para: fallback sem embedding, caso ambíguo
- [ ] 1.3 Avaliar se os pesos atuais produzem separação suficiente (margem ≥ 0.10 entre
         positivo e negativo mais difícil); ajustar `W_*` em `RecognitionParams` se necessário
- [ ] 1.4 Determinar o menor `THRESHOLD_CONFIDENT` que rejeita todos os negativos reais e
         aceita todos os positivos reais — usar esse valor como limiar definitivo
- [ ] 1.5 Atualizar `RecognitionParams.THRESHOLD_CONFIDENT` (e pesos se ajustados) com os
         novos valores e remover comentário "provisório" / referência à F4.1

## 2. Ampliar golden set de testes

- [ ] 2.1 Adicionar fixtures de `FeatureSet` em `RecognitionScorerTest` para todos os modos:
         com embedding real (de `RealPillFixtures`), sem embedding (sintético), com imprint,
         sem imprint — garantindo cobertura de todos os branches de `RecognitionScorer.score()`
- [ ] 2.2 Adicionar testes de controle ponta-a-ponta em `RecognitionEngineTest` usando scores
         reais dos pares de `RealPillFixtures`: positivo decide CONFIANTE, todos os pares
         negativos decidem NÃO-CONFIANTE/AMBÍGUO, retrocompatibilidade sem embedding (valores F4.1)
- [ ] 2.3 Verificar que todos os branches de `RecognitionScorer.score()` (componente
         presente/ausente) têm ao menos um teste — incluir caso de zero componentes comparáveis

## 3. Limpar e documentar `RecognitionParams`

- [ ] 3.1 Remover todos os comentários de "provisório", "F4.1" e "Será recalibrado" de `RecognitionParams`
- [ ] 3.2 Atualizar KDoc de `THRESHOLD_CONFIDENT` e `MARGIN` descrevendo o regime final (com embedding) e os valores dos controles que justificam as constantes
- [ ] 3.3 Revisar comentários de `W_EMBEDDING`, `W_COLOR`, `W_SHAPE`, `W_IMPRINT` — garantir que reflitam o estado atual (embedding ativo, OCR ativo)

## 4. Sincronizar spec e documentação

- [ ] 4.1 Executar `openspec sync-specs --change recalibrate-recognition` para promover o delta de `visual-recognition` ao spec principal
- [ ] 4.2 Atualizar `CHANGELOG.md` com entrada na seção "Não lançado" descrevendo a calibração final e os novos valores de threshold
- [ ] 4.3 Rodar `./gradlew test` e confirmar que todos os testes passam (incluindo os novos controles)
