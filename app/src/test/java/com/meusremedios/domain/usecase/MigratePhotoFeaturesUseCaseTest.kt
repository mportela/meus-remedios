package com.meusremedios.domain.usecase

import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.domain.model.MedicationPhoto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class MigratePhotoFeaturesUseCaseTest {
    private val fakeFeatures =
        PhotoFeatures(
            embedding = floatArrayOf(0.1f, 0.2f, 0.3f),
            dominantColorLab = floatArrayOf(50f, 10f, 5f),
            aspectRatio = 0.75f,
            imprintText = "A10",
        )

    private fun photo(
        id: Long,
        embedding: FloatArray? = null,
    ) = MedicationPhoto(
        id = id,
        medicationId = 1L,
        filePath = "/fake/pill_$id.jpg",
        embedding = embedding,
        createdAt = Instant.EPOCH,
    )

    @Test
    fun `fotos sem embedding sao reprocessadas e atualizadas`() =
        runTest {
            val repo =
                object : StubPhotoRepository() {
                    val updated = mutableListOf<MedicationPhoto>()

                    override suspend fun getPhotosWithoutEmbedding() = listOf(photo(1L), photo(2L))

                    override suspend fun update(p: MedicationPhoto) {
                        updated += p
                    }
                }
            val extractor = mockk<FeatureExtractor>()
            coEvery { extractor.extract(any()) } returns fakeFeatures

            MigratePhotoFeaturesUseCase(repo, extractor)()

            assertEquals(2, repo.updated.size)
            coVerify(exactly = 2) { extractor.extract(any()) }
            repo.updated.forEach { p ->
                assertEquals(fakeFeatures.embedding, p.embedding)
                assertEquals(fakeFeatures.imprintText, p.imprintText)
            }
        }

    @Test
    fun `sem fotos elegiveis nao chama extractor`() =
        runTest {
            val repo =
                object : StubPhotoRepository() {
                    override suspend fun getPhotosWithoutEmbedding() = emptyList<MedicationPhoto>()
                }
            val extractor = mockk<FeatureExtractor>()

            MigratePhotoFeaturesUseCase(repo, extractor)()

            coVerify(exactly = 0) { extractor.extract(any()) }
        }

    @Test
    fun `falha em uma foto nao interrompe as demais`() =
        runTest {
            val repo =
                object : StubPhotoRepository() {
                    val updated = mutableListOf<MedicationPhoto>()

                    override suspend fun getPhotosWithoutEmbedding() = listOf(photo(1L), photo(2L), photo(3L))

                    override suspend fun update(p: MedicationPhoto) {
                        updated += p
                    }
                }
            val extractor = mockk<FeatureExtractor>()
            coEvery { extractor.extract("/fake/pill_1.jpg") } throws RuntimeException("arquivo ausente")
            coEvery { extractor.extract(match { it != "/fake/pill_1.jpg" }) } returns fakeFeatures

            MigratePhotoFeaturesUseCase(repo, extractor)()

            assertEquals(2, repo.updated.size)
        }
}

private abstract class StubPhotoRepository : MedicationPhotoRepository {
    override fun observeByMedication(medicationId: Long): Flow<List<MedicationPhoto>> = throw UnsupportedOperationException()

    override suspend fun getByMedication(medicationId: Long): List<MedicationPhoto> = throw UnsupportedOperationException()

    override suspend fun getAll(): List<MedicationPhoto> = throw UnsupportedOperationException()

    override suspend fun add(photo: MedicationPhoto): Long = throw UnsupportedOperationException()

    override suspend fun update(photo: MedicationPhoto): Unit = throw UnsupportedOperationException()

    override suspend fun delete(photo: MedicationPhoto): Unit = throw UnsupportedOperationException()

    override suspend fun getPhotosWithoutEmbedding(): List<MedicationPhoto> = throw UnsupportedOperationException()
}
