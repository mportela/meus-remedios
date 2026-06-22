## Context

O repositório contém apenas documentação (PRDs, specs técnicas, plano OpenSpec).
Esta change estabelece o esqueleto Android compilável que servirá de base para
todas as fases funcionais (F1–F9). As decisões abaixo definem como montar o
projeto respeitando as restrições inegociáveis (offline total, privacidade,
acessibilidade) e as convenções de arquitetura MVVM em camadas.

## Goals / Non-Goals

**Goals:**
- Projeto Gradle (Kotlin DSL) com módulo único `app`, compilável e testável.
- Stack base declarada via version catalog (`gradle/libs.versions.toml`).
- Estrutura de pacotes por camada criada (mesmo que vazia ou com placeholders).
- Hilt + Compose + tema base configurados; tela inicial placeholder em pt-BR.
- Restrições de plataforma aplicadas (sem `INTERNET`, sem backup, dados privados).
- Base de testes e workflow de CI prontos.

**Non-Goals:**
- Qualquer feature funcional (cadastro, reconhecimento, lembretes, etc.).
- Modelo de dados Room concreto (entidades/DAOs ficam para a F1).
- Telas reais de produto além do placeholder.

## Decisions

- **Gradle Kotlin DSL + version catalog**: centraliza versões e facilita
  manutenção; alternativa (Groovy DSL) descartada por menor consistência com
  Kotlin e pior suporte de tipos.
- **Módulo único `app`**: simplicidade adequada ao escopo atual; modularização
  multi-módulo descartada por overhead prematuro — pode ser revisada depois.
- **Hilt para DI**: alinhado às convenções do projeto e a fakes em testes;
  alternativas (Koin/manual) descartadas para manter padrão documentado.
- **KSP em vez de kapt**: builds mais rápidos para Hilt/Room; kapt descartado.
- **Compose + Material 3**: stack de UI definida; tema base com fontes/contraste
  adequados à acessibilidade desde o início.
- **Enforcement offline no manifest**: ausência de `INTERNET` é a barreira mais
  forte e verificável; complementada por não incluir libs de rede.
- **Privacidade**: `allowBackup=false`, `dataExtractionRules`/`fullBackupContent`
  bloqueando cópia; dados em `filesDir`/Room internos.

## Risks / Trade-offs

- [Dependências TFLite/CameraX aumentam o tamanho do APK e o tempo de build] →
  declarar no catálogo agora, mas adicionar/usar incrementalmente nas fases que
  as exigem (F2/F4) para manter o scaffolding leve.
- [Versões de plugins Compose/Kotlin/Hilt podem conflitar] → fixar versões
  compatíveis no version catalog e validar com `assembleDebug`.
- [CI sem device para `connectedCheck`] → CI roda apenas `test` + `assembleDebug`;
  testes instrumentados ficam para execução local/emulador.

## Migration Plan

Não aplicável (projeto novo). Reversão = reverter os arquivos de scaffolding.

## Open Questions

- Versão exata do TFLite e do modelo de embeddings (definir na F4).
- Estratégia de assinatura/release (fora do escopo da F0).
