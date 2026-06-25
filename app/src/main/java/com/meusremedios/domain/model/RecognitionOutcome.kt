package com.meusremedios.domain.model

/** Candidato a correspondência no reconhecimento, com seu score em `[0, 1]`. */
data class RecognitionCandidate(
    val medicationId: Long,
    val medicationName: String,
    val score: Float,
)

/** Resultado do reconhecimento de um comprimido pela câmera. */
sealed interface RecognitionOutcome {
    /** Nenhum medicamento cadastrado possui foto para comparação. */
    data object NoPhotosRegistered : RecognitionOutcome

    /** Nenhuma foto cadastrada atingiu a similaridade mínima. */
    data object NoMatch : RecognitionOutcome

    /** Identificação confiante de um medicamento. */
    data class Confident(
        val best: RecognitionCandidate,
        val candidates: List<RecognitionCandidate>,
    ) : RecognitionOutcome

    /** Resultado ambíguo: sugerir 2ª foto e/ou listar candidatos. */
    data class Ambiguous(
        val candidates: List<RecognitionCandidate>,
    ) : RecognitionOutcome
}
