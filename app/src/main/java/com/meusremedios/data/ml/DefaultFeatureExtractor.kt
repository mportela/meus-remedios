package com.meusremedios.data.ml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementação padrão de [FeatureExtractor]: decodifica a imagem com downsample,
 * recorta a região central e calcula a cor dominante em Lab e a proporção.
 */
@Singleton
class DefaultFeatureExtractor @Inject constructor() : FeatureExtractor {

    override suspend fun extract(imagePath: String): PhotoFeatures =
        withContext(Dispatchers.Default) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(imagePath, bounds)
            val srcWidth = bounds.outWidth
            val srcHeight = bounds.outHeight
            require(srcWidth > 0 && srcHeight > 0) { "Imagem inválida: $imagePath" }

            val options = BitmapFactory.Options().apply {
                inSampleSize = computeSampleSize(srcWidth, srcHeight, TARGET_SAMPLE)
            }
            val bitmap = BitmapFactory.decodeFile(imagePath, options)
                ?: error("Não foi possível decodificar a imagem: $imagePath")

            try {
                val pixels = centerCropPixels(bitmap)
                PhotoFeatures(
                    embedding = null,
                    dominantColorLab = LabColor.dominantLab(pixels),
                    aspectRatio = srcWidth.toFloat() / srcHeight.toFloat(),
                )
            } finally {
                bitmap.recycle()
            }
        }

    private fun centerCropPixels(bitmap: Bitmap): IntArray {
        val side = minOf(bitmap.width, bitmap.height)
        val left = (bitmap.width - side) / 2
        val top = (bitmap.height - side) / 2
        val pixels = IntArray(side * side)
        bitmap.getPixels(pixels, 0, side, left, top, side, side)
        return pixels
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
        const val TARGET_SAMPLE = 256
    }
}
