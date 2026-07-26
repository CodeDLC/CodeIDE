package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IntelliJDarkColorScheme = darkColorScheme(
  primary = IJAccentBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF2B3859),
  onPrimaryContainer = Color(0xFFCBE2FF),
  secondary = IJLightBlue,
  onSecondary = Color.White,
  background = IJBackground,
  onBackground = IJTextPrimary,
  surface = IJSurface,
  onSurface = IJTextPrimary,
  surfaceVariant = IJHeader,
  onSurfaceVariant = IJTextSecondary,
  outline = IJBorder,
  error = IJRedError,
  onError = Color.White
)

private val IntelliJLightColorScheme = lightColorScheme(
  primary = IJAccentBlue,
  onPrimary = Color.White,
  background = Color(0xFFF7F8FA),
  onBackground = Color(0xFF1C1D1F),
  surface = Color.White,
  onSurface = Color(0xFF1C1D1F),
  surfaceVariant = Color(0xFFEBECF0),
  onSurfaceVariant = Color(0xFF5C6166),
  outline = Color(0xFFD1D4DB),
  error = IJRedError
)

@Composable
fun CodeIDETheme(
  darkTheme: Boolean = true, // Default to dark IDE theme
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) IntelliJDarkColorScheme else IntelliJLightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
