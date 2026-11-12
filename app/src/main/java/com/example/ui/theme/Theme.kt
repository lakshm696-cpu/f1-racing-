package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val F1DarkColorScheme = darkColorScheme(
  primary = F1Red,
  onPrimary = RacingWhite,
  primaryContainer = F1DarkRed,
  onPrimaryContainer = RacingWhite,
  secondary = TelemetryCyan,
  onSecondary = F1CarbonDark,
  secondaryContainer = F1BorderDark,
  onSecondaryContainer = TelemetryCyan,
  tertiary = SectorPurple,
  onTertiary = RacingWhite,
  background = F1CarbonDark,
  onBackground = RacingWhite,
  surface = F1SurfaceDark,
  onSurface = RacingWhite,
  surfaceVariant = F1CardSurface,
  onSurfaceVariant = RacingTextMuted,
  outline = F1BorderDark,
  error = WarningRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to sleek carbon dark F1 styling
  dynamicColor: Boolean = false, // Keep signature F1 racing livery colors
  content: @Composable () -> Unit,
) {
  val colorScheme = F1DarkColorScheme

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = F1CarbonDark.toArgb()
        window.navigationBarColor = F1CarbonDark.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
