package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.WeekArchiveItem
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.viewmodel.PlannerUiState
import com.example.util.PersianUtils

@Composable
fun ArchiveScreen(
    uiState: PlannerUiState,
    onArchiveCurrentWeek: () -> Unit,
    onRestoreArchive: (String) -> Unit,
    onRepeatFromArchive: (archiveId: String, archiveCurrentFirst: Boolean, resetCompleted: Boolean, resetReflection: Boolean) -> Unit,
    onRepeatCurrentWeek: (archiveCurrentFirst: Boolean, resetCompleted: Boolean, resetReflection: Boolean) -> Unit,
    onDeleteArchive: (String) -> Unit,
    onUncheckAllTasks: () -> Unit,
    onArchiveAndClearCompleted: () -> Unit,
    onClearAllTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    val usePersian = uiState.settings.persianNumbers

    var repeatTargetArchive by remember { mutableStateOf<WeekArchiveItem?>(null) }
    var showRepeatCurrentDialog by remember { mutableStateOf(false) }
    var confirmRestoreArchive by remember { mutableStateOf<WeekArchiveItem?>(null) }
    var confirmDeleteArchive by remember { mutableStateOf<WeekArchiveItem?>(null) }
    var confirmClearAllDialog by remember { mutableStateOf(false) }
    var confirmArchiveClearCompletedDialog by remember { mutableStateOf(false) }

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
                    text = "آرشیو هفته‌ها و مدیریت چرخه هفته",
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textHeading,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Quick Archive & Repeat Actions
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
                            text = "ثبت و تکرار برنامه هفتگی",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        Text(
                            text = "هفته جاری را با حفظ جزئیات و یادداشت‌ها در تاریخچه ذخیره کنید یا با گزینه‌های هوشمند برای هفته جدید تکرار نمایید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onArchiveCurrentWeek,
                                colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("archive_current_week_button")
                            ) {
                                Icon(Icons.Rounded.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("آرشیو هفته جاری")
                            }
                            OutlinedButton(
                                onClick = { showRepeatCurrentDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("repeat_current_week_button")
                            ) {
                                Icon(Icons.Rounded.Repeat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("تکرار هفته جدید")
                            }
                        }
                    }
                }
            }

            // Reset Week Section
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = palette.surfaceCard,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "بازنشانی وضعیت هفته جاری",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        OutlinedButton(
                            onClick = onUncheckAllTasks,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("uncheck_all_tasks_button")
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("برداشتن تمام تیک‌ها (شروع تازه بدون حذف کارها)")
                        }
                        OutlinedButton(
                            onClick = { confirmArchiveClearCompletedDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("آرشیو هفته + حذف کارهای انجام‌شده")
                        }
                        OutlinedButton(
                            onClick = { confirmClearAllDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("clear_all_tasks_button")
                        ) {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("پاک‌سازی کامل تمام کارهای هفته")
                        }
                    }
                }
            }

            // Archive History Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = palette.accentPrimary)
                    Text(
                        text = PersianUtils.formatNumber("تاریخچه هفته‌های آرشیوشده (${uiState.archives.size})", usePersian),
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textHeading,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.archives.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = palette.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                    ) {
                        Text(
                            text = "هنوز هیچ هفته‌ای آرشیو نشده است. پس از پایان هفته می‌توانید گزارش آن را اینجا ذخیره کنید.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textMuted,
                            modifier = Modifier.padding(18.dp)
                        )
                    }
                }
            } else {
                items(uiState.archives, key = { it.id }) { arch ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = palette.surfaceCard,
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = arch.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = palette.textHeading
                                    )
                                    Text(
                                        text = PersianUtils.formatShortJalaliIsoDate(arch.date, usePersian),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(palette.accentSecondary.copy(alpha = 0.14f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = PersianUtils.formatNumber(
                                            "${arch.completedTasks}/${arch.totalTasks} (${arch.rate}%)",
                                            usePersian
                                        ),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.accentSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val wins = arch.reflection?.wins.orEmpty()
                            if (wins.isNotBlank()) {
                                Text(
                                    text = "نکات مثبت: $wins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textMain
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { repeatTargetArchive = arch },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("تکرار")
                                }
                                OutlinedButton(
                                    onClick = { confirmRestoreArchive = arch },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("بازیابی")
                                }
                                TextButton(
                                    onClick = { confirmDeleteArchive = arch },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("حذف")
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showRepeatCurrentDialog) {
        RepeatWeekOptionsDialog(
            title = "تکرار برنامه جاری برای هفته جدید",
            onDismiss = { showRepeatCurrentDialog = false },
            onConfirm = { archiveFirst, resetChecks, resetRef ->
                onRepeatCurrentWeek(archiveFirst, resetChecks, resetRef)
                showRepeatCurrentDialog = false
            }
        )
    }

    repeatTargetArchive?.let { arch ->
        RepeatWeekOptionsDialog(
            title = "تکرار هفته از آرشیو «${arch.title}»",
            onDismiss = { repeatTargetArchive = null },
            onConfirm = { archiveFirst, resetChecks, resetRef ->
                onRepeatFromArchive(arch.id, archiveFirst, resetChecks, resetRef)
                repeatTargetArchive = null
            }
        )
    }

    confirmRestoreArchive?.let { arch ->
        AlertDialog(
            onDismissRequest = { confirmRestoreArchive = null },
            title = { Text("بازیابی برنامه آرشیوشده") },
            text = { Text("آیا مطمئن هستید؟ برنامه فعلی جایگزین برنامه «${arch.title}» خواهد شد.") },
            confirmButton = {
                Button(onClick = {
                    onRestoreArchive(arch.id)
                    confirmRestoreArchive = null
                }) { Text("بله، بازیابی شود") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestoreArchive = null }) { Text("انصراف") }
            }
        )
    }

    confirmDeleteArchive?.let { arch ->
        AlertDialog(
            onDismissRequest = { confirmDeleteArchive = null },
            title = { Text("حذف از آرشیو") },
            text = { Text("آیا رکورد «${arch.title}» برای همیشه حذف شود؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteArchive(arch.id)
                        confirmDeleteArchive = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteArchive = null }) { Text("انصراف") }
            }
        )
    }

    if (confirmArchiveClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = { confirmArchiveClearCompletedDialog = false },
            title = { Text("آرشیو و پاک‌سازی انجام‌شده‌ها") },
            text = { Text("ابتدا از هفته جاری نسخه آرشیو ساخته می‌شود و سپس کارهای تیک‌خورده حذف می‌شوند.") },
            confirmButton = {
                Button(onClick = {
                    onArchiveAndClearCompleted()
                    confirmArchiveClearCompletedDialog = false
                }) { Text("تایید") }
            },
            dismissButton = {
                TextButton(onClick = { confirmArchiveClearCompletedDialog = false }) { Text("انصراف") }
            }
        )
    }

    if (confirmClearAllDialog) {
        AlertDialog(
            onDismissRequest = { confirmClearAllDialog = false },
            title = { Text("حذف تمام کارهای هفته") },
            text = { Text("هشدار: تمام کارهای هفته جاری حذف خواهند شد. آیا ادامه می‌دهید؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllTasks()
                        confirmClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("بله، حذف کن") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearAllDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun RepeatWeekOptionsDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (archiveCurrentFirst: Boolean, resetCompleted: Boolean, resetReflection: Boolean) -> Unit
) {
    var archiveFirst by remember { mutableStateOf(true) }
    var resetCompleted by remember { mutableStateOf(true) }
    var resetReflection by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = archiveFirst, onCheckedChange = { archiveFirst = it })
                    Text("ابتدا هفته جاری در آرشیو ذخیره شود", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = resetCompleted, onCheckedChange = { resetCompleted = it })
                    Text("تمام تیک‌ها برای شروع هفته جدید برداشته شوند", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = resetReflection, onCheckedChange = { resetReflection = it })
                    Text("فرم بازتاب هفتگی برای هفته جدید خالی شود", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(archiveFirst, resetCompleted, resetReflection) }) {
                Text("اجرای تکرار هفته")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف") }
        }
    )
}
