## Why

O app permite cadastrar dois medicamentos com o mesmo nome, gerando duplicatas confusas na
listagem e candidatos redundantes no ranking de reconhecimento visual. Resolver agora
(antes de F11) garante que o alerta de colisão de fotos não retorne candidatos duplicados
do próprio cadastro.

## What Changes

- Adicionar consulta `MedicationDao.existsByName(name, excludeId)` (case-insensitive).
- Incluir validação em `SaveMedicationUseCase`: retornar `MedicationValidationError.DuplicateName`
  quando outro medicamento com o mesmo nome (trim + case-insensitive) já existir.
- Exibir erro inline no campo "Nome do remédio" no formulário de cadastro/edição.
- Edição do próprio medicamento é excluída da checagem (`excludeId = medication.id`).

## Capabilities

### New Capabilities

_(nenhuma — sem nova capability; a lógica pertence inteiramente ao catálogo existente)_

### Modified Capabilities

- `medication-catalog`: adicionar requisito de unicidade de nome de medicamento com
  feedback de erro no formulário.

## Impact

- `data/local/MedicationDao.kt` — nova query `existsByName`.
- `domain/usecase/SaveMedicationUseCase.kt` — nova validação pré-persist.
- `domain/model/MedicationValidationError.kt` (ou equivalente) — novo valor `DuplicateName`.
- `ui/medications/form/MedicationFormViewModel.kt` — tratar erro e expor para a UI.
- `ui/medications/form/MedicationFormScreen.kt` — exibir mensagem inline no campo de nome.
- Testes: `MedicationDaoTest`, `SaveMedicationUseCaseTest`.
- Sem novas dependências.
