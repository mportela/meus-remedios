package com.meusremedios.data.ml

import kotlin.math.sqrt

/**
 * Operações puras e determinísticas sobre vetores de embedding, testáveis fora
 * do Android.
 */
object EmbeddingMath {
    /**
     * Normaliza o vetor para norma L2 unitária. Retorna o próprio vetor (sem
     * alteração) quando vazio ou de norma zero, evitando divisão por zero.
     */
    fun l2Normalize(vector: FloatArray): FloatArray {
        if (vector.isEmpty()) return vector
        var sumSquares = 0.0
        for (v in vector) sumSquares += v.toDouble() * v.toDouble()
        if (sumSquares == 0.0) return vector
        val norm = sqrt(sumSquares).toFloat()
        return FloatArray(vector.size) { i -> vector[i] / norm }
    }
}
