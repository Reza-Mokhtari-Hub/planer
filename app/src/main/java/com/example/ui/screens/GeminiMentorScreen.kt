package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AiModelOption
import com.example.ui.components.MarkdownText
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.viewmodel.PlannerUiState

@Composable
fun GeminiMentorScreen(
    uiState: PlannerUiState,
    onSendPreset: (String) -> Unit,
    onSendPrompt: (String) -> Unit,
    onSelectModel: (AiModelOption) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    val clipboard = LocalClipboardManager.current
    var promptText by remember { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.chatMessages.size, uiState.streamingAiText) {
        val targetIndex = (uiState.chatMessages.size + if (uiState.streamingAiText != null) 1 else 0)
        if (targetIndex > 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
        ) {
            // Top Header
            Surface(
                color = palette.surfaceCard,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor),
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = palette.accentPrimary
                            )
                            Column {
                                Text(
                                    text = "منتور هوشمند Gemini (ترم ۵ دندانپزشکی)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textHeading
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Shield,
                                        contentDescription = null,
                                        tint = palette.accentSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "اتصال امن Firebase AI Logic + App Check",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.accentSecondary
                                    )
                                }
                            }
                        }

                        if (uiState.chatMessages.isNotEmpty()) {
                            IconButton(
                                onClick = { showClearConfirm = true },
                                modifier = Modifier.testTag("clear_ai_chat_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteSweep,
                                    contentDescription = "پاک کردن تاریخچه گفتگو",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Model Selector Chips
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AiModelOption.entries.forEach { opt ->
                            FilterChip(
                                selected = uiState.settings.aiModel == opt,
                                onClick = { onSelectModel(opt) },
                                label = { Text(opt.labelFa) }
                            )
                        }
                    }
                }
            }

            // Messages + Presets
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 4 Preset Analysis Cards
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "تحلیل‌های تخصصی یک‌کلیکی بر اساس برنامه فعلی شما:",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textMuted
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetCard(
                                title = "توازن درسی و سلامت",
                                subtitle = "ناهار، دروس سنگین و ورزش",
                                icon = Icons.Rounded.Psychology,
                                accent = palette.accentPrimary,
                                enabled = !uiState.isAiGenerating,
                                onClick = { onSendPreset("comprehensive") },
                                modifier = Modifier.weight(1f).testTag("ai_preset_comprehensive")
                            )
                            PresetCard(
                                title = "استراتژی امتحانات",
                                subtitle = "مرور فعال و فارماکولوژی",
                                icon = Icons.Rounded.School,
                                accent = Color(0xFF4F46E5),
                                enabled = !uiState.isAiGenerating,
                                onClick = { onSendPreset("exams") },
                                modifier = Modifier.weight(1f).testTag("ai_preset_exams")
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetCard(
                                title = "ارگونومی و ۳ ورزش",
                                subtitle = "جمعه، یکشنبه، سه‌شنبه + خواب",
                                icon = Icons.Rounded.FitnessCenter,
                                accent = palette.accentSecondary,
                                enabled = !uiState.isAiGenerating,
                                onClick = { onSendPreset("recovery") },
                                modifier = Modifier.weight(1f).testTag("ai_preset_recovery")
                            )
                            PresetCard(
                                title = "بهینه‌سازی چیدمان",
                                subtitle = "شناسایی گلوگاه‌های خستگی",
                                icon = Icons.Rounded.Tune,
                                accent = Color(0xFFEC4899),
                                enabled = !uiState.isAiGenerating,
                                onClick = { onSendPreset("optimize") },
                                modifier = Modifier.weight(1f).testTag("ai_preset_optimize")
                            )
                        }
                    }
                }

                items(uiState.chatMessages, key = { it.id }) { msg ->
                    when (msg.role) {
                        "user" -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                                    color = palette.accentPrimary,
                                    modifier = Modifier.widthIn(max = 520.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                        "error" -> {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFFFF1F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = Color(0xFFBE123C))
                                    Text(
                                        text = "${msg.content}\n(در حالت آفلاین، تمام قابلیت‌های مدیریت برنامه هفتگی بدون محدودیت فعال هستند.)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF881337)
                                    )
                                }
                            }
                        }
                        else -> {
                            Surface(
                                shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp),
                                color = palette.surfaceCard,
                                shadowElevation = 1.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "پیشنهاد منتور Gemini",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = palette.accentSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(
                                            onClick = { clipboard.setText(AnnotatedString(msg.content)) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.ContentCopy,
                                                contentDescription = "کپی پاسخ",
                                                tint = palette.textMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    MarkdownText(markdown = msg.content)
                                }
                            }
                        }
                    }
                }

                // Live Streaming Bubble
                if (uiState.isAiGenerating) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = palette.surfaceWarmAlt,
                            border = androidx.compose.foundation.BorderStroke(1.dp, palette.accentPrimary)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text(
                                        text = "در حال تحلیل برنامه و نگارش پاسخ...",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = palette.accentPrimary
                                    )
                                }
                                val streamText = uiState.streamingAiText.orEmpty()
                                if (streamText.isNotBlank()) {
                                    MarkdownText(markdown = streamText)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Prompt Composer
            Surface(
                color = palette.surfaceCard,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = { Text("سوال خود را درباره برنامه درسی بپرسید...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_prompt_input")
                    )
                    Button(
                        onClick = {
                            if (promptText.isNotBlank() && !uiState.isAiGenerating) {
                                val text = promptText.trim()
                                promptText = ""
                                onSendPrompt(text)
                            }
                        },
                        enabled = promptText.isNotBlank() && !uiState.isAiGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("ai_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "ارسال پیام")
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("پاک کردن تاریخچه گفتگو") },
            text = { Text("تمام پیام‌های قبلی با منتور Gemini از حافظه پاک شوند؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChat()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("پاک کن") }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun PresetCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(palette.surfaceCard)
            .border(1.dp, palette.borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = palette.textHeading,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = palette.textMuted
        )
    }
}
