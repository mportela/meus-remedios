package com.meusremedios.ui.recognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Encapsula os use cases CameraX para o preview ao vivo da tela de reconhecimento.
 *
 * Criado pela [RecognitionScreen] quando [autoCaptureEnabled] está ativo.
 * Comunica eventos ao ViewModel via callbacks (sem referência direta ao ViewModel).
 */
class CameraXPreviewController(private val context: Context) {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * Inicia o preview e o ImageAnalysis.
     *
     * @param lifecycleOwner proprietário do ciclo de vida (a Activity/Fragment).
     * @param previewView    view de preview do CameraX.
     * @param onFrameReady   callback chamado para cada frame (bitmap 224×224, ~5 fps).
     */
    fun start(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onFrameReady: (Bitmap) -> Unit,
    ) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            cameraProvider = provider

            val preview =
                Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture =
                ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

            val analysis =
                ImageAnalysis.Builder()
                    .setTargetResolution(Size(224, 224))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also { ia ->
                        ia.setAnalyzer(analysisExecutor) { imageProxy ->
                            val rotation = imageProxy.imageInfo.rotationDegrees
                            val bitmap = imageProxy.toBitmap().rotated(rotation)
                            imageProxy.close()
                            onFrameReady(bitmap)
                        }
                    }

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                analysis,
            )
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Captura uma foto e salva no [outputFile] fornecido.
     * Retorna o [Uri] do arquivo salvo, ou lança exceção em caso de falha.
     *
     * Captura em memória e **assa a rotação do sensor nos pixels** antes de gravar o
     * JPEG. Necessário porque o `ImageCapture` grava em orientação de sensor (deitada)
     * sem tag EXIF; as fotos cadastradas (câmera nativa) ficam em pé. Sem isso, a foto
     * de consulta entra girada 90°, derrubando embedding e forma no reconhecimento.
     */
    suspend fun capturePhoto(outputFile: File): Uri =
        suspendCancellableCoroutine { cont ->
            val capture =
                imageCapture ?: run {
                    cont.resumeWithException(IllegalStateException("ImageCapture não inicializado"))
                    return@suspendCancellableCoroutine
                }
            capture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val bitmap = image.toBitmap().rotated(image.imageInfo.rotationDegrees)
                            FileOutputStream(outputFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                            }
                            cont.resume(Uri.fromFile(outputFile))
                        } catch (t: Throwable) {
                            cont.resumeWithException(t)
                        } finally {
                            image.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWithException(exception)
                    }
                },
            )
        }

    /**
     * Aplica a rotação do sensor ([degrees]) para que o bitmap fique na mesma
     * orientação das fotos cadastradas, evitando degradar a similaridade do embedding.
     */
    private fun Bitmap.rotated(degrees: Int): Bitmap {
        if (degrees == 0) return this
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    private companion object {
        const val JPEG_QUALITY = 95
    }

    /** Libera recursos. Deve ser chamado quando a composição for descartada. */
    fun shutdown() {
        cameraProvider?.unbindAll()
        analysisExecutor.shutdown()
        try {
            analysisExecutor.awaitTermination(1, TimeUnit.SECONDS)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
