# Skills — Meus Remédios

Guia de skills customizadas disponíveis para **GitHub Copilot** e **Claude Code**.

As skills são procedimentos de IA especializados que automatizam tarefas comuns do projeto.
Você pode ativar uma skill digitando uma frase natural relacionada à tarefa.

---

## bump-version

**O que faz:** Automatiza lançamento de novas versões com bumping semântico, atualização de CHANGELOG, badge no README e tags git.

**Quando usar:**
- `"bump release"` ou `"vou lançar uma nova versão"`
- `"lançar versão 1.3.0"`
- Qualquer contexto envolvendo versionamento

**Fluxo:**
1. Analisa `CHANGELOG.md` (seção "Não lançado")
2. Propõe bump (major/minor/patch) seguindo Versionamento Semântico
3. Confirma com você antes de qualquer mudança
4. Atualiza:
   - `app/build.gradle.kts` (versionCode, versionName)
   - `README.md` (badge de versão)
   - `CHANGELOG.md` (reorganiza "Não lançado" → nova seção [X.Y.Z])
5. Commita, cria tag `vX.Y.Z` e faz push
6. Dispara GitHub Actions para build de release assinada

**Localização:** `.github/skills/bump-version/SKILL.md`

**Compatibilidade:** Copilot ✅ | Claude Code ✅

---

## openspec-* (OpenSpec Workflow)

Skills para o fluxo de **OpenSpec SDD** (Specification-Driven Development).

### openspec-new-change
Cria uma nova change com proposta, tasks e especificações.

**Quando usar:**
- `"criar nova change"`
- `"quero adicionar uma feature"`
- `"vai criar uma feature de X"`

**Localização:** `.github/skills/openspec-new-change/SKILL.md`

### openspec-apply-change
Implementa tasks de uma change existente.

**Quando usar:**
- `"implementar a change"` ou `"vou implementar"`
- Quando mudanças já foram propostas e você quer começar a codar

**Localização:** `.github/skills/openspec-apply-change/SKILL.md`

### openspec-archive-change
Finaliza e arquiva uma change após implementação completa.

**Quando usar:**
- `"arquivar a change"` ou `"terminou a implementação"`
- Depois de verificar que tudo está pronto

**Localização:** `.github/skills/openspec-archive-change/SKILL.md`

### openspec-verify-change
Valida que a implementação corresponde aos artefatos da change.

**Quando usar:**
- `"verificar a implementação"`
- Antes de arquivar, para garantir coerência

**Localização:** `.github/skills/openspec-verify-change/SKILL.md`

**Documentação completa:** [`docs/openspec-plan.md`](docs/openspec-plan.md)

**Compatibilidade:** Copilot ✅ | Claude Code ✅

---

## Convenções

- **Skills são acionadas automaticamente** pela descrição que você oferece — não é necessário nomear a skill explicitamente
- Se sua solicitação corresponde a uma skill, o agente vai usá-la para guiar o trabalho
- Você sempre pode pedir mais controle: *"não use a skill, faça manualmente"* ou detalhar mais o que quer
- Skills documentam procedimentos, mas o agente adapta conforme necessário

---

## Como as skills funcionam

1. **Reconhecimento:** Você digita uma solicitação natural
2. **Seleção:** O agente reconhece a intenção e carrega a skill relevante
3. **Execução:** A skill define os passos, o agente executa seguindo os guardrails
4. **Confirmação:** Antes de mudanças críticas, você é consultado
5. **Rastreamento:** Mudanças são commitadas com contexto apropriado

---

## Para contribuidores

As skills estão em `.github/skills/` e seguem o padrão:

```
.github/skills/
├── bump-version/
│   └── SKILL.md
├── openspec-new-change/
│   └── SKILL.md
├── openspec-apply-change/
│   └── SKILL.md
└── ... (outras skills)
```

Cada `SKILL.md` começa com frontmatter YAML:

```yaml
---
name: skill-name
description: Descrição breve
license: MIT
compatibility: Descrição do que é necessário
agents:
  - copilot
  - claude-code
metadata:
  author: seu-email
  version: "1.0"
---
```

O campo `agents` deve listar os agentes suportados. Se suportar ambos:

```yaml
agents:
  - copilot
  - claude-code
```

---

## Referências

- **Documentação técnica:** [`docs/technical/`](docs/technical/)
- **Roadmap:** [`docs/product/roadmap.md`](docs/product/roadmap.md)
- **Instruções Copilot:** [`copilot-instructions.md`](copilot-instructions.md)
- **Instruções Claude Code:** [`CLAUDE.md`](CLAUDE.md)
- **AI Instructions gerais:** [`.github/ai-instructions.md`](.github/ai-instructions.md)
