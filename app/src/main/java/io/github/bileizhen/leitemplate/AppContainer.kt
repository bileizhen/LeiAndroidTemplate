package io.github.bileizhen.leitemplate

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import io.github.bileizhen.leitemplate.core.config.AppMetadata
import io.github.bileizhen.leitemplate.core.logging.AppLogger
import io.github.bileizhen.leitemplate.core.update.GitHubUpdateChecker
import io.github.bileizhen.leitemplate.core.update.UpdateService
import io.github.bileizhen.leitemplate.data.settings.SettingsRepository
import io.github.bileizhen.leitemplate.data.update.UpdateSettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val settingsStore = PreferenceDataStoreFactory.create(
        scope = appScope,
        produceFile = { context.preferencesDataStoreFile("app.preferences_pb") },
    )

    val updateTransfer = io.github.bileizhen.leitemplate.core.update.UpdateTransfer(
        io.github.bileizhen.leitemplate.core.update.UpdateDownloader(okhttp3.OkHttpClient(), java.io.File(context.cacheDir, "updates")),
        io.github.bileizhen.leitemplate.core.update.AndroidUpdateInstaller(context.applicationContext), appScope)
    val logger = AppLogger(context)
    val settings = SettingsRepository(settingsStore, appScope)
    val updateSettings = UpdateSettingsRepository(settingsStore, appScope)
    val updates = UpdateService(
        checker = GitHubUpdateChecker(
            owner = AppMetadata.GITHUB_OWNER,
            repository = AppMetadata.GITHUB_REPO,
            installedVersion = BuildConfig.VERSION_NAME,
            userAgent = "${AppMetadata.APP_NAME}/${BuildConfig.VERSION_NAME}",
        ),
        settings = updateSettings,
        logger = logger,
    )
}
