package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalLibrary
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.model.CategoryItem
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.theme.PlannerPalette
import com.example.ui.theme.parseHexColorSafe
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun PlannerAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalPlannerPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundMain)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.glowTopStart, Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.08f),
                    radius = size.maxDimension * 0.55f
                ),
                radius = size.maxDimension * 0.55f,
                center = Offset(size.width * 0.85f, size.height * 0.08f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.glowTopEnd, Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height * 0.22f),
                    radius = size.maxDimension * 0.48f
                ),
                radius = size.maxDimension * 0.48f,
                center = Offset(size.width * 0.12f, size.height * 0.22f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(palette.glowBottom, Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.92f),
                    radius = size.maxDimension * 0.52f
                ),
                radius = size.maxDimension * 0.52f,
                center = Offset(size.width * 0.5f, size.height * 0.92f)
            )
        }
        content()
    }
}

data class CategoryVisualStyle(
    val bg: Color,
    val border: Color,
    val text: Color,
    val accent: Color,
    val icon: ImageVector
)

fun resolveCategoryStyle(
    categoryId: String,
    categories: List<CategoryItem>,
    palette: PlannerPalette
): CategoryVisualStyle {
    return when (categoryId) {
        "uni" -> CategoryVisualStyle(
            bg = palette.uniBg,
            border = palette.uniBorder,
            text = palette.uniText,
            accent = Color(0xFF4F46E5),
            icon = Icons.Rounded.School
        )
        "study" -> CategoryVisualStyle(
            bg = palette.studyBg,
            border = palette.studyBorder,
            text = palette.studyText,
            accent = Color(0xFFE11D48),
            icon = Icons.Rounded.LocalLibrary
        )
        "sport" -> CategoryVisualStyle(
            bg = palette.sportBg,
            border = palette.sportBorder,
            text = palette.sportText,
            accent = Color(0xFF059669),
            icon = Icons.Rounded.FitnessCenter
        )
        "rest" -> CategoryVisualStyle(
            bg = palette.restBg,
            border = palette.restBorder,
            text = palette.restText,
            accent = Color(0xFF0284C7),
            icon = Icons.Rounded.Hotel
        )
        "personal" -> CategoryVisualStyle(
            bg = palette.personalBg,
            border = palette.personalBorder,
            text = palette.personalText,
            accent = Color(0xFFD97706),
            icon = Icons.Rounded.Person
        )
        else -> {
            val cat = categories.find { it.id == categoryId }
            val base = parseHexColorSafe(cat?.color ?: "#6366F1")
            CategoryVisualStyle(
                bg = base.copy(alpha = 0.12f),
                border = base.copy(alpha = 0.45f),
                text = palette.textHeading,
                accent = base,
                icon = Icons.Rounded.Tag
            )
        }
    }
}

@Composable
fun CategoryBadge(
    label: String,
    style: CategoryVisualStyle,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(style.bg)
            .border(1.dp, style.border, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = style.accent,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = style.text,
            maxLines = 1
        )
    }
}

private data class ConfettiParticle(
    val angle: Float,
    val speed: Float,
    val color: Color,
    val sizeDp: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiOverlay(
    triggerKey: Int,
    grandCelebration: Boolean,
    modifier: Modifier = Modifier
) {
    if (triggerKey <= 0) return

    val progress = remember(triggerKey) { Animatable(0f) }
    val particles = remember(triggerKey, grandCelebration) {
        val count = if (grandCelebration) 70 else 36
        val paletteColors = listOf(
            Color(0xFFF59E0B),
            Color(0xFF10B981),
            Color(0xFFF43F5E),
            Color(0xFF6366F1),
            Color(0xFF0EA5E9),
            Color(0xFFEC4899)
        )
        val rng = Random(triggerKey)
        List(count) {
            ConfettiParticle(
                angle = rng.nextFloat() * 360f,
                speed = 250f + rng.nextFloat() * 520f,
                color = paletteColors[it % paletteColors.size],
                sizeDp = 6f + rng.nextFloat() * 7f,
                rotationSpeed = (rng.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = if (grandCelebration) 1900 else 1150, easing = LinearEasing)
        )
    }

    val p = progress.value
    if (p < 0.99f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val originX = size.width * 0.5f
            val originY = size.height * 0.35f
            val alpha = (1f - p).coerceIn(0f, 1f)
            particles.forEach { pt ->
                val rad = Math.toRadians(pt.angle.toDouble())
                val dx = (cos(rad) * pt.speed * p).toFloat()
                val dy = (sin(rad) * pt.speed * p + 420f * p * p).toFloat()
                val x = originX + dx
                val y = originY + dy
                val rectSize = pt.sizeDp.dp.toPx()
                rotate(degrees = pt.rotationSpeed * p, pivot = Offset(x, y)) {
                    drawRect(
                        color = pt.color.copy(alpha = alpha),
                        topLeft = Offset(x - rectSize / 2f, y - rectSize / 2f),
                        size = Size(rectSize, rectSize * 0.65f)
                    )
                }
            }
        }
    }
}
