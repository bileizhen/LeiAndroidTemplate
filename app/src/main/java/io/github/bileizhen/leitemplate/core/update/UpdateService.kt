package io.github.bileizhen.leitemplate.core.update

import io.github.bileizhen.leitemplate.core.logging.AppLogger
import io.github.bileizhen.leitemplate.data.update.UpdateSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class UpdateService(
    private val checker: GitHubUpdateChecker,
    val settings: UpdateSettingsRepository,
    private val logger: AppLogger,
) {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = mutableState

    suspend fun check() = mutex.withLock {
        mutableState.value = UpdateState.Checking
        val channel = settings.state.value.channel
        logger.info("Update", "Checking GitHub releases on $channel channel")
        mutableState.value = try {
            val release = withContext(Dispatchers.IO) { checker.check(channel) }
            if (release == null) {
                logger.info("Update", "Already up to date")
                UpdateState.UpToDate
            } else {
                logger.info("Update", "New release found: ${release.version}")
                UpdateState.Available(release)
            }
        } catch (error: CancellationException) {
            mutableState.value = UpdateState.Idle
            throw error
        } catch (error: Exception) {
            logger.warn("Update", "Update check failed", error)
            UpdateState.Failed(error.message ?: error::class.java.simpleName)
        }
    }
}
