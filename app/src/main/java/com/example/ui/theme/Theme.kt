package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AcademicCobalt,
    secondary = AcademicAmber,
    tertiary = AcademicNavy,
    background = AcademicSlateBg,
    surface = AcademicSlateSurf,
    onPrimary = AcademicTextPrimary,
    onSecondary = AcademicSlateBg,
    onBackground = AcademicTextPrimary,
    onSurface = AcademicTextPrimary,
    surfaceVariant = AcademicSlateCard,
    outline = AcademicBorder
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightPrimary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = AcademicSlateBg,
    onBackground = AcademicSlateBg,
    onSurface = AcademicSlateBg,
    outline = LightBorder
)

@Composable
fun FacultyAidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Locked to preserve professional academic branding guidelines
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
