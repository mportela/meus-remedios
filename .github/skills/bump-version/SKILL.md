---
name: bump-version
description: >
  Determina e aplica o próximo bump de versão semântica do projeto (major/minor/patch),
  analisa o CHANGELOG, ajusta o CHANGELOG com a nova seção de release, atualiza
  versionName/versionCode no build.gradle.kts, commita e cria a tag git.
  Use quando o usuário quer lançar uma nova versão ou criar uma release tag.
license: MIT
compatibility: Projeto Android com Gradle Kotlin DSL, CHANGELOG.md no formato Keep a Changelog.
metadata:
  author: marcel.portela@gmail.com
  version: "1.0"
---

Fluxo completo de bump de versão semântica para o projeto **Meus Remédios**.

---

## Passo 1 — Ler o contexto atual

Execute em paralelo:

```bash
grep versionName app/build.gradle.kts
grep versionCode app/build.gradle.kts
git tag -l --sort=-version:refname | head -5
```

Leia também a seção `## [Não lançado]` do `CHANGELOG.md` (do topo até a próxima seção `## [`).

Extraia:
- `CURRENT_VERSION`: valor de `versionName` (ex.: `1.1.0`)
- `CURRENT_CODE`: valor de `versionCode` (ex.: `2`)
- Entradas do "Não lançado": lista de categorias (`### Adicionado`, `### Corrigido`, `### Removido`, `### Alterado`) e seus itens.

---

## Passo 2 — Analisar e propor o bump

Com base nas entradas do "Não lançado", aplique as regras do **Versionamento Semântico**:

| Condição nas entradas | Bump sugerido |
|---|---|
| Há `### Removido` com itens OU breaking change explícito | **major** |
| Há `### Adicionado` com itens (nova funcionalidade) | **minor** |
| Apenas `### Corrigido` e/ou `### Alterado` | **patch** |
| "Não lançado" vazio | Perguntar ao usuário o que mudou |

Calcule as três opções de próxima versão a partir de `CURRENT_VERSION`:
- patch: incrementa Z (1.1.0 → 1.1.1)
- minor: incrementa Y, zera Z (1.1.0 → 1.2.0)
- major: incrementa X, zera Y e Z (1.1.0 → 2.0.0)

**Apresente ao usuário:**
1. Sumário das mudanças detectadas no "Não lançado" (categorias + quantidade de itens)
2. Sua sugestão de bump com racional (ex.: "Sugiro **minor 1.2.0** — há 2 novas funcionalidades em `### Adicionado`")
3. As três opções numeradas para o usuário escolher

Use o **vscode_askQuestions tool** com opções:
- `major (X.0.0)` — mudança incompatível com versões anteriores
- `minor (X.Y.0)` — nova funcionalidade compatível` (recomendada se sugerida)
- `patch (X.Y.Z)` — correção de bug

Permita resposta livre caso o usuário queira especificar diretamente (ex.: "1.3.0").

**Aguarde a resposta antes de prosseguir.**

---

## Passo 3 — Confirmar a versão final

Derive `NEW_VERSION` e `NEW_CODE` (`CURRENT_CODE + 1`) da escolha do usuário.

Se o usuário digitou uma versão livre (ex.: "1.3.0"), valide que:
- Segue o formato `X.Y.Z`
- É maior que `CURRENT_VERSION`

Se inválido, peça para corrigir.

Confirme em texto: "Vou lançar a versão **vX.Y.Z** (versionCode N). Posso prosseguir?"

**Aguarde confirmação (sim/não) antes de aplicar qualquer mudança.**

---

## Passo 4 — Atualizar o CHANGELOG.md

Edite `CHANGELOG.md`:

1. Logo abaixo de `## [Não lançado]`, insira uma linha em branco e a nova seção:
   ```
   ## [X.Y.Z] - YYYY-MM-DD
   ```
   onde `YYYY-MM-DD` é a data atual (use `date +%Y-%m-%d`).

2. Mova **todo o conteúdo** das subseções do "Não lançado" para dentro da nova seção `[X.Y.Z]`.

3. Deixe `## [Não lançado]` vazio (sem subseções).

**Regras:**
- Preserve a ordem das subseções (`### Adicionado`, `### Corrigido`, `### Removido`, `### Alterado`).
- Remova subseções vazias da nova seção (ex.: se não há `### Corrigido`, não inclua).
- Não altere nenhuma seção de versão já existente.

---

## Passo 5 — Atualizar app/build.gradle.kts

Edite `app/build.gradle.kts`:

```kotlin
// Antes:
versionCode = N
versionName = "X.Y.Z"

// Depois:
versionCode = N+1
versionName = "X.Y.Z_novo"
```

Use replace_string_in_file incluindo 2 linhas de contexto antes e depois.

---

## Passo 6 — Validar antes de commitar

Execute:
```bash
./gradlew ktlintCheck --daemon -q
```

Se houver falhas, rode `./gradlew ktlintFormat` e reporte ao usuário antes de continuar.

---

## Passo 7 — Commit, tag e push

Execute em sequência:

```bash
git add CHANGELOG.md app/build.gradle.kts
git commit -m "chore: bump version to X.Y.Z"
git tag vX.Y.Z
git push origin main --tags
```

Reporte ao usuário:
- Versão lançada: `vX.Y.Z`
- Tag criada e publicada
- Workflow do GitHub Actions disparado (se `.github/workflows/release.yml` existir e o trigger for tag `v*.*.*`)

---

## Guardrails

- **Nunca** aplique mudanças antes da confirmação do usuário no Passo 3.
- Se `## [Não lançado]` estiver vazio, avise o usuário: "Não há mudanças documentadas em 'Não lançado'. Tem certeza que quer criar uma release?" e aguarde confirmação.
- Não altere seções de versão já existentes no CHANGELOG.
- Não faça push de outras branches além de `main`.
- Se o push falhar (ex.: conflito remoto), reporte o erro sem tentar resolver automaticamente — oriente o usuário a fazer `git pull --rebase` e re-executar o Passo 7.
