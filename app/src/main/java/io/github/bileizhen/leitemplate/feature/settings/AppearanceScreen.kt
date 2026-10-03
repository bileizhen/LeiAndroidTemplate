package io.github.bileizhen.leitemplate.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.bileizhen.leitemplate.data.settings.AppearanceSettings
import io.github.bileizhen.leitemplate.data.settings.ThemeMode
import io.github.bileizhen.leitemplate.ui.component.SettingsSwitch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun AppearanceScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val config by viewModel.settings.collectAsStateWithLifecycle()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
    ) {
        item {
            BasicComponent(title = "‹ 返回", onClick = onBack)
            Text("外观", modifier = Modifier.padding(top = 12.dp))
            TabRow(
                tabs = listOf("跟随系统", "浅色", "深色"),
                selectedTabIndex = config.themeMode.ordinal,
                onTabSelected = { index -> viewModel.edit { it.copy(themeMode = ThemeMode.entries[index]) } },
                height = 48.dp,
            )
            Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                SettingsSwitch("Monet 动态颜色", config.monet, { value -> viewModel.edit { it.copy(monet = value) } }, enabled = Build.VERSION.SDK_INT >= 31)
                SettingsSwitch("模糊", config.blur, { value -> viewModel.edit { it.copy(blur = value) } }, enabled = Build.VERSION.SDK_INT >= 33)
                SettingsSwitch("悬浮底栏", config.floatingBar, { value -> viewModel.edit { it.copy(floatingBar = value) } })
                SettingsSwitch("液态玻璃", config.liquidGlass, { value -> viewModel.edit { it.copy(liquidGlass = value) } }, enabled = config.blur && config.floatingBar && Build.VERSION.SDK_INT >= 33)
                SettingsSwitch("预测性返回手势", config.predictiveBack, { value -> viewModel.edit { it.copy(predictiveBack = value) } }, enabled = Build.VERSION.SDK_INT >= 34)
            }
            var scale by remember(config.uiScale) { mutableFloatStateOf(config.uiScale) }
            Card(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                BasicComponent(
                    title = "显示缩放",
                    summary = "${(scale * 100).toInt()}%",
                    bottomAction = {
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            onValueChangeFinished = { viewModel.edit { it.copy(uiScale = scale) } },
                            valueRange = AppearanceSettings.MIN_SCALE..AppearanceSettings.MAX_SCALE,
                        )
                    },
                )
            }
        }
    }
}
