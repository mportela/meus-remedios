package com.meusremedios.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meusremedios.R
import com.meusremedios.domain.model.DailyReport
import com.meusremedios.domain.model.DayPeriod
import com.meusremedios.domain.model.DoseStatus
import com.meusremedios.domain.model.ScheduledDose
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val PT_BR: Locale = Locale("pt", "BR")
private val DATE_HEADER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PT_BR)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", PT_BR)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onOpenMedication: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDateState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.today_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WeekSelector(
                selectedDate = selectedDate,
                onSelectDate = viewModel::selectDate,
                modifier = Modifier.padding(top = 8.dp),
            )

            DateHeader(
                selectedDate = selectedDate,
                onPrevious = viewModel::goToPreviousDay,
                onNext = viewModel::goToNextDay,
            )

            val report = uiState.report
            when {
                uiState.isLoading || report == null -> Unit
                report.isEmpty -> EmptyState()
                else -> {
                    SummaryRow(report = report)
                    report.byPeriod().forEach { (period, doses) ->
                        PeriodSection(
                            period = period,
                            doses = doses,
                            onOpenMedication = onOpenMedication,
                            onMarkTaken = viewModel::markTaken,
                            onMarkSkipped = viewModel::markSkipped,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekSelector(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekStart = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val days = (0L..6L).map { weekStart.plusDays(it) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        days.forEach { day ->
            DayChip(
                day = day,
                selected = day == selectedDate,
                onClick = { onSelectDate(day) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayChip(
    day: LocalDate,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val weekdayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, PT_BR).take(3)
    val container = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        color = container,
        contentColor = content,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = weekdayLabel,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DateHeader(
    selectedDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.today_previous_day),
            )
        }
        Text(
            text = selectedDate.format(DATE_HEADER).replaceFirstChar { it.titlecase(PT_BR) },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        IconButton(onClick = onNext) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.today_next_day),
            )
        }
    }
}

@Composable
private fun SummaryRow(report: DailyReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCard(
            value = report.takenCount,
            label = stringResource(R.string.today_summary_taken),
            color = StatusColors.taken(),
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            value = report.pendingCount,
            label = stringResource(R.string.today_summary_pending),
            color = StatusColors.pending(),
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            value = report.lateCount,
            label = stringResource(R.string.today_summary_late),
            color = StatusColors.late(),
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            value = report.skippedCount,
            label = stringResource(R.string.today_summary_skipped),
            color = StatusColors.skipped(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(
    value: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.heightIn(min = 88.dp)) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun PeriodSection(
    period: DayPeriod,
    doses: List<ScheduledDose>,
    onOpenMedication: (Long) -> Unit,
    onMarkTaken: (ScheduledDose) -> Unit,
    onMarkSkipped: (ScheduledDose) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(period.labelRes()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
        doses.forEach { dose ->
            DoseCard(
                dose = dose,
                onOpenMedication = { onOpenMedication(dose.medicationId) },
                onMarkTaken = { onMarkTaken(dose) },
                onMarkSkipped = { onMarkSkipped(dose) },
            )
        }
    }
}

@Composable
private fun DoseCard(
    dose: ScheduledDose,
    onOpenMedication: () -> Unit,
    onMarkTaken: () -> Unit,
    onMarkSkipped: () -> Unit,
) {
    Card(
        onClick = onOpenMedication,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = dose.time.format(TIME_FORMAT),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = dose.medicationName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                DoseStatusBadge(status = dose.status)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                when (dose.status) {
                    DoseStatus.PENDING, DoseStatus.LATE -> {
                        TextButton(onClick = onMarkSkipped) {
                            Text(stringResource(R.string.today_action_skip))
                        }
                        Button(onClick = onMarkTaken) {
                            Text(stringResource(R.string.today_action_taken))
                        }
                    }
                    DoseStatus.TAKEN -> {
                        TextButton(onClick = onMarkTaken) {
                            Text(stringResource(R.string.today_action_undo))
                        }
                    }
                    DoseStatus.SKIPPED -> {
                        TextButton(onClick = onMarkSkipped) {
                            Text(stringResource(R.string.today_action_undo))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoseStatusBadge(status: DoseStatus) {
    val (label, color) =
        when (status) {
            DoseStatus.TAKEN -> stringResource(R.string.dose_status_taken) to StatusColors.taken()
            DoseStatus.PENDING -> stringResource(R.string.dose_status_pending) to StatusColors.pending()
            DoseStatus.LATE -> stringResource(R.string.dose_status_late) to StatusColors.late()
            DoseStatus.SKIPPED -> stringResource(R.string.dose_status_skipped) to StatusColors.skipped()
        }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier =
                Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
                    .clearAndSetSemantics { },
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.today_empty),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

private fun DayPeriod.labelRes(): Int =
    when (this) {
        DayPeriod.MORNING -> R.string.period_morning
        DayPeriod.AFTERNOON -> R.string.period_afternoon
        DayPeriod.NIGHT -> R.string.period_night
    }

/** Cores semânticas para status de dose, com bom contraste em tema claro/escuro. */
private object StatusColors {
    @Composable fun taken(): Color = MaterialTheme.colorScheme.primary

    @Composable fun pending(): Color = MaterialTheme.colorScheme.tertiary

    @Composable fun late(): Color = MaterialTheme.colorScheme.error

    @Composable fun skipped(): Color = MaterialTheme.colorScheme.outline
}
