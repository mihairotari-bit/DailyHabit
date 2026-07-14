package com.mihai.dailyhabit

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun HelloTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    val light = lightColorScheme(
        primary = Color(0xFF2B8A4D),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE6F4EA),
        onPrimaryContainer = Color(0xFF0F3B21),
        secondary = Color(0xFF526354),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD5E8D6),
        onSecondaryContainer = Color(0xFF101F13),
        surface = Color(0xFFFDFCF8), // Bianco caldo
        background = Color(0xFFF7F5F0), // Crema
        onBackground = Color(0xFF1A1C1A),
        surfaceContainer = Color(0xFFF0EEE8),
        onSurface = Color(0xFF1A1C1A), // Antracite
        surfaceVariant = Color(0xFFDFE4DD),
        onSurfaceVariant = Color(0xFF434844), // Grigio morbido
        outline = Color(0xFF737973)
    )
    val dark = darkColorScheme(
        primary = Color(0xFF8CD8A1),
        onPrimary = Color(0xFF00391A),
        primaryContainer = Color(0xFF005228),
        onPrimaryContainer = Color(0xFFA7F5BC),
        secondary = Color(0xFFB9CCB9),
        onSecondary = Color(0xFF253427),
        surface = Color(0xFF111411),
        background = Color(0xFF111411),
        onBackground = Color(0xFFE1E3DF),
        onSurface = Color(0xFFE1E3DF),
        surfaceContainer = Color(0xFF1D211E),
        surfaceVariant = Color(0xFF434844),
        onSurfaceVariant = Color(0xFFC3C8C1),
        outline = Color(0xFF8D938C)
    )
    MaterialTheme(colorScheme = if (darkTheme) dark else light, content = content)
}
