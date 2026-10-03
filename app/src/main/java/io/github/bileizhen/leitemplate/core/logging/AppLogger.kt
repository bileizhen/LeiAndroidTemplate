package io.github.bileizhen.leitemplate.core.logging

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppLogger(context: Context) {
    private val directory = File(context.filesDir, "logs").apply { mkdirs() }
    private val file = File(directory, "app.log")
    private val backup = File(directory, "app.log.1")
    private val lock = Any()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun debug(tag: String, message: String) = write("D", tag, message, null)
    fun info(tag: String, message: String) = write("I", tag, message, null)
    fun warn(tag: String, message: String, error: Throwable? = null) = write("W", tag, message, error)
    fun error(tag: String, message: String, error: Throwable? = null) = write("E", tag, message, error)

    fun read(): String = synchronized(lock) {
        runCatching {
            buildString {
                if (backup.isFile) append(backup.readText(Charsets.UTF_8)).append('\n')
                if (file.isFile) append(file.readText(Charsets.UTF_8))
            }.trim()
        }.getOrElse { "Unable to read logs: ${it.message}" }
    }

    fun clear() = synchronized(lock) {
        file.delete()
        backup.delete()
    }

    private fun write(level: String, tag: String, message: String, error: Throwable?) {
        when (level) {
            "E" -> Log.e(tag, message, error)
            "W" -> Log.w(tag, message, error)
            "D" -> Log.d(tag, message, error)
            else -> Log.i(tag, message, error)
        }
        synchronized(lock) {
            val line = buildString {
                append(formatter.format(Date()))
                append(' ').append(level).append('/').append(tag).append(": ").append(message)
                if (error != null) append('\n').append(Log.getStackTraceString(error))
                append('\n')
            }
            rotateIfNeeded(line.length)
            runCatching { file.appendText(line, Charsets.UTF_8) }
        }
    }

    private fun rotateIfNeeded(incomingChars: Int) {
        val incomingBytes = incomingChars * 3L
        if (file.isFile && file.length() + incomingBytes > MAX_LOG_BYTES) {
            backup.delete()
            file.renameTo(backup)
        }
    }

    companion object {
        private const val MAX_LOG_BYTES = 768L * 1024L
    }
}
