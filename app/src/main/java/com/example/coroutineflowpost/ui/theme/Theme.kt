package com.example.coroutineflowpost.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FlowPostColorScheme = darkColorScheme(
    background        = DarkBg,
    surface           = DarkSurface,
    primary           = Violet,
    onBackground      = TextPrimary,
    onSurface         = TextPrimary,
    onSurfaceVariant  = TextMuted,
    outline           = BorderSubtle,
    error             = Color(0xFFEF4444)
)

@Composable
fun CoroutineFlowPostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FlowPostColorScheme,
        content     = content
    )
}