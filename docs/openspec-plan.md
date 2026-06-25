# Plano OpenSpec SDD — Meus Remédios

> Este documento descreve **como** o projeto será especificado e implementado com OpenSpec
> (Spec-Driven Development) na próxima sessão. Os artefatos OpenSpec (`openspec/project.md`,
> capabilities e changes) serão **criados na sessão de implementação**; aqui fica o plano de
> referência.

## openspec/project.md (conteúdo de referência)
- **Produto**: Meus Remédios — confirmação visual de comprimidos, 100% offline, foco idoso.
- **Stack**: Kotlin, Jetpack Compose + Material 3, Hilt, Room, CameraX, TensorFlow Lite,
  Coroutines/Flow, WorkManager, AlarmManager. `minSdk 24`.
  - **Reconhecimento (on-device, offline)**: MobileNetV3 (embedding TFLite) + ML Kit Text
    Recognition *bundled* (OCR de inscrições) + segmentação/detecção do comprimido (TFLite/
    ML Kit Subject Segmentation) + cor (Lab) + forma. Todos os modelos embarcados em
    `assets/`/lib; nenhuma chamada de rede; sem permissão `INTERNET`.
- **Convenções**: MVVM + camadas (ui/domain/data); DI via Hilt; nomes em inglês no código,
  textos de UI em pt-BR; testes com JUnit/MockK/Turbine/coroutines-test/Robolectric/Compose
  test; sem rede; manter CHANGELOG.
- **Restrições**: offline total; sem login/nuvem/ads; privacidade (storage privado);
  segurança no reconhecimento (limiar conservador).
- **Comandos**: `./gradlew test`, `./gradlew connectedCheck`, `./gradlew assembleDebug`.

## Capabilities (openspec/specs/<capability>/spec.md)
| Capability | Descrição | PRDs |
|------------|-----------|------|
| `medication-catalog` | cadastro/edição/listagem/busca de remédios | PRD-1, PRD-2 |
| `medication-photos` | fotos do comprimido + extração/persistência de features (embedding, cor, forma, inscrição) | PRD-1 |
| `visual-recognition` | captura, segmentação, scoring multimodal e decisão de identificação | PRD-3 |
| `pill-imprint-ocr` | leitura on-device de letras/números gravados no comprimido (ML Kit bundled) | PRD-3 |
| `scheduling-reminders` | horários, alarmes exatos, reagendamento no boot | PRD-4 |
| `intake-tracking` | marcar/registrar tomadas e pendentes | PRD-5 |
| `reporting` | relatório do dia, timeline, histórico | PRD-2 |
| `app-settings` | retenção, auto-captura, lembretes globais, limpeza | PRD-6 |
| `accessibility-ui` | requisitos transversais de acessibilidade | TECH-4 |

## Mapeamento Fases → OpenSpec changes
Sugestão de change-ids (verbo + escopo), criados na ordem de dependência:
- **F0** → `add-project-scaffolding` (tooling/estrutura; doc + tasks). ✅ feito
- **F1** → `add-local-data-layer` (Room, entidades, repos, settings). ✅ feito
- **F2** → `add-medication-catalog` + `add-medication-photos` (capabilities 1 e 2). ✅ feito
- **F3** → `add-reporting-and-browsing` (capabilities 2/6). ✅ feito
- **F4** → `add-visual-recognition` (capability 3) + `design.md` detalhado do engine. ✅ feito
  (engine cor+forma; `embedding`/OCR ainda **não** plugados — ver F4.1–F4.7 abaixo).
- **F4.1–F4.7** → **Reconhecimento inteligente (TF)** — objetivo core; detalhado na seção
  seguinte. **Prioridade imediata, antes de F5.** (F4.1 ✅ feito)
- **F5** → `add-intake-tracking` (capability 5). ✅ feito
- **F6** → `add-scheduling-reminders` (capability 4). ✅ feito
- **F7** → `add-app-settings-and-retention` (capability 7). ✅ feito
- **F8** → `add-accessibility-baseline` (capability 8). ✅ feito
- **F9** → `add-test-automation` (estratégia de testes + CI). ✅ feito

## Reconhecimento inteligente (TF) — fases faltantes (objetivo core)
> **Motivação.** A F4 entregou o engine, mas com `embedding == null` o score usa só cor+forma.
> Isso (a) reintroduz "ambíguo" em pílulas parecidas e (b) gera **falsos positivos** — um
> controle negativo real (`frente-druse.jpeg`, remédio diferente) foi reconhecido como
> CONFIANTE com score 0.822 (limiar 0.82), porque a cor dominava e a "forma" era só o aspect
> ratio da foto inteira. Um falso positivo em remédio é **risco de segurança**. Estas fases
> entregam a inteligência prevista no PRD-3/TECH-3: identificar o **comprimido** (não a foto)
> pela **aparência (embedding), inscrições (OCR), cor e forma**, ignorando o fundo.

