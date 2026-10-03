package io.github.bileizhen.leitemplate.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Switch

@Composable
fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
) {
    BasicComponent(
        modifier = modifier,
        title = title,
        summary = summary,
        enabled = enabled,
        role = Role.Switch,
        onClick = { onCheckedChange(!checked) },
        endActions = { Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange) },
    )
}
