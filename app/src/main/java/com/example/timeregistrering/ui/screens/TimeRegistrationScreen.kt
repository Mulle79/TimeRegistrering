package com.example.timeregistrering.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timeregistrering.R
import com.example.timeregistrering.model.Project
import com.example.timeregistrering.model.UiState
import com.example.timeregistrering.ui.components.ErrorView
import com.example.timeregistrering.ui.components.LoadingView
import com.example.timeregistrering.ui.components.ProjectSelector
import com.example.timeregistrering.ui.components.TimeInput
import com.example.timeregistrering.viewmodel.TimeRegistrationViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeRegistrationScreen(
    onNavigateToMap: () -> Unit,
    viewModel: TimeRegistrationViewModel = hiltViewModel()
) {
    val registrationState by viewModel.registrationState.collectAsState()
    val timeRegistrations by viewModel.timeRegistrations.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val selectedProject by viewModel.selectedProject.collectAsState()
    var showManualTimeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar med titel og knapper
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tidsregistrering",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row {
                IconButton(onClick = { showManualTimeDialog = true }) {
                    Icon(Icons.Default.Edit, "Manuel indtastning")
                }
                IconButton(onClick = onNavigateToMap) {
                    Icon(Icons.Default.Place, "Vis kort")
                }
            }
        }

        // Projekt vælger
        ProjectSelector(
            selectedProject = selectedProject,
            projects = projects,
            onProjectSelected = viewModel::selectProject,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = registrationState) {
            is UiState.Loading -> {
                LoadingView()
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = viewModel::checkCurrentRegistration
                )
            }
            is UiState.Success -> {
                when (val registration = state.data) {
                    is TimeRegistrationViewModel.TimeRegistrationState.Active -> {
                        // Vis aktiv registrering
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Aktiv registrering",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Projekt: ${selectedProject?.name ?: "Intet projekt valgt"}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Startet: ${
                                        registration.registration.startTime.format(
                                            DateTimeFormatter.ofPattern("HH:mm")
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedTextField(
                                    value = registration.registration.description,
                                    onValueChange = viewModel::updateDescription,
                                    label = { Text("Beskrivelse") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                                Button(
                                    onClick = { viewModel.stopTimeRegistration() },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Stop registrering")
                                }
                            }
                        }
                    }
                    is TimeRegistrationViewModel.TimeRegistrationState.Inactive -> {
                        // Vis start registrering knap
                        Button(
                            onClick = { 
                                if (selectedProject != null) {
                                    viewModel.startTimeRegistration(
                                        projectId = selectedProject.id,
                                        description = ""
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            enabled = selectedProject != null
                        ) {
                            Icon(Icons.Default.PlayArrow, "Start")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start registrering")
                        }
                    }
                }
            }
        }

        // Liste af tidligere registreringer
        when (val state = timeRegistrations) {
            is UiState.Loading -> {
                LoadingView()
            }
            is UiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = viewModel::loadTimeRegistrations
                )
            }
            is UiState.Success -> {
                Text(
                    text = "Tidligere registreringer",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                state.data.forEach { registration ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Projekt: ${registration.project.name}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Start: ${
                                    registration.startTime.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    )
                                }",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            registration.endTime?.let { endTime ->
                                Text(
                                    text = "Slut: ${
                                        endTime.format(
                                            DateTimeFormatter.ofPattern("HH:mm")
                                        )
                                    }",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (registration.description.isNotEmpty()) {
                                Text(
                                    text = registration.description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Manuel tid dialog
    if (showManualTimeDialog) {
        AlertDialog(
            onDismissRequest = { showManualTimeDialog = false },
            title = { Text("Manuel tidsregistrering") },
            text = {
                Column {
                    ProjectSelector(
                        selectedProject = selectedProject,
                        projects = projects,
                        onProjectSelected = viewModel::selectProject,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    var startTime by remember { mutableStateOf(LocalTime.now()) }
                    var endTime by remember { mutableStateOf(LocalTime.now()) }
                    var description by remember { mutableStateOf("") }

                    TimeInput(
                        time = startTime,
                        onTimeChanged = { startTime = it },
                        label = "Start tidspunkt",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    TimeInput(
                        time = endTime,
                        onTimeChanged = { endTime = it },
                        label = "Slut tidspunkt",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Beskrivelse") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedProject != null) {
                            viewModel.createManualTimeRegistration(
                                projectId = selectedProject.id,
                                startTime = startTime,
                                endTime = endTime,
                                description = description
                            )
                        }
                        showManualTimeDialog = false
                    },
                    enabled = selectedProject != null
                ) {
                    Text("Gem")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualTimeDialog = false }) {
                    Text("Annuller")
                }
            }
        )
    }
}
