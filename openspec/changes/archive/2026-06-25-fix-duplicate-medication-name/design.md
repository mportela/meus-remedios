## Context

O `SaveMedicationUseCase` persiste um medicamento sem verificar se outro com o mesmo nome
já existe no banco. A UI não exibe nenhum feedback de unicidade. Duas entradas com o mesmo
nome confundem a listagem e introduzem candidatos redundantes no ranking de reconhecimento.

A validação deve ocorrer na camada de domínio (use case), não no DAO nem na UI, para manter
a lógica de negócio testável e desacoplada.

## Goals / Non-Goals

**Goals:**
- Impedir persistência de medicamentos com nome duplicado (case-insensitive, trim).
- Exibir erro inline no campo "Nome do remédio" no formulário.
- Garantir que editar um medicamento mantendo o mesmo nome não dispara erro.

**Non-Goals:**
- Normalização avançada de nomes (ex.: ignorar acentos) — escopo futuro.
- Validação de duplicatas por princípio ativo ou fórmula química.
- Migração de dados já duplicados existentes no banco (itens cadastrados antes desta change
  permanecem; usuários verão o conflito apenas ao tentar salvar novamente).

## Decisions

### D1 — Validação no use case, não no DAO
A checagem `existsByName` é feita dentro de `SaveMedicationUseCase`, antes de persistir.
O DAO expõe apenas a query SQL; a regra de negócio fica no domínio.
**Alternativa descartada:** constraint `UNIQUE` no Room — quebraria silenciosamente com
exception, sem mensagem amigável; também bloquearia migração de dados já existentes.

### D2 — Erro por sealed class / enum value
`SaveMedicationUseCase` retorna `Result<Unit, MedicationValidationError>` (ou lança exceção
tipada). `MedicationValidationError.DuplicateName` é o novo valor.
**Alternativa descartada:** `Boolean` de retorno — não carrega semântica suficiente para
a UI distinguir entre erros futuros.

### D3 — Exclusão do próprio id na checagem
A query SQL usa `WHERE name = :name COLLATE NOCASE AND id != :excludeId`. `excludeId = 0L`
para novos medicamentos (id 0 nunca existirá no banco por auto-incremento).

## Risks / Trade-offs

- **Dados já duplicados**: usuários que já cadastraram duplicatas não serão afetados
  até tentar salvar novamente. Risco: baixo (edição de nome não é fluxo frequente).
  Mitigação: documentar no CHANGELOG que dados antigos não são migrados.

- **Race condition**: dois saves simultâneos do mesmo nome poderiam passar pela checagem.
  Risco: desprezível (app single-user, single-thread de escrita).
