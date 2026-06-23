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

    @Test
    fun `textSimilarity retorna nulo quando algum imprint e null`() {
        assertNull(RecognitionScorer.textSimilarity(null, "ABC"))
        assertNull(RecognitionScorer.textSimilarity("ABC", null))
        assertNull(RecognitionScorer.textSimilarity(null, null))
    }

    @Test
    fun `textSimilarity strings iguais retorna 1`() {
        assertEquals(1f, RecognitionScorer.textSimilarity("ABC 10", "ABC 10")!!, 1e-4f)
    }

    @Test
    fun `score com imprint identico e maior que sem imprint`() {
        val base = FeatureSet(colorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f)
        val scoreBase = RecognitionScorer.score(base, base)

        val withImprint = base.copy(imprintText = "ABC 10")
        val scoreWithImprint = RecognitionScorer.score(withImprint, withImprint)

        // Ambos são 1.0 quando tudo idêntico; o score com imprint deve ser >= ao sem.
        assertTrue(scoreWithImprint >= scoreBase)
        assertEquals(1f, scoreWithImprint, 1e-4f)
    }

    @Test
    fun `score ignora imprint quando ausente em um dos lados`() {
        val query = FeatureSet(colorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f, imprintText = "ABC")
        val candidate = FeatureSet(colorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f, imprintText = null)
        // imprintText ausente no candidato → componente ignorado → score = 1 (cor+forma idênticos)
        assertEquals(1f, RecognitionScorer.score(query, candidate), 1e-4f)
    }

    @Test
    fun `score com imprints divergentes e menor que com imprints identicos`() {
        val base = FeatureSet(colorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f)
        val sameImprint = base.copy(imprintText = "ABC")
        val diffImprint = FeatureSet(colorLab = floatArrayOf(50f, 0f, 0f), aspectRatio = 1f, imprintText = "XYZ")

        val scoreSame = RecognitionScorer.score(sameImprint, sameImprint)
        val scoreDiff = RecognitionScorer.score(sameImprint, diffImprint)

        assertTrue("Score com imprint igual deve ser > divergente", scoreSame > scoreDiff)
    }
}
