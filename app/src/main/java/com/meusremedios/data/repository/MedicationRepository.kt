package com.meusremedios.data.repository

import com.meusremedios.domain.model.Medication
import kotlinx.coroutines.flow.Flow

/** Mediação entre o domínio e a fonte de dados local de medicamentos. */
interface MedicationRepository {
    fun observeAll(): Flow<List<Medication>>

    fun observeById(id: Long): Flow<Medication?>

    fun search(query: String): Flow<List<Medication>>

    suspend fun getById(id: Long): Medication?

    suspend fun add(medication: Medication): Long

    suspend fun update(medication: Medication)

    suspend fun delete(medication: Medication)
}
