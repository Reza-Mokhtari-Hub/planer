package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.CategoryItem
import com.example.model.TaskItem
import com.example.model.TaskPriority
import com.example.ui.theme.LocalPlannerPalette
import com.example.util.PersianUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: TaskItem,
    categories: List<CategoryItem>,
    usePersianDigits: Boolean,
    compactMode: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    showDayChip: Boolean = false,
    showDragHandle: Boolean = false
) {
    val palette = LocalPlannerPalette.current
    val catStyle = resolveCategoryStyle(task.category, categories, palette)
    val catName = categories.find { it.id == task.category }?.name ?: "شخصی"

    val cardBg by animateColorAsState(
        targetValue = if (task.completed) catStyle.bg.copy(alpha = 0.55f) else palette.surfaceCard,
        animationSpec = tween(250),
        label = "taskBg"
    )
    val scale by animateFloatAsState(
        targetValue = if (task.completed) 0.99f else 1f,
        animationSpec = tween(200),
        label = "taskScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(if (task.completed) 0.80f else 1f)
            .testTag("task_item_card_${task.id}"),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        shadowElevation = if (task.completed) 0.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (task.priority == "high" && !task.completed) 1.5.dp else 1.dp,
            color = if (task.completed) catStyle.border.copy(alpha = 0.45f) else catStyle.border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compactMode) 10.dp else 14.dp,
                    vertical = if (compactMode) 8.dp else 12.dp
                ),
            verticalAlignment = Alignment.Top
        ) {
            // Color accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (compactMode) 36.dp else 46.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(catStyle.accent)
            )
            Spacer(modifier = Modifier.width(10.dp))

            // Completion checkbox with 48dp accessible hit target
            Box(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onToggleComplete)
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            if (task.completed) palette.accentSecondary else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (task.completed) palette.accentSecondary else catStyle.accent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.completed) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "تکمیل‌شده",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Main content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (task.time.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.surfaceWarmAlt)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = palette.accentPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = PersianUtils.formatNumber(task.time, usePersianDigits),
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textHeading
                            )
                        }
                    }

                    if (showDayChip) {
                        val dayFa = PersianUtils.DAYS_MAP[task.day]?.fa ?: task.day
                        Text(
                            text = dayFa,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.accentPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (task.priority == "high" && !task.completed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFE4E6))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "فوری",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFBE123C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = PersianUtils.formatNumber(task.title, usePersianDigits),
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textHeading,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = if (compactMode) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!compactMode && task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = PersianUtils.formatNumber(task.description, usePersianDigits),
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!compactMode) {
                    Spacer(modifier = Modifier.height(7.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CategoryChipSmall(
                            label = catName,
                            bg = catStyle.bg,
                            border = catStyle.border,
                            text = catStyle.text
                        )
                        if (task.difficulty.isNotBlank()) {
                            CategoryChipSmall(
                                label = "سختی: ${task.difficulty}",
                                bg = palette.surfaceWarmAlt,
                                border = palette.borderColor,
                                text = palette.textMain
                            )
                        }
                        val priorityLabel = TaskPriority.fromId(task.priority).labelFa
                        if (task.priority != "medium") {
                            CategoryChipSmall(
                                label = priorityLabel,
                                bg = Color.Transparent,
                                border = palette.borderColor,
                                text = palette.textMuted
                            )
                        }
                    }
                }
            }

            // Action icons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row {
                    if (showDragHandle) {
                        Icon(
                            imageVector = Icons.Rounded.DragIndicator,
                            contentDescription = "نگه‌داشتن برای جابجایی",
                            tint = palette.textMuted,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onMove,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("task_move_btn_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = "جابجایی به روز یا ساعت دیگر",
                            tint = palette.accentSecondary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("task_edit_btn_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "ویرایش کار",
                            tint = palette.accentPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("task_delete_btn_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "حذف کار",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipSmall(
    label: String,
    bg: Color,
    border: Color,
    text: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(0.8.dp, border, RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = text,
            maxLines = 1
        )
    }
}
