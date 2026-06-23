package com.meusremedios.data.ml

/**
 * Normalização e similaridade de imprint de comprimido.
 * Funções puras e determinísticas — testáveis sem Android.
 */
object ImprintMatch {

    /** Normaliza: maiúsculas, só alfanuméricos, espaços colapsados. `null` se vazio. */
    fun normalize(raw: String?): String? {
        if (raw == null) return null
        val normalized = raw.uppercase()
            .replace(Regex("[^A-Z0-9 ]"), "")
            .replace(Regex(" {2,}"), " ")
            .trim()
        return normalized.ifEmpty { null }
    }

    /**
     * Similaridade em `[0, 1]` combinando Jaccard de tokens e similaridade de
     * edição, ponderados igualmente. Retorna `0` quando algum argumento é `null`.
     */
    fun similarity(a: String?, b: String?): Float {
        if (a == null || b == null) return 0f
        if (a == b) return 1f
        if (a.isEmpty() || b.isEmpty()) return 0f

        val tokensA = a.split(" ").toSet()
        val tokensB = b.split(" ").toSet()
        val jaccard = tokensA.intersect(tokensB).size.toFloat() /
            tokensA.union(tokensB).size.toFloat()

        val maxLen = maxOf(a.length, b.length).toFloat()
        val editSim = 1f - editDistance(a, b).toFloat() / maxLen

        return (jaccard + editSim) / 2f
    }

    private fun editDistance(a: String, b: String): Int {
        val m = a.length
        val n = b.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) {
            for (j in 1..n) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[m][n]
    }
}
