package io.github.bileizhen.leitemplate.core.logging

import android.content.Context
import android.net.Uri
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

    suspend fun save(context: Context, file: File, destination: Uri) = withContext(Dispatchers.IO) {
        val output = checkNotNull(context.contentResolver.openOutputStream(destination, "wt")) {
            "无法打开保存位置"
        }
        output.use { target -> file.inputStream().use { it.copyTo(target) } }
    }
}
