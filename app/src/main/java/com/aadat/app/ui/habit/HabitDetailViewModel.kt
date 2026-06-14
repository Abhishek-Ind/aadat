package com.aadat.app.ui.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aadat.app.data.repository.HabitRepository
import com.aadat.app.domain.model.Habit
import com.aadat.app.domain.model.HabitCompletion
import com.aadat.app.domain.model.StreakResult
import com.aadat.app.domain.model.GardenState
import com.aadat.app.domain.usecase.GardenStateCalculator
import com.aadat.app.domain.usecase.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitDetailUiState(
    val habit: Habit? = null,
    val completions: List<HabitCompletion> = emptyList(),
    val streakResult: StreakResult? = null,
    val gardenState: GardenState? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HabitDetailViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val streakCalculator: StreakCalculator,
    private val gardenStateCalculator: GardenStateCalculator
) : ViewModel() {

    private val _habitId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadHabit(habitId: String) {
        if (_habitId.value == habitId) return
        _habitId.value = habitId

        viewModelScope.launch {
            combine(
                habitRepository.getHabitById(habitId).filterNotNull(),
                habitRepository.getCompletionsForHabit(habitId)
            ) { habit, completions ->
                val dates = completions.map { LocalDate.parse(it.completedOn) }
                val streak = streakCalculator.calculate(habit, dates)
                val garden = gardenStateCalculator.calculate(completions.size, streak)
                HabitDetailUiState(
                    habit = habit,
                    completions = completions,
                    streakResult = streak,
                    gardenState = garden,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleCompletion(date: LocalDate) {
        val habitId = _habitId.value ?: return
        viewModelScope.launch {
            habitRepository.toggleCompletion(habitId, date)
        }
    }
}
