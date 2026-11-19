package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.TaskItem
import com.example.ui.theme.LocalPlannerPalette
import com.example.util.PersianUtils

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoveTaskDialog(
    task: TaskItem,
    usePersianDigits: Boolean,
    onDismiss: () -> Unit,
    onConfirmMove: (dayId: String, slotId: String) -> Unit
) {
    val palette = LocalPlannerPalette.current
    var selectedDay by remember { mutableStateOf(task.day) }
    var selectedSlot by remember { mutableStateOf(PersianUtils.getTaskSlot(task)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surfaceCard,
        title = {
            Text(
                text = "انتقال سریع فعالیت",
                style = MaterialTheme.typography.titleMedium,
                color = palette.textHeading
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = PersianUtils.formatNumber(task.title, usePersianDigits),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.accentPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "انتخاب روز مقصد:",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textHeading
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PersianUtils.DEFAULT_DAYS_ORDER.forEach { dayKey ->
                        val dayFa = PersianUtils.DAYS_MAP[dayKey]?.fa ?: dayKey
                        FilterChip(
                            selected = selectedDay == dayKey,
                            onClick = { selectedDay = dayKey },
                            label = { Text(dayFa) }
                        )
                    }
                }

                Text(
                    text = "انتخاب بازه زمانی مقصد:",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.textHeading
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PersianUtils.TIME_SLOTS.forEach { slot ->
                        val isSelected = selectedSlot == slot.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) palette.accentPrimary.copy(alpha = 0.14f)
                                    else palette.surfaceWarmAlt
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) palette.accentPrimary else palette.borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedSlot = slot.id }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = slot.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = palette.textHeading
                            )
                            Text(
                                text = PersianUtils.formatNumber(slot.timeRange, usePersianDigits),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) palette.accentPrimary else palette.textMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmMove(selectedDay, selectedSlot)
                    onDismiss()
                },
                modifier = Modifier.testTag("confirm_move_task_button")
            ) {
                Text("انتقال بده")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
