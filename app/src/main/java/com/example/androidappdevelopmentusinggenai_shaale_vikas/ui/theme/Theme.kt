package com.example.androidappdevelopmentusinggenai_shaale_vikas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SchoolPrimary,
    secondary = SchoolSecondary,
    tertiary = SchoolTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF121212)
)

private val LightColorScheme = lightColorScheme(
    primary = SchoolPrimary,
    secondary = SchoolSecondary,
    tertiary = SchoolTertiary,
    background = SchoolBackground,
    surface = SchoolSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White
)

@Composable
fun AndroidAppDevelopmentUsingGenAIShaaleVikasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
