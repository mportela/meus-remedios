package com.meusremedios.data.ml

import kotlin.math.pow

/**
 * Conversão de cor sRGB para o espaço CIE L*a*b* (iluminante D65, observador 2°)
 * e cálculo de cor dominante. Operações puras e determinísticas, testáveis fora
 * do Android. A cor Lab é robusta a variações de brilho, útil ao reconhecimento.
 */
object LabColor {

    /** Branco de referência D65 (observador 2°), em escala 0–100. */
    private const val REF_X = 95.047
    private const val REF_Y = 100.000
    private const val REF_Z = 108.883

    /**
     * Converte uma cor ARGB ([android.graphics.Color]-like, canal alfa ignorado)
     * para `[L, a, b]`.
     */
    fun argbToLab(argb: Int): FloatArray {
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        return rgbToLab(r, g, b)
    }

    /** Converte componentes sRGB (0–255) para `[L, a, b]`. */
    fun rgbToLab(r: Int, g: Int, b: Int): FloatArray {
        val (x, y, z) = rgbToXyz(r, g, b)

        val fx = pivotXyz(x / REF_X)
        val fy = pivotXyz(y / REF_Y)
        val fz = pivotXyz(z / REF_Z)

        val l = (116.0 * fy) - 16.0
        val a = 500.0 * (fx - fy)
        val bb = 200.0 * (fy - fz)
        return floatArrayOf(l.toFloat(), a.toFloat(), bb.toFloat())
    }

    /**
     * Cor dominante (média) de um conjunto de pixels ARGB, calculada em Lab.
     * Pixels totalmente transparentes (alfa = 0) são ignorados. Retorna
     * `[0, 0, 0]` quando não há pixels válidos.
     */
    fun dominantLab(pixels: IntArray): FloatArray {
        var sumL = 0.0
        var sumA = 0.0
        var sumB = 0.0
        var count = 0
        for (pixel in pixels) {
            val alpha = (pixel shr 24) and 0xFF
            if (alpha == 0) continue
            val lab = argbToLab(pixel)
            sumL += lab[0]
            sumA += lab[1]
            sumB += lab[2]
            count++
        }
        if (count == 0) return floatArrayOf(0f, 0f, 0f)
        return floatArrayOf(
            (sumL / count).toFloat(),
            (sumA / count).toFloat(),
            (sumB / count).toFloat(),
        )
    }

    private fun rgbToXyz(r: Int, g: Int, b: Int): Triple<Double, Double, Double> {
        val rl = linearize(r / 255.0) * 100.0
        val gl = linearize(g / 255.0) * 100.0
        val bl = linearize(b / 255.0) * 100.0

        val x = rl * 0.4124 + gl * 0.3576 + bl * 0.1805
        val y = rl * 0.2126 + gl * 0.7152 + bl * 0.0722
        val z = rl * 0.0193 + gl * 0.1192 + bl * 0.9505
        return Triple(x, y, z)
    }

    private fun linearize(channel: Double): Double =
        if (channel > 0.04045) ((channel + 0.055) / 1.055).pow(2.4) else channel / 12.92

    private fun pivotXyz(t: Double): Double =
        if (t > 0.008856) t.pow(1.0 / 3.0) else (7.787 * t) + (16.0 / 116.0)
}
