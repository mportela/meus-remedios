package com.meusremedios.data.ml

import com.meusremedios.domain.model.RecognitionCandidate
import com.meusremedios.domain.model.RecognitionOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionEngineTest {

    private fun candidate(id: Long, score: Float) =
        RecognitionCandidate(medicationId = id, medicationName = "Remédio $id", score = score)

    @Test
    fun `lista vazia resulta em sem correspondencia`() {
        assertTrue(RecognitionEngine.decide(emptyList()) is RecognitionOutcome.NoMatch)
    }

    @Test
    fun `top1 abaixo do minimo resulta em sem correspondencia`() {
        val outcome = RecognitionEngine.decide(listOf(candidate(1, 0.4f)))
        assertTrue(outcome is RecognitionOutcome.NoMatch)
    }

    @Test
    fun `alta confianca com margem resulta em confiante`() {
        val outcome = RecognitionEngine.decide(
            listOf(candidate(1, 0.95f), candidate(2, 0.60f)),
        )
        assertTrue(outcome is RecognitionOutcome.Confident)
        assertEquals(1L, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }

    @Test
    fun `candidatos proximos resultam em ambiguo`() {
        val outcome = RecognitionEngine.decide(
            listOf(candidate(1, 0.90f), candidate(2, 0.88f)),
        )
        assertTrue(outcome is RecognitionOutcome.Ambiguous)
    }

    @Test
    fun `acima do minimo mas abaixo do limiar resulta em ambiguo`() {
        val outcome = RecognitionEngine.decide(listOf(candidate(1, 0.70f)))
        assertTrue(outcome is RecognitionOutcome.Ambiguous)
    }

    @Test
    fun `unico candidato muito confiante e confiante`() {
        val outcome = RecognitionEngine.decide(listOf(candidate(1, 0.95f)))
        assertTrue(outcome is RecognitionOutcome.Confident)
    }

    @Test
    fun `ordena candidatos por score decrescente`() {
        val outcome = RecognitionEngine.decide(
            listOf(candidate(1, 0.70f), candidate(2, 0.78f), candidate(3, 0.60f)),
        ) as RecognitionOutcome.Ambiguous
        assertEquals(listOf(2L, 1L, 3L), outcome.candidates.map { it.medicationId })
    }
}
