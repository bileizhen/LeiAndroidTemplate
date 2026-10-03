package io.github.bileizhen.leitemplate.feature.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.bileizhen.leitemplate.BuildConfig
import io.github.bileizhen.leitemplate.core.config.AppMetadata
import io.github.bileizhen.leitemplate.core.update.UpdateService
import io.github.bileizhen.leitemplate.core.update.UpdateState
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun AboutScreen(updateService: UpdateService, onOpenLogs: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateState by updateService.state.collectAsStateWithLifecycle()

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    val updateSummary = when (val state = updateState) {
        UpdateState.Idle -> "通过 GitHub Releases 获取新版本"
        UpdateState.Checking -> "正在检查…"
        UpdateState.UpToDate -> "当前已是最新版本"
        is UpdateState.Available -> "发现 ${state.release.version}，点击查看发布页"
        is UpdateState.Failed -> "检查失败：${state.reason}"
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 120.dp),
    ) {
        item { Text("关于", fontSize = 30.sp) }
        item {
            Card(Modifier.padding(top = 16.dp).fillMaxWidth()) {
                androidx.compose.foundation.layout.Column(Modifier.padding(20.dp)) {
                    Text(AppMetadata.APP_NAME, fontSize = 22.sp)
                    Text(AppMetadata.APP_DESCRIPTION, modifier = Modifier.padding(top = 6.dp))
                    Text("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        item {
            Card(Modifier.padding(top = 14.dp).fillMaxWidth()) {
                BasicComponent(
                    title = "检查更新",
                    summary = updateSummary,
                    onClick = {
                        val release = (updateState as? UpdateState.Available)?.release
                        if (release != null) openUrl(release.pageUrl) else scope.launch { updateService.check() }
                    },
                )
                BasicComponent(title = "日志与诊断", summary = "查看、清空或分享运行日志", onClick = onOpenLogs)
                BasicComponent(title = "GitHub", summary = AppMetadata.PROJECT_URL, onClick = { openUrl(AppMetadata.PROJECT_URL) })
                BasicComponent(title = "问题反馈", summary = AppMetadata.ISSUES_URL, onClick = { openUrl(AppMetadata.ISSUES_URL) })
            }
        }
        item {
            Card(Modifier.padding(top = 14.dp).fillMaxWidth()) {
                BasicComponent(title = "开发者", summary = AppMetadata.AUTHOR)
                BasicComponent(title = "开源许可", summary = AppMetadata.LICENSE)
            }
        }
    }
}
