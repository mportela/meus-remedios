package com.meusremedios.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.meusremedios.ui.theme.MeusRemediosTheme
import org.junit.Rule
import org.junit.Test

/**
 * Teste de UI Compose da tela placeholder. Valida o setup de testes de UI.
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_showsAppName() {
        composeTestRule.setContent {
            MeusRemediosTheme {
                HomeScreen()
            }
        }

        composeTestRule.onNodeWithText("Meus Remédios").assertIsDisplayed()
    }
}
