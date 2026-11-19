package com.example.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PdfOrientation
import com.example.model.PdfTemplateType
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.viewmodel.PlannerUiState
import com.example.ui.viewmodel.PlannerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportCenterSheet(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel,
    onDismiss: () -> Unit
) {
    val palette = LocalPlannerPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    var template by remember { mutableStateOf(PdfTemplateType.MATRIX) }
    var orientation by remember { mutableStateOf(PdfOrientation.LANDSCAPE) }
    var includeStats by remember { mutableStateOf(true) }
    var includeNotes by remember { mutableStateOf(true) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRenderingPreview by remember { mutableStateOf(true) }
    var rawJsonDialogText by remember { mutableStateOf<String?>(null) }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.savePdfToUri(uri, template, orientation, includeStats, includeNotes)
        }
    }

    val savePngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        if (uri != null) {
            viewModel.savePngToUri(uri, template, orientation, includeStats, includeNotes)
        }
    }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.saveJsonToUri(uri)
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importJsonFromUri(uri)
            onDismiss()
        }
    }

    LaunchedEffect(template, orientation, includeStats, includeNotes, uiState.tasks) {
        isRenderingPreview = true
        previewBitmap = viewModel.generatePreviewBitmap(template, orientation, includeStats, includeNotes)
        isRenderingPreview = false
    }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PictureAsPdf,
                        contentDescription = null,
                        tint = palette.accentPrimary
                    )
                    Text(
                        text = "مرکز خروجی PDF، تصویر و پشتیبان JSON",
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.textHeading
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "بستن")
                }
            }

            // Template selector
            Text("انتخاب قالب خروجی", style = MaterialTheme.typography.titleSmall, color = palette.textHeading)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PdfTemplateType.entries.forEach { t ->
                    val selected = template == t
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selected) palette.accentPrimary.copy(alpha = 0.14f) else palette.surfaceWarmAlt)
                            .border(
                                if (selected) 1.5.dp else 1.dp,
                                if (selected) palette.accentPrimary else palette.borderColor,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { template = t }
                            .padding(10.dp)
                    ) {
                        Text(t.labelFa, style = MaterialTheme.typography.labelMedium, color = palette.textHeading)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(t.descFa, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    }
                }
            }

            // Orientation selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PdfOrientation.entries.forEach { o ->
                    FilterChip(
                        selected = orientation == o,
                        onClick = { orientation = o },
                        label = { Text(o.labelFa) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("درج نوار آمار پیشرفت هفتگی", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = includeStats, onCheckedChange = { includeStats = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("درج راهنمای تایم ناهار، ورزش و خواب", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = includeNotes, onCheckedChange = { includeNotes = it })
            }

            // Live Preview Box
            Text("پیش‌نمایش زنده خروجی", style = MaterialTheme.typography.titleSmall, color = palette.textHeading)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (orientation == PdfOrientation.LANDSCAPE) 1.414f else 0.78f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, palette.borderColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bmp = previewBitmap
                if (isRenderingPreview || bmp == null) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "پیش‌نمایش خروجی برنامه",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    )
                }
            }

            // PDF & PNG Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { savePdfLauncher.launch("weekly-schedule-dental-term5.pdf") },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentPrimary),
                    modifier = Modifier.weight(1f).testTag("export_save_pdf_button")
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("ذخیره PDF")
                }
                OutlinedButton(
                    onClick = { viewModel.sharePdf(template, orientation, includeStats, includeNotes) },
                    modifier = Modifier.weight(1f).testTag("export_share_pdf_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("اشتراک PDF")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { savePngLauncher.launch("weekly-schedule-dental-term5.png") },
                    modifier = Modifier.weight(1f).testTag("export_save_png_button")
                ) {
                    Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("ذخیره تصویر PNG")
                }
                OutlinedButton(
                    onClick = { viewModel.sharePng(template, orientation, includeStats, includeNotes) },
                    modifier = Modifier.weight(1f).testTag("export_share_png_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("اشتراک PNG")
                }
            }

            HorizontalDivider(color = palette.borderColor)

            Text(
                text = "پشتیبان‌گیری و انتقال داده (JSON)",
                style = MaterialTheme.typography.titleSmall,
                color = palette.textHeading
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { saveJsonLauncher.launch("weekly-planner-backup.json") },
                    modifier = Modifier.weight(1f).testTag("export_save_json_button")
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("ذخیره JSON")
                }
                OutlinedButton(
                    onClick = { viewModel.shareJsonBackup() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("اشتراک JSON")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { importJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.weight(1f).testTag("import_json_button")
                ) {
                    Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("بازگردانی فایل JSON")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            rawJsonDialogText = viewModel.buildJsonBackupString()
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("view_raw_json_button")
                ) {
                    Icon(Icons.Rounded.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("مشاهده JSON خام")
                }
            }
        }
    }

    rawJsonDialogText?.let { raw ->
        AlertDialog(
            onDismissRequest = { rawJsonDialogText = null },
            title = { Text("ساختار JSON پشتیبان") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    Text(
                        text = raw,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = Color(0xFFE2E8F0)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    clipboard.setText(AnnotatedString(raw))
                    rawJsonDialogText = null
                }) {
                    Text("کپی در حافظه")
                }
            },
            dismissButton = {
                TextButton(onClick = { rawJsonDialogText = null }) {
                    Text("بستن")
                }
            }
        )
    }
}
