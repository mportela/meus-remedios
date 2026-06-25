# 💊 Meus Remédios

> **O comprimido na sua mão é mesmo o que você espera tomar?**
> Um app Android que responde essa pergunta — silenciosamente, offline e sem enviar nada a lugar nenhum.

<p align="center">
  <a href="https://github.com/mportela/meus-remedios/actions/workflows/ci.yml">
    <img src="https://github.com/mportela/meus-remedios/actions/workflows/ci.yml/badge.svg" alt="CI">
  </a>
  <img src="https://img.shields.io/badge/versão-1.1.1-blue" alt="Versão">
  <img src="https://img.shields.io/badge/Android-7.0%2B-brightgreen?logo=android" alt="Android 7.0+">
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/TensorFlow%20Lite-on--device-FF6F00?logo=tensorflow&logoColor=white" alt="TFLite">
  <img src="https://img.shields.io/badge/internet-NENHUMA-critical?logo=shield" alt="Sem internet">
  <img src="https://img.shields.io/badge/gratuito-open--source-success" alt="Gratuito">
</p>

---

## O problema

Idosos geralmente tomam vários remédios ao dia — alguns parecidos, em caixas organizadoras com
comprimidos já separados por dia e horário. O momento de engolir o comprimido é o mais crítico:
**aquele comprido branco é o Atenolol ou o Caltrat?**

Esse app resolve exatamente esse momento de dúvida.

---

## Como funciona

**1. Cadastro** — O usuário fotografa cada remédio (frente e verso) com a câmera do celular.
O app extrai features visuais (embedding + cor Lab + forma) e armazena tudo localmente.

**2. Confirmação** — Na hora de tomar, o usuário aponta a câmera para o comprimido.
O app compara em tempo real com os remédios cadastrados e confirma (ou pede uma segunda foto).

**3. Lembretes** — Alarmes por nome do remédio e horário, sem depender de internet.

> A confirmação visual é um **auxílio**, não um diagnóstico. Em caso de baixa confiança, o
> app sempre pede uma 2ª foto ou mostra candidatos — nunca afirma identidade incerta.

---

## Features

### Reconhecimento visual 100% on-device
- Pipeline híbrido: **embedding TFLite** (MobileNetV3) + **similaridade de cor** (espaço Lab)
  \+ **forma/tamanho** — cada dimensão contribui com peso calibrado
- Score `= w1·cos(embedding) + w2·sim(cor) + w3·sim(forma)`
- Confirma apenas quando `top1 ≥ THRESHOLD_CONFIDENT` **e** `(top1 − top2) ≥ MARGIN` —
  dois critérios independentes para minimizar falso positivo
- Em dúvida: solicita foto do verso antes de dar qualquer resposta

### Auto-captura inteligente (UX para idosos)
- Preview ao vivo via CameraX analisa ~5 fps direto no TFLite
- Quando detecta score alto, **dispara a foto automaticamente** — sem precisar apertar botão
- Flash branco + vibração háptica confirmam a captura
- Cooldown de 2 s evita disparos múltiplos acidentais
- Fluxo manual preservado para quem preferir

### Proteção contra falso positivo no cadastro
- Ao salvar fotos de um remédio, o app compara com **todos os remédios já cadastrados**
- Se houver colisão visual (`score ≥ 0.85`), exibe aviso antes de salvar:
  *"Este comprimido parece muito com [Atenolol]. Deseja tirar uma foto mais distinta?"*
- Proteção contra nomes duplicados (case-insensitive)
- O usuário pode ignorar o aviso e salvar assim mesmo — ele tem a palavra final

### Lembretes e agenda
- Alarmes locais configuráveis por remédio e horário (WorkManager + AlarmManager)
- Funciona sem internet, sem conta, sem push externo

### UX desenhada para idosos
- Fontes e botões grandes em todas as telas
- Alto contraste e linguagem simples
- Suporte completo a TalkBack (acessibilidade é requisito, não opcional)
- Configurações de display acessíveis na tela de ajustes

### Privacidade total por design
- **Zero permissão de internet** — impossível vazar dados por acidente
- Fotos e features armazenadas no diretório privado do app
- Sem login, sem conta, sem analytics, sem anúncios
- Dados ficam no celular do usuário e em lugar nenhum mais

---

## Stack técnica

| Camada | Tecnologias |
|--------|-------------|
| UI | Jetpack Compose (Material 3) · CameraX |
| Arquitetura | MVVM · Hilt · Coroutines/Flow · StateFlow |
| Persistência | Room (SQLite) |
| ML / Visão | TensorFlow Lite · MobileNetV3-Small · cor Lab · forma |
| Notificações | AlarmManager · WorkManager |
| Build | Kotlin 2.0.21 · Gradle 8.9 · JVM 17 |
| Testes | JUnit · MockK · Turbine · Compose UI Tests |

**minSdk 24** (Android 7.0 Nougat) · **targetSdk 35** · **181 testes**, zero dívida técnica aberta

---

## Começando

**Pré-requisitos:** JDK 17 e Android SDK (com emulador configurado).

```bash
make run      # sobe o emulador, builda, instala e abre o app
make reopen   # recompila + reinstala (ciclo rápido de dev)
make test     # testes unitários (JVM)
make check    # testes + ktlint + build (rode antes de commitar)
make help     # lista todos os atalhos
```

---

## Documentação

| Documento | Conteúdo |
|-----------|----------|
| [`docs/README.md`](docs/README.md) | Índice completo — produto, técnico, roadmap |
| [`docs/product/roadmap.md`](docs/product/roadmap.md) | Direção do produto pós-MVP |
| [`docs/technical/`](docs/technical/) | Arquitetura, engine de reconhecimento, NFRs |
| [`AGENTS.md`](AGENTS.md) | Guia para contribuidores e agentes de IA |
| [`CLAUDE.md`](CLAUDE.md) | Instruções específicas para Claude Code |
| [`CHANGELOG.md`](CHANGELOG.md) | Histórico de versões |

---

## Contribuindo

O projeto segue **OpenSpec SDD** — especificar antes de implementar:

1. Criar/atualizar a change em `openspec/changes/` (`proposal.md` + `tasks.md`)
2. Validar com `openspec validate`
3. Implementar → `make fmt && make check` antes de commitar
4. Arquivar com `openspec archive`

Convenções completas em [`AGENTS.md`](AGENTS.md).

---

*Gratuito, open-source, sem monetização. Foco em alcance e impacto social.*
