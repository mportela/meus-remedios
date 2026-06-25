package com.meusremedios.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meusremedios.R
import com.meusremedios.domain.model.AppSettings

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreenContent(
        settings = settings,
        onSettingsChange = { newSettings -> viewModel.save(newSettings) },
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreenContent(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Histórico
        Text(
            stringResource(R.string.settings_section_history),
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        RetentionDropdown(
            currentValue = settings.historyRetentionDays,
            onSelected = { days ->
                onSettingsChange(settings.copy(historyRetentionDays = days))
            },
        )

        // Câmera
        Text(
            stringResource(R.string.settings_section_camera),
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_auto_capture_label),
                    fontSize = 16.sp,
                )
                Text(
                    stringResource(R.string.settings_auto_capture_desc),
                    fontSize = 14.sp,
                )
            }
            Switch(
                checked = settings.autoCapture,
                onCheckedChange = { auto ->
                    onSettingsChange(settings.copy(autoCapture = auto))
                },
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        // Lembretes
        Text(
            stringResource(R.string.settings_section_reminders),
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.settings_reminders_global_label),
                fontSize = 16.sp,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = settings.remindersGlobal,
                onCheckedChange = { enable ->
                    onSettingsChange(settings.copy(remindersGlobal = enable))
                },
            )
        }

        if (settings.remindersGlobal) {
            ReminderLeadDropdown(
                currentValue = settings.reminderLeadMinutes,
                onSelected = { mins ->
                    onSettingsChange(settings.copy(reminderLeadMinutes = mins))
                },
            )
        }

        // Acessibilidade
        Text(
            stringResource(R.string.settings_section_accessibility),
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        AccessibilityFontDropdown(
            currentValue = settings.fontScale,
            onSelected = { scale ->
                onSettingsChange(settings.copy(fontScale = scale))
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.settings_high_contrast_label),
                    fontSize = 16.sp,
                )
                Text(
                    stringResource(R.string.settings_high_contrast_desc),
                    fontSize = 14.sp,
                )
            }
            Switch(
                checked = settings.highContrast == true,
                onCheckedChange = { hc ->
                    onSettingsChange(settings.copy(highContrast = hc))
                },
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        // Sobre
        Text(
            stringResource(R.string.settings_section_about),
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        Text(
            stringResource(R.string.settings_about_disclaimer),
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Text(
            stringResource(R.string.settings_about_offline),
            fontSize = 14.sp,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun RetentionDropdown(
    currentValue: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(30, 60, 90, 180, 365)
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.settings_history_retention_label),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.Button(
            onClick = { expanded = true },
        ) {
            Text(
                stringResource(R.string.settings_history_retention_days, currentValue),
                fontSize = 14.sp,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { days ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.settings_history_retention_days, days))
                    },
                    onClick = {
                        onSelected(days)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ReminderLeadDropdown(
    currentValue: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(0, 1, 5, 10, 15, 30)
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.settings_reminder_lead_label),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.Button(
            onClick = { expanded = true },
        ) {
            Text(
                stringResource(R.string.settings_reminder_lead_minutes, currentValue),
                fontSize = 14.sp,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { mins ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(R.string.settings_reminder_lead_minutes, mins))
                    },
                    onClick = {
                        onSelected(mins)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AccessibilityFontDropdown(
    currentValue: Float?,
    onSelected: (Float?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options: List<Pair<Float?, Int>> = listOf(
        null to R.string.settings_font_size_default,
        1.15f to R.string.settings_font_size_large,
        1.30f to R.string.settings_font_size_larger,
    )
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == currentValue }?.second
        ?: R.string.settings_font_size_default

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.settings_font_size_label),
            fontSize = 16.sp,
            modifier = Modifier.weight(1f),
        )
        androidx.compose.material3.Button(
            onClick = { expanded = true },
        ) {
            Text(stringResource(currentLabel), fontSize = 14.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (scale, labelRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes)) },
                    onClick = {
                        onSelected(scale)
                        expanded = false
                    },
                )
            }
        }
    }
}
