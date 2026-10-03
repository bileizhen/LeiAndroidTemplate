package io.github.bileizhen.leitemplate.feature.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.bileizhen.leitemplate.BuildConfig
import io.github.bileizhen.leitemplate.core.config.AppMetadata
import io.github.bileizhen.leitemplate.core.update.AppRelease
import io.github.bileizhen.leitemplate.core.update.UpdateChannel
import io.github.bileizhen.leitemplate.feature.update.UpdateDialogContent
import io.github.bileizhen.leitemplate.core.update.UpdateService
import io.github.bileizhen.leitemplate.core.update.UpdateState
import io.github.bileizhen.leitemplate.ui.util.openExternalLink
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun AboutScreen(updateService: UpdateService, onOpenLogs: () -> Unit, onOpenDocument: (LegalDocument) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateState by updateService.state.collectAsStateWithLifecycle()
    val updateSettings by updateService.settings.state.collectAsStateWithLifecycle()
    var previewVisible by rememberSaveable { mutableStateOf(false) }

    fun openUrl(url: String) {
        openExternalLink(context, url)
    }

    val updateSummary = when (val state = updateState) {
        UpdateState.Idle -> "通过 GitHub Releases 获取新版本"
        UpdateState.Checking -> "正在检查…"
        UpdateState.UpToDate -> "当前已是最新版本"
        is UpdateState.Available -> "发现 ${state.release.version}，点击查看更新说明"
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
                    BasicComponent(title = "复制版本信息", onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("version", "${AppMetadata.APP_NAME} ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n${BuildConfig.APPLICATION_ID}"))
                        Toast.makeText(context, "版本信息已复制", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
        item {
            Card(Modifier.padding(top = 14.dp).fillMaxWidth()) {
                BasicComponent(
                    title = "检查更新",
                    summary = if (BuildConfig.UPDATE_DIALOG_PREVIEW) "点击预览更新弹窗（调试版）" else updateSummary,
                    onClick = {
                        if (BuildConfig.UPDATE_DIALOG_PREVIEW) previewVisible = true
                        else scope.launch { updateService.present() }
                    },
                )
                BasicComponent(title = "日志与诊断", summary = "导出应用日志和诊断信息", onClick = onOpenLogs)
                BasicComponent(title = "GitHub", summary = AppMetadata.PROJECT_URL, onClick = { openUrl(AppMetadata.PROJECT_URL) })
                BasicComponent(title = "问题反馈", summary = AppMetadata.ISSUES_URL, onClick = { openUrl(AppMetadata.ISSUES_URL) })
            }
        }
        item {
            Card(Modifier.padding(top = 14.dp).fillMaxWidth()) {
                BasicComponent(title = "开发者", summary = AppMetadata.AUTHOR, onClick = { openUrl(AppMetadata.AUTHOR_URL) })
                if (AppMetadata.WEBSITE_URL.isNotBlank()) BasicComponent(title = "网站", summary = AppMetadata.WEBSITE_URL, onClick = { openUrl(AppMetadata.WEBSITE_URL) })
                BasicComponent(title = "开源许可", summary = AppMetadata.LICENSE, onClick = { onOpenDocument(LegalDocument.LICENSE) })
                BasicComponent(title = "第三方声明", summary = "组件来源、版权与许可证", onClick = { onOpenDocument(LegalDocument.NOTICES) })
                BasicComponent(title = "Apache License 2.0", summary = "第三方组件许可全文", onClick = { onOpenDocument(LegalDocument.APACHE) })
                BasicComponent(title = "隐私说明", summary = "本地数据、检查更新与诊断导出", onClick = { onOpenDocument(LegalDocument.PRIVACY) })
                if (AppMetadata.PRIVACY_URL.isNotBlank()) BasicComponent(title = "在线隐私政策", onClick = { openUrl(AppMetadata.PRIVACY_URL) })
            }
        }
    }
    if (BuildConfig.UPDATE_DIALOG_PREVIEW && previewVisible) {
        val prerelease = updateSettings.channel == UpdateChannel.PRERELEASE
        val release = AppRelease(
            version = if (prerelease) "0.2.0-rc.1" else "0.2.0",
            notes = "测试预览更新说明\n\n• Liquid Glass 浮动导航支持折射、高光与模糊切换。\n• 通用更新弹窗支持正式版 / 预发布版通道、更新说明和忽略版本。\n• 日志可导出为脱敏诊断文件。\n• 关于、开源许可、第三方声明和隐私说明支持离线查看。\n\n这里只用于查看弹窗效果。忽略此版本和稍后均关闭预览，再次点击检查更新即可重新打开。",
            pageUrl = "${AppMetadata.PROJECT_URL}/releases",
            apkUrl = null,
            assetName = "示例 APK（仅用于预览）",
            prerelease = prerelease,
        )
        UpdateDialogContent(UpdateState.Available(release),
            onDismiss = { previewVisible = false }, onRetry = {},
            onIgnore = { previewVisible = false }, onOpenRelease = ::openUrl, preview = true)
    }
}
