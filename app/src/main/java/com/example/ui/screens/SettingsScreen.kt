package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.model.AiModelOption
import com.example.model.CheerfulTheme
import com.example.model.ViewMode
import com.example.model.WeekStartOption
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.theme.paletteForTheme
import com.example.ui.theme.parseHexColorSafe
import com.example.ui.viewmodel.PlannerUiState

@Composable
fun SettingsScreen(
    uiState: PlannerUiState,
    onSelectTheme: (CheerfulTheme) -> Unit,
    onUpdateWeekTitle: (String) -> Unit,
    onSelectWeekStart: (WeekStartOption) -> Unit,
    onSelectViewMode: (ViewMode) -> Unit,
    onSetSoundEnabled: (Boolean) -> Unit,
    onSetPersianNumbers: (Boolean) -> Unit,
    onSetCompactView: (Boolean) -> Unit,
    onSelectAiModel: (AiModelOption) -> Unit,
    onAddCategory: (name: String, colorHex: String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onOpenExportCenter: () -> Unit,
    onRestoreDentalSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    var weekTitleDraft by remember(uiState.settings.currentWeekTitle) {
        mutableStateOf(uiState.settings.currentWeekTitle)
    }
    var newCategoryName by remember { mutableStateOf("") }
    val colorOptions = remember {
        listOf("#6366F1", "#E11D48", "#10B981", "#0EA5E9", "#F59E0B", "#8B5CF6", "#EC4899")
    }
    var selectedCategoryColor by remember { mutableStateOf(colorOptions.first()) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

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
                    text = "تنظیمات، پوسته‌ها و شخصی‌سازی",
                    style = MaterialTheme.typography.headlineSmall,
                    color = palette.textHeading,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // 4 Cheerful Themes
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
                            text = "انتخاب پوسته رنگی پرانرژی",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CheerfulTheme.entries.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { theme ->
                                        val preview = paletteForTheme(theme)
                                        val selected = uiState.settings.theme == theme
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(preview.surfaceWarmAlt)
                                                .border(
                                                    width = if (selected) 2.dp else 1.dp,
                                                    color = if (selected) preview.accentPrimary else preview.borderColor,
                                                    shape = RoundedCornerShape(16.dp)
                                                )
                                                .clickable { onSelectTheme(theme) }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(preview.accentPrimary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(theme.emoji)
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = theme.titleFa,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = preview.textHeading
                                                )
                                                Text(
                                                    text = theme.subtitleFa,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = preview.textMuted
                                                )
                                            }
                                            if (selected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = "فعال",
                                                    tint = preview.accentPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // General Settings & Switches
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
                            text = "تنظیمات عمومی برنامه",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = weekTitleDraft,
                                onValueChange = { weekTitleDraft = it },
                                label = { Text("عنوان برنامه هفته") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (weekTitleDraft.isNotBlank()) onUpdateWeekTitle(weekTitleDraft.trim())
                                }
                            ) {
                                Text("ثبت")
                            }
                        }

                        SettingSwitchRow(
                            title = "افکت‌های صوتی شاد هنگام تیک زدن",
                            checked = uiState.settings.soundEnabled,
                            onCheckedChange = onSetSoundEnabled,
                            testTag = "setting_sound_switch"
                        )
                        SettingSwitchRow(
                            title = "نمایش اعداد فارسی (۰ تا ۹)",
                            checked = uiState.settings.persianNumbers,
                            onCheckedChange = onSetPersianNumbers,
                            testTag = "setting_persian_numbers_switch"
                        )
                        SettingSwitchRow(
                            title = "نمای فشرده کارت‌های برنامه",
                            checked = uiState.settings.compactView,
                            onCheckedChange = onSetCompactView,
                            testTag = "setting_compact_switch"
                        )

                        Text("نمای پیش‌فرض برنامه هفتگی", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ViewMode.entries.forEach { vm ->
                                FilterChip(
                                    selected = uiState.settings.viewMode == vm,
                                    onClick = { onSelectViewMode(vm) },
                                    label = { Text(vm.labelFa) }
                                )
                            }
                        }

                        Text("روز شروع هفته", style = MaterialTheme.typography.titleSmall)
                        WeekStartOption.entries.forEach { opt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectWeekStart(opt) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.settings.weekStart == opt,
                                    onClick = { onSelectWeekStart(opt) }
                                )
                                Text(opt.labelFa, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Categories Manager
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
                            text = "مدیریت دسته‌بندی‌ها",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        uiState.categories.forEach { cat ->
                            val dotColor = parseHexColorSafe(cat.color)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.surfaceWarmAlt)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Text(cat.name, style = MaterialTheme.typography.bodyMedium, color = palette.textHeading)
                                }
                                if (uiState.categories.size > 1) {
                                    IconButton(onClick = { onDeleteCategory(cat.id) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "حذف دسته‌بندی",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("نام دسته‌بندی جدید") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        onAddCategory(newCategoryName.trim(), selectedCategoryColor)
                                        newCategoryName = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "افزودن دسته")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colorOptions.forEach { hex ->
                                val c = parseHexColorSafe(hex)
                                val selected = selectedCategoryColor == hex
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(
                                            width = if (selected) 3.dp else 0.dp,
                                            color = if (selected) palette.textHeading else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedCategoryColor = hex }
                                )
                            }
                        }
                    }
                }
            }

            // Gemini Model Selector
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
                            text = "تنظیمات هوش مصنوعی Gemini",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        Text(
                            text = "تمامی درخواست‌ها از طریق Firebase AI Logic و Firebase App Check بدون نیاز به کلید در سمت کاربر ارسال می‌شوند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textMuted
                        )
                        AiModelOption.entries.forEach { opt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectAiModel(opt) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = uiState.settings.aiModel == opt,
                                    onClick = { onSelectAiModel(opt) }
                                )
                                Column {
                                    Text(opt.labelFa, style = MaterialTheme.typography.titleSmall)
                                    Text(opt.descriptionFa, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                                }
                            }
                        }
                    }
                }
            }

            // Export & Restore Actions
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
                            text = "خروجی، پشتیبان‌گیری و بازیابی الگو",
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.textHeading
                        )
                        Button(
                            onClick = onOpenExportCenter,
                            colors = ButtonDefaults.buttonColors(containerColor = palette.accentSecondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("باز کردن مرکز خروجی PDF، تصویر و JSON")
                        }
                        OutlinedButton(
                            onClick = { showRestoreConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("بازگردانی برنامه استاندارد ترم ۵ دندانپزشکی")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("بازگردانی برنامه ترم ۵ دندانپزشکی") },
            text = { Text("برنامه فعلی در آرشیو ذخیره و برنامه ۶۳ وظیفه‌ای استاندارد ترم ۵ جایگزین خواهد شد.") },
            confirmButton = {
                Button(onClick = {
                    onRestoreDentalSchedule()
                    showRestoreConfirm = false
                }) { Text("تایید بازیابی") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
