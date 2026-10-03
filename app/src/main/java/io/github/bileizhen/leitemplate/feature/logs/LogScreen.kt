package io.github.bileizhen.leitemplate.feature.logs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bileizhen.leitemplate.core.logging.AppLogger
import io.github.bileizhen.leitemplate.core.logging.DiagnosticExporter
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun LogScreen(logger: AppLogger, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var status by rememberSaveable { mutableStateOf("") }
    var pendingExport by rememberSaveable { mutableStateOf<String?>(null) }

    suspend fun exportAction(action: suspend () -> Unit) {
        busy = true
        try {
            action()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            logger.warn("Diagnostics", "Diagnostic export failed", error)
            status = "导出失败，请重试：${error.localizedMessage.orEmpty()}"
            pendingExport = null
        } finally {
            busy = false
        }
    }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val path = pendingExport
        pendingExport = null
        if (uri != null && path != null) scope.launch {
            exportAction {
                DiagnosticExporter.save(context, File(path), uri)
                status = "诊断日志已导出"
            }
        } else status = if (uri == null) "已取消导出" else "导出文件已失效，请重试"
    }

    LazyColumn(Modifier.fillMaxSize().testTag("log_list"),
        contentPadding = PaddingValues(16.dp, 24.dp, 16.dp, 48.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BasicComponent(title = "‹ 返回", onClick = onBack) }
        item { Text("日志与诊断", fontSize = 30.sp) }
        item {
            Text("导出包含应用版本、系统版本、设备型号和运行日志的诊断文件，便于排查问题。常见敏感信息已脱敏，发送文件前请检查内容。")
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                BasicComponent(title = "导出日志", summary = "选择位置保存为 TXT 文件",
                    enabled = !busy && pendingExport == null, onClick = {
                        scope.launch { exportAction {
                            val file = DiagnosticExporter.create(context, logger)
                            pendingExport = file.absolutePath
                            save.launch("diagnostics-${System.currentTimeMillis()}.txt")
                        } }
                    })
            }
        }
        item { Text(if (busy) "正在导出…" else status, fontSize = 13.sp) }
    }
}
