package io.github.bileizhen.leitemplate.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text

@Composable
fun HomeScreen() {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Lei Android Template", fontSize = 30.sp)
        Text("MIUIX + Compose 的可复用应用骨架")
        Card {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("开始开发", fontSize = 20.sp)
                Text("把业务代码放进 feature / data / core，UI 组件继续放在 ui/component。")
            }
        }
    }
}
