## 1. Engine de reconhecimento (`data/ml`)

- [x] 1.1 `RecognitionParams` — pesos (`W_EMBEDDING`/`W_COLOR`/`W_SHAPE`) e limiares
      (`THRESHOLD_CONFIDENT`, `MARGIN`, `MIN_SCORE`, `MAX_DELTA_E`), centralizados.
- [x] 1.2 `RecognitionScorer` — `cosineSimilarity`, `colorSimilarity`, `shapeSimilarity` e
      `score(query, candidate)` normalizado pelos componentes disponíveis (puro).
- [x] 1.3 `RecognitionEngine.decide(ranked)` — confiante × ambíguo × sem correspondência.

## 2. Modelos de domínio (`domain/model`)

- [x] 2.1 `RecognitionCandidate` (medicationId, medicationName, score).
- [x] 2.2 `RecognitionOutcome` (sealed): `NoPhotosRegistered`, `NoMatch`, `Confident`,
      `Ambiguous`.

## 3. Use case (`domain/usecase`)

- [x] 3.1 `RecognizeMedicationUseCase(queryImagePaths)` — extrai features, compara com fotos
      cadastradas, agrega por medicamento (melhor score), combina lados e decide.

## 4. UI de reconhecimento (`ui/recognition`)

- [x] 4.1 `RecognitionViewModel` — captura via câmera, fases (ocioso/analisando/resultado),
      reanálise com 2ª foto, limpeza de temporários.
- [x] 4.2 `RecognitionScreen` — botão grande "Confirmar remédio", estado de análise,
      resultado confiante (nome grande), candidatos, fluxo de 2ª foto e erros amigáveis.

## 5. Navegação e strings

- [x] 5.1 Rota/aba "Confirmar" como destino inicial em `Routes`/`MeusRemediosNavHost`.
- [x] 5.2 Strings pt-BR (botão, análise, resultado, ambíguo, sem fotos, não reconhecido) e
      semântica de acessibilidade.

## 6. Testes e verificação

- [x] 6.1 `RecognitionScorerTest` — similaridades e score com vetores sintéticos.
- [x] 6.2 `RecognitionEngineTest` — limiar/margem (confiante, ambíguo, sem correspondência).
- [x] 6.3 `RecognizeMedicationUseCaseTest` — agregação por medicamento, 2 lados, sem fotos.
- [x] 6.4 `RecognitionViewModelTest` — fases e resultado.
- [x] 6.5 `./gradlew test assembleDebug` verde; atualizar `CHANGELOG.md`.
