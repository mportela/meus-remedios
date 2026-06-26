## Why

Na tela de reconhecimento com auto-captura, o preview ao vivo da câmera é hoje um
retângulo grande (3:4) que ocupa quase toda a largura e domina a tela, parecendo um
"quadradão" de câmera ligada. Uma moldura circular com a preview recortada no centro
comunica melhor "aponte o comprimido aqui", fica mais leve visualmente e reforça o foco
no objeto — alinhado ao público idoso e à estética do app.

## What Changes

- O preview ao vivo do CameraX na auto-captura passa a ser exibido **recortado em um
  círculo**, centralizado, em vez de um retângulo 3:4 que preenche a largura.
- Adicionar uma **moldura (anel/borda) ao redor do círculo**, dando o aspecto de visor.
- O recorte circular é apenas apresentação: a captura real, a análise por frame e o
  pipeline de reconhecimento continuam usando o frame completo do sensor (sem perda de
  área para o reconhecimento).
- A área do preview continua com altura visível não-nula mesmo dentro do contêiner
  rolável (comportamento preservado, agora com dimensão definida pelo diâmetro do círculo).

## Capabilities

### New Capabilities
<!-- Nenhuma capability nova: é refinamento de apresentação de uma capability existente. -->

### Modified Capabilities
- `visual-recognition`: o requisito de exibição do preview ao vivo na auto-captura passa
  a especificar uma apresentação **circular com moldura**, mantendo o frame completo para
  detecção/captura e a garantia de área visível não-nula.

## Impact

- **Código:** `app/src/main/java/com/meusremedios/ui/recognition/RecognitionScreen.kt`
  (composable `AutoCaptureContent` — `AndroidView`/`PreviewView` com `aspectRatio(3f/4f)`).
  Possível ajuste de strings de acessibilidade em `strings.xml` (descrição do visor).
- **Sem impacto** no `CameraXPreviewController`, no pipeline ML, nem na captura/orientação
  da foto: o recorte é puramente visual (clip de Compose) sobre a preview.
- **Sem novas dependências**; usa `Modifier.clip(CircleShape)` e `border` do Compose.
