# AGENTS.md

Guia para agentes de IA e pessoas contribuindo no **Meus Remédios**.
Visão geral e início rápido no [`README.md`](README.md) da raiz.

## O que é o projeto
App Android **100% offline** que confirma visualmente, pela câmera, qual remédio
**pré-cadastrado** é o comprimido em mãos — pensado para idosos, reduzindo erros de medicação.
O reconhecimento compara a foto da câmera **apenas com as fotos cadastradas do próprio
usuário** (sem internet, sem base externa).

Documentação completa em [`docs/`](docs/README.md). Implementação via **OpenSpec SDD**
(ver [`docs/openspec-plan.md`](docs/openspec-plan.md)).

## Stack e restrições
- **Stack:** Kotlin + Jetpack Compose (Material 3), Hilt, Room, CameraX, TensorFlow Lite,
  Coroutines/Flow, WorkManager, AlarmManager.
- **minSdk 24** (Android 7.0 Nougat).
- **Reconhecimento:** híbrido on-device = embeddings TFLite + cor (Lab) + forma/tamanho.
- **Restrições inegociáveis:**
  - **100% offline** — nenhuma chamada de rede; sem permissão `INTERNET` no manifest.
  - Sem login, sem nuvem, sem anúncios nesta fase.
  - Dados e fotos em **armazenamento privado** do app.
  - **Segurança no reconhecimento:** nunca afirmar identidade com baixa confiança; em dúvida,
    pedir 2ª foto ou mostrar candidatos.

## Arquitetura (resumo)
MVVM + camadas, módulo único `app`:
- `ui/<feature>` (Compose + ViewModels) · `domain/model` · `domain/usecase`
- `data/local` (Room) · `data/repository` · `data/ml` (TFLite + cor/forma) · `data/media`
- `notifications/` (AlarmManager + receivers) · `di/` (Hilt)

Detalhes em [`docs/technical/tech-1-arquitetura.md`](docs/technical/tech-1-arquitetura.md).

## Convenções
- Código (identificadores) em **inglês**; textos de UI em **pt-BR**.
- Fluxo de dados unidirecional: `StateFlow → UI`; eventos → ViewModel → UseCase → Repository.
- Injeção via Hilt (permite fakes em testes: recognizer fake, DAO in-memory).
- Lógica de negócio em use cases puros/determinísticos (testáveis fora da UI).
- Acessibilidade é requisito, não opcional (fontes/botões grandes, alto contraste, TalkBack).

## Testes
- **Unit (JVM):** use cases, repos (fake DAO), algoritmo de scoring (vetores sintéticos
  determinísticos), agenda/lembretes, retenção. JUnit + MockK + Turbine + coroutines-test.
- **DB:** Room in-memory (Robolectric/instrumented).
- **UI:** Compose UI tests dos fluxos críticos com fakes via Hilt.
- Rodar: `./gradlew testDebugUnitTest` e `./gradlew connectedCheck` (ou `make test` / `make connected`).
- Ver [`docs/technical/tech-5-estrategia-testes.md`](docs/technical/tech-5-estrategia-testes.md).

## Fluxo OpenSpec SDD
1. Especificar antes de implementar: criar/atualizar a **change** em `openspec/changes/`
   (`proposal.md` + `tasks.md` + deltas de spec por capability).
2. Validar com `openspec validate`, implementar, depois `openspec archive`.
3. Manter rastreabilidade **PRD ↔ capability ↔ change**.
4. As skills `openspec-*` em `.github/skills/` guiam cada etapa.

## Regras para o agente
- **Sempre** atualizar o [`CHANGELOG.md`](CHANGELOG.md) quando fizer mudanças relevantes
  (seção "Não lançado").
- Para build/teste/execução local use os atalhos do [`Makefile`](Makefile)
  (`make run`, `make reopen`, `make test`, `make check`); rode `make help` para a lista.
- **Antes de qualquer commit**, rodar `make fmt` (ktlintFormat) seguido de `make check`
  (testes + ktlintCheck + build). O hook `pre-commit` bloqueia violações de ktlint;
  instale-o com `make install-hooks` em novos clones.
- Manter este `AGENTS.md` e o [`.github/copilot-instructions.md`](.github/copilot-instructions.md)
  sincronizados quando convenções/arquitetura mudarem.
- Não introduzir dependências de rede nem chamadas externas.
- Não criar arquivos de documentação extra sem necessidade; preferir editar os existentes.

## Regras de performance — ML/TF (obrigatório)

> Violações aqui causam jank visível de 1–3 s, "Skipped 100+ frames" e bloqueio de
> InputDispatcher — confirmados em logcat (Davey! 2644 ms, 175 frames skipped).

1. **Nunca inicializar clientes ML na main thread.**
   - Singletons de ML Kit, TFLite `Interpreter` ou qualquer `*Reader`/`*Embedder` NÃO devem
     executar lógica pesada no construtor. Use `by lazy { ... }` para adiar ao primeiro uso.
   - Exemplo do erro: `private val recognizer = TextRecognition.getClient(...)` → bloqueia a
     main thread quando o singleton é criado por Hilt na primeira navegação.
   - Exemplo correto: `private val recognizer by lazy { TextRecognition.getClient(...) }`.

2. **Toda função `suspend` que acessa componente ML deve declarar seu próprio dispatcher.**
   - `ImprintReader.read()` → `withContext(Dispatchers.IO)`.
   - `TfliteFeatureExtractor.extract()` → internamente usa `withContext(Dispatchers.Default)`.
   - Nunca depender do dispatcher do chamador para operações de I/O ou computação pesada.

3. **Warm-up obrigatório no `Application.onCreate()`.**
   - Qualquer novo componente ML (embedder, OCR, segmentação, etc.) deve ter seu primeiro
     acesso disparado em `MeusRemediosApplication.warmUpTflite()` (ou método análogo) via
     `appScope.launch { ... }` em `Dispatchers.Default`.
   - Isso pré-carrega as `.so` nativas antes do usuário interagir com o formulário.

4. **Ao adicionar nova biblioteca ML, verificar:**
   - O construtor do `@Singleton` é leve (sem I/O, sem carregamento de modelo, sem JNI init)?
   - A suspend function que usa a biblioteca declara `withContext` adequado?
   - O warm-up em `MeusRemediosApplication` cobre a nova biblioteca?

## Dívida técnica conhecida

Lista rastreada em [`docs/technical/tech-debt.md`](docs/technical/tech-debt.md).
Ao implementar um item, referenciar `TD-N` no commit e remover/marcar o item do arquivo.

## Mapa de fases
F0 Scaffolding · F1 Dados · F2 Cadastro · F3 Consulta/Relatórios · F4 Reconhecimento ·
F5 Registro de tomadas · F6 Lembretes · F7 Configurações · F8 Acessibilidade · F9 Testes/CI.
