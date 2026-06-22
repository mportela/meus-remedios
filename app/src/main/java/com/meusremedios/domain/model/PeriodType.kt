package com.meusremedios.domain.model

/**
 * Tipo de período de uso de um medicamento.
 * - [CONTINUOUS]: uso contínuo, sem data de término prevista.
 * - [RANGED]: uso por período definido (com data de início e fim).
 */
enum class PeriodType {
    CONTINUOUS,
    RANGED,
}
