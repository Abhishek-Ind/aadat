package com.aadat.app.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aadat.app.data.repository.HabitRepository
import com.aadat.app.domain.model.FrequencyType
import com.aadat.app.domain.model.Habit
import com.aadat.app.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddEditHabitUiState(
    val name: String = "",
    val colorHex: String = "#E8A020",
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val timesPerPeriod: Int = 1,
    val everyNDays: Int = 3,
    val notificationEnabled: Boolean = false,
    val notificationHour: Int? = null,
    val notificationMinute: Int? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditHabitViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditHabitUiState())
    val uiState = _uiState.asStateFlow()

    fun loadHabit(habit: Habit) {
        _uiState.update {
            it.copy(
                name = habit.name,
                colorHex = habit.colorHex,
                frequencyType = habit.frequencyType,
                timesPerPeriod = habit.timesPerPeriod,
                everyNDays = habit.everyNDays,
                notificationEnabled = habit.notificationTimeHour != null,
                notificationHour = habit.notificationTimeHour,
                notificationMinute = habit.notificationTimeMinute
            )
        }
    }

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateColor(colorHex: String) = _uiState.update { it.copy(colorHex = colorHex) }
    fun updateFrequency(type: FrequencyType) = _uiState.update { it.copy(frequencyType = type) }
    fun updateTimesPerPeriod(times: Int) = _uiState.update { it.copy(timesPerPeriod = times) }
    fun updateEveryNDays(days: Int) = _uiState.update { it.copy(everyNDays = days) }
    fun updateNotificationEnabled(enabled: Boolean) = _uiState.update {
        it.copy(notificationEnabled = enabled, notificationHour = if (!enabled) null else it.notificationHour, notificationMinute = if (!enabled) null else it.notificationMinute)
    }
    fun updateNotificationTime(hour: Int, minute: Int) = _uiState.update {
        it.copy(notificationHour = hour, notificationMinute = minute)
    }

    fun saveHabit(existingHabit: Habit? = null) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a habit name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val habit = Habit(
                id = existingHabit?.id ?: UUID.randomUUID().toString(),
                name = state.name.trim(),
                colorHex = state.colorHex,
                frequencyType = state.frequencyType,
                timesPerPeriod = state.timesPerPeriod,
                everyNDays = state.everyNDays,
                notificationTimeHour = if (state.notificationEnabled) state.notificationHour else null,
                notificationTimeMinute = if (state.notificationEnabled) state.notificationMinute else null,
                createdAt = existingHabit?.createdAt ?: System.currentTimeMillis(),
                isArchived = false
            )

            if (existingHabit != null) {
                habitRepository.updateHabit(habit)
                notificationScheduler.cancelNotification(habit.id)
            } else {
                habitRepository.insertHabit(habit)
            }

            if (state.notificationEnabled && state.notificationHour != null) {
                notificationScheduler.scheduleNotification(habit)
            }

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
