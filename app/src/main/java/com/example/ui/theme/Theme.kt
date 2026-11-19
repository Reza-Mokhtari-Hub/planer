package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CheerfulTheme

val VazirmatnFamily = FontFamily(
    Font(R.font.vazirmatn, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
    Font(R.font.vazirmatn_extrabold, FontWeight.ExtraBold)
)

val PlusJakartaFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal)
)

val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 17.sp
    ),
    labelMedium = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 15.sp
    ),
    labelSmall = TextStyle(
        fontFamily = VazirmatnFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

data class PlannerPalette(
    val backgroundMain: Color,
    val surfaceCard: Color,
    val surfaceWarmAlt: Color,
    val borderColor: Color,
    val textHeading: Color,
    val textMain: Color,
    val textMuted: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val glowTopStart: Color,
    val glowTopEnd: Color,
    val glowBottom: Color,
    val uniBg: Color = Color(0xFFEEF2FF),
    val uniBorder: Color = Color(0xFFA5B4FC),
    val uniText: Color = Color(0xFF312E81),
    val studyBg: Color = Color(0xFFFFF1F2),
    val studyBorder: Color = Color(0xFFFDA4AF),
    val studyText: Color = Color(0xFF881337),
    val sportBg: Color = Color(0xFFECFDF5),
    val sportBorder: Color = Color(0xFF6EE7B7),
    val sportText: Color = Color(0xFF064E3B),
    val restBg: Color = Color(0xFFF0F9FF),
    val restBorder: Color = Color(0xFF7DD3FC),
    val restText: Color = Color(0xFF0C4A6E),
    val personalBg: Color = Color(0xFFFFFBEB),
    val personalBorder: Color = Color(0xFFFCD34D),
    val personalText: Color = Color(0xFF78350F)
)

val LocalPlannerPalette = staticCompositionLocalOf { paletteForTheme(CheerfulTheme.ENERGETIC_LIGHT) }

fun paletteForTheme(theme: CheerfulTheme): PlannerPalette = when (theme) {
    CheerfulTheme.ENERGETIC_LIGHT -> PlannerPalette(
        backgroundMain = Color(0xFFFFFDFA),
        surfaceCard = Color(0xFFFFFFFF),
        surfaceWarmAlt = Color(0xFFFFFBEB),
        borderColor = Color(0xFFFDE68A),
        textHeading = Color(0xFF0F172A),
        textMain = Color(0xFF1E293B),
        textMuted = Color(0xFF64748B),
        accentPrimary = Color(0xFFF59E0B),
        accentSecondary = Color(0xFF10B981),
        glowTopStart = Color(0x55FED7AA),
        glowTopEnd = Color(0x44D8B4FE),
        glowBottom = Color(0x44A7F3D0)
    )
    CheerfulTheme.SPRING_BLOSSOM -> PlannerPalette(
        backgroundMain = Color(0xFFFFF5F7),
        surfaceCard = Color(0xFFFFFFFF),
        surfaceWarmAlt = Color(0xFFFFF1F2),
        borderColor = Color(0xFFFECDD3),
        textHeading = Color(0xFF831843),
        textMain = Color(0xFF1E293B),
        textMuted = Color(0xFF64748B),
        accentPrimary = Color(0xFFF43F5E),
        accentSecondary = Color(0xFFEC4899),
        glowTopStart = Color(0x55FECDD3),
        glowTopEnd = Color(0x44FDE68A),
        glowBottom = Color(0x44BBF7D0)
    )
    CheerfulTheme.OCEAN_MINT -> PlannerPalette(
        backgroundMain = Color(0xFFF0FDFA),
        surfaceCard = Color(0xFFFFFFFF),
        surfaceWarmAlt = Color(0xFFECFDF5),
        borderColor = Color(0xFF99F6E4),
        textHeading = Color(0xFF134E4A),
        textMain = Color(0xFF0F172A),
        textMuted = Color(0xFF475569),
        accentPrimary = Color(0xFF0D9488),
        accentSecondary = Color(0xFF0284C7),
        glowTopStart = Color(0x55CCFBF1),
        glowTopEnd = Color(0x44E0E7FF),
        glowBottom = Color(0x44BAE6FD)
    )
    CheerfulTheme.SUNSHINE_WARM -> PlannerPalette(
        backgroundMain = Color(0xFFFFFBEB),
        surfaceCard = Color(0xFFFFFFFF),
        surfaceWarmAlt = Color(0xFFFEF3C7),
        borderColor = Color(0xFFFCD34D),
        textHeading = Color(0xFF7C2D12),
        textMain = Color(0xFF1C1917),
        textMuted = Color(0xFF57534E),
        accentPrimary = Color(0xFFEA580C),
        accentSecondary = Color(0xFFD97706),
        glowTopStart = Color(0x55FDE047),
        glowTopEnd = Color(0x44FB923C),
        glowBottom = Color(0x44F472B6)
    )
}

@Composable
fun WeeklyPlannerTheme(
    selectedTheme: CheerfulTheme,
    content: @Composable () -> Unit
) {
    val palette = paletteForTheme(selectedTheme)
    val colorScheme = lightColorScheme(
        primary = palette.accentPrimary,
        onPrimary = Color.White,
        primaryContainer = palette.surfaceWarmAlt,
        onPrimaryContainer = palette.textHeading,
        secondary = palette.accentSecondary,
        onSecondary = Color.White,
        secondaryContainer = palette.sportBg,
        onSecondaryContainer = palette.sportText,
        tertiary = Color(0xFF7C3AED),
        onTertiary = Color.White,
        background = palette.backgroundMain,
        onBackground = palette.textMain,
        surface = palette.surfaceCard,
        onSurface = palette.textMain,
        surfaceVariant = palette.surfaceWarmAlt,
        onSurfaceVariant = palette.textMuted,
        outline = palette.borderColor,
        error = Color(0xFFE11D48),
        onError = Color.White
    )

    CompositionLocalProvider(LocalPlannerPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

fun parseHexColorSafe(hex: String, fallback: Color = Color(0xFF6366F1)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (_: Exception) {
        fallback
    }
}
