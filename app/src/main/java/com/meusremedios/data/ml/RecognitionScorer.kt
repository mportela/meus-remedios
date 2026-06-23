package com.meusremedios.data.ml

import kotlin.math.sqrt

/**
 * Conjunto de features comparáveis no reconhecimento. Componentes ausentes
 * (`null`) são ignorados no cálculo do score.
 */
data class FeatureSet(
    val embedding: FloatArray? = null,
    val colorLab: FloatArray? = null,
    val aspectRatio: Float? = null,
    val imprintText: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeatureSet) return false
        if (embedding != null) {
            if (other.embedding == null || !embedding.contentEquals(other.embedding)) return false
        } else if (other.embedding != null) {
            return false
        }
        if (colorLab != null) {
            if (other.colorLab == null || !colorLab.contentEquals(other.colorLab)) return false
        } else if (other.colorLab != null) {
            return false
        }
        return aspectRatio == other.aspectRatio && imprintText == other.imprintText
    }

    override fun hashCode(): Int {
        var result = embedding?.contentHashCode() ?: 0
        result = 31 * result + (colorLab?.contentHashCode() ?: 0)
        result = 31 * result + (aspectRatio?.hashCode() ?: 0)
        result = 31 * result + (imprintText?.hashCode() ?: 0)
        return result
    }
}

/**
 * Cálculo de similaridades e score de reconhecimento. Funções puras e
 * determinísticas, testáveis fora do Android (RN-3.4).
 *
 * `score = Σ(w_i · sim_i) / Σ(w_i)` sobre os componentes presentes, mantendo o
 * resultado em `[0, 1]` independentemente de quais features existem.
 */
object RecognitionScorer {

    /**
     * Similaridade de cosseno mapeada para `[0, 1]` (`(cos + 1) / 2`). Retorna
     * `null` quando algum vetor é ausente, vazio, de tamanhos diferentes ou de
     * norma zero — caso em que o componente não entra no score.
     */
    fun cosineSimilarity(a: FloatArray?, b: FloatArray?): Float? {
        if (a == null || b == null || a.isEmpty() || a.size != b.size) return null
        var dot = 0.0
        var normA = 0.0
        var normB = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        if (normA == 0.0 || normB == 0.0) return null
        val cos = dot / (sqrt(normA) * sqrt(normB))
        val mapped = (cos + 1.0) / 2.0
        return mapped.coerceIn(0.0, 1.0).toFloat()
    }

    /**
     * Similaridade de cor em Lab: `1 − ΔE / MAX_DELTA_E`, com clamp em `[0, 1]`.
     * Retorna `null` se algum vetor for ausente ou não tiver 3 componentes.
     */
    fun colorSimilarity(a: FloatArray?, b: FloatArray?): Float? {
        if (a == null || b == null || a.size != 3 || b.size != 3) return null
        val dl = (a[0] - b[0]).toDouble()
        val da = (a[1] - b[1]).toDouble()
        val db = (a[2] - b[2]).toDouble()
        val deltaE = sqrt(dl * dl + da * da + db * db)
        val sim = 1.0 - deltaE / RecognitionParams.MAX_DELTA_E
        return sim.coerceIn(0.0, 1.0).toFloat()
    }

    /**
     * Similaridade de forma a partir das proporções: `min(a,b) / max(a,b)`,
     * naturalmente em `[0, 1]`. Retorna `null` se alguma proporção for ausente
     * ou não positiva.
     */
    fun shapeSimilarity(a: Float?, b: Float?): Float? {
        if (a == null || b == null || a <= 0f || b <= 0f) return null
        return minOf(a, b) / maxOf(a, b)
    }

    /**
     * Similaridade de imprint delegada para [ImprintMatch.similarity]. Entra no
     * score apenas quando **ambos** os lados possuírem imprint não nulo.
     */
    fun textSimilarity(a: String?, b: String?): Float? {
        if (a == null || b == null) return null
        return ImprintMatch.similarity(a, b)
    }

    /**
     * Score ponderado entre [query] e [candidate], normalizado pelos
     * componentes disponíveis. Retorna `0` quando nenhum componente é comparável.
     */
    fun score(query: FeatureSet, candidate: FeatureSet): Float {
        var weighted = 0f
        var totalWeight = 0f

        cosineSimilarity(query.embedding, candidate.embedding)?.let { sim ->
            weighted += RecognitionParams.W_EMBEDDING * sim
            totalWeight += RecognitionParams.W_EMBEDDING
        }
        colorSimilarity(query.colorLab, candidate.colorLab)?.let { sim ->
            weighted += RecognitionParams.W_COLOR * sim
            totalWeight += RecognitionParams.W_COLOR
        }
        shapeSimilarity(query.aspectRatio, candidate.aspectRatio)?.let { sim ->
            weighted += RecognitionParams.W_SHAPE * sim
            totalWeight += RecognitionParams.W_SHAPE
        }
        textSimilarity(query.imprintText, candidate.imprintText)?.let { sim ->
            weighted += RecognitionParams.W_IMPRINT * sim
            totalWeight += RecognitionParams.W_IMPRINT
        }

        return if (totalWeight > 0f) weighted / totalWeight else 0f
    }
}
