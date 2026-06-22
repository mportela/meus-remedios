package com.meusremedios.domain.model

/**
 * Configurações globais do aplicativo (registro único).
 *
 * @param historyRetentionDays dias de retenção do histórico de tomadas.
 * @param autoCapture se a captura automática está habilitada.
 * @param remindersGlobal interruptor global de lembretes.
 * @param reminderLeadMinutes antecedência (min) do lembrete.
 * @param fontScale escala de fonte preferida, opcional.
 * @param highContrast preferência por alto contraste, opcional.
 */
data class AppSettings(
    val historyRetentionDays: Int = DEFAULT_RETENTION_DAYS,
    val autoCapture: Boolean = false,
    val remindersGlobal: Boolean = true,
    val reminderLeadMinutes: Int = DEFAULT_REMINDER_LEAD_MINUTES,
    val fontScale: Float? = null,
    val highContrast: Boolean? = null,
) {
    companion object {
        const val DEFAULT_RETENTION_DAYS: Int = 90
        const val DEFAULT_REMINDER_LEAD_MINUTES: Int = 1
    }
}
