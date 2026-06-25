package com.meusremedios.domain.usecase

import android.net.Uri
import com.meusremedios.data.media.CameraTarget
import com.meusremedios.data.media.MedicationImageStore
import com.meusremedios.data.ml.FeatureExtractor
import com.meusremedios.data.ml.PhotoFeatures
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PhotoSide
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Fake in-memory de [MedicationPhotoRepository] para testes. */
class FakeMedicationPhotoRepository : MedicationPhotoRepository {
    private val items = MutableStateFlow<List<MedicationPhoto>>(emptyList())
    private var nextId = 1L

    fun snapshot(): List<MedicationPhoto> = items.value

    override fun observeByMedication(medicationId: Long): Flow<List<MedicationPhoto>> =
        items.map { list -> list.filter { it.medicationId == medicationId } }

    override suspend fun getByMedication(medicationId: Long): List<MedicationPhoto> = items.value.filter { it.medicationId == medicationId }

    override suspend fun getAll(): List<MedicationPhoto> = items.value

    override suspend fun add(photo: MedicationPhoto): Long {
        val id = nextId++
        items.value = items.value + photo.copy(id = id)
        return id
    }

    override suspend fun update(photo: MedicationPhoto) {
        items.value = items.value.map { if (it.id == photo.id) photo else it }
    }

    override suspend fun delete(photo: MedicationPhoto) {
        items.value = items.value.filterNot { it.id == photo.id }
    }

    override suspend fun getPhotosWithoutEmbedding(): List<MedicationPhoto> = items.value.filter { it.embedding == null }
}

/** Fake de [MedicationImageStore] que rastreia chamadas. */
class FakeMedicationImageStore : MedicationImageStore {
    val persisted = mutableListOf<String>()
    val deleted = mutableListOf<String>()

    override suspend fun stage(uri: Uri): String = "/tmp/staged.jpg"

    override suspend fun createCameraTarget(): CameraTarget = CameraTarget(tempPath = "/tmp/camera.jpg", uri = Uri.EMPTY)

    override suspend fun persist(
        tempPath: String,
        medicationId: Long,
        side: PhotoSide,
    ): String {
        val finalPath = "/files/$medicationId/${side.name.lowercase()}.jpg"
        persisted += finalPath
        return finalPath
    }

    override suspend fun delete(path: String) {
        deleted += path
    }
}

/** Fake de [FeatureExtractor] determinístico. */
class FakeFeatureExtractor(
    private val features: PhotoFeatures =
        PhotoFeatures(
            embedding = null,
            dominantColorLab = floatArrayOf(50f, 0f, 0f),
            aspectRatio = 1f,
        ),
) : FeatureExtractor {
    override suspend fun extract(imagePath: String): PhotoFeatures = features
}
