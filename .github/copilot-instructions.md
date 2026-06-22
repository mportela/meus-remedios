# Instruções do Copilot — Meus Remédios

> App Android **100% offline** que confirma visualmente, pela câmera, qual remédio
> **pré-cadastrado** é o comprimido em mãos. Público idoso. Veja [`AGENTS.md`](../AGENTS.md) e
> [`docs/README.md`](../docs/README.md).

## Princípios inegociáveis
- **Offline total:** nenhuma chamada de rede; sem permissão `INTERNET` no manifest.
- **Privacidade:** dados e fotos somente em armazenamento privado do app.
- **Sem** login, nuvem, anúncios ou base externa de medicamentos nesta fase.
- **Segurança no reconhecimento:** nunca afirmar identidade com baixa confiança; em dúvida,
  pedir 2ª foto (verso) ou mostrar candidatos. Confirmação visual é auxílio, não diagnóstico.

## Stack
Kotlin · Jetpack Compose (Material 3) · Hilt · Room · CameraX · TensorFlow Lite ·
Coroutines/Flow · WorkManager · AlarmManager. **minSdk 24**.

## Arquitetura e convenções
- MVVM + camadas, módulo único `app`: `ui/<feature>`, `domain/model`, `domain/usecase`,
  `data/local` (Room), `data/repository`, `data/ml`, `data/media`, `notifications/`, `di/`.
- Fluxo unidirecional: `StateFlow → UI`; eventos → ViewModel → UseCase → Repository.
- Identificadores em **inglês**; textos de UI em **pt-BR**.
- Lógica em use cases puros/determinísticos; DI via Hilt para permitir fakes em testes.
- **Acessibilidade é requisito:** fontes/botões grandes, alto contraste, TalkBack,
  linguagem simples.

## Reconhecimento (resumo)
`score = w1·cos(embedding) + w2·sim(cor Lab) + w3·sim(forma)`. Confirma quando
`top1 ≥ THRESHOLD_CONFIDENT` e `(top1 − top2) ≥ MARGIN`; senão, fluxo de 2ª foto. Tudo
on-device. Detalhes em
[`docs/technical/tech-3-engine-reconhecimento.md`](../docs/technical/tech-3-engine-reconhecimento.md).

## Testes
- Escrever testes para use cases, repos (fake DAO), **scoring** (vetores sintéticos
  determinísticos), agenda/lembretes e retenção.
- UI crítica com Compose tests e fakes via Hilt.
- Rodar `./gradlew test` (e `connectedCheck` quando aplicável) antes de concluir.
- Atalhos no [`Makefile`](../Makefile): `make test`, `make check`, `make run`, `make reopen`
  (rode `make help` para a lista).

## Fluxo de trabalho (OpenSpec SDD)
- Especificar antes de codar: criar/atualizar a change em `openspec/changes/` e validar com
  `openspec validate`; usar as skills `openspec-*`.
- Manter rastreabilidade **PRD ↔ capability ↔ change**.

## Sempre
- **Atualizar o [`CHANGELOG.md`](../CHANGELOG.md)** (seção "Não lançado") em mudanças
  relevantes.
- Manter [`AGENTS.md`](../AGENTS.md) e este arquivo sincronizados quando
  convenções/arquitetura mudarem.
- Editar arquivos existentes em vez de criar documentação redundante.