### Fórmula de score (alvo, ao fim destas fases)
```
score = w1·cosine(embedding) + w2·colorSim(Lab) + w3·shapeSim(forma) + w4·imprintSim(OCR)
```
- `w1` (embedding) é o componente dominante; `w4` (inscrição) desempata letras/números.
- Componentes ausentes são ignorados e o score é renormalizado (mantém retrocompat. da F4).
- Pesos/limiares em `RecognitionParams`, calibrados na F4.5 com conjunto de referência.

### Changes (ordem de dependência)
- **F4.1** → `harden-recognition-thresholds` — **segurança imediata** (sem novas deps). ✅ feito
  - Endurecer `THRESHOLD_CONFIDENT`/`MARGIN` enquanto não há embedding/OCR, de modo que o
    controle negativo **não** seja afirmado como confiante.
  - Promover os testes de controle a permanentes: **positivo** (verso do mesmo remédio,
    score ~0.958 → confiante) e **negativo** (`frente-druse` → nunca confiante).
  - Capability: `visual-recognition`.
- **F4.2** → `add-pill-segmentation` — ❌ **cancelada**.
  Rationale (decidido após F4.5): o usuário fotografa o comprimido isolado (fundo não é
  variável relevante); a calibração empírica com 3 pills visualmente idênticos confirmou que
  a limitação está na **identidade do embedding** (MobileNetV3 genérico não discrimina pills
  da mesma cor/forma), não no ruído de fundo. Segmentação reduziria background mas não
  resolveria a confusão de identidade — o único caminho real é fine-tuning do modelo (TODO
  registrado em `docs/openspec-plan.md` e no README). Adicionar um modelo de segmentação
  aumentaria o APK e a complexidade do pipeline sem ganho proporcional de acurácia para o
  caso de uso real. ML Kit Subject Segmentation também exigiria download (viola offline).
- **F4.3** → `add-tflite-embedding` — **núcleo de inteligência**. ✅ feito
  - Embarcar MobileNetV3 em `assets/`; `FeatureExtractor` real preenche `embedding` no
    cadastro e na consulta (sobre a imagem completa; F4.2 cancelada).
  - **Entregue:** `mobilenet_v3_small.tflite` (MobileNetV3-Small, `224×224×3`→`1024`,
    float32, ~4,1 MB), L2-normalizado, fallback gracioso. **float32 é o padrão** (prioriza
    precisão numa tarefa sensível); int8 fica como **avaliação opcional** na F4.7 (não há
    variante int8 publicada deste embedder e ~4,1 MB num APK de ~33 MB não é gargalo).
    Validar acurácia no golden set (F4.5).
  - Ativa `W_EMBEDDING` (já reservado = 0.6). Capabilities: `medication-photos`,
    `visual-recognition`.
- **F4.4** → `add-imprint-ocr` — **letras/números gravados**. ✅ feito
  - ML Kit Text Recognition *bundled* (offline) lê inscrições; normaliza o texto.
  - Novo campo `MedicationPhoto.imprintText`; nova similaridade `imprintSim` (Jaccard de tokens
    + edit distance) e peso `W_IMPRINT = 0.2`; schema Room v2 + `MIGRATION_1_2`.
  - Guard offline: `INTERNET` removida no merge e confirmada ausente no manifesto mergeado.
  - Capabilities: `pill-imprint-ocr`, `medication-photos`, `visual-recognition`.
- **F4.5** → `recalibrate-recognition` — **calibração final multimodal**. ✅ feito
  - Reequilibrar `W_*` e limiares com um **conjunto de referência** (positivos + controles
    negativos), agora com embedding+OCR disponíveis; relaxar o endurecimento provisório da
    F4.1 sem reabrir falsos positivos.
  - Golden set determinístico de testes (vetores/imagens fixas). Capability:
    `visual-recognition`.
- **F4.6** → `migrate-existing-photo-features` — **reprocessar fotos antigas**. ✅ feito
  - Migração automática: reabrir fotos cadastradas antes da F4.3/F4.4 e gerar
    embedding + imprint faltantes (rotina no startup/WorkManager, idempotente).
    Segmentação excluída do escopo (F4.2 cancelada).
  - Capability: `medication-photos`.
