## Context

O engine de reconhecimento calcula:
```
score = Σ(w_i · sim_i) / Σ(w_i)   [componentes presentes]
```
com `W_EMBEDDING=0.6`, `W_COLOR=0.3`, `W_SHAPE=0.1`, `W_IMPRINT=0.2`.

Na F4.1, `THRESHOLD_CONFIDENT` foi elevado de 0.82 para 0.90 como proteção temporária contra
falso positivo — sem embedding, o score era dominado por cor e um controle negativo real
(`frente-druse`, remédio diferente de cor parecida) atingia 0.822 (acima do limiar original).
Com embedding (F4.3) e OCR (F4.4) ativos, o regime mudou: o embedding discrimina muito melhor
que cor+forma, e o limiar 0.90 pode ser excessivamente conservador para fotos legítimas.

**Fixtures de referência conhecidas** (medidas nas images reais antes da F4.1):
- Controle positivo: verso do mesmo remédio → score ~0.958 (cor+forma apenas) → deve permanecer CONFIANTE.
- Controle negativo: `frente-druse` (remédio diferente) → score 0.822 (cor+forma) → deve permanecer NÃO-CONFIANTE.

Com embedding disponível, espera-se que:
- Positivo legítimo: cosine(embedding) alto (~0.85–0.99) → score final bem acima de 0.85.
- Negativo real: cosine(embedding) baixo (~0.3–0.5) → score final abaixo de 0.75 mesmo com cor parecida.

## Goals / Non-Goals

**Goals:**
- Determinar analiticamente o novo `THRESHOLD_CONFIDENT` seguro para o regime com embedding.
- Confirmar (ou ajustar) que os pesos atuais produzem separação suficiente entre positivo e negativo.
- Cobrir com testes determinísticos todos os modos de componente: só cor+forma, + embedding, + imprint, completo.
- Remover comentários de "provisório" — os parâmetros passam a ser os definitivos (recalibráveis na F4.5 seguinte se necessário).

**Non-Goals:**
- Não usar um dataset maior que os 3 comprimidos de controle definidos nesta fase.
- Não alterar a arquitetura do engine nem a fórmula de score.
- Não implementar TD-2 (alerta de foto similar no cadastro) — isso é dívida técnica separada.

## Decisions

### D1 — Calibração empírica com vetores reais extraídos do modelo, não sintéticos

**Decisão:** usar embeddings e features extraídos pelo TFLite/ML Kit reais a partir de 6 fotos
de celular de comprimidos reais (3 comprimidos visualmente parecidos × frente+verso). Os vetores
são extraídos uma única vez via teste instrumentado, depois hardcodados como constantes JVM.

**Rationale:** vetores sintéticos aproximam o comportamento mas não garantem que o limiar é
seguro para o modelo MobileNetV3 específico que roda em produção. Embeddings reais refletem
a discriminação exata do modelo — incluindo nuances de pré-processamento (resize 224×224,
normalização ±127.5) e a distribuição real do espaço latente. Com 3 comprimidos parecidos,
os pares negativos reais são os mais difíceis possíveis: mesma cor, mesma forma aproximada
mas identidades diferentes — o cenário exato que mais preocupa.

**Fluxo de extração:** fotos em `app/src/androidTest/assets/` → `RealPillFeatureExtractorTest`
roda no emulador via `make connected` → output loggado → copiado para `RealPillFixtures.kt`
como constantes Kotlin imutáveis. Após hardcode, os testes são puramente JVM e rodam em CI.

**Alternativa descartada:** vetores sintéticos arbitrários — descartada por não representar
o espaço latente real do modelo. Mantida apenas para cenários sem embedding (fallback).

### D2 — Novo `THRESHOLD_CONFIDENT` no intervalo 0.82–0.86

**Rationale:** com embedding ativo, um positivo legítimo deve ter cosine(embedding) alto (~0.9),
puxando o score para ~0.92 mesmo com cor/forma medianas. Um negativo com cor parecida mas
embedding distinto (cosine ~0.4) terá score ~0.58. O limiar 0.85 mantém margem confortável
acima do negativo e abaixo do positivo. O valor exato será derivado pelo teste de controle:
escolher o menor valor que rejeita o negativo sintético mais pessimista E aceita o positivo
mais conservador.

