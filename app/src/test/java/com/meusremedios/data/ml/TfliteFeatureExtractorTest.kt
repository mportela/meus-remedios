package com.meusremedios.data.ml

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TfliteFeatureExtractorTest {

    private val base = mockk<DefaultFeatureExtractor>()
    private val embedder = mockk<TfliteEmbedder>()
    private val imprintReader = mockk<ImprintReader>()
    private val extractor = TfliteFeatureExtractor(base, embedder, imprintReader)

    private val baseFeatures = PhotoFeatures(
        embedding = null,
        dominantColorLab = floatArrayOf(50f, 0f, 0f),
        aspectRatio = 1f,
    )

    @Test
    fun `embedding ausente e imprint nulo preserva features de base`() = runTest {
        coEvery { base.extract(any()) } returns baseFeatures
        every { embedder.embed(any()) } returns null
        coEvery { imprintReader.read(any()) } returns null

        val result = extractor.extract("qualquer.jpg")

        assertNull(result.embedding)
        assertNull(result.imprintText)
        assertEquals(baseFeatures, result)
    }

    @Test
    fun `embedding calculado e imprint nulo sao anexados`() = runTest {
        coEvery { base.extract(any()) } returns baseFeatures
        every { embedder.embed(any()) } returns floatArrayOf(0.1f, 0.2f, 0.3f)
        coEvery { imprintReader.read(any()) } returns null

        val result = extractor.extract("qualquer.jpg")

        assertArrayEquals(floatArrayOf(0.1f, 0.2f, 0.3f), result.embedding, 0f)
        assertArrayEquals(baseFeatures.dominantColorLab, result.dominantColorLab, 0f)
        assertEquals(baseFeatures.aspectRatio, result.aspectRatio, 0f)
        assertNull(result.imprintText)
    }

    @Test
    fun `imprint lido pelo reader e anexado ao resultado`() = runTest {
        coEvery { base.extract(any()) } returns baseFeatures
        every { embedder.embed(any()) } returns null
        coEvery { imprintReader.read(any()) } returns "ABC 123"

        val result = extractor.extract("qualquer.jpg")

        assertEquals("ABC 123", result.imprintText)
    }

    @Test
    fun `falha do imprint reader retorna null sem propagar excecao`() = runTest {
        coEvery { base.extract(any()) } returns baseFeatures
        every { embedder.embed(any()) } returns null
        coEvery { imprintReader.read(any()) } returns null

        val result = extractor.extract("qualquer.jpg")

        assertNull(result.imprintText)
        assertArrayEquals(baseFeatures.dominantColorLab, result.dominantColorLab, 0f)
    }
}
