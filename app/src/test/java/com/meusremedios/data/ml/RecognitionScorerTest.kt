package com.meusremedios.data.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionScorerTest {

    @Test
    fun `cosseno de vetores iguais mapeia para 1`() {
        val v = floatArrayOf(1f, 2f, 3f)
        val sim = RecognitionScorer.cosineSimilarity(v, v.copyOf())
        assertEquals(1f, sim!!, 1e-4f)
    }

    @Test
    fun `cosseno de vetores opostos mapeia para 0`() {
        val sim = RecognitionScorer.cosineSimilarity(floatArrayOf(1f, 0f), floatArrayOf(-1f, 0f))
        assertEquals(0f, sim!!, 1e-4f)
    }

    @Test
    fun `cosseno retorna nulo para entradas ausentes ou incompativeis`() {
        assertNull(RecognitionScorer.cosineSimilarity(null, floatArrayOf(1f)))
        assertNull(RecognitionScorer.cosineSimilarity(floatArrayOf(1f), floatArrayOf(1f, 2f)))
        assertNull(RecognitionScorer.cosineSimilarity(floatArrayOf(0f, 0f), floatArrayOf(1f, 1f)))
    }

    @Test
    fun `cor identica tem similaridade 1`() {
        val lab = floatArrayOf(50f, 10f, -20f)
        assertEquals(1f, RecognitionScorer.colorSimilarity(lab, lab.copyOf())!!, 1e-4f)
    }

    @Test
    fun `cor distante reduz similaridade`() {
        val a = floatArrayOf(0f, 0f, 0f)
        val b = floatArrayOf(60f, 0f, 0f)
        val sim = RecognitionScorer.colorSimilarity(a, b)!!
        // ΔE = 60, MAX_DELTA_E = 100 → sim = 0.4
        assertEquals(0.4f, sim, 1e-4f)
    }

    @Test
    fun `forma usa razao minmax`() {
        assertEquals(1f, RecognitionScorer.shapeSimilarity(1.5f, 1.5f)!!, 1e-4f)
        assertEquals(0.5f, RecognitionScorer.shapeSimilarity(1f, 2f)!!, 1e-4f)
        assertNull(RecognitionScorer.shapeSimilarity(0f, 1f))
    }

    @Test
    fun `score sem embedding usa apenas cor e forma`() {
        val query = FeatureSet(embedding = null, colorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f)
        val candidate = FeatureSet(embedding = null, colorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f)
        // Tudo idêntico → score 1 mesmo sem embedding.
        assertEquals(1f, RecognitionScorer.score(query, candidate), 1e-4f)
    }

    @Test
    fun `score fica no intervalo valido`() {
        val query = FeatureSet(colorLab = floatArrayOf(0f, 0f, 0f), aspectRatio = 1f)
        val candidate = FeatureSet(colorLab = floatArrayOf(80f, 50f, 50f), aspectRatio = 3f)
        val score = RecognitionScorer.score(query, candidate)
        assertTrue(score in 0f..1f)
    }

    @Test
    fun `score zero quando nenhum componente comparavel`() {
        assertEquals(0f, RecognitionScorer.score(FeatureSet(), FeatureSet()), 1e-4f)
    }
}
