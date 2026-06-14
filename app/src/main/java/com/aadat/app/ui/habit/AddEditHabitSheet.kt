package com.aadat.app.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aadat.app.domain.model.FrequencyType
import com.aadat.app.domain.model.Habit
import com.aadat.app.ui.theme.HabitColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitSheet(
    habitToEdit: Habit?,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddEditHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = uiState.notificationHour ?: 8,
        initialMinute = uiState.notificationMinute ?: 0
    )

    LaunchedEffect(habitToEdit) {
        habitToEdit?.let { viewModel.loadHabit(it) }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set reminder time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateNotificationTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                if (habitToEdit == null) "Add a habit" else "Edit habit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Name field
            val nameLength = uiState.name.length
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { if (it.length <= 60) viewModel.updateName(it) },
                label = { Text("What do you want to do?") },
                placeholder = { Text("e.g. Read for 20 minutes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                supportingText = if (nameLength >= 40) {
                    { Text("$nameLength/60") }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Frequency
            Text("How often?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            val frequencies = listOf(FrequencyType.DAILY, FrequencyType.WEEKLY, FrequencyType.MONTHLY, FrequencyType.CUSTOM)
            val freqLabels = listOf("Daily", "Weekly", "Monthly", "Custom")

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                frequencies.forEachIndexed { index, freq ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = frequencies.size),
                        onClick = { viewModel.updateFrequency(freq) },
                        selected = uiState.frequencyType == freq,
                        label = { Text(freqLabels[index], style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            if (uiState.frequencyType == FrequencyType.CUSTOM) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.timesPerPeriod.toString(),
                        onValueChange = { v -> v.toIntOrNull()?.let { viewModel.updateTimesPerPeriod(it.coerceIn(1, 99)) } },
                        label = { Text("Times") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = uiState.everyNDays.toString(),
                        onValueChange = { v -> v.toIntOrNull()?.let { viewModel.updateEveryNDays(it.coerceIn(1, 365)) } },
                        label = { Text("Every N days") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
                Text(
                    "Aim for ${uiState.timesPerPeriod} check-in(s) every ${uiState.everyNDays} days",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notification
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Set a reminder", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.notificationEnabled,
                    onCheckedChange = {
                        viewModel.updateNotificationEnabled(it)
                        if (it) showTimePicker = true
                    }
                )
            }

            if (uiState.notificationEnabled && uiState.notificationHour != null) {
                val timeStr = String.format(
                    "%d:%02d %s",
                    if (uiState.notificationHour!! > 12) uiState.notificationHour!! - 12 else uiState.notificationHour!!,
                    uiState.notificationMinute ?: 0,
                    if (uiState.notificationHour!! >= 12) "PM" else "AM"
                )
                TextButton(onClick = { showTimePicker = true }) {
                    Text("Reminder at $timeStr")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color picker
            Text("Pick a color", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HabitColors.forEach { color ->
                    val hexColor = "#%06X".format(0xFFFFFF and color.hashCode()).let {
                        "#%06X".format(0xFFFFFF and (color.red * 255).toInt().shl(16)
                            .or((color.green * 255).toInt().shl(8))
                            .or((color.blue * 255).toInt()))
                    }
                    val isSelected = try {
                        Color(android.graphics.Color.parseColor(uiState.colorHex)) == color
                    } catch (e: Exception) {
                        false
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(if (isSelected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
                            .clickable {
                                val hex = "#%02X%02X%02X".format(
                                    (color.red * 255).toInt(),
                                    (color.green * 255).toInt(),
                                    (color.blue * 255).toInt()
                                )
                                viewModel.updateColor(hex)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.saveHabit(habitToEdit) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = uiState.name.isNotBlank() && !uiState.isSaving,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (habitToEdit == null) "Add habit" else "Save changes",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
