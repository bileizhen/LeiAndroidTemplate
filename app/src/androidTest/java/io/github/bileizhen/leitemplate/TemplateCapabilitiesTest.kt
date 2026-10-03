package io.github.bileizhen.leitemplate

import android.content.ContentValues
import android.provider.MediaStore
import android.os.Build
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import io.github.bileizhen.leitemplate.core.logging.DiagnosticExporter
import io.github.bileizhen.leitemplate.core.update.AppRelease
import io.github.bileizhen.leitemplate.core.update.UpdateState
import io.github.bileizhen.leitemplate.core.update.UpdateChannel
import io.github.bileizhen.leitemplate.feature.update.UpdateDialogContent
import io.github.bileizhen.leitemplate.ui.LeiTemplateApp
import io.github.bileizhen.leitemplate.ui.theme.LeiTheme
import io.github.bileizhen.leitemplate.data.settings.AppearanceSettings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TemplateCapabilitiesTest {
    @get:Rule val compose = createComposeRule()
    private val application get() = ApplicationProvider.getApplicationContext<LeiTemplateApplication>()
    private val container get() = application.container

    @Before fun prepare() = runBlocking {
        container.updateSettings.setAutoCheck(false)
        container.settings.edit { AppearanceSettings(blur = false) }
    }

    @Test fun navigationLegalDocumentsAndBackRemainAccessible() {
        compose.setContent { LeiTemplateApp(container) }
        compose.onNodeWithTag("tab_2").performClick()
        compose.onNodeWithText("开源许可").performScrollTo().performClick()
        try {
            compose.waitUntil(5000) { compose.onAllNodesWithText("GNU GENERAL PUBLIC LICENSE", substring = true).fetchSemanticsNodes().isNotEmpty() }
        } catch (error: Throwable) {
            throw AssertionError(compose.onRoot(useUnmergedTree = true).printToString(), error)
        }
        compose.onNodeWithText("‹ 返回").performClick()
        compose.onNodeWithText("隐私说明").performScrollTo().performClick()
        try {
            compose.waitUntil(5000) { compose.onAllNodesWithText("本地数据").fetchSemanticsNodes().isNotEmpty() }
        } catch (error: Throwable) {
            throw AssertionError(compose.onRoot(useUnmergedTree = true).printToString(), error)
        }
        compose.onNodeWithText("‹ 返回").performClick()
        compose.onNodeWithText("第三方声明").performScrollTo().performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("MIUIX", substring = true).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun floatingBlurAndGlassSwitchesSelectRenderingWithoutLosingTabs() {
        compose.setContent { LeiTemplateApp(container) }
        compose.onNodeWithTag("plain_floating_bar").assertExists()
        runBlocking { container.settings.edit { it.copy(blur = true, liquidGlass = true) } }
        if (Build.VERSION.SDK_INT >= 33) {
            compose.waitUntil(5000) { compose.onAllNodesWithTag("glass_floating_bar").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("tab_1").performClick()
            runBlocking { container.settings.edit { it.copy(liquidGlass = false) } }
            compose.waitUntil(5000) { compose.onAllNodesWithTag("blur_floating_bar").fetchSemanticsNodes().isNotEmpty() }
        } else compose.onNodeWithTag("plain_floating_bar").assertExists()
        runBlocking { container.settings.edit { it.copy(floatingBar = false) } }
        compose.waitUntil(5000) { compose.onAllNodesWithTag("standard_navigation_bar").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("tab_2").performClick()
        compose.onNodeWithText("复制版本信息").assertExists()
    }

    @Test fun updateNotesAndActionsAreUsable() {
        runBlocking {
            container.updateSettings.ignoreRelease(UpdateChannel.STABLE, "1.2.0")
            container.updateSettings.ignoreRelease(UpdateChannel.PRERELEASE, "1.3.0-rc.1")
            val stored = container.updateSettings.snapshot().ignoredVersions
            assertEquals("1.2.0", stored[UpdateChannel.STABLE])
            assertEquals("1.3.0-rc.1", stored[UpdateChannel.PRERELEASE])
        }
        var opened = ""
        var ignored = false
        val release = AppRelease("1.2.0-rc.1", "A test release", "https://github.com/example/app/releases/tag/v1.2.0-rc.1",
            null, null, true)
        compose.setContent {
            LeiTheme(AppearanceSettings()) {
                UpdateDialogContent(UpdateState.Available(release), {}, {}, { ignored = true }, { opened = it })
            }
        }
        compose.onNodeWithText("A test release").assertExists()
        compose.onNodeWithText("查看发布 / 下载").performClick()
        assertEquals(release.pageUrl, opened)
        compose.onNodeWithText("忽略此版本").performClick()
        assertTrue(ignored)
    }

    @Test fun debugUpdatePreviewCanBeOpenedDismissedAndReopened() {
        assertTrue(BuildConfig.UPDATE_DIALOG_PREVIEW)
        compose.setContent { LeiTemplateApp(container) }
        compose.onNodeWithTag("tab_2").performClick()
        compose.onNodeWithText("检查更新").performScrollTo().performClick()
        compose.onNodeWithText("应用更新（测试预览）").assertExists()
        compose.onNodeWithText("稍后").performClick()
        compose.onNodeWithText("应用更新（测试预览）").assertDoesNotExist()
        compose.onNodeWithText("检查更新").performScrollTo().performClick()
        compose.onNodeWithText("忽略此版本").performClick()
        compose.onNodeWithText("检查更新").performScrollTo().performClick()
        compose.onNodeWithText("应用更新（测试预览）").assertExists()
    }

    @Test fun logExportIncludesRedactedDiagnostics() {
        container.logger.error("Test", "sample failure token=fake-secret", java.io.IOException("offline"))
        compose.setContent { LeiTemplateApp(container) }
        compose.onNodeWithTag("tab_1").performClick()
        compose.onNodeWithText("日志与诊断").performScrollTo().performClick()
        compose.onNodeWithText("导出日志").assertIsEnabled()
        compose.onNodeWithText("搜索日志").assertDoesNotExist()
        compose.onNodeWithText("清空日志").assertDoesNotExist()
        val file = runBlocking { DiagnosticExporter.create(application, container.logger) }
        val report = file.readText()
        assertFalse(report.contains("fake-secret"))
        assertTrue(report.contains("[REDACTED]"))
        assertTrue(report.contains("java.io.IOException: offline"))
        assertTrue(report.contains("package=${BuildConfig.APPLICATION_ID}"))
        if (Build.VERSION.SDK_INT >= 29) {
            val resolver = application.contentResolver
            val uri = checkNotNull(resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "template-export-test-${System.nanoTime()}.txt")
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }))
            try {
                runBlocking { DiagnosticExporter.save(application, file, uri) }
                val saved = resolver.openInputStream(uri)!!.bufferedReader().use { it.readText() }
                assertEquals(report, saved)
            } finally { resolver.delete(uri, null, null) }
        }
    }
}
