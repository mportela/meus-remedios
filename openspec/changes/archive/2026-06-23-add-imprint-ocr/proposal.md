# OCR de inscrição do comprimido (imprint)

## Why

Com o embedding TFLite ativo (F4.3), o reconhecimento já distingue comprimidos pelo
conteúdo visual. Porém comprimidos podem ser **visualmente quase idênticos** (mesma cor,
forma e textura) e se diferenciarem apenas pelas **letras/números gravados** (imprint).
A medição on-device da F4.3 mostrou que dois lados do mesmo comprimido marcam ~0.89 e um
comprimido diferente de cor parecida marca ~0.75 — margens estreitas para uma tarefa
sensível. O imprint é um sinal **altamente discriminativo e textual** que complementa o
embedding e reduz ambiguidade sem reabrir falsos positivos.

## What Changes

- Ler, ao processar uma foto (cadastro e consulta), o **texto gravado** no comprimido via
  **ML Kit Text Recognition v2 _bundled_** (modelo embarcado no APK, 100% on-device, sem
  Play Services em runtime e sem download), normalizando o texto (maiúsculas, apenas
  alfanuméricos, espaços colapsados).
- Persistir o novo campo `imprintText` junto das demais features da foto (nova coluna na
  tabela `medication_photos` + migração Room v1→v2 aditiva).
- Incluir a **similaridade de texto** (`imprintSim`) no score, com um novo peso `w4`
  (`W_IMPRINT`), entrando na combinação ponderada **apenas quando ambas as fotos** (consulta
  e cadastrada) possuírem imprint — mantendo a normalização pelos componentes presentes.
- **Garantir offline:** as libs do ML Kit podem injetar `android.permission.INTERNET` via
  _manifest merge_; remover explicitamente com `tools:node="remove"` e **verificar o
  manifesto mergeado** para confirmar que nenhuma permissão de rede entra no APK.
- **Fallback gracioso:** se o OCR falhar, não encontrar texto, ou a foto não tiver imprint,
  o campo fica `null` e o reconhecimento prossegue com embedding + cor + forma.

## Capabilities

- `pill-imprint-ocr` (nova): leitura on-device do texto gravado no comprimido.
- `medication-photos` (modificada): o cadastro passa a extrair e persistir também o imprint.
- `visual-recognition` (modificada): o score passa a usar a similaridade de imprint quando
  disponível.

## Impact

- Código: nova interface `ImprintReader` + impl `MlKitImprintReader`; helper puro
  `ImprintMatch` (normalização + similaridade textual); `TfliteFeatureExtractor` passa a
  preencher `imprintText`; binding em `MediaModule`; novo peso `W_IMPRINT` em
  `RecognitionParams`; `textSimilarity()` + uso em `RecognitionScorer.score()`.
- Modelo de dados: `imprintText: String?` em `PhotoFeatures`, `FeatureSet`,
  `MedicationPhoto`, `MedicationPhotoEntity` (coluna `imprint_text TEXT`); mapeadores
  `toDomain`/`toEntity`; `MeusRemediosDatabase` v2 + `MIGRATION_1_2`.
- Manifest/APK: ML Kit Text Recognition _bundled_ aumenta o APK (orçamento tratado na F4.7);
  `INTERNET` removida no merge e verificada.
- Comportamento: a calibração fina de `w4`/limiares (incluindo possível relaxamento do
  limiar 0.90) fica para a **F4.5** (`recalibrate-recognition`).

## Documentation

- Atualizar `CHANGELOG.md` (Não lançado), `docs/technical/tech-2-modelo-dados.md` (campo
  `imprintText` + versão do schema), `docs/technical/tech-3-engine-reconhecimento.md`
  (fórmula com `w4`/OCR), `docs/product/prd-3-reconhecimento.md` (sinal de imprint) e
  `docs/openspec-plan.md` (F4.4 concluída).

## Notas

- **Offline é inegociável:** usar o artefato _bundled_ (`com.google.mlkit:text-recognition`),
  **nunca** o `com.google.android.gms:play-services-mlkit-text-recognition` (que baixa o
  modelo do Play Services). Verificação do manifesto mergeado é tarefa obrigatória.
- **Escopo Latin:** inscrições de comprimidos usam alfanuméricos latinos; o reconhecedor
  Latin _bundled_ é suficiente e mais leve que multi-script.
