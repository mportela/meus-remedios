# PRD-3 — Reconhecimento Visual (Jornada Principal)

- **Fase:** F4
- **Capability OpenSpec:** `visual-recognition`
- **Convenção de IDs:** `RF-3.n` (funcional), `RN-3.n` (regra de negócio)

## Objetivo
Identificar, pela câmera, qual remédio **cadastrado** é o comprimido em mãos, de forma simples
e segura, totalmente offline.

## Usuário / Contexto
Idoso na frente do porta-comprimidos, com dúvida sobre qual pílula é qual. Esta é a tela
principal do app.

## Requisitos funcionais
- **RF-3.1** Tela inicial com **botão principal grande** "Confirmar remédio" que abre a câmera.
- **RF-3.2** Preview com **autofoco** e **auto-captura** quando a imagem estiver focada/estável
  (auto-captura configurável em [PRD-6](prd-6-configuracoes.md)); permitir captura manual.
- **RF-3.3** Extrair features da foto e **comparar com as fotos cadastradas** (embedding + cor
  + forma/tamanho), gerando ranking de candidatos com score.
- **RF-3.4** Se o score do top-1 ≥ limiar de confiança → exibir **"É o Remédio X, tomar às
  HH:mm"**.
- **RF-3.5** Se ambíguo (diferença pequena entre os melhores candidatos ou score baixo) →
  **sugerir 2ª foto do outro lado** do comprimido e combinar resultados; ou mostrar candidatos
  para o usuário confirmar.
- **RF-3.6** A partir do resultado, permitir **marcar como tomado** (integra
  [PRD-5](prd-5-registro-tomadas.md)).
- **RF-3.7** Mensagens de erro amigáveis (sem foto cadastrada, comprimido não reconhecido,
  permissão de câmera negada).

## Regras de negócio
- **RN-3.1** **Nunca** afirmar identidade com baixa confiança; em dúvida, pedir nova foto ou
  apresentar opções — segurança acima de conveniência.
- **RN-3.2** Apenas remédios com foto cadastrada entram na comparação.
- **RN-3.3** Todo processamento é local; nenhuma imagem sai do dispositivo.
- **RN-3.4** Score = `w1·cos(embedding) + w2·sim(cor) + w3·sim(forma)`; pesos e limiares são
  configuráveis no código e cobertos por testes.

## Fluxos
Home → botão → câmera → (auto)captura → análise → resultado confiante **OU** pedido de 2ª foto
→ (opcional) marcar tomado.

## UI / Acessibilidade
Botão central enorme; resultado com nome grande, cor/ícone do período, leitura por TalkBack;
instrução clara para a 2ª foto.

## Critérios de aceite
- Com fotos cadastradas, identifica corretamente nos testes do conjunto de referência.
- Caso ambíguo dispara o fluxo de 2ª foto.
- Algoritmo de scoring coberto por testes determinísticos (vetores sintéticos).

## Dependências
F1, F2 (fotos + features), CameraX, `data/ml` (TFLite embedder).

## Fora de escopo
Identificar remédios não cadastrados; OCR de texto do comprimido (possível refinamento).
