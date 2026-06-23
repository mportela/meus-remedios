package com.meusremedios.data.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Lê o texto gravado no comprimido (imprint) usando ML Kit Text Recognition
 * Latin *bundled* — modelo embarcado no APK, sem rede e sem Play Services.
 */
@Singleton
class MlKitImprintReader @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImprintReader {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun read(imagePath: String): String? = try {
        val uri = Uri.fromFile(File(imagePath))
        val image = InputImage.fromFilePath(context, uri)
        val raw = suspendCancellableCoroutine<String?> { cont ->
            recognizer.process(image)
                .addOnSuccessListener { text -> cont.resume(text.text) }
                .addOnFailureListener { cont.resume(null) }
        }
        ImprintMatch.normalize(raw)
    } catch (_: Exception) {
        null
    }
}
