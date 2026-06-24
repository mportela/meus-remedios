## Why

Fotos cadastradas antes da F4.3/F4.4 não têm embedding TFLite nem imprint OCR — o
reconhecimento para esses remédios opera degradado, usando só cor+forma. A migração
automática elimina essa assimetria sem exigir recadastro do usuário.

## What Changes

- Novo use case `MigratePhotoFeaturesUseCase` que itera sobre `MedicationPhoto`s sem
  embedding ou imprint, executa `TfliteFeatureExtractor` e persiste os campos atualizados.
- Rotina executada uma vez no startup do app (Application ou via WorkManager), idempotente:
  fotos já migradas (embedding não nulo) são puladas.
- Progresso visível opcionalmente (notificação silent ou log interno); não bloqueia UI.
- Segmentação excluída do escopo (F4.2 cancelada — pill isolado, limitação é no modelo).

## Capabilities

### New Capabilities

_(nenhuma — nenhuma capability nova é introduzida)_

### Modified Capabilities

- `medication-photos`: novo requisito de que features (embedding, imprint) devem estar
  presentes em todas as fotos ativas — migração automática ao detectar lacuna.

## Impact

- **Novos arquivos:** `MigratePhotoFeaturesUseCase`, worker ou launcher no startup.
- **Modificados:** `MedicationPhotoRepository` (método de busca por fotos sem embedding),
  `MeusRemediosApplication` (trigger da migração).
- **Room:** sem nova migração de schema — apenas UPDATE nas linhas existentes.
- **Dependências:** nenhuma nova — reutiliza `TfliteFeatureExtractor` (F4.3) e
  `MlKitImprintReader` (F4.4), já injetados via Hilt.
- **Offline:** mantido — sem rede, sem permissões novas.