- **F4.7** → `enforce-apk-size-budget` — ❌ **cancelada**.
  Rationale: o APK atual (~33 MB com MobileNetV3 float32) está dentro de limites aceitáveis
  para distribuição direta; CI gates de tamanho adicionam fricção sem resolver um problema
  real no estágio atual. int8 não tem variante publicada para este embedder e sacrificaria
  acurácia numa tarefa sensível. Reavaliar se/quando o APK ultrapassar ~80 MB ou se houver
  gate de loja. Prioridade cedeu para F6 (lembretes — impacto direto no usuário).

### Dependências e impacto técnico
- **Novas libs (todas on-device/offline):** TensorFlow Lite runtime + support/task-vision;
  ML Kit Text Recognition *bundled* (modelo embarcado, sem Play Services em runtime).
  Segmentação descartada (F4.2 cancelada — viola offline e sem ganho para o caso de uso real).
- **Modelo de dados (TECH-2):** `MedicationPhoto` ganha `imprintText` (e, se útil, bbox/ROI
  da segmentação); `embedding` (BLOB) já existe.
- **Offline (NFR):** confirmar a cada change que **nenhuma** dependência exige rede e que o
  manifest **não** ganha `INTERNET`; modelos versionados em `assets/`.
- **Tamanho do APK:** **float32 é o padrão** (precisão); manter orçamento explícito e gate de
  CI na F4.7 (`enforce-apk-size-budget`), e **avaliar int8** por modelo só se o orçamento
  apertar — sem sacrificar acurácia numa tarefa sensível.
- **Acessibilidade/UX:** manter decisão conservadora — em dúvida, pedir 2ª foto ou listar
  candidatos; nunca afirmar identidade com baixa confiança.
- **Docs a sincronizar quando estas fases entrarem:** PRD-3, TECH-2, TECH-3 (fórmula com
  `w4`/OCR), README/AGENTS e CHANGELOG. Segmentação removida do escopo.

### Decisões adotadas (autônomas, revisáveis)
- **OCR:** ML Kit Text Recognition *bundled* (offline, melhor precisão em inscrições do que
  Tesseract). Reavaliar Tesseract se quisermos zero dependência do Google.
- **Segmentação:** ❌ cancelada (F4.2). Usuário fotografa pill isolado; limitação é de
  identidade no embedding, não de fundo. Fine-tuning resolve; segmentação não.
- **Embedding:** MobileNetV3 genérico (ImageNet) em **float32** agora (precisão; int8
  opcional na F4.7); **fine-tune específico de comprimidos** fica como tarefa futura (TODO
  no README): treinar/ajustar com as
  **fotos reais do cadastro** de devices controlados (já temos imagens + nome do remédio como
  rótulo), mantendo o pipeline offline.
- **Fotos antigas:** **migração automática** (reprocessamento), sem exigir recadastro.


## Exemplo de delta de spec (capability `visual-recognition`)
```
## ADDED Requirements
### Requirement: Identificação confiante de comprimido
O sistema DEVE comparar a foto capturada com as fotos cadastradas do usuário e, quando a
confiança do melhor candidato exceder o limiar e a margem sobre o segundo, identificar o
remédio correspondente.

#### Scenario: Comprimido cadastrado e nítido
- **QUANDO** o usuário captura um comprimido cujo remédio está cadastrado com foto
- **ENTÃO** o app exibe "É o Remédio X, tomar às HH:mm"

#### Scenario: Resultado ambíguo
- **QUANDO** a diferença entre os dois melhores candidatos é menor que a margem mínima
- **ENTÃO** o app solicita uma segunda foto do outro lado do comprimido
- **E** combina os resultados antes de decidir

### Requirement: Operação 100% offline
O sistema DEVE realizar todo o reconhecimento no dispositivo, sem qualquer acesso à rede.

#### Scenario: Sem conectividade
- **QUANDO** o dispositivo está sem internet
- **ENTÃO** o reconhecimento funciona normalmente
```

## Fluxo de trabalho na sessão SDD (resumo)
1. Inicializar OpenSpec e criar `openspec/project.md` + capabilities base.
2. Para cada fase: criar a change (`proposal.md` + `tasks.md` + deltas de spec), validar
   (`openspec validate`), implementar, arquivar (`openspec archive`).
3. Manter rastreabilidade **PRD ↔ capability ↔ change** e atualizar o CHANGELOG por feature.

## Rastreabilidade (visão geral)
```mermaid
flowchart LR
  OP[ONE PAGER] --> P1[PRD-1..6]
  P1 --> CAP[Capabilities]
  CAP --> CH[Changes F0-F9]
  CH --> IMPL[Implementação + Testes]
```

