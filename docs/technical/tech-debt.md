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

## TD-2 — Alerta de foto visualmente similar no cadastro (prevenção de erro de reconhecimento) ✅ resolvido

> Resolvido em F11 (`add-photo-collision-warning`). `CheckPhotoCollisionUseCase` compara
> features das fotos pendentes com as cadastradas; `MedicationFormViewModel` emite
> `CollisionWarning`; `MedicationFormScreen` exibe `AlertDialog` com "Salvar assim mesmo" /
> "Cancelar". 8 testes cobrindo o fluxo.

**Nota de design:** o aviso é especialmente útil para comprimidos brancos redondos sem
imprint (cenário mais comum de falso positivo). Combinar com melhoria da UI de captura
(instrução de iluminação, ângulo) como oportunidade de UX.
