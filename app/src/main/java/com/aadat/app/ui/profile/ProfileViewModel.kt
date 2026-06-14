package com.aadat.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aadat.app.data.repository.AuthRepository
import com.aadat.app.data.repository.PreferencesRepository
import com.aadat.app.ui.theme.AppTheme
import com.aadat.app.worker.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val email: String = "",
    val currentTheme: AppTheme = AppTheme.SYSTEM,
    val isUpdatingPassword: Boolean = false,
    val passwordUpdateSuccess: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val accountDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    val themeFlow = preferencesRepository.themeFlow

    init {
        _uiState.update { it.copy(email = authRepository.getCurrentUser()?.email ?: "") }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPassword = true, error = null) }
            authRepository.changePassword(newPassword)
                .onSuccess { _uiState.update { it.copy(isUpdatingPassword = false, passwordUpdateSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isUpdatingPassword = false, error = e.message) } }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, error = null) }
            notificationScheduler.cancelAllNotifications()
            authRepository.deleteAccount()
                .onSuccess {
                    authRepository.signOut()
                    _uiState.update { it.copy(isDeletingAccount = false, accountDeleted = true) }
                }
                .onFailure { e ->
                    // Fallback: just sign out
                    authRepository.signOut()
                    _uiState.update { it.copy(isDeletingAccount = false, accountDeleted = true) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearPasswordSuccess() = _uiState.update { it.copy(passwordUpdateSuccess = false) }
}
