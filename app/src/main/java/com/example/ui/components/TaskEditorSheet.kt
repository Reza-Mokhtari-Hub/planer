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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CategoryItem
import com.example.model.TaskItem
import com.example.model.TaskPriority
import com.example.ui.theme.LocalPlannerPalette
import com.example.util.PersianUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditorSheet(
    initialTask: TaskItem?,
    defaultDayId: String,
    defaultSlotId: String?,
    categories: List<CategoryItem>,
    usePersianDigits: Boolean,
    onDismiss: () -> Unit,
    onSave: (TaskItem, Boolean) -> Unit
) {
    val palette = LocalPlannerPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isNew = initialTask == null

    val resolvedInitialSlot = initialTask?.let { PersianUtils.getTaskSlot(it) }
        ?: defaultSlotId
        ?: "morning_main"
    val initialSlotInfo = PersianUtils.TIME_SLOTS.find { it.id == resolvedInitialSlot }

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var selectedDay by remember { mutableStateOf(initialTask?.day ?: defaultDayId) }
    var selectedSlot by remember { mutableStateOf(resolvedInitialSlot) }
    var timeText by remember {
        mutableStateOf(initialTask?.time?.ifBlank { initialSlotInfo?.defaultStartTime ?: "09:00" }
            ?: initialSlotInfo?.defaultStartTime ?: "09:00")
    }
    var selectedCategory by remember {
        mutableStateOf(initialTask?.category ?: categories.firstOrNull()?.id ?: "study")
    }
    var selectedPriority by remember { mutableStateOf(initialTask?.priority ?: "medium") }
    var difficulty by remember { mutableStateOf(initialTask?.difficulty ?: "") }
    var titleError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.surfaceCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddTask,
                        contentDescription = null,
                        tint = palette.accentPrimary
                    )
                    Text(
                        text = if (isNew) "افزودن کار جدید" else "ویرایش فعالیت",
                        style = MaterialTheme.typography.headlineSmall,
                        color = palette.textHeading
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "بستن")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("عنوان فعالیت یا درس *") },
                placeholder = { Text("مثلاً: مرور اندودانتیکس یا جلسه ورزش") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("لطفاً عنوان کار را وارد کنید") }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            // Day selector
            Text(
                text = "روز هفته",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textHeading
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PersianUtils.DEFAULT_DAYS_ORDER.forEach { dayKey ->
                    val label = PersianUtils.DAYS_MAP[dayKey]?.fa ?: dayKey
                    FilterChip(
                        selected = selectedDay == dayKey,
                        onClick = { selectedDay = dayKey },
                        label = { Text(label) }
                    )
                }
            }

            // Time slot selector
            Text(
                text = "بازه زمانی (ساعت استاندارد)",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textHeading
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PersianUtils.TIME_SLOTS.forEach { slot ->
                    val selected = selectedSlot == slot.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) palette.accentPrimary else palette.surfaceWarmAlt)
                            .border(
                                1.dp,
                                if (selected) palette.accentPrimary else palette.borderColor,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedSlot = slot.id
                                timeText = slot.defaultStartTime
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = PersianUtils.formatNumber("${slot.title} (${slot.defaultStartTime})", usePersianDigits),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else palette.textMain
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("ساعت دقیق") },
                    placeholder = { Text("09:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = difficulty,
                    onValueChange = { difficulty = it },
                    label = { Text("سطح سختی / تگ") },
                    placeholder = { Text("سنگین، تئوری...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Category selector
            Text(
                text = "دسته‌بندی",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textHeading
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.id,
                        onClick = { selectedCategory = cat.id },
                        label = { Text(cat.name) }
                    )
                }
            }

            // Priority selector
            Text(
                text = "اولویت",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textHeading
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.entries.forEach { pr ->
                    FilterChip(
                        selected = selectedPriority == pr.id,
                        onClick = { selectedPriority = pr.id },
                        label = { Text(pr.labelFa) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("توضیحات یا نکات مطالعه") },
                placeholder = { Text("سرفصل‌ها، مکان کلاس یا نکات تمرینی...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("انصراف")
                }
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = true
                            return@Button
                        }
                        val task = TaskItem(
                            id = initialTask?.id ?: "",
                            day = selectedDay,
                            time = timeText.trim(),
                            title = title.trim(),
                            description = description.trim(),
                            category = selectedCategory,
                            priority = selectedPriority,
                            completed = initialTask?.completed ?: false,
                            difficulty = difficulty.trim(),
                            timeSlot = selectedSlot,
                            createdAt = initialTask?.createdAt ?: ""
                        )
                        onSave(task, isNew)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("save_task_button")
                ) {
                    Text(
                        text = if (isNew) "ثبت در برنامه" else "ذخیره تغییرات",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
