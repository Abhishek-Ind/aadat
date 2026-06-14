package com.aadat.app.ui.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aadat.app.domain.model.FrequencyType
import com.aadat.app.domain.model.Habit
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    onBack: () -> Unit,
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditSheet by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) { viewModel.loadHabit(habitId) }

    if (showEditSheet && uiState.habit != null) {
        AddEditHabitSheet(
            habitToEdit = uiState.habit,
            onDismiss = { showEditSheet = false },
            onSaved = { showEditSheet = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Filled.Edit, "Edit")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val habit = uiState.habit ?: return@Scaffold
        val streakResult = uiState.streakResult ?: return@Scaffold
        val gardenState = uiState.gardenState ?: return@Scaffold
        val completedDates = uiState.completions.map { LocalDate.parse(it.completedOn) }.toHashSet()

        val habitColor = try {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(habitColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = habit.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = habitColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(habit.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        val freqLabel = when (habit.frequencyType) {
                            FrequencyType.DAILY -> "Every day"
                            FrequencyType.WEEKLY -> "${habit.timesPerPeriod}x per week"
                            FrequencyType.MONTHLY -> "${habit.timesPerPeriod}x per month"
                            FrequencyType.CUSTOM -> "${habit.timesPerPeriod}x every ${habit.everyNDays} days"
                        }
                        val createdDate = java.time.Instant.ofEpochMilli(habit.createdAt)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        Text(
                            "$freqLabel · since ${createdDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                // Stats row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        emoji = "🔥",
                        value = if (streakResult.currentStreak == 0) "0" else "${streakResult.currentStreak}",
                        label = if (streakResult.currentStreak == 1) "day in a row" else "days in a row",
                        color = habitColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "📈",
                        value = "${streakResult.longestStreak}",
                        label = "longest streak",
                        color = habitColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val todayPct = if (streakResult.allTimePossible > 0) {
                        (streakResult.allTimeCompletions * 100 / streakResult.allTimePossible)
                    } else 0
                    StatCard(
                        emoji = "🎯",
                        value = "$todayPct%",
                        label = "${streakResult.allTimeCompletions}/${streakResult.allTimePossible} done",
                        color = habitColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        emoji = "📅",
                        value = "${streakResult.allTimeCompletions}",
                        label = "total completions",
                        color = habitColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Garden section
                Text("Your garden", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = habitColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GardenComposable(
                            gardenState = gardenState,
                            habitColor = habitColor,
                            modifier = Modifier.size(180.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (gardenState.flowerCount == 0) "No flowers yet" else "${gardenState.flowerCount} 🌸 bloomed",
                                style = MaterialTheme.typography.labelMedium,
                                color = habitColor
                            )
                            if (gardenState.daysToNextFlower > 0) {
                                Text(
                                    "${gardenState.daysToNextFlower} completions to next 🌸",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Progress bars
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProgressCard("Week", streakResult.weekProgress, streakResult.weekTarget, habitColor, Modifier.weight(1f))
                    ProgressCard("Month", streakResult.monthProgress, streakResult.monthTarget, habitColor, Modifier.weight(1f))
                    ProgressCard("Year", streakResult.yearProgress, streakResult.yearTarget, habitColor, Modifier.weight(1f))
                }
            }

            item {
                // Calendar
                HabitCalendar(
                    completedDates = completedDates,
                    habitColor = habitColor,
                    onDateTapped = { date -> viewModel.toggleCompletion(date) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun ProgressCard(label: String, progress: Int, target: Int, color: Color, modifier: Modifier = Modifier) {
    val fraction = if (target > 0) progress.toFloat() / target else 0f
    val pct = (fraction * 100).toInt().coerceIn(0, 100)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$progress/$target", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text("$pct%", style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
private fun HabitCalendar(
    completedDates: Set<LocalDate>,
    habitColor: Color,
    onDateTapped: (LocalDate) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.Filled.ChevronLeft, "Previous month")
            }
            Text(
                "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = { if (currentMonth < YearMonth.now()) currentMonth = currentMonth.plusMonths(1) },
                enabled = currentMonth < YearMonth.now()
            ) {
                Icon(Icons.Filled.ChevronRight, "Next month")
            }
        }

        // Day of week header
        val dayNames = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val firstDay = currentMonth.atDay(1)
        val startOffset = firstDay.dayOfWeek.value % 7
        val daysInMonth = currentMonth.lengthOfMonth()

        val cells = (0 until startOffset).map { null } + (1..daysInMonth).map { currentMonth.atDay(it) }
        val rows = cells.chunked(7) { row -> row + List(7 - row.size) { null } }

        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val isCompleted = date in completedDates
                            val isToday = date == today
                            val isFuture = date.isAfter(today)

                            val bgColor = when {
                                isCompleted -> habitColor
                                else -> Color.Transparent
                            }
                            val borderModifier = when {
                                isToday && !isCompleted -> Modifier.border(2.dp, habitColor, RoundedCornerShape(8.dp))
                                else -> Modifier
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .then(borderModifier)
                                    .then(
                                        if (!isFuture) Modifier.clickable { onDateTapped(date) } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = when {
                                        isCompleted -> Color.White
                                        isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Tap any past or today's date to mark it done.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
