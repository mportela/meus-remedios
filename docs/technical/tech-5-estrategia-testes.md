# TECH-5 — Estratégia de Testes (automação local)

## Objetivos
Permitir adicionar features com segurança; evitar regressão de lógica e de telas-chave.

## Camadas de teste
- **Unitários (JVM)**: use cases, repositórios (fake DAO), **algoritmo de scoring** (vetores
  sintéticos determinísticos), cálculo de agenda/lembretes, política de retenção.
  Ferramentas: JUnit, MockK, Turbine (Flows), kotlinx-coroutines-test.
- **DB**: Room **in-memory** (Robolectric ou instrumented) — DAOs, migrações.
- **UI (Compose)**: testes de instrumentação/Robolectric para fluxos críticos: cadastrar
  remédio, listar/buscar, marcar como tomado, render do card de resultado — com **recognizer
  fake** e **DAO fake** injetados via **Hilt test**.
- **Verificação offline**: teste que garante ausência de permissão/uso de rede.

## Convenções
- Pirâmide: muitos unit, alguns DB/UI.
- Fakes injetáveis via Hilt.
- Dados de teste determinísticos.

## Execução / CI
- Local: `./gradlew test` (unit/Robolectric) e `./gradlew connectedCheck` (instrumented).
- CI opcional (GitHub Actions/GitLab): roda `test` em cada MR; lint/format (ktlint/detekt).

## Casos prioritários (anti-regressão)
| Área | Teste |
|------|-------|
| Scoring | ranking determinístico com vetores sintéticos |
| Reconhecimento | caso confiante × caso ambíguo (pede 2ª foto) |
| Cadastro | salvar gera features persistidas |
| Tomadas | marcar/desmarcar reflete no relatório do dia |
| Retenção | limpeza remove só histórico antigo |
| Lembretes | agenda no horário e reagenda após boot |
| Offline | sem permissão/uso de rede |
