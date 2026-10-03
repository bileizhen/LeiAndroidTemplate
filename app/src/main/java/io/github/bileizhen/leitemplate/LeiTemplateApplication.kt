package io.github.bileizhen.leitemplate

import android.app.Application
import io.github.bileizhen.leitemplate.core.logging.CrashLogger

class LeiTemplateApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        CrashLogger.install(container.logger)
        container.logger.info("Application", "Application started")
    }
}
