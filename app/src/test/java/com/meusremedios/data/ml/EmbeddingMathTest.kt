package com.meusremedios.data.ml

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import kotlin.math.sqrt

class EmbeddingMathTest {

    @Test
    fun `l2Normalize produz norma unitaria`() {
        val out = EmbeddingMath.l2Normalize(floatArrayOf(3f, 4f))
        assertArrayEquals(floatArrayOf(0.6f, 0.8f), out, 1e-6f)

        var norm = 0.0
        for (v in out) norm += v.toDouble() * v.toDouble()
        assertEquals(1.0, sqrt(norm), 1e-6)
    }

    @Test
    fun `l2Normalize e idempotente`() {
        val once = EmbeddingMath.l2Normalize(floatArrayOf(1f, 2f, 2f))
        val twice = EmbeddingMath.l2Normalize(once)
        assertArrayEquals(once, twice, 1e-6f)
    }

    @Test
    fun `vetor zero permanece inalterado`() {
        val zero = floatArrayOf(0f, 0f, 0f)
        assertSame(zero, EmbeddingMath.l2Normalize(zero))
    }

    @Test
    fun `vetor vazio permanece inalterado`() {
        val empty = FloatArray(0)
        assertSame(empty, EmbeddingMath.l2Normalize(empty))
    }

    @Test
    fun `vetor nao nulo gera nova instancia`() {
        val input = floatArrayOf(2f, 0f)
        assertNotSame(input, EmbeddingMath.l2Normalize(input))
    }
}
