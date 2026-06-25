## Why

Ao cadastrar um comprimido, o usuário pode adicionar fotos visualmente idênticas às de
outro medicamento já cadastrado. O problema só aparece em campo — quando o reconhecimento
retorna o remédio errado com confiança alta — sem nenhum aviso preventivo durante o
cadastro. Este é um risco de segurança médica para o público idoso que o app atende.

## What Changes

- Criar `CheckPhotoCollisionUseCase`: reutiliza `RecognitionScorer.score()` para comparar
  as features da(s) foto(s) pendentes no formulário com as fotos já cadastradas de outros
  medicamentos; retorna a lista de candidatos com score ≥ `THRESHOLD_CONFIDENT`.
- Em `MedicationFormViewModel.save()`: chamar `CheckPhotoCollisionUseCase` com as features
  das `pendingPhotos` que possuem embedding não-nulo; se houver colisão, emitir evento de
  aviso com o nome do medicamento mais similar.
- Em `MedicationFormScreen`: exibir dialog de aviso não-bloqueante quando o ViewModel
  emitir o evento de colisão, com ações "Salvar assim mesmo" e "Cancelar".
- Adicionar método `toFeatureSet()` em `PendingPhoto` ou criar modelo `PendingPhotoFeatures`
  para transferir features até o momento do save (as `pendingPhotos` não têm features —
  precisam de `FeatureExtractor` ou receber as features após extração em `addMedicationPhoto`).

## Capabilities

### New Capabilities

_(nenhuma — lógica pertence às capabilities existentes de fotos e reconhecimento)_

### Modified Capabilities

- `medication-photos`: adicionar requisito de verificação de colisão antes de persistir
  novas fotos, com aviso ao usuário.

## Impact

- `domain/usecase/CheckPhotoCollisionUseCase.kt` — novo use case.
- `domain/model/CollisionCandidate.kt` — novo modelo (medicationName + score).
- `ui/medications/form/MedicationFormViewModel.kt` — chamar use case no `save()`;
  novo evento `CollisionWarning(candidateName)`.
- `ui/medications/form/MedicationFormScreen.kt` — dialog de aviso não-bloqueante.
- `ui/medications/form/PendingPhoto` — precisa carregar as features extraídas para
  que `CheckPhotoCollisionUseCase` possa comparar antes do save final.
- Testes: `CheckPhotoCollisionUseCaseTest`.
- Sem novas dependências (`RecognitionScorer` e `FeatureExtractor` já existem).
