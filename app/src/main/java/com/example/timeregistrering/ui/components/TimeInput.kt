package com.example.timeregistrering.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeInput(
    time: LocalTime,
    onTimeChanged: (LocalTime) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
        ) {
            Text(
                text = time.format(formatter),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (showDialog) {
            TimePickerDialog(
                onDismissRequest = { showDialog = false },
                onConfirm = { hour, minute ->
                    onTimeChanged(LocalTime.of(hour, minute))
                    showDialog = false
                },
                initialHour = time.hour,
                initialMinute = time.minute
            )
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Vælg tidspunkt") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer
                NumberPicker(
                    value = selectedHour,
                    onValueChange = { selectedHour = it },
                    range = 0..23,
                    format = { "%02d".format(it) }
                )
                
                Text(":")
                
                // Minutter
                NumberPicker(
                    value = selectedMinute,
                    onValueChange = { selectedMinute = it },
                    range = 0..59,
                    format = { "%02d".format(it) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedHour, selectedMinute) }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Annuller")
            }
        }
    )
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    format: (Int) -> String = { it.toString() }
) {
    Column {
        IconButton(
            onClick = { 
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Text("▲")
        }
        
        Text(
            text = format(value),
            style = MaterialTheme.typography.titleLarge
        )
        
        IconButton(
            onClick = { 
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Text("▼")
        }
    }
}
