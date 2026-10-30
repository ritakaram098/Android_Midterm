package com.example.petcare.ui.theme

import android.app.Activity
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
    primary = SageGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSageAccent,
    onPrimaryContainer = DarkGreenText,
    secondary = MutedGreenText,
    onSecondary = Color.White,
    background = DarkGreenText,
    onBackground = CreamBackground,
    surface = SageGreenPrimary,
    onSurface = CreamBackground
)

private val LightColorScheme = lightColorScheme(
    primary = SageGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSageAccent,
    onPrimaryContainer = DarkGreenText,
    secondary = MutedGreenText,
    onSecondary = Color.White,
    background = CreamBackground,
    onBackground = DarkGreenText,
    surface = WhiteCardField,
    onSurface = DarkGreenText,
    surfaceVariant = LightSageAccent,
    onSurfaceVariant = MutedGreenText
)

@Composable
fun PetCareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled by default to strictly enforce custom brand palette
    dynamicColor: Boolean = false,
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
