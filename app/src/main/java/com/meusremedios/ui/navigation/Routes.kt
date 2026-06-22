package com.meusremedios.ui.navigation

/** Rotas de navegação do app. */
object Routes {
    const val MEDICATIONS_LIST = "medications"

    /** Formulário de cadastro/edição. `id` ausente (0) = novo medicamento. */
    const val MEDICATION_FORM = "medications/form"
    const val ARG_MEDICATION_ID = "id"
    const val MEDICATION_FORM_PATTERN = "$MEDICATION_FORM?$ARG_MEDICATION_ID={$ARG_MEDICATION_ID}"

    /** Constrói a rota do formulário para um id específico (0 = novo). */
    fun medicationForm(id: Long = 0L): String = "$MEDICATION_FORM?$ARG_MEDICATION_ID=$id"
}
