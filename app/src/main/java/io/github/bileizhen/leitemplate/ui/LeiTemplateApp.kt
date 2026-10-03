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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.bileizhen.leitemplate.AppContainer
import io.github.bileizhen.leitemplate.feature.about.AboutScreen
import io.github.bileizhen.leitemplate.feature.about.LegalDocument
import io.github.bileizhen.leitemplate.feature.about.LegalDocumentScreen
import io.github.bileizhen.leitemplate.feature.update.UpdateDialog
import io.github.bileizhen.leitemplate.feature.home.HomeScreen
import io.github.bileizhen.leitemplate.feature.logs.LogExportDialog
import io.github.bileizhen.leitemplate.feature.settings.AppearanceScreen
import io.github.bileizhen.leitemplate.feature.settings.SettingsScreen
import io.github.bileizhen.leitemplate.feature.settings.SettingsViewModel
import io.github.bileizhen.leitemplate.feature.settings.UpdateSettingsViewModel
import io.github.bileizhen.leitemplate.ui.component.PlainFloatingBar
import io.github.bileizhen.leitemplate.ui.component.StandardNavigationBar
import io.github.bileizhen.leitemplate.ui.component.HighApiFloatingNavigation
import io.github.bileizhen.leitemplate.ui.theme.LeiTheme
import io.github.bileizhen.leitemplate.ui.util.viewModelFactory
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets

private enum class DetailPage { APPEARANCE, ABOUT, LEGAL }

@Composable
fun LeiTemplateApp(container: AppContainer) {
    val settings by container.settings.state.collectAsStateWithLifecycle()
    val updateSettings by container.updateSettings.state.collectAsStateWithLifecycle()
    val settingsVm: SettingsViewModel = viewModel(factory = viewModelFactory { SettingsViewModel(container.settings) })
    val updateVm: UpdateSettingsViewModel = viewModel(factory = viewModelFactory { UpdateSettingsViewModel(container.updateSettings) })

    LaunchedEffect(updateSettings.autoCheckOnLaunch) {
        if (updateSettings.autoCheckOnLaunch) container.updates.checkOnLaunch()
    }

    LeiTheme(settings) {
        var selected by rememberSaveable { mutableIntStateOf(0) }
        var detail by rememberSaveable { mutableStateOf<DetailPage?>(null) }
        var document by rememberSaveable { mutableStateOf(LegalDocument.LICENSE) }
        var showLogs by rememberSaveable { mutableStateOf(false) }
        var previewUpdates by rememberSaveable { mutableStateOf(false) }
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        val context = androidx.compose.ui.platform.LocalContext.current
        val openUpdates: () -> Unit = {
            if (io.github.bileizhen.leitemplate.BuildConfig.UPDATE_DIALOG_PREVIEW) previewUpdates = true
            else scope.launch { container.updates.present() }
        }
        var backProgress by remember { mutableFloatStateOf(0f) }
        val predictiveBack = settings.predictiveBack && Build.VERSION.SDK_INT >= 34
        fun returnToParent() { detail = if (detail == DetailPage.LEGAL) DetailPage.ABOUT else null }
        val hasDetail = detail != null
        BackHandler(enabled = hasDetail && !predictiveBack) { returnToParent() }
        PredictiveBackHandler(enabled = hasDetail && predictiveBack) { progress ->
            try {
                progress.collect { backProgress = it.progress }
                returnToParent()
            } finally {
                backProgress = 0f
            }
        }
        val labels = listOf("首页", "设置")
        val icons = listOf(Icons.Default.Home, Icons.Default.Settings)

        top.yukonga.miuix.kmp.basic.Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MiuixTheme.colorScheme.background) {
        val showNavigation = detail == null
        Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.background)) {
            val page: @Composable () -> Unit = {
            Box(Modifier.fillMaxSize().then(if (detail == DetailPage.ABOUT) Modifier else Modifier.navigationBarsPadding())
                .padding(bottom = if (showNavigation) 92.dp else 0.dp)
                .then(if (detail == null) Modifier.statusBarsPadding() else Modifier)
                .graphicsLayer {
                    scaleX = 1f - backProgress * 0.08f
                    scaleY = 1f - backProgress * 0.08f
                }) {
                when (detail) {
                    DetailPage.APPEARANCE -> AppearanceScreen(settingsVm, onBack = { detail = null })
                    DetailPage.LEGAL -> LegalDocumentScreen(document, onBack = ::returnToParent,
                        onOpenDocument = { document = it })
                    DetailPage.ABOUT -> AboutScreen(onBack = ::returnToParent, enableBlur = settings.blur,
                        onOpenDocument = { document = it; detail = DetailPage.LEGAL })
                    null -> when (selected) {
                        0 -> HomeScreen()
                        1 -> SettingsScreen(
                            updateViewModel = updateVm,
                            onAppearance = { detail = DetailPage.APPEARANCE },
                            onLogs = { showLogs = true },
                            onAbout = { detail = DetailPage.ABOUT }, onUpdates = openUpdates,
                        )

                    }
                }
            }
            }
            if (settings.floatingBar && settings.blur && Build.VERSION.SDK_INT >= 33 && LocalView.current.isHardwareAccelerated) {
                HighApiFloatingNavigation(
                    selectedIndex = selected, labels = labels, icons = icons, onSelect = { selected = it },
                    blur = settings.blur, glass = settings.liquidGlass, visible = showNavigation,
                    content = page,
                )
            } else {
            page()
            if (settings.floatingBar && showNavigation) {
                Box(
                    Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
                        .padding(horizontal = 26.dp, vertical = 12.dp).widthIn(max = 480.dp),
                ) {
                    PlainFloatingBar(selected, labels, icons) { selected = it }
                }
            } else if (showNavigation) {
                Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                    StandardNavigationBar(selected, labels, icons) { selected = it }
                }
            }
            }
        }
        LogExportDialog(showLogs, container.logger) { showLogs = false }
        UpdateDialog(container.updates, container.updateTransfer)
        if (previewUpdates) {
            val prerelease = updateSettings.channel == io.github.bileizhen.leitemplate.core.update.UpdateChannel.PRERELEASE
            val release = io.github.bileizhen.leitemplate.core.update.AppRelease(
                if (prerelease) "0.2.0-rc.1" else "0.2.0",
                "本次更新改善日常使用体验。\n\n外观与交互\n\n• 新增主题预览，支持浅色、深色和跟随系统。\n\n• 改善悬浮底栏的模糊、液态玻璃效果与返回动画。\n\n通用功能\n\n• 支持正式版与预发布版更新渠道。\n\n• 支持保存和分享脱敏日志，便于排查问题。\n\n• 可离线查看开源许可和隐私说明。",
                io.github.bileizhen.leitemplate.core.config.AppMetadata.RELEASES_URL,
                null, "preview.apk", prerelease, size = 13L * 1024 * 1024)
            io.github.bileizhen.leitemplate.feature.update.UpdateDialogContent(
                io.github.bileizhen.leitemplate.core.update.UpdateState.Available(release),
                onDismiss = { previewUpdates = false }, onRetry = {}, onIgnore = { previewUpdates = false },
                onOpenRelease = { io.github.bileizhen.leitemplate.ui.util.openExternalLink(context, it) }, preview = true)
        }
        }
    }
}
