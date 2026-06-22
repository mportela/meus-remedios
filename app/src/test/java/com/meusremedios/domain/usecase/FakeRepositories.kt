package com.meusremedios.domain.usecase

import com.meusremedios.domain.model.AppSettings
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.Medication
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

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

    override fun observeAll(): Flow<List<ScheduleTime>> = items

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

/** Fake in-memory de [IntakeLogRepository] para testes determinísticos. */
class FakeIntakeLogRepository : IntakeLogRepository {
    private val items = MutableStateFlow<List<IntakeLog>>(emptyList())
    private var nextId = 1L

    fun seed(logs: List<IntakeLog>) {
        items.value = logs
    }

    override fun observeByDate(date: LocalDate): Flow<List<IntakeLog>> =
        items.map { list -> list.filter { it.date == date } }

    override fun observeByMedication(medicationId: Long): Flow<List<IntakeLog>> =
        items.map { list -> list.filter { it.medicationId == medicationId } }

    override suspend fun add(log: IntakeLog): Long {
        val id = nextId++
        items.value = items.value + log.copy(id = id)
        return id
    }

    override suspend fun update(log: IntakeLog) {
        items.value = items.value.map { if (it.id == log.id) log else it }
    }

    override suspend fun delete(log: IntakeLog) {
        items.value = items.value.filterNot { it.id == log.id }
    }

    override suspend fun deleteOlderThan(thresholdDate: LocalDate): Int {
        val before = items.value.size
        items.value = items.value.filterNot { it.date.isBefore(thresholdDate) }
        return before - items.value.size
    }
}

/** Fake in-memory de [SettingsRepository] para testes determinísticos. */
class FakeSettingsRepository(
    initial: AppSettings = AppSettings(),
) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override fun observe(): Flow<AppSettings> = state

    override suspend fun get(): AppSettings = state.value

    override suspend fun update(settings: AppSettings) {
        state.value = settings
    }
}
