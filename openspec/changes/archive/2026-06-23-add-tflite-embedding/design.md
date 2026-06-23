# Design — add-tflite-embedding

## Context

A F4 deixou o pipeline pronto para embedding: `PhotoFeatures.embedding` existe, o BLOB
`FloatArray↔bytes` já é persistido por Room, `RecognitionScorer.cosineSimilarity` já está
testado e `W_EMBEDDING = 0.6` já está reservado. Falta apenas um `FeatureExtractor` que
produza o vetor. Hoje `DefaultFeatureExtractor` calcula cor (Lab) + aspect ratio e devolve
`embedding = null`.

## Goals

- Ativar o embedding on-device, 100% offline, com fallback que nunca quebra o reconhecimento.
- Reaproveitar a extração de cor/forma existente (não duplicar lógica).
- Manter as partes determinísticas testáveis em JVM puro.

## Non-Goals

- Segmentação do comprimido / remoção de fundo (F4.2, adiada).
- OCR de inscrições (F4.4).
- Recalibração de pesos/limiares (F4.5) e quantização int8 / orçamento de APK (F4.7).

## Decisions

### Modelo: MobileNetV3-Small embedder (float32, 1024-d)
Embedder de imagem pronto, offline, entrada `224×224×3`, saída `1024`. Não existe variante
int8 publicada deste embedder; o float32 (~4,1 MB) é aceitável e a quantização vira tarefa
da F4.7. Versionado em `assets/`, sem download em runtime.

### Decorator em vez de substituir o extractor
`TfliteFeatureExtractor` injeta o `DefaultFeatureExtractor` (cor/forma) e o `TfliteEmbedder`
(embedding), combinando os dois em um `PhotoFeatures`. Mantém uma única fonte de verdade para
cor/forma e isola o código nativo de TFLite.

### Pré-processamento e normalização
Redimensiona a imagem para `224×224`, normaliza `(pixel − 127,5) / 127,5` (padrão MobileNetV3)
e L2-normaliza a saída antes de persistir. Como cadastro e consulta usam exatamente o mesmo
pré-processamento, o cosseno é consistente — a escolha de mean/std não afeta o ranking relativo.

### Carregamento preguiçoso, thread-safe e com fallback
O `Interpreter` é criado sob demanda (double-checked) e reutilizado. Qualquer falha
(modelo ausente, erro de inferência) resulta em `embedding = null`: o engine continua com
cor + forma, preservando o princípio conservador.

### `noCompress` para `.tflite`
`androidResources { noCompress += "tflite" }` permite mapear o modelo em memória e evita
descompactar em runtime.

## Risks / Trade-offs

- **Tamanho do APK:** +4,1 MB. Aceitável agora; int8 na F4.7.
- **Inferência não é testável em JVM puro:** cobrir o determinístico (`EmbeddingMath.l2Normalize`,
  fallback do extractor quando o embedder devolve `null`); a inferência real é validada em
  device/emulador.
- **Domínio do modelo é genérico (ImageNet):** suficiente como sinal forte agora; fine-tuning
  com fotos reais fica para o futuro (TODO já documentado no README).

## Migration

Sem migração de schema. Fotos cadastradas antes desta change permanecem com `embedding = null`
e continuam comparáveis por cor/forma; o reprocessamento automático é a F4.6
(`migrate-existing-photo-features`).
