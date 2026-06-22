package com.meusremedios.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Medicamento cadastrado pelo usuário.
 *
 * @param id identificador (0 = ainda não persistido).
 * @param name nome do medicamento.
 * @param dosage dosagem (ex.: "500 mg"), opcional.
 * @param notes observações livres, opcional.
 * @param periodType tipo de período de uso.
 * @param startDate data de início (uso [PeriodType.RANGED]), opcional.
 * @param endDate data de término (uso [PeriodType.RANGED]), opcional.
 * @param remindersEnabled se os lembretes deste medicamento estão ativos.
 * @param createdAt instante de criação.
 */
data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String? = null,
    val notes: String? = null,
    val periodType: PeriodType = PeriodType.CONTINUOUS,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val remindersEnabled: Boolean = true,
    val createdAt: Instant = Instant.now(),
)
