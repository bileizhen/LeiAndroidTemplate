package io.github.bileizhen.leitemplate.core.logging

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DiagnosticExporter {
    suspend fun create(context: Context, logger: AppLogger): File = withContext(Dispatchers.IO) {
        val directory = File(context.cacheDir, "diagnostics").apply { check(mkdirs() || isDirectory) }
        val now = System.currentTimeMillis()
        directory.listFiles()?.filter { it.isFile }
            ?.sortedByDescending { it.lastModified() }?.forEachIndexed { index, file ->
                if (index >= 4 || now - file.lastModified() > 86_400_000L) file.delete()
            }
        File.createTempFile("diagnostics-", ".txt", directory).apply {
            writeText(DiagnosticReport.create(logger), Charsets.UTF_8)
        }
    }

    fun shareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.diagnostics", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Application diagnostics")
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("diagnostics", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
