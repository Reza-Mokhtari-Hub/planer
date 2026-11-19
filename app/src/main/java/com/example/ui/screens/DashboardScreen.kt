package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.ReflectionData
import com.example.model.TaskItem
import com.example.ui.components.TaskCard
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.viewmodel.PlannerUiState
import com.example.util.PersianUtils
import java.util.Calendar

@Composable
fun DashboardScreen(
    uiState: PlannerUiState,
    onToggleTask: (String) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onMoveTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onAddTaskToday: (String) -> Unit,
    onUpdateReflection: (ReflectionData) -> Unit,
    onUpdateWeekTitle: (String) -> Unit,
    onCycleTheme: () -> Unit,
    onOpenExportCenter: () -> Unit,
    onOpenAiMentor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    val usePersian = uiState.settings.persianNumbers
    val tasks = uiState.tasks
    val total = tasks.size
    val completed = tasks.count { it.completed }
    val pending = total - completed
    val highPriorityPending = tasks.count { !it.completed && it.priority == "high" }
    val progressRatio = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(550),
        label = "dashboardProgress"
    )
    val percent = (progressRatio * 100f).toInt()

    val todayId = remember { PersianUtils.getCurrentDayId() }
    val todayTasks = remember(tasks, todayId) {
        tasks.filter { it.day == todayId }
            .sortedBy { PersianUtils.parseTimeMinutes(it.time) }
    }
    val quoteIndex = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % PersianUtils.MOTIVATIONAL_QUOTES.size
    }

    var showEditTitleDialog by remember { mutableStateOf(false) }

    var winsText by remember(uiState.reflection.wins) { mutableStateOf(uiState.reflection.wins) }
    var challengesText by remember(uiState.reflection.challenges) { mutableStateOf(uiState.reflection.challenges) }
    var improvementsText by remember(uiState.reflection.improvements) { mutableStateOf(uiState.reflection.improvements) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Header Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = palette.surfaceCard,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        palette.surfaceCard,
                                        palette.surfaceWarmAlt
                                    )
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "برنامه‌ریزی هفتگی هوشمند",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = palette.textHeading,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = uiState.settings.theme.emoji,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.clickable { showEditTitleDialog = true }
                                ) {
                                    Text(
                                        text = uiState.settings.currentWeekTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = palette.accentPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "ویرایش عنوان هفته",
                                        tint = palette.accentPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = onCycleTheme,
                                    modifier = Modifier.testTag("dashboard_theme_cycle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Palette,
                                        contentDescription = "تغییر پوسته رنگی",
                                        tint = palette.accentPrimary
                                    )
                                }
                                IconButton(
                                    onClick = onOpenExportCenter,
                                    modifier = Modifier.testTag("dashboard_export_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PictureAsPdf,
                                        contentDescription = "خروجی PDF و JSON",
                                        tint = palette.accentSecondary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.surfaceCard.copy(alpha = 0.85f))
                                .border(1.dp, palette.borderColor, RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    tint = palette.accentPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = PersianUtils.formatJalaliDate(usePersianDigits = usePersian),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.textHeading
                                )
                            }
                            Text(
                                text = "ناهار: ۱۲ تا ۱۳ | شام: ۲۱:۳۰",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted
                            )
                        }

                        Text(
                            text = "«${PersianUtils.MOTIVATIONAL_QUOTES[quoteIndex]}»",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMain
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onAddTaskToday(todayId) },
                                colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_add_task_button")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("کار جدید")
                            }
                            Button(
                                onClick = onOpenAiMentor,
                                colors = ButtonDefaults.buttonColors(containerColor = palette.accentSecondary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dashboard_ai_mentor_button")
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("تحلیل Gemini")
                            }
                        }
                    }
                }
            }

            // Progress & KPIs
            item {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "پیشرفت کلی هفته",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textHeading
                            )
                            Text(
                                text = PersianUtils.formatNumber("$percent%", usePersian),
                                style = MaterialTheme.typography.headlineSmall,
                                color = palette.accentSecondary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            color = palette.accentSecondary,
                            trackColor = palette.surfaceWarmAlt
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KpiTile(
                                    label = "کل برنامه",
                                    value = PersianUtils.formatNumber(total, usePersian),
                                    icon = Icons.Rounded.CalendarToday,
                                    accent = palette.accentPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                KpiTile(
                                    label = "تکمیل‌شده",
                                    value = PersianUtils.formatNumber(completed, usePersian),
                                    icon = Icons.Rounded.CheckCircle,
                                    accent = palette.accentSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KpiTile(
                                    label = "باقیمانده",
                                    value = PersianUtils.formatNumber(pending, usePersian),
                                    icon = Icons.Rounded.PendingActions,
                                    accent = Color(0xFF0284C7),
                                    modifier = Modifier.weight(1f)
                                )
                                KpiTile(
                                    label = "فوری مانده",
                                    value = PersianUtils.formatNumber(highPriorityPending, usePersian),
                                    icon = Icons.Rounded.PriorityHigh,
                                    accent = Color(0xFFE11D48),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 7-Day Productivity Mini-Chart
            item {
                WeeklyMiniBarChartCard(
                    tasks = tasks,
                    orderedDays = PersianUtils.getOrderedDays(uiState.settings.weekStart),
                    usePersianDigits = usePersian
                )
            }

            // Today's Tasks Section Header
            item {
                val todayName = PersianUtils.DAYS_MAP[todayId]?.fa ?: "امروز"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "برنامه امروز ($todayName)",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textHeading,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = PersianUtils.formatNumber(
                            "${todayTasks.count { it.completed }} از ${todayTasks.size} انجام‌شده",
                            usePersian
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.accentPrimary
                    )
                }
            }

            if (todayTasks.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = palette.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "امروز کاری ثبت نشده است!",
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textHeading
                            )
                            Text(
                                text = "از دکمه بالا یک فعالیت برای امروز اضافه کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textMuted
                            )
                        }
                    }
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        categories = uiState.categories,
                        usePersianDigits = usePersian,
                        compactMode = uiState.settings.compactView,
                        onToggleComplete = { onToggleTask(task.id) },
                        onEdit = { onEditTask(task) },
                        onMove = { onMoveTask(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }

            // Weekly Reflection Card
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
                            text = "بازتاب و جمع‌بندی هفتگی",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "امتیاز رضایت از هفته:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textMain
                            )
                            Row {
                                (1..5).forEach { star ->
                                    IconButton(
                                        onClick = {
                                            onUpdateReflection(uiState.reflection.copy(rating = star))
                                        },
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (star <= uiState.reflection.rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                            contentDescription = "امتیاز $star از ۵",
                                            tint = Color(0xFFF59E0B)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = winsText,
                            onValueChange = {
                                winsText = it
                                onUpdateReflection(uiState.reflection.copy(wins = it))
                            },
                            label = { Text("موفقیت‌ها و نقاط قوت این هفته") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reflection_wins_input")
                        )

                        OutlinedTextField(
                            value = challengesText,
                            onValueChange = {
                                challengesText = it
                                onUpdateReflection(uiState.reflection.copy(challenges = it))
                            },
                            label = { Text("چالش‌ها و موانع") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reflection_challenges_input")
                        )

                        OutlinedTextField(
                            value = improvementsText,
                            onValueChange = {
                                improvementsText = it
                                onUpdateReflection(uiState.reflection.copy(improvements = it))
                            },
                            label = { Text("برنامه بهبود برای هفته آینده") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reflection_improvements_input")
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showEditTitleDialog) {
        var draft by remember(uiState.settings.currentWeekTitle) {
            mutableStateOf(uiState.settings.currentWeekTitle)
        }
        AlertDialog(
            onDismissRequest = { showEditTitleDialog = false },
            title = { Text("ویرایش عنوان هفته") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("عنوان برنامه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (draft.isNotBlank()) onUpdateWeekTitle(draft.trim())
                    showEditTitleDialog = false
                }) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTitleDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun KpiTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(palette.surfaceWarmAlt)
            .border(1.dp, palette.borderColor, RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = palette.textHeading,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun WeeklyMiniBarChartCard(
    tasks: List<TaskItem>,
    orderedDays: List<String>,
    usePersianDigits: Boolean
) {
    val palette = LocalPlannerPalette.current
    val dayStats = remember(tasks, orderedDays) {
        orderedDays.map { dayKey ->
            val list = tasks.filter { it.day == dayKey }
            Triple(
                PersianUtils.DAYS_MAP[dayKey]?.shortFa ?: dayKey,
                list.count { it.completed },
                list.size
            )
        }
    }
    val maxTotal = (dayStats.maxOfOrNull { it.third } ?: 1).coerceAtLeast(1)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "نمودار بهره‌وری ۷ روز هفته",
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textHeading
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendDot("انجام‌شده", palette.accentSecondary)
                    LegendDot("کل برنامه", palette.accentPrimary.copy(alpha = 0.45f))
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .padding(top = 6.dp)
            ) {
                val count = dayStats.size.coerceAtLeast(1)
                val slotWidth = size.width / count
                val barWidth = (slotWidth * 0.42f).coerceAtMost(32.dp.toPx())
                val chartHeight = size.height

                dayStats.forEachIndexed { idx, (_, done, total) ->
                    val centerX = slotWidth * idx + slotWidth / 2f
                    val totalH = (total.toFloat() / maxTotal.toFloat()) * chartHeight
                    val doneH = (done.toFloat() / maxTotal.toFloat()) * chartHeight

                    drawRoundRect(
                        color = palette.accentPrimary.copy(alpha = 0.25f),
                        topLeft = Offset(centerX - barWidth / 2f, chartHeight - totalH),
                        size = Size(barWidth, totalH.coerceAtLeast(4f)),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                    if (done > 0) {
                        drawRoundRect(
                            color = palette.accentSecondary,
                            topLeft = Offset(centerX - barWidth / 2f, chartHeight - doneH),
                            size = Size(barWidth, doneH.coerceAtLeast(4f)),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                dayStats.forEach { (shortFa, done, total) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = shortFa,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textHeading
                        )
                        Text(
                            text = PersianUtils.formatNumber("$done/$total", usePersianDigits),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalPlannerPalette.current.textMuted
        )
    }
}
