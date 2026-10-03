package io.github.bileizhen.leitemplate.ui

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.bileizhen.leitemplate.AppContainer
import io.github.bileizhen.leitemplate.feature.about.AboutScreen
import io.github.bileizhen.leitemplate.feature.home.HomeScreen
import io.github.bileizhen.leitemplate.feature.logs.LogScreen
import io.github.bileizhen.leitemplate.feature.settings.AppearanceScreen
import io.github.bileizhen.leitemplate.feature.settings.SettingsScreen
import io.github.bileizhen.leitemplate.feature.settings.SettingsViewModel
import io.github.bileizhen.leitemplate.feature.settings.UpdateSettingsViewModel
import io.github.bileizhen.leitemplate.ui.component.PlainFloatingBar
import io.github.bileizhen.leitemplate.ui.component.StandardNavigationBar
import io.github.bileizhen.leitemplate.ui.theme.LeiTheme
import io.github.bileizhen.leitemplate.ui.util.viewModelFactory
import top.yukonga.miuix.kmp.theme.MiuixTheme

private enum class DetailPage { APPEARANCE, LOGS }

@Composable
fun LeiTemplateApp(container: AppContainer) {
    val settings by container.settings.state.collectAsStateWithLifecycle()
    val updateSettings by container.updateSettings.state.collectAsStateWithLifecycle()
    val settingsVm: SettingsViewModel = viewModel(factory = viewModelFactory { SettingsViewModel(container.settings) })
    val updateVm: UpdateSettingsViewModel = viewModel(factory = viewModelFactory { UpdateSettingsViewModel(container.updateSettings) })

    LaunchedEffect(updateSettings.autoCheckOnLaunch) {
        if (updateSettings.autoCheckOnLaunch) container.updates.check()
    }

    LeiTheme(settings) {
        var selected by rememberSaveable { mutableIntStateOf(0) }
        var detail by rememberSaveable { mutableStateOf<DetailPage?>(null) }
        var backProgress by remember { mutableFloatStateOf(0f) }
        val predictiveBack = settings.predictiveBack && Build.VERSION.SDK_INT >= 34
        BackHandler(enabled = detail != null && !predictiveBack) { detail = null }
        PredictiveBackHandler(enabled = detail != null && predictiveBack) { progress ->
            try {
                progress.collect { backProgress = it.progress }
                detail = null
            } finally {
                backProgress = 0f
            }
        }
        val labels = listOf("首页", "设置", "关于")
        val icons = listOf(Icons.Default.Home, Icons.Default.Settings, Icons.Default.Info)

        Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background).statusBarsPadding()) {
            Box(Modifier.fillMaxSize().navigationBarsPadding()
                .padding(bottom = if (detail == null) 92.dp else 0.dp)
                .graphicsLayer {
                    scaleX = 1f - backProgress * 0.08f
                    scaleY = 1f - backProgress * 0.08f
                }) {
                when (detail) {
                    DetailPage.APPEARANCE -> AppearanceScreen(settingsVm, onBack = { detail = null })
                    DetailPage.LOGS -> LogScreen(container.logger, onBack = { detail = null })
                    null -> when (selected) {
                        0 -> HomeScreen()
                        1 -> SettingsScreen(
                            updateViewModel = updateVm,
                            onAppearance = { detail = DetailPage.APPEARANCE },
                            onLogs = { detail = DetailPage.LOGS },
                        )
                        else -> AboutScreen(container.updates, onOpenLogs = { detail = DetailPage.LOGS })
                    }
                }
            }
            if (settings.floatingBar && detail == null) {
                Box(
                    Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
                        .padding(horizontal = 26.dp, vertical = 12.dp).widthIn(max = 480.dp),
                ) {
                    PlainFloatingBar(selected, labels, icons) { selected = it }
                }
            } else if (detail == null) {
                Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                    StandardNavigationBar(selected, labels, icons) { selected = it }
                }
            }
        }
    }
}
