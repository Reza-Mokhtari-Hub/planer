package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.resolveCategoryStyle
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.viewmodel.PlannerUiState
import com.example.util.PersianUtils

@Composable
fun StatsScreen(
    uiState: PlannerUiState,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    val usePersian = uiState.settings.persianNumbers
    val tasks = uiState.tasks
    val orderedDays = PersianUtils.getOrderedDays(uiState.settings.weekStart)

    val dayMetrics = remember(tasks, orderedDays) {
        orderedDays.map { dayKey ->
            val list = tasks.filter { it.day == dayKey }
            val done = list.count { it.completed }
            val pending = list.size - done
            val rate = if (list.isEmpty()) 0 else ((done * 100f) / list.size).toInt()
            DayStatMetric(
                dayId = dayKey,
                labelFa = PersianUtils.DAYS_MAP[dayKey]?.fa ?: dayKey,
                shortFa = PersianUtils.DAYS_MAP[dayKey]?.shortFa ?: dayKey,
                done = done,
                pending = pending,
                total = list.size,
                rate = rate
            )
        }
    }

    val bestDay = remember(dayMetrics) {
        dayMetrics.filter { it.total > 0 }
            .maxWithOrNull(compareBy<DayStatMetric> { it.rate }.thenBy { it.done })
    }

    val sportTasks = remember(tasks) { tasks.filter { it.category == "sport" } }
    val lunchTasks = remember(tasks) {
        tasks.filter { PersianUtils.getTaskSlot(it) == "lunch_uni" }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "آمار و تحلیل عملکرد هفتگی",
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textHeading,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Most Productive Day Card
            if (bestDay != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = palette.surfaceWarmAlt,
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, palette.accentPrimary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(palette.accentPrimary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "پربازده‌ترین روز هفته: ${bestDay.labelFa}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textHeading,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = PersianUtils.formatNumber(
                                        "${bestDay.done} از ${bestDay.total} فعالیت تکمیل شده (${bestDay.rate}%)",
                                        usePersian
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMain
                                )
                            }
                        }
                    }
                }
            }

            // Grouped Bar Chart (Completed vs Pending)
            item {
                val maxVal = (dayMetrics.maxOfOrNull { maxOf(it.done, it.pending) } ?: 1).coerceAtLeast(1)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.surfaceCard,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "نمودار مقایسه‌ای انجام‌شده در برابر باقیمانده",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(palette.accentSecondary))
                                Text("تکمیل‌شده", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Text("در انتظار", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            val groupWidth = size.width / dayMetrics.size.coerceAtLeast(1)
                            val barW = (groupWidth * 0.28f).coerceAtMost(20.dp.toPx())
                            val gap = 4.dp.toPx()

                            dayMetrics.forEachIndexed { idx, m ->
                                val centerX = groupWidth * idx + groupWidth / 2f
                                val doneH = (m.done.toFloat() / maxVal.toFloat()) * size.height
                                val pendH = (m.pending.toFloat() / maxVal.toFloat()) * size.height

                                drawRoundRect(
                                    color = palette.accentSecondary,
                                    topLeft = Offset(centerX - barW - gap / 2f, size.height - doneH),
                                    size = Size(barW, doneH.coerceAtLeast(4f)),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                                drawRoundRect(
                                    color = Color(0xFFF59E0B),
                                    topLeft = Offset(centerX + gap / 2f, size.height - pendH),
                                    size = Size(barW, pendH.coerceAtLeast(4f)),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            dayMetrics.forEach { m ->
                                Text(
                                    text = m.shortFa,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted,
                                    modifier = Modifier.weight(1f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Daily progress list
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.surfaceCard,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "روند روزانه تکمیل کارها",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        dayMetrics.forEach { m ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(m.labelFa, style = MaterialTheme.typography.bodyMedium, color = palette.textHeading)
                                    Text(
                                        text = PersianUtils.formatNumber("${m.done}/${m.total} (${m.rate}%)", usePersian),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.accentSecondary
                                    )
                                }
                                LinearProgressIndicator(
                                    progress = { if (m.total == 0) 0f else m.done.toFloat() / m.total.toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    color = palette.accentSecondary,
                                    trackColor = palette.surfaceWarmAlt
                                )
                            }
                        }
                    }
                }
            }

            // Category Distribution Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.surfaceCard,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "توزیع دسته‌بندی فعالیت‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        uiState.categories.forEach { cat ->
                            val catTasks = tasks.filter { it.category == cat.id }
                            val catDone = catTasks.count { it.completed }
                            val style = resolveCategoryStyle(cat.id, uiState.categories, palette)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(style.bg)
                                    .border(1.dp, style.border, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(style.icon, contentDescription = null, tint = style.accent, modifier = Modifier.size(18.dp))
                                    Text(cat.name, style = MaterialTheme.typography.titleSmall, color = style.text)
                                }
                                Text(
                                    text = PersianUtils.formatNumber("$catDone از ${catTasks.size}", usePersian),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = style.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Dental Wellness & Habits Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.surfaceCard,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "پایش عادت‌های کلیدی ترم ۵ دندانپزشکی",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.sportBg)
                                .border(1.dp, palette.sportBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = palette.sportText)
                                Column {
                                    Text("۳ جلسه ورزش هفتگی (جمعه، یک‌شنبه، سه‌شنبه)", style = MaterialTheme.typography.bodySmall, color = palette.sportText, fontWeight = FontWeight.Bold)
                                    Text("حفظ ارگونومی و سلامت عضلانی دندانپزشک", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                }
                            }
                            Text(
                                text = PersianUtils.formatNumber("${sportTasks.count { it.completed }}/${sportTasks.size}", usePersian),
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.sportText,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(palette.personalBg)
                                .border(1.dp, palette.personalBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = palette.personalText)
                                Column {
                                    Text("تایم ثابت ناهار ۱۲ تا ۱۳", style = MaterialTheme.typography.bodySmall, color = palette.personalText, fontWeight = FontWeight.Bold)
                                    Text("تایم استاندارد شام ۲۱:۳۰ و خواب ۲۳:۰۰", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                }
                            }
                            Text(
                                text = PersianUtils.formatNumber("${lunchTasks.count { it.completed }}/${lunchTasks.size}", usePersian),
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.personalText,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

private data class DayStatMetric(
    val dayId: String,
    val labelFa: String,
    val shortFa: String,
    val done: Int,
    val pending: Int,
    val total: Int,
    val rate: Int
)
