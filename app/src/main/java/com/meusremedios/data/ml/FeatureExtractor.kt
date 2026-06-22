package com.meusremedios.data.ml

/**
 * Extrai as features de reconhecimento de uma imagem em disco.
 *
 * Nesta fase calcula cor dominante (Lab) e proporção; o `embedding` permanece
 * `null` e será preenchido pelo engine de reconhecimento (F4).
 */
interface FeatureExtractor {
    suspend fun extract(imagePath: String): PhotoFeatures
}
