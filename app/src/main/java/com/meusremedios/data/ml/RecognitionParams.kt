package com.meusremedios.data.ml

/**
 * Pesos e limiares do engine de reconhecimento, centralizados e cobertos por
 * testes (RN-3.4). O `embedding` ainda não é preenchido (refinamento futuro): o
 * score é normalizado pelos componentes disponíveis, então a ausência do
 * embedding apenas redistribui o peso entre cor e forma.
 */
object RecognitionParams {

    /** Peso da similaridade de embedding (cosseno). */
    const val W_EMBEDDING: Float = 0.6f

    /** Peso da similaridade de cor (Lab). */
    const val W_COLOR: Float = 0.3f

    /** Peso da similaridade de forma (proporção). */
    const val W_SHAPE: Float = 0.1f

    /** Score mínimo do top-1 para afirmar a identidade. */
    const val THRESHOLD_CONFIDENT: Float = 0.82f

    /** Diferença mínima entre top-1 e top-2 para afirmar (evita ambiguidade). */
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
