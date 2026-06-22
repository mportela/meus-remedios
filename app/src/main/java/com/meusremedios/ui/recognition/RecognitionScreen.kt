package com.meusremedios.ui.recognition

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meusremedios.R
import com.meusremedios.domain.model.RecognitionCandidate
import com.meusremedios.domain.model.RecognitionOutcome

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RecognitionScreen(
    modifier: Modifier = Modifier,
    viewModel: RecognitionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success: Boolean ->
        if (success) viewModel.onCaptured()
    }
    val capture: () -> Unit = {
        viewModel.prepareCapture { uri -> cameraLauncher.launch(uri) }
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
                RecognitionPhase.IDLE -> IdleContent(onCapture = capture)
                RecognitionPhase.ANALYZING -> AnalyzingContent()
                RecognitionPhase.RESULT -> ResultContent(
                    outcome = uiState.outcome,
                    canAddSecondPhoto = uiState.canAddSecondPhoto,
                    onCapture = capture,
                    onReset = viewModel::reset,
                )
                RecognitionPhase.ERROR -> ErrorContent(onReset = viewModel::reset)
            }
        }
    }
}

@Composable
private fun IdleContent(onCapture: () -> Unit) {
    Text(
        text = stringResource(R.string.recognition_intro),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
    )
    BigConfirmButton(onClick = onCapture)
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
    onCapture: () -> Unit,
    onReset: () -> Unit,
) {
    when (outcome) {
        is RecognitionOutcome.Confident -> ConfidentResult(outcome)
        is RecognitionOutcome.Ambiguous -> AmbiguousResult(
            outcome = outcome,
            canAddSecondPhoto = canAddSecondPhoto,
            onCapture = onCapture,
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

@Composable
private fun ConfidentResult(outcome: RecognitionOutcome.Confident) {
    Icon(
        Icons.Default.CheckCircle,
        contentDescription = null,
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
}

@Composable
private fun AmbiguousResult(
    outcome: RecognitionOutcome.Ambiguous,
    canAddSecondPhoto: Boolean,
    onCapture: () -> Unit,
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
