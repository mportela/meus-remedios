package com.meusremedios.data.ml

/**
 * Features de reconhecimento extraídas de uma foto.
 *
 * @param embedding vetor do modelo de embedding; `null` até o engine de
 *   reconhecimento (F4) preenchê-lo.
 * @param dominantColorLab cor dominante em espaço Lab `[L, a, b]`.
 * @param aspectRatio proporção largura/altura da imagem.
 * @param imprintText texto gravado no comprimido (imprint), normalizado; `null`
 *   quando ausente ou quando o OCR não encontrou texto.
 */
data class PhotoFeatures(
    val embedding: FloatArray? = null,
    val dominantColorLab: FloatArray,
    val aspectRatio: Float,
    val imprintText: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhotoFeatures) return false
        if (embedding != null) {
            if (other.embedding == null) return false
            if (!embedding.contentEquals(other.embedding)) return false
        } else if (other.embedding != null) {
            return false
        }
        return dominantColorLab.contentEquals(other.dominantColorLab) &&
            aspectRatio == other.aspectRatio &&
            imprintText == other.imprintText
    }

    override fun hashCode(): Int {
        var result = embedding?.contentHashCode() ?: 0
        result = 31 * result + dominantColorLab.contentHashCode()
        result = 31 * result + aspectRatio.hashCode()
        result = 31 * result + (imprintText?.hashCode() ?: 0)
        return result
    }
}
