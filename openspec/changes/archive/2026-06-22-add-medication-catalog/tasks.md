## 1. Use cases de catálogo (`domain/usecase`)

- [x] 1.1 `ObserveMedicationsUseCase` (lista reativa ordenada por nome)
- [x] 1.2 `SearchMedicationsUseCase` (filtra por consulta de nome)
- [x] 1.3 `GetMedicationUseCase` (carrega medicamento + horários por id)
- [x] 1.4 `SaveMedicationUseCase` (cria/atualiza medicamento + horários, valida
      nome obrigatório e `endDate ≥ startDate`, retorna `Result`)
- [x] 1.5 `DeleteMedicationUseCase` (exclui medicamento; horários caem em cascade)

## 2. ViewModels (`ui/medications`)

- [x] 2.1 `MedicationListViewModel` (estado de lista + busca via `StateFlow`)
- [x] 2.2 `MedicationFormViewModel` (estado do formulário, carga para edição,
      gestão de horários, salvar com tratamento de erros de validação)

## 3. UI Compose (`ui/medications`)

- [x] 3.1 `MedicationListScreen` (lista acessível, campo de busca, FAB adicionar,
      estado vazio em pt-BR)
- [x] 3.2 `MedicationFormScreen` (nome, dosagem, observações, tipo de período,
      datas opcionais, horários + dias da semana, toggle "avise-me", salvar)
- [x] 3.3 Componentes de horário (lista editável de `ScheduleTime` + seletor de
      hora e chips de dias da semana)
- [x] 3.4 Exclusão com diálogo de confirmação
- [x] 3.5 Acessibilidade: rótulos/`contentDescription` pt-BR, alvos ≥ 48dp,
      semântica de cabeçalho e agrupamento

## 4. Navegação (`ui/navigation`)

- [x] 4.1 `NavHost` com rotas `medications` e `medications/form?id={id}`
- [x] 4.2 Ligar `MainActivity` ao `NavHost` (substituir placeholder da Home)

## 5. Strings e acessibilidade

- [x] 5.1 Adicionar strings pt-BR (rótulos, ajudas, mensagens de erro/sucesso)

## 6. Testes e verificação

- [x] 6.1 Testes de `SaveMedicationUseCase` (validações + persistência via fakes)
- [x] 6.2 Testes de `ObserveMedicationsUseCase`/`SearchMedicationsUseCase`
      (ordenação, filtro) com fake de repositório e Turbine
- [x] 6.3 Testes de `MedicationFormViewModel` (carga, validação, salvar) e
      `MedicationListViewModel` (busca) com coroutines-test
- [x] 6.4 Rodar `./gradlew test` e `./gradlew assembleDebug`
- [x] 6.5 Atualizar `CHANGELOG.md` (seção "Não lançado")
