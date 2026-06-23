# Design — harden-recognition-thresholds

## Context

Engine atual (F4): `score = Σ(w_i·sim_i)/Σ(w_i)` sobre componentes presentes. Sem embedding,
apenas `W_COLOR=0.3` e `W_SHAPE=0.1` participam. Decisão em `RecognitionEngine.decide`:
`top1 ≥ THRESHOLD_CONFIDENT` **e** `(top1 − top2) ≥ MARGIN` → confiante; senão ambíguo;
`top1 < MIN_SCORE` → sem correspondência.

## Goals

- Eliminar o falso positivo do controle negativo (`frente-druse`, 0.822) sem perder o match
  legítimo (0.958), com a menor mudança possível.
- Travar a regressão com testes determinísticos que rodem em CI (sem depender dos arquivos de
  imagem, que são locais/gitignored).

## Non-Goals

- Plugar embedding (F4.3), segmentação (F4.2) ou OCR (F4.4).
- Recalibração multimodal definitiva de pesos/limiares (F4.5).

## Decisions

- **Elevar `THRESHOLD_CONFIDENT` 0.82 → 0.90.** As medições reais dão uma folga clara entre o
  match legítimo (0.958) e o controle negativo (0.822); 0.90 fica no meio. É o único parâmetro
  alterado — `MARGIN=0.08` e `MIN_SCORE=0.55` permanecem.
- **Por que não mexer em `MARGIN`:** no falso positivo havia um único candidato (top2≈0), então
  a margem não ajudaria; o limiar é a alavanca correta.
- **Testes determinísticos com fixtures medidas.** Em vez de testes que decodificam as imagens
  (Robolectric + arquivos locais, não reproduzíveis em CI), os cenários usam as features já
  medidas pelo extrator de produção como constantes:
  - frente: `Lab=[68.18981, -1.4912211, 3.4981222]`, `ar=1.7777778`
  - verso:  `Lab=[73.67061, -1.735405, 2.4459465]`, `ar=1.7777778`
  - druse:  `Lab=[68.32766, -2.3755991, 3.70237]`, `ar=0.5625`
  Os testes exercem `RecognitionScorer.score` + `RecognitionEngine.decide` (funções puras),
  reproduzindo exatamente o pipeline de agregação do use case.

## Risks / Trade-offs

- **Mais "ambíguo":** capturas legítimas com score 0.82–0.90 deixam de ser afirmadas e caem no
  fluxo de 2ª foto. Aceitável e desejado nesta fase (conservador > falso positivo); F4.3/F4.5
  recuperam a confiança com sinais melhores.
- **Banda baseada em poucas amostras:** o limiar é provisório, calibrado com um positivo e um
  negativo reais. O teste de controle garante esta regressão específica; a calibração ampla é
  da F4.5 com um golden set maior.

## Migration Plan

Nenhuma migração de dados. Apenas mudança de constante e novos testes.
