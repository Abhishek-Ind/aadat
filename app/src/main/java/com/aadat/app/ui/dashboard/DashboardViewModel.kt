package com.aadat.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aadat.app.data.repository.AuthRepository
import com.aadat.app.data.repository.HabitRepository
import com.aadat.app.domain.model.Habit
import com.aadat.app.domain.model.HabitCompletion
import com.aadat.app.domain.model.StreakResult
import com.aadat.app.domain.usecase.GardenStateCalculator
import com.aadat.app.domain.usecase.StreakCalculator
import com.aadat.app.domain.model.GardenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HabitWithState(
    val habit: Habit,
    val completions: List<HabitCompletion>,
    val streakResult: StreakResult,
    val gardenState: GardenState,
    val isCompletedToday: Boolean
)

data class DashboardUiState(
    val habits: List<HabitWithState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val authRepository: AuthRepository,
    private val streakCalculator: StreakCalculator,
    private val gardenStateCalculator: GardenStateCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadHabits() {
        viewModelScope.launch {
            habitRepository.getAllActiveHabits()
                .flatMapLatest { habits ->
                    if (habits.isEmpty()) {
                        flowOf(DashboardUiState(habits = emptyList(), isLoading = false))
                    } else {
                        val completionFlows = habits.map { habit ->
                            habitRepository.getCompletionsForHabit(habit.id)
                                .map { completions -> habit to completions }
                        }
                        combine(completionFlows) { pairs ->
                            val habitStates = pairs.map { (habit, completions) ->
                                val dates = completions.map { LocalDate.parse(it.completedOn) }
                                val streak = streakCalculator.calculate(habit, dates)
                                val garden = gardenStateCalculator.calculate(completions.size, streak)
                                HabitWithState(
                                    habit = habit,
                                    completions = completions,
                                    streakResult = streak,
                                    gardenState = garden,
                                    isCompletedToday = streak.isCompletedToday
                                )
                            }
                            DashboardUiState(habits = habitStates, isLoading = false)
                        }
                    }
                }
                .catch { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .collect { state -> _uiState.value = state }
        }
    }

    fun toggleCompletion(habitId: String) {
        viewModelScope.launch {
            habitRepository.toggleCompletion(habitId, LocalDate.now())
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            habitRepository.deleteHabit(habitId)
        }
    }

    suspend fun signOut(): Result<Unit> = authRepository.signOut()

    fun clearError() = _uiState.update { it.copy(error = null) }
}
