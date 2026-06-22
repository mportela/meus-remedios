# PRD-5 — Registro de Tomadas (Marcar como Tomado)

- **Fase:** F5
- **Capability OpenSpec:** `intake-tracking`
- **Convenção de IDs:** `RF-5.n` (funcional), `RN-5.n` (regra de negócio)

## Objetivo
Registrar que um remédio foi tomado, evitando esquecimento ou dose duplicada.

## Usuário / Contexto
Idoso confirmando que tomou — durante a identificação pela câmera ou depois, pela lista.

## Requisitos funcionais
- **RF-5.1** Marcar **tomado** diretamente no resultado da jornada de reconhecimento
  (ver [PRD-3](prd-3-reconhecimento.md)).
- **RF-5.2** Marcar **tomado** posteriormente, fora da câmera (na lista/relatório do dia).
- **RF-5.3** **Sugerir** os remédios do dia ainda **não marcados** como tomados.
- **RF-5.4** Registrar data/hora da tomada em `IntakeLog`; permitir desfazer marcação recente.
- **RF-5.5** Status por ocorrência: pendente, tomado, pulado/atrasado.

## Regras de negócio
- **RN-5.1** Uma marcação vincula remédio + horário agendado + data.
- **RN-5.2** Evitar marcação duplicada do mesmo horário/dia.
- **RN-5.3** Registros respeitam a retenção de histórico (ver
  [PRD-6](prd-6-configuracoes.md)).

## Fluxos
- Reconhecimento → "Tomei agora".
- Relatório do dia → item pendente → "Marcar como tomado".

## UI / Acessibilidade
Botão "Tomei" grande e claro; confirmação visual; lista de pendentes destacada.

## Critérios de aceite
- Marcar/Desmarcar reflete no relatório do dia e no histórico.
- Sugestão de pendentes corresponde aos horários não registrados de hoje.

## Dependências
F1; integra [PRD-2](prd-2-consulta-relatorios.md) e [PRD-3](prd-3-reconhecimento.md).

## Fora de escopo
Registro de efeitos colaterais/sintomas (refinamento futuro).
