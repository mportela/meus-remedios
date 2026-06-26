## Context

O preview ao vivo da auto-captura vive em `RecognitionScreen.kt`, no composable
`AutoCaptureContent`. Hoje é um `AndroidView { PreviewView }` com
`Modifier.fillMaxWidth().aspectRatio(3f / 4f)` — um retângulo grande dentro de uma
`Column` com `verticalScroll`. A detecção de frames e a captura real **não** passam pela
`PreviewView`: o frame de análise vem do `ImageAnalysis` e a foto do `ImageCapture`, ambos
no `CameraXPreviewController`. Portanto, alterar a forma da `PreviewView` é puramente
cosmético e não afeta reconhecimento nem captura.

## Goals / Non-Goals

**Goals:**
- Exibir o preview ao vivo recortado em círculo, centralizado, com uma moldura visível.
- Garantir diâmetro concreto não-nulo dentro do contêiner rolável (sem `weight`).
- Preservar 100% do frame do sensor para detecção e captura.

**Non-Goals:**
- Alterar `CameraXPreviewController`, o pipeline ML, a orientação/gravação da foto.
- Mudar o `scaleType` de captura ou a resolução analisada.
- Recortar circularmente a imagem que entra no reconhecimento.

## Decisions

**1. Recorte via Compose `Modifier.clip(CircleShape)`, não via custom view/outline.**
Envolver o `AndroidView` em um `Box` quadrado (`aspectRatio(1f)`) e aplicar
`clip(CircleShape)` no Box. É declarativo, não exige tocar no `PreviewView`, e funciona
com a composição do CameraX. Alternativa descartada: `setClipToOutline`/`ViewOutlineProvider`
no `PreviewView` (mais código imperativo, sem ganho).

**2. `PreviewView` com `scaleType = FILL_CENTER`.**
Para o quadrado circular ficar totalmente coberto pela imagem (sem barras), o preview
precisa preencher o Box e recortar o excedente — `FILL_CENTER` faz isso. É o default do
`PreviewView`, então basta confirmar que não foi alterado.

**3. Diâmetro definido por `fillMaxWidth().aspectRatio(1f)` com largura limitada.**
Mantém a regra atual (não usar `weight` em Column rolável). Opcionalmente limitar com um
`widthIn`/tamanho máximo para o círculo não ficar exageradamente grande em telas largas.

**4. Moldura via `Modifier.border(width, color, CircleShape)`.**
Anel desenhado pelo Compose ao redor do mesmo `CircleShape`, usando cor do tema (ex.
`MaterialTheme.colorScheme.primary` ou `outline`) para contraste — importante para o
público idoso/acessibilidade.

## Risks / Trade-offs

- **Recorte esconde parte do que a câmera "vê"** → o usuário enquadra pelo círculo, mas a
  detecção usa o frame inteiro; mitigação: manter círculo grande e a dica de texto orientando
  o enquadramento (o reconhecimento continua robusto pois usa o frame completo).
- **Ordem de modifiers (`clip` antes de `border`)** pode deixar a borda recortada → garantir
  que `border` use o mesmo `CircleShape` e venha na ordem correta; validar visualmente.
- **Contraste da moldura** em temas claro/escuro → usar cor de tema com contraste adequado.
