# Contribuindo com o Meus Remédios

Obrigado pelo interesse em contribuir! Este é um projeto **100% offline, gratuito e
open-source** (licença [GPL-3.0](LICENSE)), com foco em **acessibilidade** e **impacto
social** — especialmente para o público idoso. Toda contribuição que respeite esses
princípios é bem-vinda.

Antes de começar, leia também o [Código de Conduta](CODE_OF_CONDUCT.md).

## Princípios inegociáveis

Qualquer contribuição **deve** respeitar:

- **100% offline** — nenhuma chamada de rede; o app não declara a permissão `INTERNET`.
- **Privacidade** — dados e fotos ficam em armazenamento privado do app; sem login, nuvem,
  anúncios ou telemetria.
- **Acessibilidade** — fontes/botões grandes, alto contraste e suporte a TalkBack são
  requisitos, não opcionais.
- **Segurança no reconhecimento** — nunca afirmar um resultado com baixa confiança; em
  dúvida, pedir 2ª foto ou mostrar candidatos.

PRs que violem qualquer um desses pontos não serão aceitos.

## Stack

Kotlin · Jetpack Compose (Material 3) · Hilt · Room · CameraX · TensorFlow Lite ·
Coroutines/Flow · WorkManager · AlarmManager. **minSdk 24** (Android 7.0).

Documentação de arquitetura e convenções em [`AGENTS.md`](AGENTS.md) e
[`.github/ai-instructions.md`](.github/ai-instructions.md).

## Como configurar o ambiente

1. Faça o fork e clone o repositório.
2. Instale o hook de pre-commit (bloqueia violações de ktlint):
   ```bash
   make install-hooks
   ```
3. Veja os atalhos disponíveis:
   ```bash
   make help
   ```

## Fluxo de desenvolvimento (OpenSpec SDD)

O projeto segue **Spec-Driven Development**: especificar antes de implementar.

1. **Especificar** — criar/atualizar uma change em `openspec/changes/`
   (`proposal.md` + `tasks.md`; `design.md`/`specs/` quando aplicável).
2. **Validar** — `openspec validate <change>`.
3. **Implementar** — escrever o código seguindo as tasks.
4. **Verificar** — rodar `make fmt && make check` (ver abaixo).
5. **Arquivar** — `openspec archive <change>` e marcar `✅ feito` em
   [`docs/openspec-plan.md`](docs/openspec-plan.md).

As skills `openspec-*` em `.github/skills/` guiam cada etapa.

## Antes de commitar (obrigatório)

```bash
make fmt    # ktlintFormat
make check  # testes + ktlintCheck + build
```

- O hook `pre-commit` bloqueia código fora do padrão ktlint.
- **Sempre** atualize o [`CHANGELOG.md`](CHANGELOG.md) (seção "Não lançado") quando fizer
  mudanças relevantes.
- Atenção às **regras de performance ML/TF** descritas em [`AGENTS.md`](AGENTS.md)
  (nunca inicializar ML na main thread, `withContext` nas suspend functions, warm-up).

## Convenções

- **Identificadores em inglês; textos de UI em pt-BR.**
- **Mensagens de commit** no formato `tipo: descrição` (ex.: `feat:`, `fix:`, `docs:`,
  `ci:`, `chore:`, `refactor:`, `test:`), em pt-BR e no imperativo.
- Ao implementar um item de dívida técnica, referencie `TD-N` no commit
  (ver [`docs/technical/tech-debt.md`](docs/technical/tech-debt.md)).
- Não introduza dependências de rede nem chamadas externas.
- Prefira editar a documentação existente a criar arquivos novos.

## Abrindo um Pull Request

1. Crie um branch a partir de `main`.
2. Garanta que `make check` passa e que o `CHANGELOG.md` foi atualizado.
3. Descreva **o quê** e **por quê** no PR; vincule a change/issue relacionada.
4. PRs pequenos e focados são revisados mais rápido.

## Reportando bugs ou sugerindo melhorias

Abra uma [issue](https://github.com/mportela/meus-remedios/issues) descrevendo:

- O que aconteceu vs. o esperado (passos para reproduzir, se for bug).
- Versão do app e do Android, modelo do aparelho (quando relevante).
- Para sugestões: o problema do usuário que a ideia resolve.

## Licença

Ao contribuir, você concorda que sua contribuição será licenciada sob a
[GNU GPL-3.0](LICENSE), a mesma licença do projeto.
