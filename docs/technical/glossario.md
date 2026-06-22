# Glossário — Meus Remédios

- **Embedding**: vetor numérico que representa a aparência de um comprimido, extraído por um
  modelo (MobileNet TFLite) executado no dispositivo.
- **Cosine similarity**: medida de similaridade entre dois embeddings (1 = idênticos).
- **Lab (espaço de cor)**: representação de cor mais robusta a variações de brilho que o RGB;
  usada para comparar a cor dominante do comprimido.
- **Aspect ratio**: relação largura/altura do comprimido segmentado; descritor simples de
  forma.
- **Score**: nota combinada (embedding + cor + forma) usada para ranquear candidatos.
- **THRESHOLD_CONFIDENT**: limiar mínimo de score para afirmar uma identificação.
- **MARGIN**: diferença mínima entre o 1º e o 2º candidato para evitar ambiguidade.
- **IntakeLog**: registro de uma tomada (pendente/tomado/pulado/atrasado).
- **ScheduleTime**: horário do dia em que um remédio deve ser tomado.
- **Auto-captura**: tirar a foto automaticamente quando a imagem está focada/estável.
- **Lead time**: antecedência do lembrete em relação ao horário (default 1 min).
- **Retenção**: período (default 90 dias) que o histórico de tomadas é mantido.
- **On-device / offline**: todo processamento ocorre no aparelho, sem internet.
- **OpenSpec SDD**: fluxo de Spec-Driven Development com propostas de mudança e specs por
  capability.
