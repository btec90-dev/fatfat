package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
  lightColorScheme(
    primary = VintagePrimary,
    onPrimary = Color.White,
    primaryContainer = VintageContainer,
    onPrimaryContainer = VintageOnContainer,
    secondary = VintageSecondary,
    onSecondary = Color.White,
    secondaryContainer = VintageSecondaryContainer,
    onSecondaryContainer = VintageOnSecondaryContainer,
    tertiary = VintageTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEADCC6),
    onTertiaryContainer = Color(0xFF2C1E14),
    background = VintageBackground,
    surface = VintageSurface,
    onSurface = VintageOnSurface,
    surfaceVariant = Color(0xFFEEDFCC),
    onSurfaceVariant = Color(0xFF6E4D32),
    outline = VintageOutline,
    outlineVariant = VintageOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}
