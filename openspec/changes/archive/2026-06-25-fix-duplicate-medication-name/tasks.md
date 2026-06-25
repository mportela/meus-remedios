## 1. Camada de dados — DAO e repositório

- [x] 1.1 Adicionar `existsByName(name: String, excludeId: Long): Boolean` em `MedicationDao`
      com query SQL `COLLATE NOCASE` e trim via `LOWER(TRIM(name))`.
- [x] 1.2 Adicionar `existsByName(name: String, excludeId: Long): Boolean` na interface
      `MedicationRepository`.
- [x] 1.3 Implementar `existsByName` em `MedicationRepositoryImpl` delegando ao DAO.
- [x] 1.4 Adicionar `existsByName` ao fake `FakeMedicationRepository` retornando `false` por
      padrão (compatível com testes existentes).

## 2. Camada de domínio — validação

- [x] 2.1 Adicionar valor `DUPLICATE_NAME` ao enum `MedicationValidationError`
      em `SaveMedicationResult.kt`.
- [x] 2.2 Injetar `MedicationRepository` em `SaveMedicationUseCase` (já disponível) e incluir
      checagem: antes de persistir, chamar `existsByName(trimmedName, medication.id)`;
      se `true`, retornar `SaveMedicationResult.Invalid(MedicationValidationError.DUPLICATE_NAME)`.

## 3. Camada de UI — formulário

- [x] 3.1 Em `MedicationFormViewModel`, tratar `SaveMedicationResult.Invalid(DUPLICATE_NAME)`
      e propagar `validationError = MedicationValidationError.DUPLICATE_NAME` para o `UiState`.
- [x] 3.2 Em `MedicationFormScreen`, exibir mensagem de erro inline abaixo do campo
      "Nome do remédio" quando `validationError == DUPLICATE_NAME`
      (reutilizar o padrão já existente para `BLANK_NAME`).

## 4. Testes

- [x] 4.1 `MedicationDaoTest`: testar `existsByName` — retorna `true` com mesmo nome
      (case-insensitive), `false` com nome diferente, `false` ao excluir o próprio `id`.
- [x] 4.2 `SaveMedicationUseCaseTest`: salvar com nome duplicado retorna
      `Invalid(DUPLICATE_NAME)`; editar mantendo o mesmo nome retorna `Success`; nome
      diferente retorna `Success`.

## 5. Finalização

- [x] 5.1 Rodar `./gradlew testDebugUnitTest ktlintCheck` e confirmar BUILD SUCCESSFUL.
- [x] 5.2 Atualizar `CHANGELOG.md` (seção "Não lançado" → "Corrigido").
- [x] 5.3 Marcar TD-1 como resolvido em `docs/technical/tech-debt.md`.
- [x] 5.4 Marcar F10 como `✅ feito` em `docs/openspec-plan.md`.
