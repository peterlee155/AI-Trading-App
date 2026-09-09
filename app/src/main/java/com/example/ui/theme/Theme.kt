package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TradeGuardDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = DarkBg,
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = EmeraldLight,
    secondary = CyanAccent,
    onSecondary = DarkBg,
    tertiary = SkyAccent,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = RoseDanger,
    onError = TextPrimary
)

@Composable
fun TradeGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TradeGuardDarkColorScheme,
        typography = Typography,
        content = content
    )
}