**O limiar 0.90 atual é descartado** pois foi calibrado sem embedding — no novo regime ele
rejeitaria matches legítimos com embedding moderado.

### D3 — Testes de controle como golden set permanente

Fixtures baseadas em vetores reais extraídos das 6 fotos de comprimidos, complementadas
com fixtures sintéticas apenas para modos sem embedding (fallback retrocompatível):

| Cenário | Fonte | Componentes | Score esperado | Decisão |
|---|---|---|---|---|
| Positivo full — pill_A frente vs verso | real | todos | a medir | CONFIANTE |
| Positivo sem imprint — pill_A frente vs verso | real | emb+cor+forma | a medir | CONFIANTE |
| Positivo só cor+forma (legado sem emb) | real (Lab) | cor+forma | ~0.958 (F4.1) | CONFIANTE |
| Negativo hard — pill_A vs pill_B (cor parecida) | real | todos | a medir | NÃO-CONFIANTE |
| Negativo hard — pill_A vs pill_C | real | todos | a medir | NÃO-CONFIANTE |
| Negativo só cor+forma (controle F4.1) | real (Lab) | cor+forma | 0.822 (F4.1) | NÃO-CONFIANTE |
| Ambíguo — dois candidatos próximos | sintético | emb+cor | top1−top2 < MARGIN | AMBÍGUO |

O `THRESHOLD_CONFIDENT` final será o menor valor que garante zero falsos positivos em todos
os pares negativos reais, com os positivos reais ainda classificados como CONFIANTE.

### D4 — Pesos revisáveis com dados reais, limiares certamente recalibrados

Os pesos atuais (`W_EMBEDDING=0.6`, `W_COLOR=0.3`, `W_SHAPE=0.1`, `W_IMPRINT=0.2`) são o
ponto de partida. Com os vetores reais em mãos, se os scores positivo e negativo não tiverem
separação suficiente (margem < 0.10), os pesos podem ser ajustados antes de fixar o limiar.
A calibração dos pesos só acontece se o golden set real mostrar necessidade — caso contrário,
mantê-los reduz risco de regressão. Decisão tomada após ver os scores reais (tarefa 1.5).

### D5 — Fotos reais guardadas no repo como test asset

**Decisão:** as 6 fotos (`pill_a_frente.jpg` … `pill_c_verso.jpg`) ficam em
`app/src/androidTest/assets/` — são o dado primário de calibração.

**Rationale:** sem as fotos originais não é possível re-extrair embeddings se o modelo mudar
(ex.: atualização do MobileNetV3 ou mudança de pré-processamento). Como `androidTest` não
entra no APK de produção, o impacto no tamanho do app é zero. 6 JPEGs de celular ≈ 15–30 MB
— aceitável como test asset. Se o repo crescer muito, migrar para Git LFS (decisão futura).

**Convenção de nomes:** `pill_a_frente.jpg`, `pill_a_verso.jpg`, `pill_b_frente.jpg`,
`pill_b_verso.jpg`, `pill_c_frente.jpg`, `pill_c_verso.jpg`. O usuário deve deixá-las na raiz
do projeto; a tarefa 0.2 as move para o local definitivo.

## Risks / Trade-offs

- **[Risco] Emulador produz embeddings ligeiramente diferentes do device físico** → Mitigação:
  o modelo TFLite é determinístico dado o mesmo input; variações de câmera já estão na foto,
  não no emulador. Os vetores hardcoded valem para o modelo atual.
- **[Risco] 3 comprimidos podem não cobrir toda a variabilidade real** → Mitigação: os pares
  negativos devem ser os mais parecidos possíveis (mesma cor/forma) para representar o caso
  mais difícil. Mais comprimidos podem ser adicionados ao golden set em fases futuras.
- **[Risco] Threshold muito baixo reabre falso positivo** → Mitigação: manter o controle
  negativo sintético (`frente-druse`-like: cor parecida, embedding distante) como teste
  permanente que deve falhar. Se o limiar escolhido o aceitar, aumentar até rejeitá-lo.
- **[Trade-off] Conservadorismo vs. usabilidade** → Um limiar mais baixo melhora a taxa de
  confirmação automática mas aumenta risco de falso positivo. Para um app de medicação, errar
  para o lado conservador (mais "ambíguo") é preferível. O limiar escolhido deve garantir zero
  falsos positivos nos controles sintéticos antes de qualquer coisa.
