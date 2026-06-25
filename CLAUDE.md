# CLAUDE.md

Instruções específicas para Claude Code trabalhando no **Meus Remédios**.

## Início rápido

- **Documentação:** [`AGENTS.md`](AGENTS.md) (arquitetura, stack, convenções, fluxo OpenSpec)
- **IA instructions:** [`.github/ai-instructions.md`](.github/ai-instructions.md) (princípios inegociáveis, stack, testes)
- **Roadmap:** [`docs/product/roadmap.md`](docs/product/roadmap.md) (fases de desenvolvimento)
- **OpenSpec plan:** [`docs/openspec-plan.md`](docs/openspec-plan.md) (rastreamento de changes)

## Regras essenciais

### Builds, testes e commits
- Use `make` atalhos: `make run`, `make test`, `make check`, `make fmt` (veja `make help`)
- **Antes de qualquer commit:** rodar `make fmt && make check`
- Pre-commit hook bloqueia ktlint; instale com `make install-hooks`
- Sempre atualizar [`CHANGELOG.md`](CHANGELOG.md) (seção "Não lançado")

### OpenSpec SDD workflow
1. Especificar antes de codar: criar/atualizar change em `openspec/changes/`
2. Validar com `openspec validate`, implementar, depois `openspec archive`
3. Marcar `✅ feito` em [`docs/openspec-plan.md`](docs/openspec-plan.md) ao concluir

### Performance — ML/TensorFlow (obrigatório)
Violações causam jank visível de 1–3s e bloqueio de InputDispatcher (confirmado em logcat).

1. **Nunca inicializar ML na main thread** — use `by lazy { ... }` para singletons
2. **Toda suspend function com ML** deve declarar seu dispatcher: `withContext(Dispatchers.IO)` ou `Dispatchers.Default`
3. **Warm-up obrigatório** em `MeusRemediosApplication.warmUpTflite()` via `appScope.launch { ... }`
4. Ao adicionar nova biblioteca ML, verificar construtor leve, suspend com withContext, warm-up

## Stack

Kotlin · Jetpack Compose · Hilt · Room · CameraX · TensorFlow Lite · Coroutines/Flow ·
WorkManager · AlarmManager. **minSdk 24**.

## Princípios inegociáveis

- **100% offline** — nenhuma chamada de rede; sem permissão `INTERNET`
- **Privacidade:** dados/fotos em armazenamento privado do app
- **Sem login, nuvem, anúncios** nesta fase
- **Segurança no reconhecimento:** nunca afirmar com baixa confiança; em dúvida, pedir 2ª foto ou mostrar candidatos

## Dívida técnica

Rastreada em [`docs/technical/tech-debt.md`](docs/technical/tech-debt.md).
Ao implementar um item, referenciar `TD-N` no commit e remover/marcar do arquivo.
