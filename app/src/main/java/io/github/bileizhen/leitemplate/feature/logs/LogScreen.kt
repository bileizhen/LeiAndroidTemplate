package io.github.bileizhen.leitemplate.feature.logs

import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bileizhen.leitemplate.core.logging.AppLogger
import io.github.bileizhen.leitemplate.core.logging.DiagnosticReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun LogScreen(logger: AppLogger, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }

    suspend fun reload() {
        text = withContext(Dispatchers.IO) { logger.read() }
    }
    LaunchedEffect(Unit) { reload() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
    ) {
        item { BasicComponent(title = "‹ 返回", onClick = onBack) }
        item { Text("日志", fontSize = 30.sp, modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) }
        item {
            Card(Modifier.fillMaxWidth()) {
                BasicComponent(title = "刷新", summary = "重新读取本地日志", onClick = { scope.launch { reload() } })
                BasicComponent(
                    title = "分享诊断信息",
                    summary = "包含应用版本、设备信息和本地日志",
                    onClick = {
                        scope.launch {
                            val report = withContext(Dispatchers.IO) { DiagnosticReport.create(logger) }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Application diagnostics")
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(intent, "分享诊断信息"))
                        }
                    },
                )
                BasicComponent(
                    title = "清空日志",
                    summary = "删除本机保存的日志文件",
                    onClick = { scope.launch(Dispatchers.IO) { logger.clear(); withContext(Dispatchers.Main) { text = "" } } },
                )
            }
        }
        item {
            Card(Modifier.padding(top = 14.dp).fillMaxWidth()) {
                Text(
                    text.ifBlank { "暂无日志" },
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}
