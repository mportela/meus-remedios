# Endurecer limiares de reconhecimento (segurança provisória)

## Why

A fase F4 entregou o engine de reconhecimento, mas com `embedding == null` o score
usa apenas **cor + forma**. Isso torna a decisão frágil: um **controle negativo real**
(`frente-druse.jpeg`, um comprimido **diferente** do cadastrado) foi reconhecido como
**CONFIANTE** com score **0.822**, logo acima do limiar atual `THRESHOLD_CONFIDENT = 0.82`,
porque a cor dominava (`simCor ≈ 0.99`) e a "forma" era só o aspect ratio da foto inteira.

Um **falso positivo** em medicamento é **risco de segurança** e viola o princípio do produto:
*"nunca afirmar identidade com baixa confiança"*. Enquanto o embedding TFLite (F4.3) e a
segmentação (F4.2) não entram, precisamos de uma barreira imediata, conservadora, que rejeite
esse falso positivo sem perder os matches legítimos.

Dados de referência medidos com as imagens reais (extrator de produção):

| Cenário | Score | Esperado |
|---|---|---|
| Match legítimo (verso do mesmo remédio vs. frente cadastrada) | **0.958** | confiante |
| Controle negativo (`frente-druse` vs. remédio cadastrado) | **0.822** | NÃO confiante |

## What Changes

- Elevar `THRESHOLD_CONFIDENT` de `0.82` para `0.90`, faixa que rejeita o controle negativo
  (0.822) e mantém o match legítimo (0.958). `MARGIN` e `MIN_SCORE` permanecem.
- Tornar a decisão conservadora explícita na spec: enquanto o reconhecimento opera sem
  embedding, o sistema não afirma identidade com confiança apenas por semelhança de cor.
- Promover os cenários de controle a **testes determinísticos permanentes** (rodam em CI, sem
  depender dos arquivos de imagem): usam as features medidas (Lab + aspect ratio) das imagens
  reais como fixtures sintéticas — **positivo** (0.958 → confiante) e **negativo**
  (0.822 → nunca confiante).

## Capabilities

- `visual-recognition` (modificada): decisão conservadora reforçada e parâmetro de limiar.

## Impact

- Código: `RecognitionParams.THRESHOLD_CONFIDENT`.
- Testes: novo conjunto determinístico de controle (`data/ml`).
- Comportamento: capturas legítimas com score entre 0.82 e 0.90 passam a ser tratadas como
  **ambíguas** (fluxo de 2ª foto / candidatos) em vez de afirmadas — alinhado ao princípio
  conservador. Será recalibrado na F4.5 quando embedding + OCR estiverem disponíveis.

## Documentation

- Atualizar `CHANGELOG.md` (Não lançado).
- O reequilíbrio definitivo de pesos/limiares está planejado em F4.5 (`recalibrate-recognition`).
