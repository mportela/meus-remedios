package com.meusremedios.data.ml

/**
 * Pesos e limiares do engine de reconhecimento, centralizados e cobertos por
 * testes (RN-3.4). Calibrados empiricamente na F4.5 com embeddings reais extraídos
 * de 6 fotos de 3 comprimidos visualmente parecidos (pior caso intencionalmente).
 */
object RecognitionParams {
    /** Peso da similaridade de embedding TFLite (MobileNetV3 Small, cosseno mapeado [0,1]). */
    const val W_EMBEDDING: Float = 0.6f

    /** Peso da similaridade de cor (ΔE em Lab, normalizada por MAX_DELTA_E). */
    const val W_COLOR: Float = 0.3f

    /** Peso da similaridade de forma (razão min/max das proporções). */
    const val W_SHAPE: Float = 0.1f

    /**
     * Peso da similaridade de imprint (OCR via ML Kit). Entra no score apenas
     * quando ambas as fotos tiverem imprint não nulo.
     */
    const val W_IMPRINT: Float = 0.2f

    /**
     * Score mínimo do top-1 para afirmar a identidade com confiança (CONFIANTE).
     *
     * Calibrado na F4.5 com embedding TFLite + OCR ativos. A proteção principal
     * contra falsos positivos vem de [MARGIN]: com cadastro de 2 lados por
     * comprimido, os dois registros do pill errado pontuam próximos entre si
     * (ex.: A_FRE vs B_FRE_reg=0.962, A_FRE vs B_VER_reg=0.898 → margem=0.064 < 0.08
     * → AMBÍGUO). O threshold controla apenas o piso mínimo de qualidade.
     *
     * Controles de calibração (pares de 3 comprimidos visualmente idênticos):
     *   Positivos frente/verso (mesmo pill):  0.868 – 0.983
     *   Negativos cross-pill (pills diferentes): 0.859 – 0.962
     * No uso real, o par cadastro→reconhecimento é mesmo-ângulo-mesmo-pill (~0.99),
     * que passa confortavelmente com margem ≥ 0.08 sobre o verso do mesmo pill.
     */
    const val THRESHOLD_CONFIDENT: Float = 0.85f

    /**
     * Diferença mínima entre top-1 e top-2 para afirmar (evita ambiguidade).
     * Com cadastro de 2 lados, é o principal guardião contra falso CONFIANTE:
     * comprimidos diferentes sempre competem frente e verso, mantendo a margem baixa.
     */
    const val MARGIN: Float = 0.08f

    /** Abaixo deste score, considera-se que o comprimido não foi reconhecido. */
    const val MIN_SCORE: Float = 0.55f

    /**
     * Distância máxima de referência em Lab (ΔE) usada para normalizar a
     * similaridade de cor para `[0, 1]`.
     */
    const val MAX_DELTA_E: Float = 100f

    /** Número máximo de candidatos apresentados ao usuário. */
    const val MAX_CANDIDATES: Int = 5
}
