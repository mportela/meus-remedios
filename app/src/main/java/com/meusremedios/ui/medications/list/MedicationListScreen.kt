package com.meusremedios.ui.medications.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meusremedios.R
import com.meusremedios.domain.model.Medication

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onAddMedication: () -> Unit,
    onOpenMedication: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MedicationListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.queryState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.medications_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMedication,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.medications_add)) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(stringResource(R.string.medications_search_label)) },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearQuery) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = stringResource(
                                    R.string.medications_search_clear,
                                ),
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )

            when {
                uiState.isLoading -> Unit
                uiState.medications.isEmpty() -> EmptyState(isSearching = query.isNotEmpty())
                else -> MedicationList(
                    medications = uiState.medications,
                    onOpenMedication = onOpenMedication,
                )
            }
        }
    }
}

@Composable
private fun MedicationList(
    medications: List<Medication>,
    onOpenMedication: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp),
    ) {
        items(medications, key = { it.id }) { medication ->
            MedicationItem(medication = medication, onClick = { onOpenMedication(medication.id) })
        }
    }
}

@Composable
private fun MedicationItem(
    medication: Medication,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleLarge,
            )
            medication.dosage?.takeIf { it.isNotBlank() }?.let { dosage ->
                Text(
                    text = dosage,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            if (!medication.remindersEnabled) {
                Text(
                    text = stringResource(R.string.medications_reminders_off),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(isSearching: Boolean) {
    val message = if (isSearching) {
        stringResource(R.string.medications_empty_search)
    } else {
        stringResource(R.string.medications_empty)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}
