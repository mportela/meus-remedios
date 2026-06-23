package com.meusremedios.data.ml

import android.graphics.BitmapFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [FeatureExtractor] que decora o [DefaultFeatureExtractor] (cor + forma) e
 * acrescenta o `embedding` de imagem calculado on-device por [TfliteEmbedder].
 *
 * Se o embedder não estiver disponível ou falhar, o embedding fica `null` e as
 * features de cor/forma são preservadas.
 */
@Singleton
class TfliteFeatureExtractor @Inject constructor(
    private val base: DefaultFeatureExtractor,
    private val embedder: TfliteEmbedder,
) : FeatureExtractor {

    override suspend fun extract(imagePath: String): PhotoFeatures {
        val baseFeatures = base.extract(imagePath)
        val embedding = withContext(Dispatchers.Default) { computeEmbedding(imagePath) }
        return if (embedding == null) baseFeatures else baseFeatures.copy(embedding = embedding)
    }

    private fun computeEmbedding(imagePath: String): FloatArray? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(imagePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = computeSampleSize(bounds.outWidth, bounds.outHeight, INPUT_SIZE)
        }
        val bitmap = BitmapFactory.decodeFile(imagePath, options) ?: return null
        return try {
            embedder.embed(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private fun computeSampleSize(width: Int, height: Int, target: Int): Int {
        var sample = 1
        var w = width
        var h = height
        while (w / 2 >= target && h / 2 >= target) {
            w /= 2
            h /= 2
            sample *= 2
        }
        return sample
    }

    private companion object {
        const val INPUT_SIZE = 224
    }
}
