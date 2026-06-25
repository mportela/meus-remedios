package com.meusremedios.data.ml

import com.meusremedios.domain.model.RecognitionCandidate
import com.meusremedios.domain.model.RecognitionOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognitionEngineTest {
    private fun candidate(
        id: Long,
        score: Float,
    ) = RecognitionCandidate(medicationId = id, medicationName = "Remédio $id", score = score)

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
        val outcome =
            RecognitionEngine.decide(
                listOf(candidate(1, 0.95f), candidate(2, 0.60f)),
            )
        assertTrue(outcome is RecognitionOutcome.Confident)
        assertEquals(1L, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }

    @Test
    fun `candidatos proximos resultam em ambiguo`() {
        val outcome =
            RecognitionEngine.decide(
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
        val outcome =
            RecognitionEngine.decide(
                listOf(candidate(1, 0.70f), candidate(2, 0.78f), candidate(3, 0.60f)),
            ) as RecognitionOutcome.Ambiguous
        assertEquals(listOf(2L, 1L, 3L), outcome.candidates.map { it.medicationId })
    }

    // --- Golden set ponta-a-ponta com scores reais (F4.5) ---

    @Test
    fun `mesmo pill mesmo angulo com margem clara decide CONFIANTE`() {
        // Simula cadastro de 2 lados do pill B: frente registrada scores ~0.99 (mesmo ângulo),
        // verso registrado scores 0.868. Margem esperada ≈ 0.12 > MARGIN.
        val outcome =
            RecognitionEngine.decide(
                listOf(
                    candidate(id = 1L, score = 0.992f), // pill B frente registrada (mesmo ângulo)
                    candidate(id = 2L, score = 0.868f), // pill B verso registrada
                ),
            )
        assertTrue("Mesmo pill mesmo ângulo deve ser CONFIANTE", outcome is RecognitionOutcome.Confident)
        assertEquals(1L, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }

    @Test
    fun `pill diferente com 2 lados cadastrados decide AMBIGUO por margem`() {
        // Pill A cadastrado com 2 lados; usuário escaneia pill B (errado).
        // A_FRENTE vs B_FRENTE=0.962, A_VERSO vs B_FRENTE=0.940 → margem=0.022 < MARGIN.
        val outcome =
            RecognitionEngine.decide(
                listOf(
                    candidate(id = 1L, score = 0.962f), // A_FRENTE registrado
                    candidate(id = 2L, score = 0.940f), // A_VERSO registrado (mesmo medicamento id=1 na prática)
                ),
            )
        assertTrue(
            "Pill errado com 2 lados deve ser AMBÍGUO por margem insuficiente",
            outcome is RecognitionOutcome.Ambiguous,
        )
    }

    @Test
    fun `multiplos pills parecidos cadastrados decide AMBIGUO`() {
        // Pills A, B, C cadastrados (2 lados cada); usuário escaneia B_FRENTE.
        // top1 ≈ 0.99 (B_FRENTE correto), top2 = 0.962 (A_FRENTE) → margem < MARGIN.
        val outcome =
            RecognitionEngine.decide(
                listOf(
                    candidate(id = 2L, score = 0.992f), // B_FRENTE correto
                    candidate(id = 1L, score = 0.962f), // A_FRENTE similar
                    candidate(id = 3L, score = 0.940f), // A_VERSO
                    candidate(id = 3L, score = 0.918f), // C_FRENTE
                ),
            )
        assertTrue("Com múltiplos pills parecidos deve ser AMBÍGUO", outcome is RecognitionOutcome.Ambiguous)
    }

    @Test
    fun `controle negativo legado cor forma sem embedding decide AMBIGUO`() {
        // Retrocompatibilidade F4.1: controle negativo frente-druse (0.822, só cor+forma)
        // deve permanecer abaixo do threshold 0.85.
        val outcome = RecognitionEngine.decide(listOf(candidate(id = 1L, score = 0.822f)))
        assertTrue("Controle negativo legado deve ser AMBÍGUO", outcome is RecognitionOutcome.Ambiguous)
    }
}
