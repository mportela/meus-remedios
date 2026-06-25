package com.meusremedios.data.repository

import com.meusremedios.domain.model.MedicationPhoto
import kotlinx.coroutines.flow.Flow

/** Mediação entre o domínio e a fonte de dados local de fotos. */
interface MedicationPhotoRepository {
    fun observeByMedication(medicationId: Long): Flow<List<MedicationPhoto>>

    suspend fun getByMedication(medicationId: Long): List<MedicationPhoto>

    suspend fun getAll(): List<MedicationPhoto>

    suspend fun add(photo: MedicationPhoto): Long

    suspend fun update(photo: MedicationPhoto)

    suspend fun delete(photo: MedicationPhoto)

    suspend fun getPhotosWithoutEmbedding(): List<MedicationPhoto>
}
