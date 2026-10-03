package io.github.bileizhen.leitemplate.feature.logs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bileizhen.leitemplate.core.logging.AppLogger
import io.github.bileizhen.leitemplate.core.logging.DiagnosticExporter
import io.github.bileizhen.leitemplate.core.logging.LogHistory
import io.github.bileizhen.leitemplate.core.logging.LogRecord
import io.github.bileizhen.leitemplate.ui.component.TemplateDialog
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField

@Composable
fun LogScreen(logger: AppLogger, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var records by remember { mutableStateOf<List<LogRecord>>(emptyList()) }
    var query by rememberSaveable { mutableStateOf("") }
    var level by rememberSaveable { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("正在读取日志…") }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var pendingExport by rememberSaveable { mutableStateOf<String?>(null) }
    val levels = listOf(null, "D", "I", "W", "E")

    suspend fun reload() {
        records = withContext(Dispatchers.IO) { LogHistory.parse(logger.read()) }
        status = "共 ${records.size} 条记录，最新记录在前"
    }
    suspend fun userAction(action: suspend () -> Unit) {
        busy = true
        try { action()
        } catch (cancelled: CancellationException) { throw cancelled
        } catch (error: Exception) {
            logger.warn("Diagnostics", "Diagnostic action failed", error)
            status = "操作失败，请重试：${error.localizedMessage.orEmpty()}"
        } finally { busy = false }
    }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val path = pendingExport
        pendingExport = null
        if (uri != null && path != null) scope.launch {
            userAction {
                withContext(Dispatchers.IO) {
                    val output = checkNotNull(context.contentResolver.openOutputStream(uri)) { "无法打开保存位置" }
                    output.use { target -> File(path).inputStream().use { it.copyTo(target) } }
                }
                status = "诊断文件已保存"
            }
        } else status = "已取消保存"
    }
    LaunchedEffect(Unit) { userAction { reload() } }
    val filtered = remember(records, query, level) { LogHistory.filter(records, levels[level], query) }

    LazyColumn(Modifier.fillMaxSize().testTag("log_list"), contentPadding = PaddingValues(16.dp, 24.dp, 16.dp, 48.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { BasicComponent(title = "‹ 返回", onClick = onBack) }
        item { Text("日志与诊断", fontSize = 30.sp) }
        item {
            TextField(value = query, onValueChange = { query = it }, label = "搜索日志", singleLine = true,
                modifier = Modifier.fillMaxWidth())
        }
        item { TabRow(tabs = listOf("全部", "调试", "信息", "警告", "错误"), selectedTabIndex = level,
            onTabSelected = { level = it }, height = 48.dp, minWidth = 48.dp, itemSpacing = 4.dp) }
        item {
            Card(Modifier.fillMaxWidth()) {
                BasicComponent(title = "刷新", enabled = !busy, onClick = { scope.launch { userAction { reload() } } })
                BasicComponent(title = "分享诊断信息", enabled = !busy,
                    summary = "以文件分享完整脱敏日志和设备版本信息", onClick = {
                        scope.launch { userAction {
                            val file = DiagnosticExporter.create(context, logger)
                            context.startActivity(Intent.createChooser(DiagnosticExporter.shareIntent(context, file), "分享诊断文件"))
                            status = "已打开分享面板，请检查文件内容后分享"
                        } }
                    })
                BasicComponent(title = "保存诊断文件", enabled = !busy && pendingExport == null, onClick = {
                    scope.launch { userAction {
                        val file = DiagnosticExporter.create(context, logger)
                        pendingExport = file.absolutePath
                        save.launch("diagnostics-${System.currentTimeMillis()}.txt")
                    } }
                })
                BasicComponent(title = "清空日志", enabled = !busy,
                    summary = "删除本机保存的运行日志", onClick = { confirmClear = true })
            }
        }
        item { Text(if (busy) "正在处理…" else status, fontSize = 13.sp) }
        item { Text("显示 ${filtered.size} 条；导出包含所有等级的日志，不受筛选影响。", fontSize = 13.sp) }
        if (filtered.isEmpty()) item {
            Text(if (records.isEmpty()) "暂无日志" else "没有符合筛选条件的日志", modifier = Modifier.padding(16.dp))
        }
        items(filtered) { record ->
            Card(Modifier.fillMaxWidth()) {
                SelectionContainer { Text(record.text, modifier = Modifier.padding(14.dp),
                    fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 17.sp) }
            }
        }
    }
    if (confirmClear) TemplateDialog("清空日志", { confirmClear = false }) {
        Text("将删除本机的当前日志和轮转日志。已保存或分享的诊断文件不会删除。")
        TextButton("取消", onClick = { confirmClear = false }, modifier = Modifier.fillMaxWidth())
        TextButton("确认清空", onClick = {
            confirmClear = false
            scope.launch { userAction {
                withContext(Dispatchers.IO) { logger.clear() }
                reload()
                status = "日志已清空"
            } }
        }, modifier = Modifier.fillMaxWidth())
    }
}
