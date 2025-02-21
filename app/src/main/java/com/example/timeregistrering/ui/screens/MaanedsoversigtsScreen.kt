package com.example.timeregistrering.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.timeregistrering.viewmodel.TimeRegistrationViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaanedsoversigtsScreen(
    navController: NavController,
    viewModel: TimeRegistrationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedMonth by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Månedsoversigt") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbage"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Måned vælger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        selectedMonth = selectedMonth.minusMonths(1)
                    }
                ) {
                    Text("Forrige måned")
                }

                Text(
                    text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("da", "DK"))),
                    style = MaterialTheme.typography.titleMedium
                )

                TextButton(
                    onClick = {
                        selectedMonth = selectedMonth.plusMonths(1)
                    }
                ) {
                    Text("Næste måned")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Månedsoversigt
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val startOfMonth = selectedMonth.withDayOfMonth(1)
                val endOfMonth = selectedMonth.with(TemporalAdjusters.lastDayOfMonth())
                
                val moederIMaaned = state.moeder.filter { moede ->
                    val moedeDate = moede.startTid.toLocalDate()
                    !moedeDate.isBefore(startOfMonth) && !moedeDate.isAfter(endOfMonth)
                }.sortedBy { it.startTid }

                items(moederIMaaned) { moede ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = moede.titel,
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = formatDateTime(moede.startTid),
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

                // Vis besked hvis der ingen møder er
                if (moederIMaaned.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Text(
                                    text = "Ingen møder i denne måned",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateTime(dateTime: java.time.LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy HH:mm", Locale("da", "DK"))
    return dateTime.format(formatter)
}
