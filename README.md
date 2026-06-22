# Meus Remédios

App Android **100% offline** que confirma visualmente, pela câmera, qual remédio
**pré-cadastrado** é o comprimido em mãos — pensado para **idosos**, para reduzir erros de
medicação. O reconhecimento compara a foto da câmera **apenas com as fotos cadastradas pelo
próprio usuário** (sem internet, sem base externa de medicamentos).

> A confirmação visual é um **auxílio**, não um diagnóstico: em caso de baixa confiança, o
> app pede uma 2ª foto ou mostra candidatos, nunca afirma identidade incerta.

## Sumário
- **Documentação completa:** [`docs/README.md`](docs/README.md) (produto, técnico, roadmap).
- **Guia para contribuidores/agentes:** [`AGENTS.md`](AGENTS.md).
- **Histórico de mudanças:** [`CHANGELOG.md`](CHANGELOG.md).
- **Plano de implementação (OpenSpec SDD):** [`docs/openspec-plan.md`](docs/openspec-plan.md).

## Stack
Kotlin · Jetpack Compose (Material 3) · Hilt · Room · CameraX · TensorFlow Lite ·
Coroutines/Flow · WorkManager · AlarmManager.
Build: Gradle 8.9 · Kotlin 2.0.21 · JVM 17 · `minSdk 24` · `targetSdk 35`.

Detalhes de arquitetura e decisões: [`docs/technical/`](docs/technical/) e
[seção "Decisões-chave"](docs/README.md#decisões-chave).

## Começando
Pré-requisitos: **JDK 17** e **Android SDK** (com `emulator` e uma imagem de sistema).

```bash
make run     # sobe o emulador, builda, instala e abre o app
make reopen  # recompila, reinstala e reabre (ciclo rápido de dev)
make test    # testes unitários (JVM)
make help    # lista todos os atalhos
```

Os atalhos estão no [`Makefile`](Makefile); a tabela completa e os pré-requisitos estão em
[docs/README.md → Desenvolvimento local](docs/README.md#desenvolvimento-local).

## Como contribuir
O projeto segue **OpenSpec SDD**: especifique antes de implementar (change em
`openspec/changes/`), valide, implemente e arquive. Convenções, fluxo de testes e regras
estão em [`AGENTS.md`](AGENTS.md) e [`.github/copilot-instructions.md`](.github/copilot-instructions.md).

## Restrições inegociáveis
100% offline (sem permissão `INTERNET`) · sem login/nuvem/anúncios · dados e fotos em
armazenamento privado do app · acessibilidade como requisito. Ver
[`docs/technical/tech-6-nfr-privacidade-offline.md`](docs/technical/tech-6-nfr-privacidade-offline.md).
