package com.meusremedios.ui.recognition

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meusremedios.BuildConfig
import com.meusremedios.R
import androidx.compose.ui.unit.sp
import com.meusremedios.domain.model.RecognitionCandidate
import com.meusremedios.domain.model.RecognitionOutcome
import com.meusremedios.domain.model.ScheduledDose
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale("pt", "BR"))

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(
    modifier: Modifier = Modifier,
    viewModel: RecognitionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success: Boolean ->
        if (success) viewModel.onCaptured()
    }
    val capture: () -> Unit = {
        viewModel.prepareCapture { uri -> cameraLauncher.launch(uri) }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) viewModel.onGalleryPicked(uri)
    }
    val pickFromGallery: (() -> Unit)? = if (BuildConfig.DEV_TOOLS_ENABLED) {
        {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
    } else {
        null
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recognition_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            when (uiState.phase) {
                RecognitionPhase.IDLE -> IdleContent(
                    onCapture = capture,
                    onPickFromGallery = pickFromGallery,
                )
                RecognitionPhase.ANALYZING -> AnalyzingContent()
                RecognitionPhase.RESULT -> ResultContent(
                    outcome = uiState.outcome,
                    canAddSecondPhoto = uiState.canAddSecondPhoto,
                    intakeRegistered = uiState.intakeRegistered,
                    onCapture = capture,
                    onPickFromGallery = pickFromGallery,
                    onReset = viewModel::reset,
                    onMarkTaken = viewModel::markTakenFromRecognition,
                )
                RecognitionPhase.ERROR -> ErrorContent(onReset = viewModel::reset)
            }
        }
    }

    if (uiState.pendingDosesToday.size > 1) {
        ModalBottomSheet(
            onDismissRequest = { coroutineScope.launch { sheetState.hide() } },
            sheetState = sheetState,
        ) {
            DosePickerSheet(
                doses = uiState.pendingDosesToday,
                onDoseSelected = { dose ->
                    coroutineScope.launch { sheetState.hide() }
                    viewModel.markTakenForDose(dose)
                },
            )
        }
    }
}

@Composable
private fun IdleContent(onCapture: () -> Unit, onPickFromGallery: (() -> Unit)?) {
    Text(
        text = stringResource(R.string.recognition_intro),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
    )
    BigConfirmButton(onClick = onCapture)
    if (onPickFromGallery != null) {
        GalleryDevButton(onClick = onPickFromGallery)
    }
}

@Composable
private fun GalleryDevButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.recognition_gallery_dev_button))
    }
}

@Composable
private fun BigConfirmButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.recognition_confirm_button),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AnalyzingContent() {
    CircularProgressIndicator(modifier = Modifier.size(64.dp))
    Text(
        text = stringResource(R.string.recognition_analyzing),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ResultContent(
    outcome: RecognitionOutcome?,
    canAddSecondPhoto: Boolean,
    intakeRegistered: Boolean,
    onCapture: () -> Unit,
    onPickFromGallery: (() -> Unit)?,
    onReset: () -> Unit,
    onMarkTaken: () -> Unit,
) {
    val resultDescription = when (outcome) {
        is RecognitionOutcome.Confident -> stringResource(R.string.cd_recognition_confident, outcome.best.medicationName)
        is RecognitionOutcome.Ambiguous -> stringResource(R.string.cd_recognition_ambiguous)
        RecognitionOutcome.NoMatch -> stringResource(R.string.cd_recognition_no_match)
        RecognitionOutcome.NoPhotosRegistered -> stringResource(R.string.recognition_no_photos)
        null -> stringResource(R.string.recognition_error)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = resultDescription
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (outcome) {
            is RecognitionOutcome.Confident -> ConfidentResult(
                outcome = outcome,
                intakeRegistered = intakeRegistered,
                onMarkTaken = onMarkTaken,
            )
            is RecognitionOutcome.Ambiguous -> AmbiguousResult(
                outcome = outcome,
                canAddSecondPhoto = canAddSecondPhoto,
                onCapture = onCapture,
                onPickFromGallery = onPickFromGallery,
            )
            RecognitionOutcome.NoMatch -> MessageResult(stringResource(R.string.recognition_no_match))
            RecognitionOutcome.NoPhotosRegistered ->
                MessageResult(stringResource(R.string.recognition_no_photos))
            null -> MessageResult(stringResource(R.string.recognition_error))
        }
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.recognition_restart))
        }
    }
}

@Composable
private fun ConfidentResult(
    outcome: RecognitionOutcome.Confident,
    intakeRegistered: Boolean,
    onMarkTaken: () -> Unit,
) {
    Icon(
        Icons.Default.CheckCircle,
        contentDescription = stringResource(R.string.cd_recognition_result_icon),
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(72.dp),
    )
    Text(
        text = stringResource(R.string.recognition_confident_prefix),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
    )
    Text(
        text = outcome.best.medicationName,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
    if (intakeRegistered) {
        Text(
            text = stringResource(R.string.recognition_intake_registered),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    } else {
        Button(
            onClick = onMarkTaken,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.recognition_action_taken))
        }
    }
}

@Composable
private fun DosePickerSheet(
    doses: List<ScheduledDose>,
    onDoseSelected: (ScheduledDose) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.recognition_dose_picker_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        doses.forEach { dose ->
            TextButton(
                onClick = { onDoseSelected(dose) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = dose.time.format(TIME_FORMAT),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AmbiguousResult(
    outcome: RecognitionOutcome.Ambiguous,
    canAddSecondPhoto: Boolean,
    onCapture: () -> Unit,
    onPickFromGallery: (() -> Unit)?,
) {
    Text(
        text = stringResource(R.string.recognition_ambiguous),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
    if (canAddSecondPhoto) {
        Text(
            text = stringResource(R.string.recognition_second_photo_hint),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onCapture,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Text(
                text = stringResource(R.string.recognition_second_photo_button),
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        if (onPickFromGallery != null) {
            GalleryDevButton(onClick = onPickFromGallery)
        }
    }
    Text(
        text = stringResource(R.string.recognition_candidates),
        style = MaterialTheme.typography.titleMedium,
    )
    outcome.candidates.forEach { candidate ->
        CandidateCard(candidate)
    }
}

@Composable
private fun CandidateCard(candidate: RecognitionCandidate) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = candidate.medicationName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}

@Composable
private fun MessageResult(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun ErrorContent(onReset: () -> Unit) {
    MessageResult(stringResource(R.string.recognition_error))
    OutlinedButton(
        onClick = onReset,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.recognition_restart))
    }
}
