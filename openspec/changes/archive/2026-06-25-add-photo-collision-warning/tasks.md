## 1. Domínio — modelo e use case

- [x] 1.1 Criar `domain/model/CollisionCandidate.kt` com `data class CollisionCandidate(val medicationId: Long, val medicationName: String, val score: Float)`.
- [x] 1.2 Criar `domain/usecase/CheckPhotoCollisionUseCase.kt`: recebe `List<FeatureSet>` e
      `excludeMedicationId: Long`; busca todas as fotos (`MedicationPhotoRepository.getAll()`)
      excluindo as do próprio medicamento; calcula `RecognitionScorer.score()` para cada par
      query×candidato; retorna candidatos com score máximo ≥ `RecognitionParams.THRESHOLD_CONFIDENT`,
      ordenados por score decrescente.

## 2. ViewModel — integração no fluxo de save

- [x] 2.1 Adicionar `MedicationFormEvent.CollisionWarning(candidateName: String)` ao sealed
      interface `MedicationFormEvent`.
- [x] 2.2 Injetar `CheckPhotoCollisionUseCase` e `FeatureExtractor` no `MedicationFormViewModel`.
- [x] 2.3 Em `MedicationFormViewModel.save()`: antes de chamar `saveMedication()`, extrair
      features de cada `pendingPhoto` via `FeatureExtractor.extract()`; chamar
      `CheckPhotoCollisionUseCase`; se houver colisão, emitir `CollisionWarning` e retornar
      sem persistir. Mostrar `isLoading = true` durante a extração.
- [x] 2.4 Criar `MedicationFormViewModel.saveIgnoringCollision()`: repete o flow de save sem
      a checagem de colisão (para quando o usuário confirma "Salvar assim mesmo").

## 3. UI — dialog de aviso

- [x] 3.1 Em `MedicationFormScreen`, coletar o evento `CollisionWarning` e armazenar o nome do
      candidato em estado local (`collisionCandidateName`).
- [x] 3.2 Exibir `AlertDialog` quando `collisionCandidateName != null`, com:
      - título: "Foto muito parecida"
      - texto: "Esta foto é muito parecida com **[nome]**. O reconhecimento pode falhar —
        considere outra foto com ângulo ou iluminação diferente."
      - botão primário: "Salvar assim mesmo" → chama `viewModel.saveIgnoringCollision()`,
        fecha o dialog
      - botão secundário: "Cancelar" → fecha o dialog (usuário permanece no formulário)
- [x] 3.3 Adicionar strings em `res/values/strings.xml`:
      `collision_warning_title`, `collision_warning_body` (com placeholder `%1$s`),
      `collision_warning_save_anyway`, `collision_warning_cancel`.

## 4. Testes

- [x] 4.1 `CheckPhotoCollisionUseCaseTest`: features sintéticas próximas a um candidato
      cadastrado → retorna `CollisionCandidate` com score ≥ THRESHOLD_CONFIDENT; features
      distintas → lista vazia; candidato do próprio medicamento (`excludeMedicationId`) →
      ignorado.
- [x] 4.2 `MedicationFormViewModelTest`: ao chamar `save()` com pendingPhoto cujo embedding
      provoca colisão, o estado emite evento `CollisionWarning` com o nome correto; chamar
      `saveIgnoringCollision()` após o aviso persiste o medicamento.

## 5. Finalização

- [x] 5.1 Rodar `./gradlew testDebugUnitTest ktlintCheck` e confirmar BUILD SUCCESSFUL.
- [x] 5.2 Atualizar `CHANGELOG.md` (seção "Não lançado" → "Adicionado").
- [x] 5.3 Marcar TD-2 como resolvido em `docs/technical/tech-debt.md`.
- [x] 5.4 Marcar F11 como `✅ feito` em `docs/openspec-plan.md`.
