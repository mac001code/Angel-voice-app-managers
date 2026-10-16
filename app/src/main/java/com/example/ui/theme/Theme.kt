package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AngelVoiceColorScheme = darkColorScheme(
    primary = ElectricSky,
    onPrimary = DeepCharcoal,
    primaryContainer = ElectricSkyContainer,
    onPrimaryContainer = ElectricSky,
    secondary = EmeraldGreen,
    onSecondary = DeepCharcoal,
    secondaryContainer = EmeraldGreenContainer,
    onSecondaryContainer = EmeraldGreen,
    tertiary = AmberGold,
    onTertiary = DeepCharcoal,
    tertiaryContainer = AmberGoldContainer,
    onTertiaryContainer = AmberGold,
    error = CrimsonCoral,
    onError = White,
    errorContainer = CrimsonCoralContainer,
    onErrorContainer = CrimsonCoral,
    background = DeepCharcoal,
    onBackground = TextPrimary,
    surface = ElevatedSlate,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedSlateVariant,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AngelVoiceColorScheme,
        typography = Typography,
        content = content
    )
}
