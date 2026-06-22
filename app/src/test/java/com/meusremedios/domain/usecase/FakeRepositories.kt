package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Fake in-memory de [MedicationRepository] para testes determinísticos. */
class FakeMedicationRepository : MedicationRepository {
    private val items = MutableStateFlow<List<Medication>>(emptyList())
    private var nextId = 1L

    fun snapshot(): List<Medication> = items.value

    override fun observeAll(): Flow<List<Medication>> =
        items.map { list -> list.sortedBy { it.name.lowercase() } }

    override fun observeById(id: Long): Flow<Medication?> =
        items.map { list -> list.firstOrNull { it.id == id } }

    override fun search(query: String): Flow<List<Medication>> =
        items.map { list ->
            list.filter { it.name.contains(query, ignoreCase = true) }
                .sortedBy { it.name.lowercase() }
        }

    override suspend fun getById(id: Long): Medication? =
        items.value.firstOrNull { it.id == id }

    override suspend fun add(medication: Medication): Long {
        val id = nextId++
        items.value = items.value + medication.copy(id = id)
        return id
    }

    override suspend fun update(medication: Medication) {
        items.value = items.value.map { if (it.id == medication.id) medication else it }
    }

    override suspend fun delete(medication: Medication) {
        items.value = items.value.filterNot { it.id == medication.id }
    }
}

/** Fake in-memory de [ScheduleRepository] para testes determinísticos. */
class FakeScheduleRepository : ScheduleRepository {
    private val items = MutableStateFlow<List<ScheduleTime>>(emptyList())
    private var nextId = 1L

    fun snapshot(): List<ScheduleTime> = items.value

    override fun observeByMedication(medicationId: Long): Flow<List<ScheduleTime>> =
        items.map { list -> list.filter { it.medicationId == medicationId } }

    override suspend fun getAll(): List<ScheduleTime> = items.value

    override suspend fun add(scheduleTime: ScheduleTime): Long {
        val id = nextId++
        items.value = items.value + scheduleTime.copy(id = id)
        return id
    }

    override suspend fun update(scheduleTime: ScheduleTime) {
        items.value = items.value.map { if (it.id == scheduleTime.id) scheduleTime else it }
    }

    override suspend fun delete(scheduleTime: ScheduleTime) {
        items.value = items.value.filterNot { it.id == scheduleTime.id }
    }
}
