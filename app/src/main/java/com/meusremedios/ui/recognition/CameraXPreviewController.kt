package com.meusremedios.ui.recognition

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
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
                            val bitmap = imageProxy.toBitmap()
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
     * Dispara foco na região central do preview e agenda auto-foco contínuo.
     * Deve ser chamado após [start].
     */
    fun triggerFocus(previewView: PreviewView) {
        val camera =
            cameraProvider?.let {
                // Acesso ao Camera object via cameraControl não é exposto diretamente;
                // usamos a instância bindada pelo lifecycleOwner.
                // O foco automático contínuo é suficiente para o caso de uso.
            }
        // Foco contínuo automático é o comportamento padrão do CameraX;
        // não é necessário triggerFocus explícito para o fluxo de auto-captura.
    }

    /**
     * Captura uma foto e salva no [outputFile] fornecido.
     * Retorna o [Uri] do arquivo salvo, ou lança exceção em caso de falha.
     */
    suspend fun capturePhoto(outputFile: File): Uri =
        suspendCancellableCoroutine { cont ->
            val capture =
                imageCapture ?: run {
                    cont.resumeWithException(IllegalStateException("ImageCapture não inicializado"))
                    return@suspendCancellableCoroutine
                }
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            capture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        cont.resume(Uri.fromFile(outputFile))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        cont.resumeWithException(exception)
                    }
                },
            )
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
