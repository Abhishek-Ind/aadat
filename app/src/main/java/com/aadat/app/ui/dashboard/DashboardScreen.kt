package com.aadat.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aadat.app.domain.model.Habit
import com.aadat.app.ui.habit.AddEditHabitSheet
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    onNavigateToHabitDetail: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<HabitWithState?>(null) }
    var showContextMenu by remember { mutableStateOf<HabitWithState?>(null) }

    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())
    val completedCount = uiState.habits.count { it.isCompletedToday }
    val totalCount = uiState.habits.size

    if (showAddSheet) {
        AddEditHabitSheet(
            habitToEdit = null,
            onDismiss = { showAddSheet = false },
            onSaved = { showAddSheet = false }
        )
    }

    habitToEdit?.let { habit ->
        AddEditHabitSheet(
            habitToEdit = habit,
            onDismiss = { habitToEdit = null },
            onSaved = { habitToEdit = null }
        )
    }

    habitToDelete?.let { habitState ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete habit?") },
            text = { Text("\"${habitState.habit.name}\" and all its history will be permanently deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHabit(habitState.habit.id)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) { Text("Cancel") }
            }
        )
    }

    showContextMenu?.let { habitState ->
        ModalBottomSheet(onDismissRequest = { showContextMenu = null }) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("Edit") },
                    leadingContent = { Icon(Icons.Filled.Edit, contentDescription = null) },
                    modifier = Modifier.clickable {
                        habitToEdit = habitState.habit
                        showContextMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        habitToDelete = habitState
                        showContextMenu = null
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "aadat",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.signOut()
                            onSignOut()
                        }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Sign out")
                    }
                }
            }

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = today.format(dateFormatter),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (totalCount > 0) {
                        Text(
                            text = "$completedCount of $totalCount on track",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { showAddSheet = true },
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New habit")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌱", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No habits yet", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start building your first habit",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showAddSheet = true }, shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add your first habit")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.habits, key = { it.habit.id }) { habitState ->
                        HabitCard(
                            habitState = habitState,
                            onToggle = { viewModel.toggleCompletion(habitState.habit.id) },
                            onTap = { onNavigateToHabitDetail(habitState.habit.id) },
                            onLongPress = { showContextMenu = habitState }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HabitCard(
    habitState: HabitWithState,
    onToggle: () -> Unit,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val habit = habitState.habit
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (habitState.isCompletedToday) habitColor else Color.Transparent)
                        .then(
                            if (!habitState.isCompletedToday) Modifier.background(habitColor.copy(alpha = 0.15f)) else Modifier
                        )
                        .clickable { onToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (habitState.isCompletedToday) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = if (habitState.isCompletedToday) "Completed" else "Not completed",
                        tint = if (habitState.isCompletedToday) Color.White else habitColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val streakText = if (habitState.streakResult.currentStreak == 0) {
                "Start your streak today!"
            } else {
                "🔥 ${habitState.streakResult.currentStreak} streak"
            }

            val freqLabel = when (habit.frequencyType) {
                com.aadat.app.domain.model.FrequencyType.DAILY -> "Every day"
                com.aadat.app.domain.model.FrequencyType.WEEKLY -> "${habit.timesPerPeriod}x/week"
                com.aadat.app.domain.model.FrequencyType.MONTHLY -> "${habit.timesPerPeriod}x/month"
                com.aadat.app.domain.model.FrequencyType.CUSTOM -> "${habit.timesPerPeriod}x/${habit.everyNDays}d"
            }

            Text(
                "$streakText · $freqLabel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Garden preview strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(habitColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(habitState.gardenState.displayEmoji, fontSize = 24.sp)
                    Column(horizontalAlignment = Alignment.End) {
                        val flowerText = if (habitState.gardenState.flowerCount == 0) {
                            "No flowers yet"
                        } else {
                            "${habitState.gardenState.flowerCount} 🌸"
                        }
                        Text(flowerText, style = MaterialTheme.typography.labelMedium, color = habitColor)
                        if (habitState.gardenState.daysToNextFlower > 0) {
                            Text(
                                "${habitState.gardenState.daysToNextFlower} to next 🌸",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
