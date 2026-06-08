package com.teodordevtech.arcadiatourism.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize

private val LightColors = lightColorScheme(
    primary = RichBrown,
    onPrimary = WarmCream,
    primaryContainer = DarkBrown,
    onPrimaryContainer = WarmCream,
    secondary = GoldenYellow,
    onSecondary = DarkBrown,
    secondaryContainer = SoftYellow,
    onSecondaryContainer = DarkBrown,
    background = SoftYellow,
    onBackground = DarkBrown,
    surface = WarmCream,
    onSurface = DarkBrown,
    surfaceVariant = GoldenYellow,
    onSurfaceVariant = DarkBrown
)

@Composable
fun ArcadiaTourismTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            content()
        }
    }
}
