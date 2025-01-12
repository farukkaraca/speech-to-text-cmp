package theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import presentation.theme.DarkColorScheme
import presentation.theme.LightColorScheme
import presentation.theme.Typography

@Composable
actual fun AppTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if(darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}