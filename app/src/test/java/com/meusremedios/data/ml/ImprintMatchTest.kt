package com.meusremedios.data.ml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImprintMatchTest {
    // --- normalize ---

    @Test
    fun `normalize converte para maiusculas`() {
        assertEquals("ABC", ImprintMatch.normalize("abc"))
    }

    @Test
    fun `normalize remove nao alfanumericos`() {
        assertEquals("A1B2", ImprintMatch.normalize("a1-b2!"))
    }

    @Test
    fun `normalize colapsa espacos multiplos`() {
        assertEquals("A B", ImprintMatch.normalize("A   B"))
    }

    @Test
    fun `normalize retorna null para string vazia`() {
        assertNull(ImprintMatch.normalize(""))
    }

    @Test
    fun `normalize retorna null para somente simbolos`() {
        assertNull(ImprintMatch.normalize("---///"))
    }

    @Test
    fun `normalize retorna null para null`() {
        assertNull(ImprintMatch.normalize(null))
    }

    // --- similarity ---

    @Test
    fun `similarity strings iguais retorna 1`() {
        assertEquals(1f, ImprintMatch.similarity("ABC 123", "ABC 123"), 1e-4f)
    }

    @Test
    fun `similarity strings completamente diferentes retorna 0`() {
        assertEquals(0f, ImprintMatch.similarity("ABC", "XYZ"), 1e-4f)
    }

    @Test
    fun `similarity null retorna 0`() {
        assertEquals(0f, ImprintMatch.similarity(null, "ABC"), 0f)
        assertEquals(0f, ImprintMatch.similarity("ABC", null), 0f)
        assertEquals(0f, ImprintMatch.similarity(null, null), 0f)
    }

    @Test
    fun `similarity e simetrica`() {
        val ab = ImprintMatch.similarity("ABC 10", "ABC 100")
        val ba = ImprintMatch.similarity("ABC 100", "ABC 10")
        assertEquals(ab, ba, 1e-4f)
    }

    @Test
    fun `similarity com pequeno erro de OCR e maior que zero`() {
        // "ABC10" vs "ABC1O" (O maiúsculo vs zero) — distância de edição 1 em 5 chars.
        // Jaccard = 0 (tokens únicos diferentes), editSim = 0.8 → combinado ~0.4.
        val sim = ImprintMatch.similarity("ABC10", "ABC1O")
        assertTrue("Esperado > 0 para pequeno erro de OCR, obtido $sim", sim > 0.35f)
    }

    @Test
    fun `similarity resultado fica em 0 a 1`() {
        val sim = ImprintMatch.similarity("LIPITOR 10MG", "ATORVASTATIN")
        assertTrue(sim in 0f..1f)
    }
}
