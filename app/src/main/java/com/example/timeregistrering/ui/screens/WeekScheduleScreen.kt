package com.example.timeregistrering.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.timeregistrering.model.UiState
import com.example.timeregistrering.model.WeekDay
import com.example.timeregistrering.ui.components.ErrorView
import com.example.timeregistrering.ui.components.LoadingView
import com.example.timeregistrering.viewmodel.WeekScheduleViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun WeekScheduleScreen(
    viewModel: WeekScheduleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val weekSchedule by viewModel.weekSchedule.collectAsState()
    val totalHours by viewModel.totalHours.collectAsState()
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    // Handle process death
    DisposableEffect(Unit) {
        viewModel.restoreState()
        onDispose { viewModel.saveState() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar med uge navigation
        TopAppBar(
            title = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.currentWeekText,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    // Vis indikator hvis det er nuværende uge
                    if (viewModel.isCurrentWeek) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = stringResource(R.string.label_nu),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.semantics { 
                        contentDescription = stringResource(R.string.cd_gaa_tilbage)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.btn_tilbage)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.previousWeek() },
                    modifier = Modifier.semantics { 
                        contentDescription = stringResource(R.string.cd_forrige_uge)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.btn_forrige_uge)
                    )
                }
                IconButton(
                    onClick = { viewModel.nextWeek() },
                    modifier = Modifier.semantics { 
                        contentDescription = stringResource(R.string.cd_naeste_uge)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.btn_naeste_uge)
                    )
                }
            }
        )

        when (val state = weekSchedule) {
            is UiState.Loading -> LoadingView()
            is UiState.Error -> ErrorView(
                message = state.message,
                onRetry = viewModel::loadWeekSchedule
            )
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = state.data,
                        key = { it.date }
                    ) { day ->
                        WeekDayItem(
                            weekDay = day,
                            onStartTimeChanged = { time ->
                                try {
                                    viewModel.updateStartTime(day.date, time)
                                } catch (e: Exception) {
                                    showErrorDialog = e.message
                                }
                            },
                            onEndTimeChanged = { time ->
                                try {
                                    viewModel.updateEndTime(day.date, time)
                                } catch (e: Exception) {
                                    showErrorDialog = e.message
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Total timer denne uge
                AnimatedCard(
                    visible = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { 
                            contentDescription = stringResource(R.string.cd_total_timer)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.label_timer_ialt_uge),
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // Animeret timetæller
                        var oldHours by remember { mutableFloatStateOf(0f) }
                        val animatedHours by animateFloatAsState(
                            targetValue = totalHours.toFloat(),
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        
                        LaunchedEffect(totalHours) {
                            oldHours = animatedHours
                        }
                        
                        Text(
                            text = "%.1f".format(animatedHours),
                            style = MaterialTheme.typography.titleLarge,
                            color = when {
                                animatedHours > oldHours -> MaterialTheme.colorScheme.primary
                                animatedHours < oldHours -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    }

    // Error dialog
    AnimatedVisibility(
        visible = showErrorDialog != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        showErrorDialog?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = { showErrorDialog = null },
                title = { Text(stringResource(R.string.error_dialog_title)) },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = null }) {
                        Text(stringResource(R.string.btn_ok))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekDayItem(
    weekDay: WeekDay,
    onStartTimeChanged: (LocalTime) -> Unit,
    onEndTimeChanged: (LocalTime) -> Unit
) {
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    AnimatedCard(
        visible = true,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weekDay.name,
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (weekDay.isHoliday) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = stringResource(R.string.label_helligdag),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            if (!weekDay.isWorkDay) {
                Text(
                    text = if (weekDay.isHoliday) 
                        stringResource(R.string.label_helligdag)
                    else 
                        stringResource(R.string.label_weekend),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Start tid
                    TimePickerButton(
                        time = weekDay.startTime,
                        label = stringResource(R.string.btn_start),
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Slut tid
                    TimePickerButton(
                        time = weekDay.endTime,
                        label = stringResource(R.string.btn_slut),
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Vis total timer for dagen hvis både start og slut er sat
                if (weekDay.startTime != null && weekDay.endTime != null) {
                    Text(
                        text = stringResource(
                            R.string.label_timer_idag,
                            weekDay.totalHours ?: 0.0
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onTimeSelected = {
                onStartTimeChanged(it)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onTimeSelected = {
                onEndTimeChanged(it)
                showEndTimePicker = false
            }
        )
    }
}

@Composable
private fun TimePickerButton(
    time: LocalTime?,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (time != null) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 300)
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (time != null) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 300)
    )

    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 120.dp)
            .semantics { 
                contentDescription = stringResource(R.string.cd_vaelg_tid)
            },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        onHoverChanged = { isHovered = it }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_schedule),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            
            Text(
                text = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    var hour by remember { mutableStateOf(8) }
    var minute by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vælg tidspunkt") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Timer
                NumberPicker(
                    value = hour,
                    onValueChange = { hour = it },
                    range = 0..23
                )
                
                Text(":")
                
                // Minutter
                NumberPicker(
                    value = minute,
                    onValueChange = { minute = it },
                    range = 0..59
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime.of(hour, minute))
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuller")
            }
        }
    )
}

@Composable
private fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange
) {
    Column {
        IconButton(
            onClick = { 
                if (value < range.last) onValueChange(value + 1)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, "Forøg")
        }
        
        Text(
            text = "%02d".format(value),
            style = MaterialTheme.typography.titleLarge
        )
        
        IconButton(
            onClick = { 
                if (value > range.first) onValueChange(value - 1)
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, "Formindsk")
        }
    }
}

@Composable
private fun AnimatedCard(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(modifier = modifier, content = content)
    }
}
