## Why

Com embedding TFLite (F4.3) e OCR de imprint (F4.4) ativos, o engine de reconhecimento agora
dispõe de todos os quatro componentes previstos (embedding, cor, forma, imprint). O limiar
`THRESHOLD_CONFIDENT = 0.90` foi fixado provisoriamente na F4.1 para rejeitar falsos positivos
**sem embedding** — com embedding dominando o score (W=0.6), esse valor é conservador demais e
pode rejeitar matches legítimos. É hora de calibrar os pesos e limiares com dados reais e cobrir
todos os cenários de componentes presentes/ausentes com testes determinísticos permanentes.

## What Changes

- Recalibrar `THRESHOLD_CONFIDENT` para o regime com embedding ativo (expectativa: reduzir de
  0.90 para ~0.82–0.85, uma vez que o embedding discrimina muito melhor que cor+forma sozinhos).
- Verificar e, se necessário, ajustar `MARGIN` e `MIN_SCORE`.
- Confirmar que os pesos atuais (`W_EMBEDDING=0.6`, `W_COLOR=0.3`, `W_SHAPE=0.1`,
  `W_IMPRINT=0.2`) produzem resultados corretos no golden set; ajustar se necessário.
- Remover o comentário "provisório" de `THRESHOLD_CONFIDENT` em `RecognitionParams`.
- Ampliar o golden set de testes com cenários multimodais (embedding presente, embedding+imprint,
  só cor+forma) e garantir cobertura determinística de todos os modos de fallback. O golden set
  será baseado em features extraídas de fotos reais de comprimidos (3 comprimidos parecidos ×
  frente+verso = 6 fotos), processadas pelo TFLite e ML Kit reais via teste instrumentado,
  depois hardcodadas como constantes JVM em `RealPillFixtures.kt`.
- Atualizar a documentação inline de `RecognitionParams` para refletir o regime final.

## Capabilities

### New Capabilities
_(nenhuma — esta change não introduz nova capability)_

### Modified Capabilities
- `visual-recognition`: os requisitos de limiar e calibração mudam — o endurecimento
  provisório da F4.1 é substituído pela calibração final multimodal.

## Impact

- `data/ml/RecognitionParams.kt` — constantes de pesos e limiares.
- `app/src/androidTest/assets/` — 6 fotos reais de comprimidos (test asset, não entra no APK de produção).
- `app/src/androidTest/.../RealPillFeatureExtractorTest.kt` — extrai features das 6 fotos.
- `app/src/test/.../RealPillFixtures.kt` — constantes Kotlin com vetores extraídos (hardcoded).
- `app/src/test/.../RecognitionScorerTest.kt` e `RecognitionEngineTest.kt` — golden set
  ampliado com fixtures reais cobrindo todos os modos de componente.
- `openspec/specs/visual-recognition/spec.md` — atualizar requisito de limiar.
- `CHANGELOG.md` — entrada na seção "Não lançado".
- Sem novas dependências, sem mudança de schema de banco, sem impacto em UI.
