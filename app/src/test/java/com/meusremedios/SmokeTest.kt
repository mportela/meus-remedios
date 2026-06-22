package com.meusremedios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Smoke test JVM: garante que a infraestrutura de testes unitários executa.
 * Substituído/expandido por testes reais de use cases nas próximas fases.
 */
class SmokeTest {

    @Test
    fun infrastructure_isReady() {
        assertEquals(4, 2 + 2)
        assertTrue(true)
    }
}
