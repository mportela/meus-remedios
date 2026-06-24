## Context

Fotos cadastradas antes da F4.3 têm `embedding = null`; fotos cadastradas antes da F4.4
têm `imprintText = null`. O reconhecimento para esses remédios opera degradado (só
cor+forma), sem que o usuário saiba ou precise agir. A migração reprocessa essas fotos
silenciosamente, no início de cada sessão, até que não haja lacunas.

Stack disponível: `TfliteFeatureExtractor` (F4.3/F4.4) já é `@Singleton` injetável via
Hilt; `MedicationPhotoRepository` já persiste embedding e imprint; Room suporta UPDATE
parcial de colunas. Nenhuma dependência nova é necessária.

## Goals / Non-Goals

**Goals:**
- Detectar, ao iniciar o app, fotos sem embedding ou sem imprint e reprocessá-las.
- Executar em background (IO dispatcher), sem bloquear a UI nem o startup.
- Idempotência total: foto já migrada (embedding não nulo) é pulada.
- Falhas individuais isoladas: um erro em uma foto não interrompe as demais.

**Non-Goals:**
- Segmentação (F4.2 cancelada).
- Progress bar ou notificação visível ao usuário — migração é transparente.
- Re-migração forçada (ex.: botão "atualizar features") — fora do escopo desta fase.
- Suporte a fotos cujo arquivo foi deletado manualmente — tratado como falha isolada.

## Decisions

### D1 — Application.onCreate() + coroutine, sem WorkManager

**Decisão:** disparar a migração em `MeusRemediosApplication.onCreate()` via
`CoroutineScope(Dispatchers.IO + SupervisorJob())`, não com WorkManager.

**Rationale:** o conjunto de fotos por usuário é pequeno (ordem de dezenas); a migração
é rápida (~1–2 s por foto com TFLite) e idempotente. WorkManager adiciona boilerplate de
`Worker`, `Constraints` e `WorkRequest` sem ganho real: o app precisa estar em foreground
para usar a câmera de qualquer forma, então a janela de execução na inicialização é
suficiente. Se o processo morrer no meio, o próximo startup retoma do ponto correto (as
fotos já migradas têm embedding não nulo e são puladas).

**Alternativa descartada:** WorkManager com `OneTimeWorkRequest` — robusto para workloads
grandes ou que precisam rodar com app em background, mas injustificado aqui.

### D2 — Critério de migração: `embedding IS NULL`

**Decisão:** uma foto é candidata à migração se `embedding == null` no banco. O imprint
(`imprintText`) é sempre (re-)tentado quando a foto passa pela migração, pois OCR pode
ter falhado anteriormente por razão transitória; o embedding é o critério de elegibilidade.

**Rationale:** embedding nulo é o indicador mais forte de "foto antiga" (pré-F4.3).
Imprint nulo pode ser legítimo (comprimido sem inscrição) — usar embedding como gate
evita reprocessamento desnecessário de fotos onde OCR simplesmente não encontrou texto.

### D3 — Falhas individuais isoladas com `SupervisorJob`

**Decisão:** cada foto é processada num `launch` filho com `SupervisorJob` no escopo pai.
Exceção em uma foto é logada e o loop continua.

**Rationale:** numa tarefa de migração, uma foto corrompida ou com arquivo deletado não
deve bloquear o restante. O usuário nunca vê o erro — a foto simplesmente permanece sem
embedding e será pulada nas próximas sessões (embedding ainda nulo → elegível → nova
tentativa na próxima abertura do app).

## Risks / Trade-offs

- **[Risco] Arquivo de foto deletado externamente** → `TfliteFeatureExtractor.extract()`
  lança exceção; capturada e logada; foto permanece sem embedding; sem crash.
- **[Risco] Muitas fotos no primeiro uso pós-atualização** → processamento sequencial
  em IO thread; não bloqueia UI; usuário pode abrir o app normalmente enquanto migra.
- **[Trade-off] Sem feedback ao usuário** → decisão consciente: migração silenciosa é
  menos intrusiva para o público idoso; o ganho de reconhecimento aparece naturalmente.
