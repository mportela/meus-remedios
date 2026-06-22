# Proposal: add-visual-recognition

## Why

As fases F1–F3 entregam dados, cadastro com fotos/features e telas de consulta. O coração
do produto (PRD-3), porém, ainda não existe: **identificar pela câmera qual remédio
cadastrado é o comprimido em mãos**, de forma simples, segura e 100% offline. Esta change
implementa a fase **F4 — Reconhecimento visual**, entregando o **engine de reconhecimento**
determinístico e o fluxo de confirmação para o público idoso.

O `embedding` TFLite ainda não é preenchido (reservado para refinamento futuro), então o
score combina **cor (Lab)** e **forma (proporção)**, normalizado pelos componentes
disponíveis — mantendo a fórmula do PRD/TECH-3 e permitindo plugar o embedding depois sem
mudar a API.

## What Changes

- **Engine (`data/ml`):** `RecognitionParams` (pesos e limiares centralizados),
  `RecognitionScorer` (similaridades de embedding/cor/forma e score ponderado, puro) e
  `RecognitionEngine` (decisão confiante × ambíguo × sem correspondência).
- **Domínio:** modelos `RecognitionCandidate` e `RecognitionOutcome` (sealed); use case
  `RecognizeMedicationUseCase` comparando a(s) foto(s) de consulta com as fotos cadastradas,
  agregando por medicamento e combinando 2 lados (frente/verso).
- **UI (`ui/recognition`):** tela inicial com botão grande "Confirmar remédio", captura por
  câmera (intent + FileProvider, como no cadastro), estado de análise, resultado confiante,
  fluxo de 2ª foto para casos ambíguos e mensagens de erro amigáveis.
- **Navegação:** nova aba "Confirmar" como destino inicial (tela principal do app).
- **Strings/acessibilidade:** rótulos pt-BR, nome do resultado em fonte grande, semântica.

## Capabilities

- **visual-recognition** (novo): engine de scoring, decisão e fluxo de confirmação.

## Impact

- Sem novas dependências; 100% offline mantido (nenhuma imagem sai do dispositivo).
- Sem alteração de schema (reuso de `MedicationPhoto` e features já persistidas na F2).
- Navegação raiz ganha a aba "Confirmar" como inicial; "Hoje" e "Meus remédios" permanecem.
- "Marcar como tomado" a partir do resultado (RF-3.6) depende da F5 e fica fora desta change.
- Auto-captura/CameraX preview (RF-3.2) depende de configuração (F7); aqui a captura é manual
  via app de câmera, com o engine pronto para qualquer fonte de imagem.

## Documentation

- Atualizar `CHANGELOG.md` (seção "Não lançado").
