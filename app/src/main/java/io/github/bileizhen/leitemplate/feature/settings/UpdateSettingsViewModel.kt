package io.github.bileizhen.leitemplate.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.bileizhen.leitemplate.core.update.UpdateChannel
import io.github.bileizhen.leitemplate.data.update.UpdateSettingsRepository
import kotlinx.coroutines.launch

class UpdateSettingsViewModel(private val repository: UpdateSettingsRepository) : ViewModel() {
    val settings = repository.state
    fun setAutoCheck(value: Boolean) = viewModelScope.launch { repository.setAutoCheck(value) }
    fun setChannel(value: UpdateChannel) = viewModelScope.launch { repository.setChannel(value) }
}
