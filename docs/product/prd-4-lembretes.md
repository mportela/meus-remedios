# PRD-4 — Lembretes e Avisos

- **Fase:** F6
- **Capability OpenSpec:** `scheduling-reminders`
- **Convenção de IDs:** `RF-4.n` (funcional), `RN-4.n` (regra de negócio)

## Objetivo
Lembrar o usuário, no horário certo, de tomar o remédio e levá-lo à jornada de reconhecimento.

## Usuário / Contexto
Idoso que esquece horários. O lembrete deve ser confiável mesmo com o app fechado.

## Requisitos funcionais
- **RF-4.1** Agendar lembretes a partir dos horários cadastrados, respeitando os dias da
  semana.
- **RF-4.2** Disparar **alarme exato** mesmo com app fechado/dispositivo em repouso.
- **RF-4.3** Avisar com **antecedência (lead) configurável** (default 1 min) e sugerir o
  clique para **iniciar a jornada principal** de reconhecimento.
- **RF-4.4** Liga/desliga **global** (ver [PRD-6](prd-6-configuracoes.md)) e **por remédio**
  (toggle "avise-me", [PRD-1](prd-1-cadastro.md)).
- **RF-4.5** **Reagendar** todos os lembretes após reboot do dispositivo.
- **RF-4.6** Notificação com ação que abre direto a tela de reconhecimento.

## Regras de negócio
- **RN-4.1** Lembrete só dispara se o global e o do remédio estiverem habilitados.
- **RN-4.2** Em Android 12+ requer permissão de **alarme exato**; tratar a ausência com
  fallback e orientação ao usuário.
- **RN-4.3** Não duplicar lembretes; cancelar/recriar ao editar horários.

## Fluxos
Cadastro/edição de horário → (re)agenda. No horário → notificação → toque → reconhecimento.

## UI / Acessibilidade
Notificação clara em pt-BR ("Hora do seu remédio das HH:mm — toque para conferir"),
som/vibração e ação grande.

## Critérios de aceite
- Lembrete dispara no horário correto (teste com agendamento simulado).
- Reagenda após boot. Respeita os toggles global/por remédio.

## Dependências
F1 (horários/settings), AlarmManager, BroadcastReceiver, BootReceiver, permissões.

## Fora de escopo
Lembretes baseados em localização; snooze avançado (refinamento futuro — snooze simples ok).
