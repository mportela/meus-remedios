package com.meusremedios.domain.usecase

import com.meusremedios.data.ml.FeatureSet
import com.meusremedios.data.ml.RecognitionParams
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckPhotoCollisionUseCaseTest {
    private val medications = FakeMedicationRepository()
    private val photos = FakeMedicationPhotoRepository()
    private val useCase = CheckPhotoCollisionUseCase(photos, medications)

    // Embedding idêntico → cosine = 1.0 → mapeado para 1.0 → score muito alto (passa THRESHOLD)
    private val embedding = FloatArray(4) { 1f }

    // Embedding ortogonal ao anterior → cosine = 0.0 → mapeado para 0.5 → score baixo
    private val orthogonalEmbedding = floatArrayOf(1f, -1f, 1f, -1f)

    private val neutralColor = floatArrayOf(50f, 0f, 0f)

    private suspend fun seedMedication(name: String, emb: FloatArray): Long {
        val id = medications.add(Medication(name = name))
        photos.add(
            MedicationPhoto(
                medicationId = id,
                filePath = "/f/$id.jpg",
                side = PhotoSide.FRONT,
                embedding = emb,
                dominantColorLab = neutralColor,
                aspectRatio = 1f,
            ),
        )
        return id
    }

    @Test
    fun `identical embedding triggers collision`() =
        runTest {
            val id = seedMedication("Caltrat", embedding)
            val query =
                listOf(
                    FeatureSet(embedding = embedding, colorLab = neutralColor, aspectRatio = 1f),
                )
            val result = useCase(query, excludeMedicationId = 0L)
            assertEquals(1, result.size)
            assertEquals("Caltrat", result.first().medicationName)
            assertTrue(result.first().score >= RecognitionParams.THRESHOLD_CONFIDENT)
            // id is used above to seed; suppress unused warning
            assertEquals(id, result.first().medicationId)
        }

    @Test
    fun `orthogonal embedding does not trigger collision`() =
        runTest {
            seedMedication("Caltrat", embedding)
            val query =
                listOf(
                    FeatureSet(embedding = orthogonalEmbedding, colorLab = neutralColor, aspectRatio = 1f),
                )
            val result = useCase(query, excludeMedicationId = 0L)
            assertTrue(result.isEmpty())
        }

    @Test
    fun `excludeMedicationId excludes own medication`() =
        runTest {
            val id = seedMedication("Caltrat", embedding)
            val query =
                listOf(
                    FeatureSet(embedding = embedding, colorLab = neutralColor, aspectRatio = 1f),
                )
            val result = useCase(query, excludeMedicationId = id)
            assertTrue(result.isEmpty())
        }

    @Test
    fun `empty query returns empty`() =
        runTest {
            seedMedication("Caltrat", embedding)
            val result = useCase(emptyList(), excludeMedicationId = 0L)
            assertTrue(result.isEmpty())
        }

    @Test
    fun `no registered photos returns empty`() =
        runTest {
            val query =
                listOf(
                    FeatureSet(embedding = embedding, colorLab = neutralColor, aspectRatio = 1f),
                )
            val result = useCase(query, excludeMedicationId = 0L)
            assertTrue(result.isEmpty())
        }

    @Test
    fun `results sorted by score descending`() =
        runTest {
            // Dois medicamentos: um com embedding idêntico, outro com cor similar
            seedMedication("MedA", embedding)
            val idB = medications.add(Medication(name = "MedB"))
            photos.add(
                MedicationPhoto(
                    medicationId = idB,
                    filePath = "/f/$idB.jpg",
                    side = PhotoSide.FRONT,
                    embedding = embedding,
                    dominantColorLab = floatArrayOf(20f, 0f, 0f), // cor diferente → score ligeiramente menor
                    aspectRatio = 1f,
                ),
            )
            val query =
                listOf(
                    FeatureSet(embedding = embedding, colorLab = neutralColor, aspectRatio = 1f),
                )
            val result = useCase(query, excludeMedicationId = 0L)
            // Ambos devem colidir; primeiro deve ter score >= segundo
            assertTrue(result.size == 2)
            assertTrue(result[0].score >= result[1].score)
        }
}
