package com.example.timeregistrering.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timeregistrering.database.Moede
import com.example.timeregistrering.viewmodel.TimeRegistrationViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoederScreen(
    navController: NavController,
    viewModel: TimeRegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedMoede by remember { mutableStateOf<Moede?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Møder") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbage"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selectedMoede = null
                        showDialog = true
                        title = ""
                        description = ""
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Tilføj møde"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(state.moeder) { moede ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = moede.titel,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row {
                                    IconButton(onClick = {
                                        selectedMoede = moede
                                        showDialog = true
                                        title = moede.titel
                                        description = moede.beskrivelse
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Rediger møde"
                                        )
                                    }
                                    IconButton(onClick = {
                                        viewModel.sletMoede(moede)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "Slet møde"
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fra: ${moede.startTid.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Til: ${moede.slutTid.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            if (moede.beskrivelse.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = moede.beskrivelse,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(text = if (selectedMoede == null) "Tilføj møde" else "Rediger møde") },
                    text = {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("Titel") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("Beskrivelse") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { showDialog = false }
                                ) {
                                    Text("Annuller")
                                }
                                
                                Button(
                                    onClick = {
                                        if (selectedMoede != null) {
                                            viewModel.updateMeeting(selectedMoede!!, title, description)
                                        } else {
                                            viewModel.createMeeting(title, description)
                                        }
                                        showDialog = false
                                    }
                                ) {
                                    Text("Gem")
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }
        }
    }
}
