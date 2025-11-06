package com.cathub.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 极简线框风格主题
 * - 纯白背景
 * - 单像素线框
 * - 几何留白
 */
private val CathubColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = PureWhite,
    onPrimaryContainer = TextPrimary,
    
    secondary = TextSecondary,
    onSecondary = Color.White,
    secondaryContainer = BackgroundGray,
    onSecondaryContainer = TextSecondary,
    
    tertiary = AccentBlue,
    onTertiary = Color.White,
    
    background = PureWhite,
    onBackground = TextPrimary,
    
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundGray,
    onSurfaceVariant = TextSecondary,
    
    outline = BorderGray,
    outlineVariant = DividerGray,
    
    error = AccentRed,
    onError = Color.White,
    
    scrim = Color.Black.copy(alpha = 0.32f)
)

@Composable
fun CathubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CathubColorScheme,
        typography = Typography,
        content = content
    )
}

