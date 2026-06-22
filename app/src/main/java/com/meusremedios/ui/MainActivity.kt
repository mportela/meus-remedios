package com.meusremedios.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.meusremedios.ui.navigation.MeusRemediosNavHost
import com.meusremedios.ui.theme.MeusRemediosTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity única do app (single-activity + Compose). Ponto de entrada de UI.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeusRemediosApp()
        }
    }
}

@Composable
private fun MeusRemediosApp() {
    MeusRemediosTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MeusRemediosNavHost()
        }
    }
}
