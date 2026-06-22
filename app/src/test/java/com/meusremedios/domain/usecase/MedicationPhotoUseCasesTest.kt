package com.meusremedios.domain.usecase

import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicationPhotoUseCasesTest {

    private val photoRepository = FakeMedicationPhotoRepository()
    private val imageStore = FakeMedicationImageStore()

    @Test
    fun `add extracts features and persists photo`() = runTest {
        val extractor = FakeFeatureExtractor(
            PhotoFeatures(
                embedding = null,
                dominantColorLab = floatArrayOf(60f, 1f, 2f),
                aspectRatio = 1.5f,
            ),
        )
        val useCase = AddMedicationPhotoUseCase(imageStore, extractor, photoRepository)

        val id = useCase(medicationId = 7L, tempPath = "/tmp/x.jpg", side = PhotoSide.FRONT)

        val saved = photoRepository.snapshot().single()
        assertEquals(id, saved.id)
        assertEquals(7L, saved.medicationId)
        assertEquals(PhotoSide.FRONT, saved.side)
        assertEquals(1.5f, saved.aspectRatio)
        assertTrue(floatArrayOf(60f, 1f, 2f).contentEquals(saved.dominantColorLab))
        assertEquals("/files/7/front.jpg", saved.filePath)
    }

    @Test
    fun `remove deletes record and file`() = runTest {
        val photo = MedicationPhoto(
            id = 3L,
            medicationId = 7L,
            filePath = "/files/7/front.jpg",
            side = PhotoSide.FRONT,
        )
        photoRepository.add(photo.copy(id = 0))
        val stored = photoRepository.snapshot().single()
        val useCase = RemoveMedicationPhotoUseCase(imageStore, photoRepository)

        useCase(stored)

        assertTrue(photoRepository.snapshot().isEmpty())
        assertEquals(listOf(stored.filePath), imageStore.deleted)
    }
}
