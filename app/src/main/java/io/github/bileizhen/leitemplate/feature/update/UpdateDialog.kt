package io.github.bileizhen.leitemplate.feature.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.bileizhen.leitemplate.core.update.UpdateService
import io.github.bileizhen.leitemplate.core.update.UpdateState
import io.github.bileizhen.leitemplate.ui.component.TemplateDialog
import io.github.bileizhen.leitemplate.ui.util.openExternalLink
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton

@Composable
fun UpdateDialog(service: UpdateService) {
    val visible by service.dialogVisible.collectAsStateWithLifecycle()
    val state by service.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (!visible) return
    UpdateDialogContent(state, service::dismissDialog,
        onRetry = { scope.launch { service.check(manual = true) } },
        onIgnore = { scope.launch { service.ignoreCurrentRelease() } },
        onOpenRelease = { url -> openExternalLink(context, url) })
}

/** Presentation can be reused and tested independently of networking. */
@Composable
fun UpdateDialogContent(state: UpdateState, onDismiss: () -> Unit, onRetry: () -> Unit,
                        onIgnore: () -> Unit, onOpenRelease: (String) -> Unit) {
    TemplateDialog("应用更新", onDismiss) {
        when (state) {
            UpdateState.Idle, UpdateState.Checking -> Text("正在检查 GitHub Releases…")
            UpdateState.UpToDate -> Text("当前已是最新版本")
            is UpdateState.Failed -> {
                Text("检查失败")
                Text(state.reason)
                TextButton("重试", onClick = onRetry, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
            }
            is UpdateState.Available -> {
                val release = state.release
                Text("发现新版本 ${release.version}" + if (release.prerelease) "（预发布版）" else "（正式版）")
                Text(release.assetName?.let { "APK：$it" } ?: "此发布未附带 APK，请查看发布说明。")
                Box(Modifier.fillMaxWidth().heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                    SelectionContainer { Text(release.notes.ifBlank { "发布者未提供更新说明。" }) }
                }
                TextButton("查看发布 / 下载", onClick = { onOpenRelease(release.pageUrl) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
                TextButton("忽略此版本", onClick = onIgnore,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
            }
        }
        TextButton(if (state is UpdateState.Available) "稍后" else "关闭", onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
    }
}
