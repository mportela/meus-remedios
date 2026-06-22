package com.meusremedios.domain.usecase

import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.model.RecognitionOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecognizeMedicationUseCaseTest {

    /** Extrator que devolve features distintas por caminho. */
    private class MapFeatureExtractor(
        private val byPath: Map<String, PhotoFeatures>,
    ) : FeatureExtractor {
        override suspend fun extract(imagePath: String): PhotoFeatures =
            byPath.getValue(imagePath)
    }

    private val medications = FakeMedicationRepository()
    private val photos = FakeMedicationPhotoRepository()

    private fun features(l: Float, aspect: Float = 1f) =
        PhotoFeatures(embedding = null, dominantColorLab = floatArrayOf(l, 0f, 0f), aspectRatio = aspect)

    private suspend fun seedMedicationWithColor(name: String, l: Float, aspect: Float = 1f): Long {
        val id = medications.add(Medication(name = name))
        photos.add(
            MedicationPhoto(
                medicationId = id,
                filePath = "/files/$id.jpg",
                side = PhotoSide.FRONT,
                embedding = null,
                dominantColorLab = floatArrayOf(l, 0f, 0f),
                aspectRatio = aspect,
            ),
        )
        return id
    }

    private fun useCase(extractor: FeatureExtractor) =
        RecognizeMedicationUseCase(photos, medications, extractor)

    @Test
    fun `sem fotos cadastradas retorna NoPhotosRegistered`() = runTest {
        medications.add(Medication(name = "Sem foto"))
        val outcome = useCase(MapFeatureExtractor(mapOf("/q.jpg" to features(50f)))).invoke(listOf("/q.jpg"))
        assertTrue(outcome is RecognitionOutcome.NoPhotosRegistered)
    }

    @Test
    fun `identifica medicamento com cor mais proxima`() = runTest {
        val target = seedMedicationWithColor("Losartana", l = 50f)
        seedMedicationWithColor("AAS", l = 95f) // bem distante (branco)

        val extractor = MapFeatureExtractor(mapOf("/q.jpg" to features(50f)))
        val outcome = useCase(extractor).invoke(listOf("/q.jpg"))

        assertTrue(outcome is RecognitionOutcome.Confident)
        assertEquals(target, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }

    @Test
    fun `cores proximas geram resultado ambiguo`() = runTest {
        seedMedicationWithColor("Remédio A", l = 50f)
        seedMedicationWithColor("Remédio B", l = 52f)

        val extractor = MapFeatureExtractor(mapOf("/q.jpg" to features(51f)))
        val outcome = useCase(extractor).invoke(listOf("/q.jpg"))

        assertTrue(outcome is RecognitionOutcome.Ambiguous)
        assertEquals(2, (outcome as RecognitionOutcome.Ambiguous).candidates.size)
    }

    @Test
    fun `segunda foto agrega pelo melhor score por medicamento`() = runTest {
        val target = seedMedicationWithColor("Remédio A", l = 50f)
        seedMedicationWithColor("Remédio B", l = 80f)

        // 1ª foto ruim para ambos; 2ª foto casa perfeitamente com o alvo.
        val extractor = MapFeatureExtractor(
            mapOf(
                "/q1.jpg" to features(10f),
                "/q2.jpg" to features(50f),
            ),
        )
        val outcome = useCase(extractor).invoke(listOf("/q1.jpg", "/q2.jpg"))

        assertTrue(outcome is RecognitionOutcome.Confident)
        assertEquals(target, (outcome as RecognitionOutcome.Confident).best.medicationId)
    }
}
