package com.meusremedios.ui.medications.detail

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meusremedios.R
import com.meusremedios.domain.model.IntakeLog
import com.meusremedios.domain.model.IntakeStatus
import com.meusremedios.domain.model.MedicationDetail
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.model.ScheduleTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PT_BR: Locale = Locale("pt", "BR")
private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", PT_BR)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MedicationDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicationDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = uiState.detail

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail?.medication?.name
                            ?: stringResource(R.string.medication_detail_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (detail != null) {
                ExtendedFloatingActionButton(
                    onClick = { onEdit(detail.medication.id) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text(stringResource(R.string.medication_detail_edit)) },
                )
            }
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Unit
            detail == null -> NotFound(modifier = Modifier.padding(innerPadding))
            else -> DetailContent(
                detail = detail,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: MedicationDetail,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DataSection(detail)
        if (detail.photos.isNotEmpty()) {
            PhotosSection(detail.photos)
        }
        SchedulesSection(detail.schedules)
        HistorySection(detail.history)
    }
}

@Composable
private fun DataSection(detail: MedicationDetail) {
    val medication = detail.medication
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        medication.dosage?.takeIf { it.isNotBlank() }?.let { dosage ->
            LabeledValue(label = stringResource(R.string.medication_detail_dosage), value = dosage)
        }
        medication.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            LabeledValue(label = stringResource(R.string.medication_detail_notes), value = notes)
        }
        val periodText = if (medication.periodType == PeriodType.RANGED) {
            val start = medication.startDate?.format(DATE_FORMAT)
                ?: stringResource(R.string.medication_form_date_not_set)
            val end = medication.endDate?.format(DATE_FORMAT)
                ?: stringResource(R.string.medication_form_date_not_set)
            stringResource(R.string.medication_detail_period_ranged, start, end)
        } else {
            stringResource(R.string.medication_detail_period_continuous)
        }
        LabeledValue(label = stringResource(R.string.medication_detail_period), value = periodText)
        LabeledValue(
            label = stringResource(R.string.medication_detail_reminders),
            value = stringResource(
                if (medication.remindersEnabled) {
                    R.string.medication_detail_reminders_on
                } else {
                    R.string.medication_detail_reminders_off
                },
            ),
        )
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.outline,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PhotosSection(photos: List<MedicationPhoto>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(stringResource(R.string.medication_detail_photos))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(photos, key = { it.id }) { photo ->
                PhotoThumbnail(photo)
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(photo: MedicationPhoto) {
    val bitmap = remember(photo.filePath) { BitmapFactory.decodeFile(photo.filePath) }
    val desc = stringResource(
        if (photo.side == PhotoSide.FRONT) {
            R.string.medication_form_photo_front_desc
        } else {
            R.string.medication_form_photo_back_desc
        },
    )
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = desc,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
    } else {
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {}
    }
}

@Composable
private fun SchedulesSection(schedules: List<ScheduleTime>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(stringResource(R.string.medication_detail_schedules))
        if (schedules.isEmpty()) {
            Text(
                text = stringResource(R.string.medication_form_no_schedules),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        } else {
            schedules.forEach { schedule ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = schedule.timeOfDay.format(TIME_FORMAT),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = weekdaysLabel(schedule.daysOfWeekMask),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySection(history: List<IntakeLog>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(stringResource(R.string.medication_detail_history))
        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.medication_detail_history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        } else {
            val zone = remember { ZoneId.systemDefault() }
            history.forEach { log ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = log.date.format(DATE_FORMAT),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = stringResource(historyStatusRes(log.status)),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun NotFound(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = stringResource(R.string.medication_detail_not_found),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun historyStatusRes(status: IntakeStatus): Int = when (status) {
    IntakeStatus.TAKEN -> R.string.dose_status_taken
    IntakeStatus.PENDING -> R.string.dose_status_pending
    IntakeStatus.LATE -> R.string.dose_status_late
    IntakeStatus.SKIPPED -> R.string.dose_status_skipped
}

@Composable
private fun weekdaysLabel(mask: Int): String {
    val allDays = ScheduleTime.ALL_DAYS_MASK
    if (mask and allDays == allDays) {
        return stringResource(R.string.weekdays_all)
    }
    val labels = listOf(
        R.string.weekday_monday,
        R.string.weekday_tuesday,
        R.string.weekday_wednesday,
        R.string.weekday_thursday,
        R.string.weekday_friday,
        R.string.weekday_saturday,
        R.string.weekday_sunday,
    )
    val selected = labels.filterIndexed { index, _ -> mask and (1 shl index) != 0 }
        .map { stringResource(it) }
    return if (selected.isEmpty()) {
        stringResource(R.string.medications_item_no_schedule)
    } else {
        selected.joinToString(", ")
    }
}
