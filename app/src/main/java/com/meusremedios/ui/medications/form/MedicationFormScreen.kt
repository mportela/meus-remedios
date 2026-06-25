package com.meusremedios.ui.medications.form

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meusremedios.R
import com.meusremedios.domain.model.MedicationPhoto
import com.meusremedios.domain.model.PeriodType
import com.meusremedios.domain.model.PhotoSide
import com.meusremedios.domain.model.ScheduleTime
import com.meusremedios.domain.usecase.MedicationValidationError
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationFormScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicationFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var collisionCandidateName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MedicationFormEvent.CollisionWarning -> collisionCandidateName = event.candidateName
                else -> onDone()
            }
        }
    }

    if (collisionCandidateName != null) {
        AlertDialog(
            onDismissRequest = { collisionCandidateName = null },
            title = { Text(stringResource(R.string.collision_warning_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.collision_warning_body,
                        collisionCandidateName ?: "",
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    collisionCandidateName = null
                    viewModel.saveIgnoringCollision()
                }) {
                    Text(stringResource(R.string.collision_warning_save_anyway))
                }
            },
            dismissButton = {
                TextButton(onClick = { collisionCandidateName = null }) {
                    Text(stringResource(R.string.collision_warning_cancel))
                }
            },
        )
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingSide by remember { mutableStateOf(PhotoSide.FRONT) }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia(),
        ) { uri: Uri? ->
            if (uri != null) viewModel.onGalleryPicked(uri, pendingSide)
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success: Boolean ->
            if (success) viewModel.onCameraCaptured(pendingSide)
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleRes =
                        if (uiState.isEditing) {
                            R.string.medication_form_title_edit
                        } else {
                            R.string.medication_form_title_new
                        }
                    Text(
                        text = stringResource(titleRes),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.medication_form_delete),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NameField(
                value = uiState.name,
                validationError = uiState.validationError,
                onValueChange = viewModel::onNameChange,
            )

            OutlinedTextField(
                value = uiState.dosage,
                onValueChange = viewModel::onDosageChange,
                label = { Text(stringResource(R.string.medication_form_dosage_label)) },
                supportingText = { Text(stringResource(R.string.medication_form_dosage_help)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.medication_form_notes_label)) },
                modifier = Modifier.fillMaxWidth(),
            )

            PeriodSection(
                periodType = uiState.periodType,
                startDate = uiState.startDate,
                endDate = uiState.endDate,
                dateError =
                    uiState.validationError ==
                        MedicationValidationError.END_DATE_BEFORE_START_DATE,
                onPeriodTypeChange = viewModel::onPeriodTypeChange,
                onStartDateChange = viewModel::onStartDateChange,
                onEndDateChange = viewModel::onEndDateChange,
            )

            SchedulesSection(
                schedules = uiState.schedules,
                onAddSchedule = viewModel::addSchedule,
                onRemoveSchedule = viewModel::removeSchedule,
                onUpdateDays = viewModel::updateScheduleDays,
            )

            PhotosSection(
                photos = uiState.photos,
                pendingPhotos = uiState.pendingPhotos,
                onRemovePhoto = viewModel::removePhoto,
                onRemovePendingPhoto = viewModel::removePendingPhoto,
                onPickCamera = { side ->
                    pendingSide = side
                    viewModel.prepareCameraCapture { uri -> cameraLauncher.launch(uri) }
                },
                onPickGallery = { side ->
                    pendingSide = side
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                        ),
                    )
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.medication_form_reminders_label),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = uiState.remindersEnabled,
                    onCheckedChange = viewModel::onRemindersChange,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.medication_form_save))
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun NameField(
    value: String,
    validationError: MedicationValidationError?,
    onValueChange: (String) -> Unit,
) {
    val isError =
        validationError == MedicationValidationError.BLANK_NAME ||
            validationError == MedicationValidationError.DUPLICATE_NAME
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        isError = isError,
        label = { Text(stringResource(R.string.medication_form_name_label)) },
        supportingText = {
            when (validationError) {
                MedicationValidationError.BLANK_NAME ->
                    Text(stringResource(R.string.error_name_required))
                MedicationValidationError.DUPLICATE_NAME ->
                    Text(stringResource(R.string.error_name_duplicate))
                else -> Text(stringResource(R.string.medication_form_name_help))
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSection(
    periodType: PeriodType,
    startDate: LocalDate?,
    endDate: LocalDate?,
    dateError: Boolean,
    onPeriodTypeChange: (PeriodType) -> Unit,
    onStartDateChange: (LocalDate?) -> Unit,
    onEndDateChange: (LocalDate?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.medication_form_period_label),
            style = MaterialTheme.typography.titleMedium,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = periodType == PeriodType.CONTINUOUS,
                onClick = { onPeriodTypeChange(PeriodType.CONTINUOUS) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(
                    stringResource(R.string.medication_form_period_continuous),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SegmentedButton(
                selected = periodType == PeriodType.RANGED,
                onClick = { onPeriodTypeChange(PeriodType.RANGED) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(
                    stringResource(R.string.medication_form_period_ranged),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (periodType == PeriodType.RANGED) {
            DateField(
                label = stringResource(R.string.medication_form_start_date_label),
                date = startDate,
                isError = false,
                onDateChange = onStartDateChange,
            )
            DateField(
                label = stringResource(R.string.medication_form_end_date_label),
                date = endDate,
                isError = dateError,
                onDateChange = onEndDateChange,
            )
            if (dateError) {
                Text(
                    text = stringResource(R.string.error_end_before_start),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    isError: Boolean,
    onDateChange: (LocalDate?) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val display = date?.format(formatter) ?: stringResource(R.string.medication_form_date_not_set)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.weight(1f),
        ) {
            Text("$label: $display")
        }
        if (date != null) {
            TextButton(onClick = { onDateChange(null) }) {
                Text(stringResource(R.string.medication_form_date_clear))
            }
        }
    }

    if (showPicker) {
        val state =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    date
                        ?.atStartOfDay(ZoneOffset.UTC)
                        ?.toInstant()
                        ?.toEpochMilli(),
            )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onDateChange(
                            Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                        )
                    }
                    showPicker = false
                }) {
                    Text(stringResource(R.string.medication_form_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.medication_delete_cancel))
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchedulesSection(
    schedules: List<ScheduleTime>,
    onAddSchedule: (LocalTime, Int) -> Unit,
    onRemoveSchedule: (Int) -> Unit,
    onUpdateDays: (Int, Int) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.medication_form_schedules_label),
            style = MaterialTheme.typography.titleMedium,
        )
        if (schedules.isEmpty()) {
            Text(
                text = stringResource(R.string.medication_form_no_schedules),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        schedules.forEachIndexed { index, schedule ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = schedule.timeOfDay.format(timeFormatter),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    IconButton(onClick = { onRemoveSchedule(index) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription =
                                stringResource(
                                    R.string.medication_form_remove_schedule,
                                ),
                        )
                    }
                }
                WeekdaySelector(
                    mask = schedule.daysOfWeekMask,
                    onMaskChange = { onUpdateDays(index, it) },
                )
            }
        }
        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(
                text = stringResource(R.string.medication_form_add_schedule),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(initialHour = 8, initialMinute = 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onAddSchedule(
                        LocalTime.of(state.hour, state.minute),
                        ScheduleTime.ALL_DAYS_MASK,
                    )
                    showTimePicker = false
                }) {
                    Text(stringResource(R.string.medication_form_add_schedule))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.medication_delete_cancel))
                }
            },
            text = { TimeInput(state = state) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun WeekdaySelector(
    mask: Int,
    onMaskChange: (Int) -> Unit,
) {
    val labels =
        listOf(
            R.string.weekday_monday,
            R.string.weekday_tuesday,
            R.string.weekday_wednesday,
            R.string.weekday_thursday,
            R.string.weekday_friday,
            R.string.weekday_saturday,
            R.string.weekday_sunday,
        )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        labels.forEachIndexed { bit, labelRes ->
            val selected = (mask and (1 shl bit)) != 0
            FilterChip(
                selected = selected,
                onClick = {
                    val newMask = if (selected) mask and (1 shl bit).inv() else mask or (1 shl bit)
                    onMaskChange(newMask)
                },
                label = { Text(stringResource(labelRes)) },
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.medication_delete_title)) },
        text = { Text(stringResource(R.string.medication_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.medication_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.medication_delete_cancel))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun PhotosSection(
    photos: List<MedicationPhoto>,
    pendingPhotos: List<PendingPhoto>,
    onRemovePhoto: (MedicationPhoto) -> Unit,
    onRemovePendingPhoto: (Int) -> Unit,
    onPickCamera: (PhotoSide) -> Unit,
    onPickGallery: (PhotoSide) -> Unit,
) {
    var showChooser by remember { mutableStateOf(false) }
    var chosenSide by remember { mutableStateOf<PhotoSide?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.medication_form_photos_label),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.medication_form_photos_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )

        if (photos.isEmpty() && pendingPhotos.isEmpty()) {
            Text(
                text = stringResource(R.string.medication_form_no_photos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            photos.forEach { photo ->
                PhotoThumbnail(
                    path = photo.filePath,
                    side = photo.side,
                    onRemove = { onRemovePhoto(photo) },
                )
            }
            pendingPhotos.forEachIndexed { index, pending ->
                PhotoThumbnail(
                    path = pending.tempPath,
                    side = pending.side,
                    onRemove = { onRemovePendingPhoto(index) },
                )
            }
        }

        OutlinedButton(
            onClick = { showChooser = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(
                text = stringResource(R.string.medication_form_add_photo),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }

    if (showChooser && chosenSide == null) {
        SideChooserDialog(
            onSelect = { chosenSide = it },
            onDismiss = { showChooser = false },
        )
    }

    chosenSide?.let { side ->
        SourceChooserDialog(
            onCamera = {
                onPickCamera(side)
                chosenSide = null
                showChooser = false
            },
            onGallery = {
                onPickGallery(side)
                chosenSide = null
                showChooser = false
            },
            onDismiss = {
                chosenSide = null
                showChooser = false
            },
        )
    }
}

@Composable
private fun PhotoThumbnail(
    path: String,
    side: PhotoSide,
    onRemove: () -> Unit,
) {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
    val sideDesc =
        stringResource(
            if (side == PhotoSide.FRONT) {
                R.string.medication_form_photo_front_desc
            } else {
                R.string.medication_form_photo_back_desc
            },
        )
    Box {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = sideDesc,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(8.dp)),
            )
        } else {
            Surface(
                modifier =
                    Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {}
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(bottomStart = 8.dp),
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.medication_form_remove_photo),
                )
            }
        }
    }
}

@Composable
private fun SideChooserDialog(
    onSelect: (PhotoSide) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.medication_form_photo_choose_side)) },
        confirmButton = {
            TextButton(onClick = { onSelect(PhotoSide.FRONT) }) {
                Text(stringResource(R.string.medication_form_photo_side_front))
            }
        },
        dismissButton = {
            TextButton(onClick = { onSelect(PhotoSide.BACK) }) {
                Text(stringResource(R.string.medication_form_photo_side_back))
            }
        },
    )
}

@Composable
private fun SourceChooserDialog(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.medication_form_photo_choose_source)) },
        confirmButton = {
            TextButton(onClick = onCamera) {
                Text(stringResource(R.string.medication_form_photo_camera))
            }
        },
        dismissButton = {
            TextButton(onClick = onGallery) {
                Text(stringResource(R.string.medication_form_photo_gallery))
            }
        },
    )
}
