# Documentação — Meus Remédios

App Android **100% offline** que confirma visualmente, pela câmera, qual remédio
pré-cadastrado é o comprimido em mãos — pensado para idosos.

> Estado: **documentação de projeto**. A implementação será feita em outra sessão usando
> **OpenSpec SDD** (ver [openspec-plan.md](openspec-plan.md)).

## Produto
- [ONE PAGER](product/one-pager.md)
- [PRD-1 — Cadastro de Remédios](product/prd-1-cadastro.md) · Fase F2
- [PRD-2 — Consulta e Relatórios](product/prd-2-consulta-relatorios.md) · Fase F3
- [PRD-3 — Reconhecimento Visual](product/prd-3-reconhecimento.md) · Fase F4
- [PRD-4 — Lembretes e Avisos](product/prd-4-lembretes.md) · Fase F6
- [PRD-5 — Registro de Tomadas](product/prd-5-registro-tomadas.md) · Fase F5
- [PRD-6 — Configurações](product/prd-6-configuracoes.md) · Fase F7

## Técnico
- [TECH-1 — Arquitetura](technical/tech-1-arquitetura.md)
- [TECH-2 — Modelo de Dados](technical/tech-2-modelo-dados.md)
- [TECH-3 — Engine de Reconhecimento](technical/tech-3-engine-reconhecimento.md)
- [TECH-4 — Acessibilidade](technical/tech-4-acessibilidade.md)
- [TECH-5 — Estratégia de Testes](technical/tech-5-estrategia-testes.md)
- [TECH-6 — NFRs, Privacidade e Offline](technical/tech-6-nfr-privacidade-offline.md)
- [Glossário](technical/glossario.md)

## Implementação (SDD)
- [Plano OpenSpec SDD](openspec-plan.md)

## Desenvolvimento local
Os fluxos de build, teste e execução no emulador estão automatizados no
[`Makefile`](../Makefile) na raiz do projeto. Rode `make help` para ver todos os alvos.

| Comando | Descrição |
|---------|-----------|
| `make run` | Sobe o emulador, aguarda o boot, builda, instala e abre o app |
| `make reopen` | Recompila, reinstala e reabre o app (ciclo rápido de dev) |
| `make build` · `make test` · `make check` | Compila o APK · testes JVM · ambos |
| `make connected` | Testes instrumentados (requer emulador/device) |
| `make emulator` · `make wait-boot` · `make kill-emulator` | Controle do emulador |
| `make ime-fix` | Habilita o teclado virtual com teclado físico conectado |
| `make install` · `make open` · `make uninstall` | Gerência do app no device |
| `make logcat` · `make screenshot` · `make devices` | Diagnóstico |
| `make avd-create` | (Re)cria o AVD usado nos testes locais |

Pré-requisitos: JDK 17 e Android SDK (com `emulator` e a imagem de sistema). O Makefile
usa `ANDROID_HOME`/`JAVA_HOME` (com fallback automático) e sempre
`--no-configuration-cache`. Para uso direto: `./gradlew test`, `./gradlew connectedCheck`,
`./gradlew assembleDebug`.

## Decisões-chave
- **Stack:** Kotlin + Jetpack Compose (Material 3), Hilt, Room, CameraX, TensorFlow Lite.
- **Reconhecimento:** híbrido on-device = embeddings TFLite + cor (Lab) + forma/tamanho.
- **Acessibilidade:** prioridade (fontes/botões grandes, alto contraste, pt-BR, TalkBack).
- **Lembretes:** alarmes exatos (AlarmManager) + reagendamento no boot.
- **Restrições:** 100% offline, sem login/nuvem/ads, `minSdk 24`, dados privados no app.

## Roadmap (fases)
F0 Scaffolding · F1 Dados · F2 Cadastro · F3 Consulta/Relatórios · F4 Reconhecimento ·
F5 Registro de tomadas · F6 Lembretes · F7 Configurações · F8 Acessibilidade · F9 Testes/CI.

## Referência visual
- [Imagem de inspiração](inspiracao.jpeg) — porta-comprimidos com pílulas variadas.
