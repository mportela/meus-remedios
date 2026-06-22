## ADDED Requirements

### Requirement: Persistência local de medicamentos
O sistema SHALL persistir medicamentos com seus atributos (nome, dosagem opcional,
observações, tipo de período, datas de início/fim opcionais, flag de lembretes e
data de criação) em um banco de dados local Room.

#### Scenario: Inserir e recuperar medicamento
- **WHEN** um medicamento é inserido na camada de dados
- **THEN** ele pode ser recuperado com todos os seus atributos preservados

#### Scenario: Listagem reativa por nome
- **WHEN** um observador assina a lista de medicamentos
- **THEN** recebe a lista ordenada por nome e atualizações automáticas a cada
  mudança no banco

### Requirement: Fotos do medicamento com features de reconhecimento
O sistema SHALL persistir fotos associadas a um medicamento, incluindo caminho do
arquivo, lado (frente/verso), embedding, cor dominante em Lab e proporção, com
relação de chave estrangeira ao medicamento.

#### Scenario: Foto vinculada ao medicamento
- **WHEN** uma foto é inserida referenciando um medicamento existente
- **THEN** a foto é recuperável ao consultar as fotos daquele medicamento

#### Scenario: Remoção em cascata
- **WHEN** um medicamento é removido
- **THEN** suas fotos associadas também são removidas

### Requirement: Horários de agendamento por medicamento
O sistema SHALL persistir horários (`ScheduleTime`) com hora do dia e máscara de
dias da semana, associados a um medicamento por chave estrangeira.

#### Scenario: Horário recuperável por medicamento
- **WHEN** um horário é inserido para um medicamento
- **THEN** ele aparece ao consultar os horários daquele medicamento

### Requirement: Registro de tomadas
O sistema SHALL persistir logs de tomada (`IntakeLog`) com data, horário agendado,
horário de tomada opcional e status (pendente/tomado/pulado/atrasado), associados
a um medicamento e, opcionalmente, a um horário.

#### Scenario: Consultar tomadas por data
- **WHEN** logs de tomada são consultados por uma data específica
- **THEN** apenas os logs daquela data são retornados

### Requirement: Configurações do aplicativo como singleton
O sistema SHALL persistir as configurações do app (`AppSettings`) como um registro
único, com valores padrão (retenção de 90 dias, lead de lembrete de 1 minuto) e
permitir sua leitura e atualização.

#### Scenario: Valores padrão na primeira leitura
- **WHEN** as configurações são lidas pela primeira vez
- **THEN** o sistema retorna os valores padrão definidos

#### Scenario: Atualização persistida
- **WHEN** uma configuração é alterada
- **THEN** a nova configuração é persistida e refletida em leituras subsequentes

### Requirement: Conversão de tipos de domínio
O sistema SHALL converter tipos de domínio para formatos persistíveis: datas e
horas em texto ISO, vetores `FloatArray` em BLOB e enums em texto, de forma
reversível e determinística.

#### Scenario: Round-trip de tipos
- **WHEN** um valor de data, hora, `FloatArray` ou enum é convertido para o
  formato de banco e de volta
- **THEN** o valor recuperado é igual ao valor original

### Requirement: Persistência 100% local
O sistema SHALL armazenar todos os dados exclusivamente no banco local em
armazenamento privado do app, sem qualquer acesso à rede.

#### Scenario: Operação sem conectividade
- **WHEN** o dispositivo está sem internet
- **THEN** todas as operações de leitura e escrita de dados funcionam normalmente
