package com.meusremedios.domain.model

import java.time.Instant

/**
 * Foto de um medicamento com as features usadas no reconhecimento visual.
 *
 * @param id identificador (0 = ainda não persistido).
 * @param medicationId medicamento ao qual a foto pertence.
 * @param filePath caminho do arquivo em armazenamento privado do app.
 * @param side lado registrado (frente/verso).
 * @param embedding vetor de embedding (preenchido na fase de reconhecimento).
 * @param dominantColorLab cor dominante em espaço Lab (L, a, b).
 * @param aspectRatio proporção (largura/altura) do recorte.
 * @param imprintText texto gravado no comprimido (imprint), normalizado; `null`
 *   quando ausente ou quando o OCR não encontrou texto.
 * @param createdAt instante de criação.
 */
data class MedicationPhoto(
    val id: Long = 0,
    val medicationId: Long,
    val filePath: String,
    val side: PhotoSide = PhotoSide.FRONT,
    val embedding: FloatArray? = null,
    val dominantColorLab: FloatArray? = null,
    val aspectRatio: Float? = null,
    val imprintText: String? = null,
    val createdAt: Instant = Instant.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MedicationPhoto) return false
        return id == other.id &&
            medicationId == other.medicationId &&
            filePath == other.filePath &&
            side == other.side &&
            embedding.contentEqualsNullable(other.embedding) &&
            dominantColorLab.contentEqualsNullable(other.dominantColorLab) &&
            aspectRatio == other.aspectRatio &&
            imprintText == other.imprintText &&
            createdAt == other.createdAt
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + medicationId.hashCode()
        result = 31 * result + filePath.hashCode()
        result = 31 * result + side.hashCode()
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        result = 31 * result + (dominantColorLab?.contentHashCode() ?: 0)
        result = 31 * result + (aspectRatio?.hashCode() ?: 0)
        result = 31 * result + (imprintText?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

private fun FloatArray?.contentEqualsNullable(other: FloatArray?): Boolean =
    when {
        this == null && other == null -> true
        this == null || other == null -> false
        else -> this.contentEquals(other)
    }
