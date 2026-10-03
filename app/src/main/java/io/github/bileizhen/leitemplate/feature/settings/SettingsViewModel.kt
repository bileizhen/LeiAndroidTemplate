package io.github.bileizhen.leitemplate.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bileizhen.leitemplate.data.settings.AppearanceSettings
import io.github.bileizhen.leitemplate.data.settings.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val settings = repository.state
    fun edit(change: (AppearanceSettings) -> AppearanceSettings) {
        viewModelScope.launch { repository.edit(change) }
    }
}
