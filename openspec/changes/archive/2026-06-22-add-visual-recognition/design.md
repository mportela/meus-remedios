# Design: add-visual-recognition

## Context

Implementa F4 (PRD-3: `docs/product/prd-3-reconhecimento.md`; TECH-3:
`docs/technical/tech-3-engine-reconhecimento.md`). Reaproveita as features já extraídas no
cadastro (F2): `MedicationPhoto.embedding` (atualmente null), `dominantColorLab` e
`aspectRatio`, além de `MedicationImageStore`/FileProvider e `FeatureExtractor`.

## Goals / Non-Goals

**Goals**
- Engine de reconhecimento puro, determinístico e coberto por testes (RN-3.4).
- Decisão conservadora: nunca afirmar com baixa confiança (RN-3.1); em dúvida, 2ª foto.
- Comparar apenas com fotos cadastradas (RN-3.2); processamento 100% local (RN-3.3).
- Fluxo de confirmação acessível (botão grande, nome do resultado em fonte grande).

**Non-Goals**
- Preencher o `embedding` com modelo TFLite — refinamento futuro (a API já o suporta).
- Auto-captura / preview CameraX (RF-3.2) — depende de configuração (F7).
- "Marcar como tomado" (RF-3.6) — integra a F5.
- Reconhecer remédios não cadastrados ou OCR — fora de escopo (PRD-3).

## Decisions

- **Score normalizado por componentes disponíveis:**
  `score = Σ(w_i · sim_i) / Σ(w_i)` sobre os componentes presentes (embedding, cor, forma).
  Com `embedding == null`, usa apenas cor+forma, mantendo o score em `[0,1]` e a fórmula do
  TECH-3. Pesos default em `RecognitionParams` (`W_EMBEDDING`, `W_COLOR`, `W_SHAPE`).
- **Similaridades (puras):**
  - embedding: similaridade de cosseno (null/|v|=0 → componente ausente).
  - cor: `1 − ΔE_Lab / MAX_DELTA_E` (distância euclidiana em Lab), com clamp em `[0,1]`.
  - forma: `min(a,b)/max(a,b)` das proporções (já em `[0,1]`).
- **Agregação por medicamento:** para cada medicamento, score = melhor (máximo) entre o
  produto cartesiano de fotos de consulta × fotos cadastradas daquele medicamento. Isso
  combina naturalmente frente/verso (RF-3.5) e múltiplas fotos.
- **Decisão (`RecognitionEngine.decide`):**
  - `top1 < MIN_SCORE` → `NoMatch` (comprimido não reconhecido).
  - `top1 ≥ THRESHOLD_CONFIDENT` **e** `(top1 − top2) ≥ MARGIN` → `Confident`.
  - caso contrário → `Ambiguous` (lista candidatos, sugere 2ª foto).
  - sem fotos cadastradas → `NoPhotosRegistered` (tratado no use case).
- **Captura:** intent `TakePicture` + `FileProvider` (mesmo padrão do cadastro), sem exigir
  permissão `CAMERA` no manifest. Arquivos temporários de consulta são apagados ao reiniciar.
- **Navegação:** "Confirmar" vira o destino inicial e a primeira aba; o engine é agnóstico à
  fonte da imagem, então trocar para preview CameraX no futuro não afeta o domínio.

## Risks / Trade-offs

- **Sem embedding, a discriminação é menor:** cor+forma podem ambiguar pílulas parecidas.
  Mitigação: limiar/margem conservadores disparam o fluxo de 2ª foto em vez de afirmar.
- **Iluminação afeta a cor:** Lab reduz o efeito de brilho; ainda assim a captura manual
  pode variar. Mitigação: decisão conservadora e candidatos visíveis para o usuário escolher.
- **Mudança do destino inicial:** "Confirmar" como tela inicial altera a entrada; alinhado ao
  PRD-3 ("tela principal"), com as demais abas preservadas.

## Migration Plan

Sem migração de dados. Apenas leitura das features já persistidas.

## Open Questions

- Pesos/limiares default são pontos de partida calibráveis; quando o embedding TFLite entrar,
  reequilibrar `W_*`/`THRESHOLD_CONFIDENT` com um conjunto de referência (testes já isolam
  esses parâmetros).
