package com.meusremedios.data.ml

import com.meusremedios.domain.model.RecognitionCandidate
import com.meusremedios.domain.model.RecognitionOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Controles de reconhecimento determinísticos (F4.1 `harden-recognition-thresholds`).
 *
 * Reproduzem, de forma reprodutível em CI, o falso positivo observado no emulador e o match
 * legítimo, usando as features **medidas pelo extrator de produção** sobre as imagens reais
 * (`frente.jpeg`, `verso.jpeg`, `frente-druse.jpeg`) como fixtures sintéticas — assim não
 * dependem dos arquivos de imagem (locais/gitignored).
 *
 * Pipeline exercido: `RecognitionScorer.score` (por foto) → melhor score por medicamento →
 * `RecognitionEngine.decide`, idêntico ao `RecognizeMedicationUseCase`.
 */
class PillRecognitionControlTest {
    // Features medidas (Lab [L,a,b] + aspect ratio) — ver design.md da change.
    private val frente = FeatureSet(colorLab = floatArrayOf(68.18981f, -1.4912211f, 3.4981222f), aspectRatio = 1.7777778f)
    private val verso = FeatureSet(colorLab = floatArrayOf(73.67061f, -1.735405f, 2.4459465f), aspectRatio = 1.7777778f)
    private val druse = FeatureSet(colorLab = floatArrayOf(68.32766f, -2.3755991f, 3.70237f), aspectRatio = 0.5625f)

    /** Replica a agregação do use case: melhor score entre as fotos de consulta e cadastradas. */
    private fun bestScore(
        query: List<FeatureSet>,
        registered: List<FeatureSet>,
    ): Float = registered.maxOf { cand -> query.maxOf { q -> RecognitionScorer.score(q, cand) } }

    @Test
    fun `controle negativo - comprimido diferente nao e afirmado como confiante`() {
        // Remédio cadastrado com frente+verso; consulta = druse (comprimido diferente).
        val score = bestScore(query = listOf(druse), registered = listOf(frente, verso))
        // Score medido ~0.822: acima do antigo limiar 0.82, abaixo do novo 0.90.
        assertEquals(0.8222f, score, 1e-3f)
        assertTrue("controle negativo deveria ficar abaixo do limiar", score < RecognitionParams.THRESHOLD_CONFIDENT)

        val outcome =
            RecognitionEngine.decide(
                listOf(RecognitionCandidate(medicationId = 1, medicationName = "Cadastrado", score = score)),
            )
        assertFalse(
            "FALSO POSITIVO: controle negativo foi afirmado como confiante",
            outcome is RecognitionOutcome.Confident,
        )
    }

    @Test
    fun `controle positivo - mesmo remedio (verso vs frente) e confiante`() {
        // Remédio cadastrado só com a frente; consulta = verso (mesmo remédio, foto diferente).
        val score = bestScore(query = listOf(verso), registered = listOf(frente))
        assertEquals(0.9581f, score, 1e-3f)
        assertTrue("match legítimo deveria atingir o limiar", score >= RecognitionParams.THRESHOLD_CONFIDENT)

        val outcome =
            RecognitionEngine.decide(
                listOf(RecognitionCandidate(medicationId = 1, medicationName = "Cadastrado", score = score)),
            )
        assertTrue(
            "match legítimo deveria ser confiante",
            outcome is RecognitionOutcome.Confident,
        )
        assertEquals(1L, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }
}
