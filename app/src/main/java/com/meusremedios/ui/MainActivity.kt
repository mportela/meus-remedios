package com.meusremedios.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.meusremedios.R
import com.meusremedios.ui.navigation.MeusRemediosNavHost
import com.meusremedios.ui.theme.MeusRemediosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var navigateToRecognition by mutableStateOf(false)
        private set
    private var showPermissionDeniedDialog by mutableStateOf(false)
    private var showExactAlarmWarning by mutableStateOf(false)

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionDeniedDialog = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        handleNavigationIntent(intent)
        setContent {
            MeusRemediosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    MeusRemediosApp(
                        navigateToRecognition = navigateToRecognition,
                        onRecognitionNavigated = { navigateToRecognition = false },
                    )
                    PermissionDeniedDialog(
                        visible = showPermissionDeniedDialog,
                        onDismiss = { showPermissionDeniedDialog = false },
                        onOpenSettings = {
                            showPermissionDeniedDialog = false
                            openAppNotificationSettings()
                        },
                    )
                    ExactAlarmWarningDialog(
                        visible = showExactAlarmWarning,
                        onDismiss = { showExactAlarmWarning = false },
                        onOpenSettings = {
                            showExactAlarmWarning = false
                            openExactAlarmSettings()
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            showExactAlarmWarning = !alarmManager.canScheduleExactAlarms()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NAVIGATE_TO_RECOGNITION, false) == true) {
            navigateToRecognition = true
            intent.removeExtra(NAVIGATE_TO_RECOGNITION)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openAppNotificationSettings() {
        startActivity(
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            },
        )
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        }
    }

    companion object {
        const val NAVIGATE_TO_RECOGNITION = "navigate_to_recognition"
    }
}

@Composable
private fun MeusRemediosApp(
    navigateToRecognition: Boolean,
    onRecognitionNavigated: () -> Unit,
) {
    MeusRemediosTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MeusRemediosNavHost(
                navigateToRecognition = navigateToRecognition,
                onRecognitionNavigated = onRecognitionNavigated,
            )
        }
    }
}

@Composable
private fun PermissionDeniedDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notification_dose_title)) },
        text = { Text(stringResource(R.string.notification_permission_rationale)) },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.notification_permission_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
    )
}

@Composable
private fun ExactAlarmWarningDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notification_dose_title)) },
        text = { Text(stringResource(R.string.notification_exact_alarm_warning)) },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text(stringResource(R.string.notification_permission_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
    )
}
