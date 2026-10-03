package io.github.bileizhen.leitemplate.core.logging

object CrashLogger {
    fun install(logger: AppLogger) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            logger.error("Crash", "Uncaught exception on ${thread.name}", error)
            previous?.uncaughtException(thread, error)
        }
    }
}
