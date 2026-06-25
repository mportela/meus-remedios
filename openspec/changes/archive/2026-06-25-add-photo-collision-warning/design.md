## Context

O formulário de cadastro extrai features (`embedding`, cor, forma, imprint) ao adicionar cada
foto via `AddMedicationPhotoUseCase`. Essas features ficam em `MedicationPhoto` já persistidas
para fotos existentes do medicamento, mas as **fotos pendentes** (`PendingPhoto`) são só um
`tempPath + side` — sem features ainda.

O `RecognitionScorer.score()` e `RecognitionEngine` já estão prontos e são puros/testáveis.
`THRESHOLD_CONFIDENT = 0.85f` é o limiar usado no reconhecimento normal.

O fluxo de save atual em `MedicationFormViewModel`:
1. Chama `saveMedication()` para persistir o medicamento.
2. Depois chama `addMedicationPhoto()` para cada `pendingPhoto` (que extrai features e persiste).

Para detectar colisão **antes** de salvar, precisamos das features das fotos pendentes. Isso
exige extraí-las antecipadamente ou redesenhar o fluxo.

## Goals / Non-Goals

**Goals:**
- Alertar o usuário (não bloquear) quando uma foto pendente for visualmente similar a outra
  foto já cadastrada de um medicamento diferente.
- Reutilizar `RecognitionScorer` e `FeatureExtractor` sem duplicação.
- Manter o fluxo offline e sem novas dependências.

**Non-Goals:**
- Comparar fotos já persistidas entre si (migração retroativa — escopo futuro).
- Bloquear o save quando há colisão.
- Exibir score numérico ao usuário (apenas o nome do candidato mais similar).

## Decisions

### D1 — Extrair features das pendingPhotos antecipadamente no `save()`

Antes de persistir, o `MedicationFormViewModel.save()` chamará `FeatureExtractor.extract()`
para cada `pendingPhoto`, produzindo uma lista de `FeatureSet` temporária usada apenas para a
checagem de colisão. O `AddMedicationPhotoUseCase` vai extrair novamente ao persistir —
aceita-se a extração dupla para manter a responsabilidade separada.

**Alternativa descartada:** mudar `PendingPhoto` para carregar `FeatureSet` — tornaria a
extração implícita ao adicionar a foto (na UI), complicaria testes e acoplaria a UI ao ML.

### D2 — `CheckPhotoCollisionUseCase` recebe lista de `FeatureSet` (não caminhos de arquivo)

O use case recebe `List<FeatureSet>` (features já extraídas) e `excludeMedicationId` (o
medicamento sendo editado é excluído da checagem). Isso mantém o use case puro e testável
com features sintéticas, sem I/O de arquivo.

**Alternativa descartada:** receber caminhos de arquivo e extrair internamente — dificultaria
testes unitários (precisaria de arquivos reais ou mocks de `FeatureExtractor`).

### D3 — Evento one-shot `CollisionWarning` no ViewModel

O ViewModel emite `MedicationFormEvent.CollisionWarning(candidateName: String)` via o canal
`_events` já existente. A tela recebe o evento e exibe um dialog com "Salvar assim mesmo" /
"Cancelar". Ao confirmar, a tela chama `viewModel.saveIgnoringCollision()` que persiste sem
a checagem.

### D4 — Limiar de colisão = `THRESHOLD_CONFIDENT`

Usa o mesmo limiar do reconhecimento normal (0.85f). Uma foto que dispararia uma identificação
confiante durante o reconhecimento também deve disparar o aviso no cadastro.

## Risks / Trade-offs

- **Extração dupla de features**: cada `pendingPhoto` terá features extraídas duas vezes (uma
  para checagem, outra em `AddMedicationPhotoUseCase`). Custo: ~50–200ms por foto (CPU/GPU).
  Mitigação: aceitável para o fluxo de cadastro (não é tempo-real); reavaliar se o número de
  fotos pendentes crescer (hoje: máx. 2 por medicamento).

- **Falsos positivos no aviso**: fotos legitimamente similares (ex.: dois comprimidos brancos
  redondos) dispararão o aviso mesmo que o usuário saiba que são remédios diferentes.
  Mitigação: aviso é não-bloqueante; texto explica que é uma sugestão, não um erro.

- **Checagem assíncrona**: a extração de features usa `Dispatchers.IO/Default`. O botão
  "Salvar" deve mostrar estado de loading durante a checagem para evitar duplo toque.
