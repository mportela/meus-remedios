package com.meusremedios.data.ml

import org.junit.Assert.assertEquals
import org.junit.Test

class LabColorTest {

    private fun assertLab(expected: FloatArray, actual: FloatArray, eps: Float = 0.5f) {
        assertEquals(expected[0], actual[0], eps)
        assertEquals(expected[1], actual[1], eps)
        assertEquals(expected[2], actual[2], eps)
    }

    @Test
    fun `black converts to Lab zero`() {
        assertLab(floatArrayOf(0f, 0f, 0f), LabColor.rgbToLab(0, 0, 0))
    }

    @Test
    fun `white converts to L100`() {
        assertLab(floatArrayOf(100f, 0f, 0f), LabColor.rgbToLab(255, 255, 255))
    }

    @Test
    fun `pure red has known Lab`() {
        // sRGB(255,0,0) -> L≈53.24, a≈80.09, b≈67.20
        assertLab(floatArrayOf(53.24f, 80.09f, 67.20f), LabColor.rgbToLab(255, 0, 0))
    }

    @Test
    fun `conversion is deterministic`() {
        val first = LabColor.rgbToLab(123, 45, 67)
        val second = LabColor.rgbToLab(123, 45, 67)
        assertEquals(first.toList(), second.toList())
    }

    @Test
    fun `dominant ignores transparent pixels`() {
        val opaqueWhite = 0xFFFFFFFF.toInt()
        val transparent = 0x00000000
        val result = LabColor.dominantLab(intArrayOf(opaqueWhite, transparent, transparent))
        assertLab(floatArrayOf(100f, 0f, 0f), result)
    }

    @Test
    fun `dominant of no valid pixels is zero`() {
        val result = LabColor.dominantLab(intArrayOf(0x00000000, 0x00000000))
        assertLab(floatArrayOf(0f, 0f, 0f), result, eps = 0.001f)
    }
}
