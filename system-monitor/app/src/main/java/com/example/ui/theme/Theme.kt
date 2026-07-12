package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkCustomColorScheme = darkColorScheme(
    primary = ThemePrimary,
    onPrimary = ThemePrimaryDark,
    background = ThemeBackground,
    onBackground = ThemeText,
    surface = ThemeBottomNav,
    onSurface = ThemeText,
    surfaceVariant = ThemeSurfaceVariant.copy(alpha = 0.2f),
    onSurfaceVariant = ThemeTextSecondary,
    secondaryContainer = ThemeBottomNavIconActive,
    error = ThemeDanger,
    tertiary = ThemeSuccess
)

private val LightCustomColorScheme = lightColorScheme(
    primary = ThemePrimaryLight,
    onPrimary = ThemePrimaryDarkLight,
    background = ThemeBackgroundLight,
    onBackground = ThemeTextLight,
    surface = ThemeBottomNavLight,
    onSurface = ThemeTextLight,
    surfaceVariant = ThemeSurfaceVariantLight.copy(alpha = 0.4f),
    onSurfaceVariant = ThemeTextSecondaryLight,
    secondaryContainer = ThemeBottomNavIconActiveLight,
    error = ThemeDangerLight,
    tertiary = ThemeSuccessLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkCustomColorScheme else LightCustomColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
