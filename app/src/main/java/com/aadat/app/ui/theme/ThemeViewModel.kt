package com.aadat.app.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aadat.app.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AppTheme { SYSTEM, LIGHT, DARK }

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val theme = preferencesRepository.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppTheme.SYSTEM)

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
        }
    }
}