## Reconhecimento inteligente — ordem das changes
```mermaid
flowchart TD
  F4[F4 add-visual-recognition ✅ cor+forma] --> H[F4.1 harden-recognition-thresholds]
  H --> S[F4.2 add-pill-segmentation]
  S --> E[F4.3 add-tflite-embedding]
  E --> O[F4.4 add-imprint-ocr]
  O --> C[F4.5 recalibrate-recognition]
  C --> M[F4.6 migrate-existing-photo-features]
  M --> B2[F4.7 enforce-apk-size-budget]
  B2 --> F5[F5 add-intake-tracking]
```

---

## Roadmap TD — Dívidas Técnicas

> Changes planejadas para quitar os itens de `docs/technical/tech-debt.md`.
> Numeradas a partir de **F10** para não colidir com as fases anteriores.

- **F10** → `fix-duplicate-medication-name` (TD-1) — ✅ feito
- **F11** → `add-photo-collision-warning` (TD-2) — ✅ feito
- **F12** → `add-camerax-auto-capture` — ainda não iniciada

### F10 — `fix-duplicate-medication-name` (TD-1)

**Objetivo:** impedir cadastro de dois medicamentos com o mesmo nome, evitando duplicatas
confusas na listagem e no ranking de reconhecimento.

**Capability afetada:** `medication-catalog`

**Escopo:**
- Adicionar `MedicationDao.existsByName(name: String, excludeId: Long): Boolean` (consulta
  SQL `COLLATE NOCASE` + trim).
- Incluir a verificação em `SaveMedicationUseCase`: se nome já existe em outro medicamento,
  retornar `MedicationValidationError.DuplicateName` (novo sealed class / valor).
- No formulário (`MedicationFormViewModel`/`MedicationFormScreen`), exibir mensagem de erro
  inline abaixo do campo "Nome do remédio" quando o use case retornar o erro.
- Edição do próprio medicamento é excluída da checagem (`excludeId = medication.id`).

**Testes planejados:**
- `MedicationDaoTest`: `existsByName` retorna `true` com mesmo nome (case-insensitive),
  `false` com nome diferente, `false` para o próprio id excluído.
- `SaveMedicationUseCaseTest`: salvar com nome duplicado retorna `DuplicateName`; editar
  mantendo o mesmo nome não dispara erro.

**Dependências:** nenhuma nova lib.

**Estimativa de esforço:** baixo — somente DAO, use case, ViewModel e UI de erro.

---

### F11 — `add-photo-collision-warning` (TD-2)

**Objetivo:** alertar o usuário, no momento do cadastro, quando a foto de um comprimido é
visualmente muito similar à de outro medicamento já cadastrado — prevenindo erros de
reconhecimento antes que ocorram em campo.

**Capabilities afetadas:** `medication-photos`, `visual-recognition`

**Escopo:**
- Criar `CheckPhotoCollisionUseCase(features: PhotoFeatures): List<CollisionCandidate>`:
  reutiliza `RecognitionEngine` internamente; retorna candidatos com score ≥
  `THRESHOLD_CONFIDENT`.
- Chamar o use case em `MedicationFormViewModel.save()` após extrair features das fotos
  pendentes com embedding não-nulo, antes de persistir.
- Exibir **aviso não-bloqueante** (Snackbar ou dialog): *"Esta foto é muito parecida com
  [Nome]. O reconhecimento pode falhar — considere outra foto com ângulo ou iluminação
  diferente."* Com ações "Salvar assim mesmo" e "Cancelar".
- Nome do medicamento mais similar exibido na mensagem (produção); score omitido da UI
  (reservado para dev/debug via log).

**Testes planejados:**
- `CheckPhotoCollisionUseCaseTest`: features sintéticas próximas de um candidato cadastrado
  → retorna `CollisionCandidate` com score ≥ limiar; features sintéticas distintas →
  lista vazia.
- `MedicationFormViewModelTest`: ao salvar com colisão detectada, estado emite
  `showCollisionWarning = true` com o nome do candidato.

**Dependências:** `RecognitionEngine` já existente; nenhuma nova lib.
**Pré-requisito:** F10 (não bloqueante tecnicamente, mas resolvê-la antes garante
que a listagem de candidatos na colisão não retorne duplicatas do próprio cadastro).

**Estimativa de esforço:** médio — new use case + integração no ViewModel + UI de aviso.

