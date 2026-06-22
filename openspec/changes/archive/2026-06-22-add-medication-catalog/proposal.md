## Why

Com a camada de dados (F1) pronta, o usuário ainda não tem como cadastrar,
consultar, editar ou excluir seus remédios. O **catálogo de medicamentos**
(PRD-1) é a primeira feature funcional visível e a porta de entrada do app:
sem itens cadastrados não há agenda, lembretes nem reconhecimento. Esta change
entrega o CRUD de medicamentos com horários, em UI acessível (idosos), sobre os
repositórios da F1. As **fotos e extração de features** (RF-1.4/1.5/1.8) ficam na
change seguinte (`add-medication-photos`).

## What Changes

- Adicionar use cases de catálogo em `domain/usecase`: observar lista, buscar por
  nome, obter por id, adicionar, atualizar e excluir medicamento, com validações
  de regra de negócio (nome obrigatório; `endDate ≥ startDate`).
- Gerenciar os **horários** (`ScheduleTime`) junto ao formulário do medicamento
  (adicionar/remover/atualizar horários e dias da semana de um medicamento).
- Adicionar a feature de UI `ui/medications` em Jetpack Compose:
  - **Lista** de remédios com busca por nome e botão "Adicionar".
  - **Formulário** de cadastro/edição (nome, dosagem, observações, tipo de
    período, datas opcionais, horários, dias da semana e toggle "avise-me").
  - **Exclusão** com confirmação.
- Adicionar `ui/navigation` (NavHost) ligando lista ↔ formulário.
- Adicionar ViewModels (MVVM + `StateFlow`) que orquestram os use cases.
- Cobrir use cases e ViewModels com testes determinísticos (JVM, fakes de repo).

## Capabilities

### New Capabilities
- `medication-catalog`: cadastro, edição, exclusão, listagem e busca de
  medicamentos com seus horários — a base do app sobre a qual agenda, lembretes,
  relatórios e reconhecimento operam.

### Modified Capabilities
<!-- Nenhuma. -->

## Impact

- **Novo código**: `domain/usecase` (use cases de catálogo), `ui/medications`
  (telas + ViewModels), `ui/navigation` (NavHost).
- **Sem novas dependências**: usa Compose, Hilt, Navigation e repositórios da F1.
- **Sem rede**: toda a feature opera sobre a persistência local.
- **Acessibilidade**: campos/botões grandes, rótulos e ajuda em pt-BR, semântica
  para TalkBack, conforme TECH-4.
- **Fora de escopo**: fotos do comprimido e extração de features
  (`add-medication-photos`); reconhecimento (F4); lembretes/alarmes (F6).
- **Documentação**: atualização do `CHANGELOG.md` (seção "Não lançado").
