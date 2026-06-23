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
    private val extractor = TfliteFeatureExtractor(base, embedder)

    private val baseFeatures = PhotoFeatures(
        embedding = null,
        dominantColorLab = floatArrayOf(50f, 0f, 0f),
        aspectRatio = 1f,
    )

    @Test
    fun `embedding ausente preserva as features de base`() = runTest {
        coEvery { base.extract(any()) } returns baseFeatures
        every { embedder.embed(any()) } returns null

        val result = extractor.extract("qualquer.jpg")

        assertNull(result.embedding)
        assertEquals(baseFeatures, result)
    }

    @Test
    fun `embedding calculado e anexado as features de base`() = runTest {
        coEvery { base.extract(any()) } returns baseFeatures
        every { embedder.embed(any()) } returns floatArrayOf(0.1f, 0.2f, 0.3f)

        val result = extractor.extract("qualquer.jpg")

        assertArrayEquals(floatArrayOf(0.1f, 0.2f, 0.3f), result.embedding, 0f)
        assertArrayEquals(baseFeatures.dominantColorLab, result.dominantColorLab, 0f)
        assertEquals(baseFeatures.aspectRatio, result.aspectRatio, 0f)
    }
}
