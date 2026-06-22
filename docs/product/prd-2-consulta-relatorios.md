# PRD-2 — Consulta, Listagem e Relatórios

- **Fase:** F3
- **Capabilities OpenSpec:** `medication-catalog`, `reporting`
- **Convenção de IDs:** `RF-2.n` (funcional), `RN-2.n` (regra de negócio)

## Objetivo
Permitir visualizar e encontrar remédios e acompanhar o que foi/precisa ser tomado.

## Usuário / Contexto
Idoso/cuidador consultando a rotina de medicação e o histórico do dia.

## Requisitos funcionais
- **RF-2.1** **Listar** todos os remédios cadastrados (nome, próximos horários, miniatura).
- **RF-2.2** **Buscar** por nome.
- **RF-2.3** **Detalhe** do remédio (dados, fotos, horários, histórico recente).
- **RF-2.4** **Timeline** por dia e por semana com os horários de cada remédio.
- **RF-2.5** **Relatório do dia**: tomados, pendentes e atrasados de hoje.
- **RF-2.6** **Histórico**: tomadas registradas dentro do período de retenção (default
  90 dias).

## Regras de negócio
- **RN-2.1** Itens marcados como tomados aparecem distintos dos pendentes.
- **RN-2.2** Histórico limitado pela retenção configurada (ver [PRD-6](prd-6-configuracoes.md)).
- **RN-2.3** Ordenação por horário do dia; agrupamento por período (manhã/tarde/noite).

## Fluxos
- Home → "Meus remédios" → lista/busca → detalhe.
- Home → "Hoje" → relatório do dia/timeline.

## UI / Acessibilidade
Cards grandes, agrupamento por período do dia com ícones, contraste alto, estados vazios
explicados ("Nenhum remédio cadastrado ainda").

## Critérios de aceite
- Busca filtra corretamente; timeline mostra horários corretos do dia/semana.
- Relatório do dia separa tomados/pendentes/atrasados conforme `IntakeLog`.

## Dependências
F1; consome dados de [PRD-1](prd-1-cadastro.md) e [PRD-5](prd-5-registro-tomadas.md).

## Fora de escopo
Exportar relatório (PDF/compartilhar) — possível refinamento futuro.
