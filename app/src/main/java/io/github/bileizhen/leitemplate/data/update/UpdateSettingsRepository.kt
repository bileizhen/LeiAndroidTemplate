package io.github.bileizhen.leitemplate.data.update

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.bileizhen.leitemplate.core.update.UpdateChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UpdateSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    scope: CoroutineScope,
) {
    private val autoCheck = booleanPreferencesKey("update_auto_check")
    private val channel = stringPreferencesKey("update_channel")

    val state: StateFlow<UpdateSettings> = dataStore.data.map { preferences ->
        UpdateSettings(
            autoCheckOnLaunch = preferences[autoCheck] ?: true,
            channel = runCatching { UpdateChannel.valueOf(preferences[channel] ?: UpdateChannel.STABLE.name) }
                .getOrDefault(UpdateChannel.STABLE),
        )
    }.stateIn(scope, SharingStarted.Eagerly, UpdateSettings(autoCheckOnLaunch = false))

    suspend fun setAutoCheck(value: Boolean) {
        dataStore.edit { it[autoCheck] = value }
    }

    suspend fun setChannel(value: UpdateChannel) {
        dataStore.edit { it[channel] = value.name }
    }
}