### Ordem sugerida de implementação
```mermaid
flowchart LR
  F9[F9 add-test-automation ✅] --> F10[F10 fix-duplicate-medication-name TD-1]
  F10 --> F11[F11 add-photo-collision-warning TD-2]
  F11 --> F12[F12 add-camerax-auto-capture]
```

---

## F12 — `add-camerax-auto-capture`

**Objetivo:** implementar a auto-captura inteligente na tela de reconhecimento — quando
`settings.autoCapture = true`, a câmera dispara automaticamente ao detectar foco estável
**e** o TFLite confirmar confiança mínima no frame, sem interação manual do usuário.

**Capabilities afetadas:** `visual-recognition`, `app-settings`

**Pré-requisito técnico obrigatório:**  
A câmera hoje usa `ActivityResultContracts.TakePicture()` (câmera nativa via Intent), que
não expõe preview ao vivo nem callbacks de foco. Para auto-captura funcionar é necessário
**migrar para CameraX com preview ao vivo no Compose** (`camera-camera2` + `camera-lifecycle`
+ `camera-view`). Isso é uma mudança arquitetural na `RecognitionScreen`.

**Fluxo de auto-captura (decisão do usuário):**
1. `RecognitionViewModel` observa `AppSettings.autoCapture` via `SettingsRepository`.
2. Se `true`, a `RecognitionScreen` usa `PreviewView` (CameraX) em vez de botão/Intent.
3. `FocusMeteringAction` detecta foco estável na região central do preview.
4. Ao estabilizar o foco, extrai **uma frame** do `ImageAnalysis` e roda TFLite.
5. Se `top1 score ≥ RecognitionParams.THRESHOLD_CONFIDENT` → captura automática.
6. Se score insuficiente → sem ação (usuário pode capturar manualmente pelo botão).
7. Após auto-captura: **flash branco** (overlay Compose animado) + **vibração háptica**.
8. **Cooldown de 2 segundos** após qualquer captura automática.
9. Se `autoCapture = false` → comportamento atual mantido (botão manual, câmera nativa).

**Escopo técnico:**
- Adicionar dependências CameraX: `camera-camera2`, `camera-lifecycle`, `camera-view`.
- `CameraXPreviewController` (nova classe em `ui/recognition/`): encapsula `ProcessCameraProvider`,
  `Preview`, `ImageCapture` e `ImageAnalysis` use cases; expõe `capturePhoto()` e callback
  de foco.
- `RecognitionScreen`: quando `autoCapture = true`, renderiza `AndroidView { PreviewView }`;
  quando `false`, mantém o fluxo atual com `TakePicture` Intent.
- `RecognitionViewModel`: observar `autoCapture` das settings; expor `autoCaptureEnabled`
  no `RecognitionUiState`; processar frame do `ImageAnalysis` em `Dispatchers.Default`.
- Flash: `Box` com `background(Color.White.copy(alpha))` animado com `AnimatedVisibility`.
- Vibração: `Vibrator` / `VibrationEffect.createOneShot(50ms)` via `HapticFeedbackConstants`.

**Decisões adotadas:**
- **Trigger:** foco estável + TFLite confirma (híbrido) — equilíbrio entre performance e precisão.
- **Cooldown:** 2 segundos.
- **Feedback:** flash branco + vibração háptica.
- **Compatibilidade retroativa:** `autoCapture = false` preserva o comportamento atual.
- **Performance:** análise TFLite em `Dispatchers.Default`; frame rate do `ImageAnalysis`
  limitado a 5 fps (suficiente para detectar foco; economiza bateria).
- **Sem câmera nativa removida:** o Intent `TakePicture` continua sendo o caminho padrão
  quando auto-captura está desligada.

**Testes planejados:**
- `RecognitionViewModelTest`: com `autoCapture = true` e frame com score ≥ limiar →
  estado muda para `ANALYZING`; com score < limiar → sem mudança.
- `RecognitionViewModelTest`: cooldown bloqueia segunda auto-captura em menos de 2s.
- Compose test (`RecognitionScreenTest`): com `autoCapture = false` → botão de captura
  visível; com `true` → `PreviewView` presente no layout.

**Dependências novas:**
```toml
[libraries]
androidx-camera-camera2 = { module = "androidx.camera:camera-camera2", version.ref = "camerax" }
androidx-camera-lifecycle = { module = "androidx.camera:camera-lifecycle", version.ref = "camerax" }
androidx-camera-view = { module = "androidx.camera:camera-view", version.ref = "camerax" }
# camerax = "1.3.4" (já compatível com minSdk 24)
```

**Estimativa de esforço:** alto — migração de câmera + integração TFLite em preview ao vivo
+ UI condicional + testes.

