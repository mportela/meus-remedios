# Embedding TFLite on-device (núcleo de inteligência)

## Why

O engine de reconhecimento (F4) já agrega `embedding + cor + forma` com pesos
centralizados (`W_EMBEDDING = 0.6`), mas o componente de embedding está **inativo**:
o extractor de produção sempre devolve `embedding = null`, então o score depende só de
cor (Lab) e forma (aspect ratio da foto). Isso é frágil — foi a causa-raiz do falso
positivo tratado na F4.1 (limiar endurecido provisoriamente para 0.90).

Ativar o **embedding** é o passo central para o objetivo do produto: distinguir comprimidos
pelo seu **conteúdo visual** (textura, formato, marcações), não só pela cor média. Com o
embedding ligado, o peso dominante (0.6) passa a refletir a aparência real do comprimido,
permitindo recalibrar os limiares na F4.5 sem reabrir falsos positivos.

## What Changes

- Embarcar em `app/src/main/assets/` um modelo **MobileNetV3-Small** de embedding de imagem
  (TFLite, entrada `224×224×3`, saída vetor `1024`), executado 100% on-device.
- Implementar um `FeatureExtractor` real que roda a inferência e preenche `PhotoFeatures.embedding`
  (vetor L2-normalizado), tanto no **cadastro** (F2) quanto na **consulta** (F4), reutilizando
  a extração de cor/forma existente.
- Carregamento preguiçoso e thread-safe do modelo; **fallback gracioso**: se o modelo falhar
  ao carregar/inferir, o embedding fica `null` e o reconhecimento continua com cor + forma
  (sem quebrar o app).
- Configurar `noCompress` para `.tflite` no AGP, permitindo mapear o modelo em memória.
- Sem rede: o modelo é versionado em assets; nenhuma permissão `INTERNET` é adicionada.

## Capabilities

- `medication-photos` (modificada): o cadastro passa a extrair e persistir também o embedding.
- `visual-recognition` (modificada): o score passa a usar o embedding quando disponível.

## Impact

- Código: novo `TfliteEmbedder` + `TfliteFeatureExtractor` (decora o extractor atual), helper
  puro `EmbeddingMath` (L2-norm), binding em `MediaModule`.
- Assets: `mobilenet_v3_small.tflite` (~4,1 MB, float32).
- Persistência: nenhuma migração — o BLOB de `embedding` (FloatArray↔bytes) já existe.
- Comportamento: capturas passam a ser comparadas pelo embedding (peso 0.6); a recalibração
  fina de pesos/limiares fica para a F4.5.

## Documentation

- Atualizar `CHANGELOG.md` (Não lançado), `docs/technical/tech-3-engine-reconhecimento.md`
  (embedding ativo) e `docs/openspec-plan.md` (F4.3 concluída, F4.2 adiada).

## Notas

- **Tamanho/int8:** não há MobileNetV3-Small int8 de embedding publicado; usar o **float32**
  (~4,1 MB) agora não estoura o APK. A quantização **int8** fica como otimização explícita na
  **F4.7** (`enforce-apk-size-budget`).
- **Robustez do ranking:** como consulta e cadastro usam o **mesmo** extractor e pré-processamento,
  o cosseno entre embeddings é consistente independentemente da escolha exata de normalização.
