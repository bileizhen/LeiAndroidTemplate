package io.github.bileizhen.leitemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.bileizhen.leitemplate.ui.LeiTemplateApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as LeiTemplateApplication).container
        setContent { LeiTemplateApp(container) }
    }
}
