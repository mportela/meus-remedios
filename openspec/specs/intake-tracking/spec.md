# intake-tracking Specification

## Purpose
Especificar o registro de tomadas (confirmadas, puladas ou atrasadas), a tela "Hoje" com cards de dose e o fluxo de confirmação via reconhecimento visual ou toque direto.
## Requirements
### Requirement: Registrar tomada de dose

O sistema SHALL permitir ao usuário marcar uma dose como tomada (TAKEN). A marcação SHALL
ser persistida como `IntakeLog` com status TAKEN e `takenAt = Instant.now()`. A ação SHALL
ser idempotente: marcar uma dose já marcada como TAKEN desfaz o registro (volta a PENDING).

#### Scenario: Marcar dose como tomada

- **WHEN** o usuário aciona a ação "tomei" sobre uma dose pendente ou atrasada
- **THEN** o sistema SHALL criar um `IntakeLog` com status TAKEN para aquela dose
- **AND** SHALL exibir a dose como tomada imediatamente, sem recarregar a tela

#### Scenario: Desmarcar dose tomada

- **WHEN** o usuário aciona a ação "tomei" sobre uma dose já marcada como TAKEN
- **THEN** o sistema SHALL remover o `IntakeLog` correspondente
- **AND** SHALL exibir a dose como pendente ou atrasada conforme o horário atual

#### Scenario: Transição TAKEN → SKIPPED

- **WHEN** o usuário aciona "pular" sobre uma dose já marcada como TAKEN
- **THEN** o sistema SHALL atualizar o `IntakeLog` para status SKIPPED
- **AND** SHALL exibir a dose como pulada imediatamente

### Requirement: Pular dose

O sistema SHALL permitir ao usuário marcar uma dose como pulada (SKIPPED), indicando
conscientemente que aquela dose não será tomada. A ação SHALL ser idempotente: pular
uma dose já marcada como SKIPPED desfaz o registro.

#### Scenario: Pular dose pendente ou atrasada

- **WHEN** o usuário aciona "pular" sobre uma dose pendente ou atrasada
- **THEN** o sistema SHALL criar um `IntakeLog` com status SKIPPED para aquela dose
- **AND** SHALL exibir a dose como pulada imediatamente

#### Scenario: Desfazer dose pulada

- **WHEN** o usuário aciona "desfazer" sobre uma dose marcada como SKIPPED
- **THEN** o sistema SHALL remover o `IntakeLog` correspondente
- **AND** SHALL exibir a dose como pendente ou atrasada conforme o horário

#### Scenario: Transição SKIPPED → TAKEN

- **WHEN** o usuário aciona "tomei" sobre uma dose já marcada como SKIPPED
- **THEN** o sistema SHALL atualizar o `IntakeLog` para status TAKEN
- **AND** SHALL exibir a dose como tomada imediatamente

### Requirement: Registrar tomada a partir do reconhecimento visual

Após identificar um remédio com confiança na tela Confirmar, o sistema SHALL oferecer
ação "Tomei" para registrar a tomada sem sair da tela de reconhecimento.

#### Scenario: Remédio com exatamente uma dose pendente hoje

- **WHEN** o reconhecimento retorna resultado confiante para um medicamento
- **AND** esse medicamento tem exatamente 1 dose pendente ou atrasada no dia de hoje
- **THEN** o sistema SHALL exibir botão "Tomei" na tela de resultado
- **AND** ao tocar, SHALL criar `IntakeLog` com status TAKEN vinculado àquele horário

#### Scenario: Remédio com múltiplas doses pendentes hoje

- **WHEN** o reconhecimento retorna resultado confiante para um medicamento
- **AND** esse medicamento tem 2 ou mais doses pendentes ou atrasadas no dia de hoje
- **THEN** ao tocar "Tomei", o sistema SHALL exibir seletor com os horários disponíveis
- **AND** após o usuário selecionar um horário, SHALL criar o `IntakeLog` correspondente

#### Scenario: Remédio sem dose agendada hoje (tomada ad-hoc)

- **WHEN** o reconhecimento retorna resultado confiante para um medicamento
- **AND** esse medicamento não tem doses agendadas para hoje (ou todas já estão marcadas)
- **THEN** o sistema SHALL exibir botão "Tomei" na tela de resultado
- **AND** ao tocar, SHALL criar `IntakeLog` com status TAKEN e `scheduleTimeId = null`,
  registrando a tomada ad-hoc no histórico do medicamento

#### Scenario: Feedback após registro via reconhecimento

- **WHEN** o usuário registra a tomada a partir da tela Confirmar
- **THEN** o sistema SHALL exibir confirmação visual na tela (botão se transforma em
  "✓ Registrado" ou equivalente) sem navegar para outra tela

### Requirement: Persistência e recuperação

O sistema SHALL persistir todos os registros de tomada localmente via Room, sem acesso
à rede. Os registros SHALL sobreviver ao fechamento e reabertura do app.

#### Scenario: Registros persistidos localmente

- **WHEN** o usuário marca ou pula uma dose e fecha o app
- **THEN** ao reabrir, a dose SHALL exibir o mesmo status persistido

