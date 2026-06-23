# Tasks — add-tflite-embedding

## 1. Embarcar o modelo
- [x] Copiar `mobilenet_v3_small.tflite` (MobileNetV3-Small embedder, float32) para
      `app/src/main/assets/`
- [x] Configurar `androidResources { noCompress += "tflite" }` em `app/build.gradle.kts`

## 2. Inferência e extractor
- [x] Criar `EmbeddingMath` (helper puro): `l2Normalize(FloatArray): FloatArray`
- [x] Criar `TfliteEmbedder`: carrega o modelo de assets (lazy, thread-safe), pré-processa
      `224×224` + normalização, roda inferência, devolve embedding L2-normalizado ou `null`
      em caso de falha
- [x] Criar `TfliteFeatureExtractor` (decorator): combina cor/forma do `DefaultFeatureExtractor`
      com o embedding do `TfliteEmbedder`

## 3. Wiring
- [x] Vincular `FeatureExtractor` → `TfliteFeatureExtractor` em `MediaModule`
- [x] Garantir que `TfliteEmbedder` recebe `@ApplicationContext`

## 4. Testes
- [x] Testar `EmbeddingMath.l2Normalize` (norma unitária, vetor zero, idempotência)
- [x] Testar fallback do `TfliteFeatureExtractor` (embedder devolve `null` → cor/forma intactos)
- [x] `./gradlew testDebugUnitTest assembleDebug` verde

## 5. Validação
- [x] Confirmar 100% offline: nenhuma permissão `INTERNET`; modelo só em assets
- [x] `openspec validate add-tflite-embedding --strict`

## 6. Docs e arquivamento
- [x] Atualizar `CHANGELOG.md` (Não lançado)
- [x] Atualizar `docs/technical/tech-3-engine-reconhecimento.md` (embedding ativo)
- [x] Atualizar `docs/openspec-plan.md` (F4.3 concluída; F4.2 adiada)
- [x] `openspec archive add-tflite-embedding --yes`
- [x] Commit
