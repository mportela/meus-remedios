package com.meusremedios.domain.usecase

import com.meusremedios.data.repository.IntakeLogRepository
import com.meusremedios.data.repository.MedicationPhotoRepository
import com.meusremedios.data.repository.MedicationRepository
import com.meusremedios.data.repository.ScheduleRepository
import com.meusremedios.data.repository.SettingsRepository
import com.meusremedios.domain.model.MedicationDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/**
 * Agrega, de forma reativa, os dados de detalhe de um medicamento: dados, horários,
 * fotos e histórico de tomadas limitado pela retenção configurada (RF-2.3, RF-2.6).
 */
class ObserveMedicationDetailUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val photoRepository: MedicationPhotoRepository,
    private val intakeLogRepository: IntakeLogRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: Clock,
) {
    operator fun invoke(medicationId: Long): Flow<MedicationDetail?> =
        combine(
            medicationRepository.observeById(medicationId),
            scheduleRepository.observeByMedication(medicationId),
            photoRepository.observeByMedication(medicationId),
            intakeLogRepository.observeByMedication(medicationId),
            settingsRepository.observe(),
        ) { medication, schedules, photos, logs, settings ->
            if (medication == null) {
                null
            } else {
                val threshold = LocalDate.now(clock).minusDays(settings.historyRetentionDays.toLong())
                val history = logs
                    .filter { !it.date.isBefore(threshold) }
                    .sortedWith(compareByDescending<com.meusremedios.domain.model.IntakeLog> { it.date }.thenByDescending { it.scheduledAt })
                MedicationDetail(
                    medication = medication,
                    schedules = schedules.sortedBy { it.timeOfDay },
                    photos = photos,
                    history = history,
                )
            }
        }
}
