package io.github.bileizhen.leitemplate.feature.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.bileizhen.leitemplate.core.update.UpdateChannel
import io.github.bileizhen.leitemplate.ui.component.SettingsSwitch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun SettingsScreen(
    updateViewModel: UpdateSettingsViewModel,
    onAppearance: () -> Unit,
    onLogs: () -> Unit,
) {
    val updateSettings by updateViewModel.settings.collectAsStateWithLifecycle()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 120.dp),
    ) {
        item { Text("设置", fontSize = 30.sp) }
        item {
            Card(Modifier.padding(top = 16.dp)) {
                BasicComponent(title = "外观", summary = "主题、Monet、模糊、液态玻璃、缩放", onClick = onAppearance)
            }
        }
        item {
            Card(Modifier.padding(top = 14.dp)) {
                SettingsSwitch(
                    title = "启动时自动检查更新",
                    summary = "打开应用后检查 GitHub Releases",
                    checked = updateSettings.autoCheckOnLaunch,
                    onCheckedChange = updateViewModel::setAutoCheck,
                )
                BasicComponent(
                    title = "更新通道",
                    summary = if (updateSettings.channel == UpdateChannel.STABLE) "正式版" else "正式版 + 预发布版",
                    onClick = {
                        updateViewModel.setChannel(
                            if (updateSettings.channel == UpdateChannel.STABLE) UpdateChannel.PRERELEASE else UpdateChannel.STABLE,
                        )
                    },
                )
            }
        }
        item {
            Card(Modifier.padding(top = 14.dp)) {
                BasicComponent(title = "日志与诊断", summary = "导出应用日志和诊断信息", onClick = onLogs)
            }
        }
    }
}
