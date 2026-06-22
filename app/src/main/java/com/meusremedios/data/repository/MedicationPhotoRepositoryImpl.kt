package com.meusremedios.data.repository

import com.meusremedios.data.local.dao.MedicationPhotoDao
import com.meusremedios.data.local.mapper.toDomain
import com.meusremedios.data.local.mapper.toEntity
import com.meusremedios.domain.model.MedicationPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationPhotoRepositoryImpl @Inject constructor(
    private val photoDao: MedicationPhotoDao,
) : MedicationPhotoRepository {

    override fun observeByMedication(medicationId: Long): Flow<List<MedicationPhoto>> =
        photoDao.observeByMedication(medicationId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getByMedication(medicationId: Long): List<MedicationPhoto> =
        photoDao.getByMedication(medicationId).map { it.toDomain() }

    override suspend fun getAll(): List<MedicationPhoto> =
        photoDao.getAll().map { it.toDomain() }

    override suspend fun add(photo: MedicationPhoto): Long =
        photoDao.insert(photo.toEntity())

    override suspend fun delete(photo: MedicationPhoto) =
        photoDao.delete(photo.toEntity())
}
