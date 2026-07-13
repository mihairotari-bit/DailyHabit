package com.mihai.android17helloworld

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HelloTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val light = lightColorScheme(
        primary = Color(0xFF4E8E2B), onPrimary = Color.White,
        primaryContainer = Color(0xFFDDEFCB), onPrimaryContainer = Color(0xFF173A20),
        secondary = Color(0xFF1C5636), surface = Color(0xFFFAF8F5),
        background = Color(0xFFFDFBF7), onBackground = Color(0xFF1D3B2B),
        surfaceContainer = Color(0xFFFFFCF8), onSurface = Color(0xFF173A20),
        surfaceVariant = Color(0xFFF0F2E8), outline = Color(0xFF76A55A)
    )
    val dark = darkColorScheme(
        primary = Color(0xFFA9D77F), onPrimary = Color(0xFF12351A),
        primaryContainer = Color(0xFF285D32), onPrimaryContainer = Color(0xFFDDEFCB),
        secondary = Color(0xFFB7D5AD), onSecondary = Color(0xFF18391F),
        surface = Color(0xFF101711), background = Color(0xFF101711), onBackground = Color(0xFFE6EDE1), onSurface = Color(0xFFE6EDE1),
        surfaceContainer = Color(0xFF1B251D), onSurfaceVariant = Color(0xFFC0CBBE),
        outline = Color(0xFF83987F)
    )
    MaterialTheme(colorScheme = if (darkTheme) dark else light, content = content)
}
