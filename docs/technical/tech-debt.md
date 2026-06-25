# Dívida Técnica — Meus Remédios

Itens conhecidos priorizados para resolução futura. Cada item tem um identificador `TD-N`
para rastreabilidade em commits e PRs.

---

## TD-1 — ~~Validação de nome único no cadastro de medicamento~~ ✅ resolvido

> Resolvido na change `fix-duplicate-medication-name` (F10). Commit referência: ver git log.

**Área:** Cadastro (F2) · `domain/usecase/SaveMedicationUseCase` · `ui/medications/form`

**Problema:**
O app permite cadastrar dois medicamentos com o mesmo nome (ex.: "Caltrat" duas vezes).
Isso não quebra nada imediatamente, mas gera confusão no reconhecimento (dois candidatos com
o mesmo nome no ranking) e na listagem do usuário.

**Comportamento esperado:**
- Ao salvar um medicamento, verificar se já existe outro com o mesmo nome (case-insensitive,
  trim) no banco de dados.
- Se existir, exibir erro de validação no campo "Nome do remédio" antes de persistir.
- Edição do próprio medicamento deve ser excluída da checagem (validar `id != self.id`).

**Impacto se não resolvido:** baixo (não quebra reconhecimento, apenas gera duplicatas
confusas na lista e no ranking).

**Sugestão de implementação:**
- Adicionar `MedicationDao.existsByName(name: String, excludeId: Long): Boolean`.
- Incluir a checagem em `SaveMedicationUseCase` e um novo `MedicationValidationError.DuplicateName`.
- Exibir mensagem de erro abaixo do campo no formulário.
- Cobrir com teste unitário (DAO in-memory + use case).

---

## TD-2 — Alerta de foto visualmente similar no cadastro (prevenção de erro de reconhecimento)

**Área:** Cadastro de fotos (F2/F4) · `domain/usecase/AddMedicationPhotoUseCase` ·
`ui/medications/form/MedicationFormViewModel`

**Problema:**
Ao adicionar uma foto frente/verso a um medicamento, o sistema extrai features (embedding,
cor, forma) mas não as compara com as fotos já cadastradas de outros medicamentos. Se o
usuário cadastrar dois comprimidos visualmente idênticos (mesma cor, forma e imprint), o
reconhecimento posterior será ambíguo — e o problema só aparece em runtime, não no cadastro.

**Comportamento esperado:**
Ao salvar as fotos de um medicamento (no momento do `Salvar`), executar a engine de
reconhecimento entre as features da(s) foto(s) recém-adicionadas e as fotos de todos os
outros medicamentos já cadastrados:
- Se o score top-1 de algum outro medicamento ultrapassar `THRESHOLD_CONFIDENT`, exibir um
  aviso (não erro bloqueante): *"Esta foto é muito parecida com [Medicamento X]. O
  reconhecimento pode falhar. Considere tirar uma foto com ângulo ou iluminação diferente."*
- O usuário pode ignorar o aviso e salvar mesmo assim (não bloquear o cadastro).
- O aviso deve mencionar qual medicamento é o mais similar e o score aproximado (debug/dev)
  ou apenas o nome (produção).

**Impacto se não resolvido:** médio — usuários descobrirão o problema apenas quando o
reconhecimento errar em situação real, sem feedback preventivo.

**Sugestão de implementação:**
- Criar `CheckPhotoCollisionUseCase(features: PhotoFeatures): List<CollisionCandidate>`
  que reutiliza `RecognitionEngine` internamente.
- Chamar no `MedicationFormViewModel.save()` antes de navegar de volta, se houver fotos
  pendentes com embedding não-nulo.
- Exibir `Snackbar` ou dialog de aviso não-bloqueante com opção "Salvar assim mesmo" /
  "Tirar nova foto".
- Cobrir com teste unitário: features sintéticas de dois medicamentos similares devem
  disparar o aviso; features diferentes não devem.

**Nota de design:** o aviso é especialmente útil para comprimidos brancos redondos sem
imprint (cenário mais comum de falso positivo). Combinar com melhoria da UI de captura
(instrução de iluminação, ângulo) como oportunidade de UX.
