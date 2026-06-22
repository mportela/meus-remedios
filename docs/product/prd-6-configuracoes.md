# PRD-6 — Configurações

- **Fase:** F7
- **Capability OpenSpec:** `app-settings`
- **Convenção de IDs:** `RF-6.n` (funcional), `RN-6.n` (regra de negócio)

## Objetivo
Controlar comportamentos globais do app de forma simples.

## Usuário / Contexto
Idoso/cuidador ajustando preferências básicas.

## Requisitos funcionais
- **RF-6.1** **Retenção de histórico** configurável; **default 90 dias**; job de limpeza
  periódica.
- **RF-6.2** **Auto-captura** ligado/desligado (focou → tira a foto automaticamente).
- **RF-6.3** **Lembretes globais** ligado/desligado e **lead time** (default 1 min).
- **RF-6.4** Ajustes de **acessibilidade** (tamanho de fonte/alto contraste) — se aplicável.
- **RF-6.5** Tela "Sobre" com disclaimer (auxílio de confirmação visual, não substitui médico)
  e confirmação de que o app é offline.

## Regras de negócio
- **RN-6.1** A limpeza remove `IntakeLog` mais antigos que a retenção; **nunca** apaga
  cadastros.
- **RN-6.2** Alterar lembretes globais reflete no agendamento (ver
  [PRD-4](prd-4-lembretes.md)).

## Fluxos
Configurações → ajusta toggle/valor → persiste e reflete no comportamento (câmera/lembretes/
limpeza).

## UI / Acessibilidade
Lista de opções com rótulos grandes, descrição curta em pt-BR e estados claros (ligado/
desligado).

## Critérios de aceite
- Mudar retenção e rodar a limpeza remove apenas o histórico antigo (teste de retenção).
- Toggles persistem e afetam câmera/lembretes conforme esperado.

## Dependências
F1 (AppSettings); integra [PRD-3](prd-3-reconhecimento.md) e [PRD-4](prd-4-lembretes.md).

## Fora de escopo
Backup/restore; exportação de dados (refinamento futuro).
