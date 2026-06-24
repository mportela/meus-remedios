# Documentação — Meus Remédios

App Android **100% offline** que confirma visualmente, pela câmera, qual remédio
pré-cadastrado é o comprimido em mãos — pensado para idosos.

> Visão geral e início rápido no [README da raiz](../README.md). Este índice reúne a
> documentação detalhada de produto e técnica.

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
- [Dívida Técnica](technical/tech-debt.md)

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

### Câmera e fotos no emulador
Para testar o reconhecimento com a câmera real do Mac e enviar imagens para a galeria:

| Comando | Descrição |
|---------|-----------|
| `make run-cam` | Igual ao `make run`, mas usa a webcam do Mac como câmera traseira |
| `make emulator-cam` | Só inicia o emulador com a webcam (`CAMERA_BACK=webcam0`) |
| `make push-photo FILE=~/Downloads/foto.jpg` | Envia uma foto para a galeria e reindexa |
| `make push-photos SRC=~/Downloads` | Envia todas as imagens (jpg/jpeg/png/webp) da pasta |
| `make scan-media` | Força a reindexação da galeria do device |

Variáveis úteis: `CAMERA_BACK` (`webcam0` padrão; use `emulated` para a câmera simulada),
`SRC` (origem das fotos, padrão `~/Downloads`) e `DEVICE_DIR` (destino, padrão
`/sdcard/Pictures`). O macOS pede permissão de câmera ao emulador na primeira captura.

### Depurar o reconhecimento com imagens de teste
A tela **Confirmar** captura a foto pela câmera. Em builds de **debug**
(`BuildConfig.DEV_TOOLS_ENABLED = true`), aparece também o botão **"Usar foto da galeria
(teste)"**, que permite escolher uma imagem já existente na galeria como foto de consulta —
útil para validar de forma repetível se uma imagem dá match com algo cadastrado.

Fluxo sugerido:
1. `make push-photos SRC=~/Downloads` envia suas imagens de teste para a galeria do emulador.
2. Na tela **Confirmar**, toque em **"Usar foto da galeria (teste)"** e escolha a imagem.
3. O resultado (confiante/ambíguo/sem match) usa o mesmo engine da câmera.

A opção é controlada exclusivamente pela flag de build: em `release`
(`DEV_TOOLS_ENABLED = false`) o botão não existe e a confirmação é apenas por câmera. Para
desligar o recurso permanentemente, basta remover a flag/botão.

Pré-requisitos: JDK 17 e Android SDK (com `emulator` e a imagem de sistema). O Makefile
usa `ANDROID_HOME`/`JAVA_HOME` (com fallback automático) e sempre
`--no-configuration-cache`. Para uso direto: `./gradlew test`, `./gradlew connectedCheck`,
`./gradlew assembleDebug`.

## Decisões-chave
- **Stack:** Kotlin + Jetpack Compose (Material 3), Hilt, Room, CameraX, TensorFlow Lite.
- **Reconhecimento:** híbrido on-device = embeddings TFLite (MobileNetV3) + inscrições (OCR
  ML Kit) + cor (Lab) + forma, com segmentação do comprimido para ignorar o fundo. Modelos
  embarcados quantizados **int8**. Ver fases F4.1–F4.7 em
  [`openspec-plan.md`](openspec-plan.md).
- **Acessibilidade:** prioridade (fontes/botões grandes, alto contraste, pt-BR, TalkBack).
- **Lembretes:** alarmes exatos (AlarmManager) + reagendamento no boot.
- **Restrições:** 100% offline, sem login/nuvem/ads, `minSdk 24`, dados privados no app.

## Roadmap (fases)
F0 Scaffolding · F1 Dados · F2 Cadastro · F3 Consulta/Relatórios · F4 Reconhecimento ·
**F4.1–F4.7 Reconhecimento inteligente (TF)** · F5 Registro de tomadas · F6 Lembretes ·
F7 Configurações · F8 Acessibilidade · F9 Testes/CI.

## TODO — modelo treinado de comprimidos (fine-tuning futuro)
- Hoje o embedding usa **MobileNetV3 genérico (ImageNet), quantizado int8**, on-device.
- **Futuro:** gerar um modelo **fine-tuned em comprimidos reais** para discriminar melhor
  pílulas parecidas (forma/cor/inscrições).
  - **Dados:** usar as **fotos do cadastro real** de devices controlados — já temos as
    imagens e o **nome do remédio** como rótulo (par imagem→rótulo pronto para treino).
  - **Como:** coletar/exportar esse conjunto (com consentimento), treinar/fine-tunar offline
    e reembarcar o `.tflite` int8, reaproveitando o golden set de testes (F4.5) para validar
    ganho de acurácia sem regressão de tamanho (F4.7).
  - **Privacidade:** coleta apenas de devices controlados e com consentimento; nada sai do
    device do usuário final em produção (o app permanece 100% offline).

## Referência visual
- [Imagem de inspiração](inspiracao.jpeg) — porta-comprimidos com pílulas variadas.
