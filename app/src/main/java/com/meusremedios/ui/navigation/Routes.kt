package com.meusremedios.ui.navigation

/** Rotas de navegação do app. */
object Routes {
    /** Tela principal: reconhecimento visual do comprimido. */
    const val RECOGNITION = "recognition"

    /** Aba: relatório do dia. */
    const val TODAY = "today"

    const val MEDICATIONS_LIST = "medications"

    /** Detalhe de consulta de um medicamento. */
    const val MEDICATION_DETAIL = "medications/detail"

    /** Formulário de cadastro/edição. `id` ausente (0) = novo medicamento. */
    const val MEDICATION_FORM = "medications/form"
    const val ARG_MEDICATION_ID = "id"
    const val MEDICATION_FORM_PATTERN = "$MEDICATION_FORM?$ARG_MEDICATION_ID={$ARG_MEDICATION_ID}"
    const val MEDICATION_DETAIL_PATTERN = "$MEDICATION_DETAIL/{$ARG_MEDICATION_ID}"

    /** Constrói a rota do formulário para um id específico (0 = novo). */
    fun medicationForm(id: Long = 0L): String = "$MEDICATION_FORM?$ARG_MEDICATION_ID=$id"

    /** Constrói a rota de detalhe de um medicamento. */
    fun medicationDetail(id: Long): String = "$MEDICATION_DETAIL/$id"
}
