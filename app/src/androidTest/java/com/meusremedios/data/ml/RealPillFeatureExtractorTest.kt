package com.meusremedios.data.ml

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Extrai features reais (embedding TFLite + cor Lab + imprint OCR) das 6 fotos de controle
 * localizadas em androidTest/assets/. O output no Logcat (tag PILL_FIXTURES) contém as
 * constantes Kotlin prontas para copiar em RealPillFixtures.kt.
 *
 * Rodar uma vez com: make connected
 */
@RunWith(AndroidJUnit4::class)
class RealPillFeatureExtractorTest {
    private val pills =
        listOf(
            "PILL_A_FRENTE" to "pill_a_frente.jpg",
            "PILL_A_VERSO" to "pill_a_verso.jpg",
            "PILL_B_FRENTE" to "pill_b_frente.jpg",
            "PILL_B_VERSO" to "pill_b_verso.jpg",
            "PILL_C_FRENTE" to "pill_c_frente.jpg",
            "PILL_C_VERSO" to "pill_c_verso.jpg",
        )

    @Test
    fun extractAndLogFeatures() {
        runBlocking {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val appContext = instrumentation.targetContext
            val testContext = instrumentation.context

            val embedder = TfliteEmbedder(appContext)
            val imprintReader = MlKitImprintReader(appContext)
            val defaultExtractor = DefaultFeatureExtractor()
            val extractor = TfliteFeatureExtractor(defaultExtractor, embedder, imprintReader)

            val sb = StringBuilder()
            sb.appendLine()
            sb.appendLine("// ---- RealPillFixtures.kt — colar abaixo ----")
            sb.appendLine("// Extraído por RealPillFeatureExtractorTest em ${java.time.LocalDate.now()}")
            sb.appendLine()

            for ((constName, assetName) in pills) {
                val tmpFile = copyAssetToTemp(testContext, appContext, assetName)
                try {
                    val features = extractor.extract(tmpFile.absolutePath)
                    sb.appendLine(buildConst(constName, features))
                } finally {
                    tmpFile.delete()
                }
            }

            sb.appendLine("// ---- fim ----")

            // Salvar em arquivo (evita truncamento do logcat para embeddings grandes)
            val outFile = File(appContext.cacheDir, "RealPillFixtures.kt.txt")
            outFile.writeText(sb.toString())
            android.util.Log.i("PILL_FIXTURES", "Output salvo em: ${outFile.absolutePath}")
            android.util.Log.i("PILL_FIXTURES", "Execute: adb pull ${outFile.absolutePath} /tmp/RealPillFixtures.kt.txt")
        } // runBlocking
    }

    private fun copyAssetToTemp(
        testContext: android.content.Context,
        appContext: android.content.Context,
        assetName: String,
    ): File {
        val tmp = File(appContext.cacheDir, assetName)
        testContext.assets.open(assetName).use { input ->
            tmp.outputStream().use { output -> input.copyTo(output) }
        }
        return tmp
    }

    private fun buildConst(
        name: String,
        f: PhotoFeatures,
    ): String {
        val emb = f.embedding
        val embStr =
            if (emb != null) {
                "floatArrayOf(${emb.joinToString(", ") { "%.6ff".format(it) }})"
            } else {
                "null"
            }
        val lab = f.dominantColorLab
        val labStr = "floatArrayOf(${lab.joinToString(", ") { "%.6ff".format(it) }})"
        val imprintStr = if (f.imprintText != null) "\"${f.imprintText}\"" else "null"
        return buildString {
            appendLine("val $name = FeatureSet(")
            appendLine("    embedding = $embStr,")
            appendLine("    colorLab = $labStr,")
            appendLine("    aspectRatio = ${"%.6ff".format(f.aspectRatio)},")
            appendLine("    imprintText = $imprintStr,")
            append(")")
        }
    }
}
