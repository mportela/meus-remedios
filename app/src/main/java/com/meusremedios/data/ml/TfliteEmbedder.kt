package com.meusremedios.data.ml

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import org.tensorflow.lite.Interpreter

/**
 * Gera o embedding de uma imagem usando um modelo MobileNetV3-Small (TFLite),
 * executado 100% no dispositivo. O modelo é versionado em `assets/` e carregado
 * de forma preguiçosa e thread-safe.
 *
 * Em qualquer falha (modelo ausente, erro de inferência) retorna `null`: o
 * reconhecimento prossegue apenas com cor e forma, sem quebrar o app.
 */
@Singleton
class TfliteEmbedder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    @Volatile
    private var interpreter: Interpreter? = null
    private var unavailable = false

    /**
     * Calcula o embedding L2-normalizado de [bitmap], ou `null` se o modelo não
     * estiver disponível ou a inferência falhar.
     */
    fun embed(bitmap: Bitmap): FloatArray? {
        val tflite = obtainInterpreter() ?: return null
        return try {
            val input = preprocess(bitmap)
            val output = Array(1) { FloatArray(EMBEDDING_SIZE) }
            tflite.run(input, output)
            EmbeddingMath.l2Normalize(output[0])
        } catch (t: Throwable) {
            null
        }
    }

    private fun obtainInterpreter(): Interpreter? {
        interpreter?.let { return it }
        if (unavailable) return null
        synchronized(this) {
            interpreter?.let { return it }
            if (unavailable) return null
            return try {
                Interpreter(loadModel()).also { interpreter = it }
            } catch (t: Throwable) {
                unavailable = true
                null
            }
        }
    }

    private fun loadModel(): MappedByteBuffer {
        context.assets.openFd(MODEL_ASSET).use { fd ->
            fd.createInputStream().channel.use { channel ->
                return channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength,
                )
            }
        }
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val buffer = ByteBuffer
            .allocateDirect(INPUT_SIZE * INPUT_SIZE * CHANNELS * Float.SIZE_BYTES)
            .order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resized.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF).toFloat()
            val g = (pixel shr 8 and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()
            buffer.putFloat((r - MEAN) / STD)
            buffer.putFloat((g - MEAN) / STD)
            buffer.putFloat((b - MEAN) / STD)
        }
        if (resized !== bitmap) resized.recycle()
        buffer.rewind()
        return buffer
    }

    private companion object {
        const val MODEL_ASSET = "mobilenet_v3_small.tflite"
        const val INPUT_SIZE = 224
        const val CHANNELS = 3
        const val EMBEDDING_SIZE = 1024
        const val MEAN = 127.5f
        const val STD = 127.5f
    }
}
