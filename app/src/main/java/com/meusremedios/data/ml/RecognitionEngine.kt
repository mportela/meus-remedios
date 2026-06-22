package com.meusremedios.data.ml

import com.meusremedios.domain.model.RecognitionCandidate
import com.meusremedios.domain.model.RecognitionOutcome

/**
 * Decide o resultado do reconhecimento a partir de um ranking de candidatos já
 * ordenado por score decrescente. Conservador por princípio (RN-3.1): só afirma
 * com confiança alta e margem suficiente; em dúvida, retorna ambíguo.
 */
object RecognitionEngine {

    fun decide(ranked: List<RecognitionCandidate>): RecognitionOutcome {
        if (ranked.isEmpty()) return RecognitionOutcome.NoMatch

        val sorted = ranked.sortedByDescending { it.score }
        val top1 = sorted[0].score
        if (top1 < RecognitionParams.MIN_SCORE) return RecognitionOutcome.NoMatch

        val top2 = sorted.getOrNull(1)?.score ?: 0f
        val candidates = sorted.take(RecognitionParams.MAX_CANDIDATES)

        val confident = top1 >= RecognitionParams.THRESHOLD_CONFIDENT &&
            (top1 - top2) >= RecognitionParams.MARGIN

        return if (confident) {
            RecognitionOutcome.Confident(best = sorted[0], candidates = candidates)
        } else {
            RecognitionOutcome.Ambiguous(candidates = candidates)
        }
    }
}
