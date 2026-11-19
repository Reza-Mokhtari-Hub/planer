package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterListOff
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.model.CategoryItem
import com.example.model.TaskItem
import com.example.model.ViewMode
import com.example.ui.components.TaskCard
import com.example.ui.components.resolveCategoryStyle
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.viewmodel.PlannerUiState
import com.example.util.PersianUtils
import kotlin.math.roundToInt

private data class DragDropZoneKey(val dayId: String, val slotId: String?)

@Composable
fun WeeklyScheduleScreen(
    uiState: PlannerUiState,
    onSelectDay: (String) -> Unit,
    onViewModeChange: (ViewMode) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onPriorityFilterChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onResetFilters: () -> Unit,
    onToggleTask: (String) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onOpenMoveDialog: (TaskItem) -> Unit,
    onDropMoveTask: (taskId: String, newDay: String, newSlotId: String?) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onAddTaskInSlot: (dayId: String, slotId: String?) -> Unit,
    onRestoreDentalSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    val usePersian = uiState.settings.persianNumbers
    val orderedDays = PersianUtils.getOrderedDays(uiState.settings.weekStart)
    var showRestoreConfirm by remember { mutableStateOf(false) }

    // Drag & Drop state across both Day tabs and Matrix cells
    val dropZoneBounds = remember { mutableStateMapOf<DragDropZoneKey, Rect>() }
    var draggingTask by remember { mutableStateOf<TaskItem?>(null) }
    var dragPointerRoot by remember { mutableStateOf(Offset.Zero) }
    var hoveredDropZone by remember { mutableStateOf<DragDropZoneKey?>(null) }
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }

    val hasActiveFilter = uiState.filter.query.isNotBlank() ||
        uiState.filter.categoryId != "all" ||
        uiState.filter.priority != "all" ||
        uiState.filter.status != "all"

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootOrigin = coords.boundsInRoot().topLeft
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Controls: Search, ViewMode, Restore Schedule
            Column(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.filter.query,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("جستجو در دروس، توضیحات...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.filter.query.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Clear, contentDescription = "پاک کردن جستجو")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("weekly_search_input")
                    )

                    IconButton(
                        onClick = { showRestoreConfirm = true },
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.surfaceWarmAlt)
                            .border(1.dp, palette.borderColor, RoundedCornerShape(14.dp))
                            .testTag("weekly_restore_dental_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Restore,
                            contentDescription = "بازگردانی برنامه ترم ۵ دندانپزشکی",
                            tint = palette.accentPrimary
                        )
                    }
                }

                // Segmented ViewMode Toggle (Table Matrix vs Cards)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = uiState.settings.viewMode == ViewMode.TABLE,
                        onClick = { onViewModeChange(ViewMode.TABLE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        icon = { Icon(Icons.Rounded.GridView, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("view_mode_table_button")
                    ) {
                        Text("جدولی (ماتریس)")
                    }
                    SegmentedButton(
                        selected = uiState.settings.viewMode == ViewMode.CARDS,
                        onClick = { onViewModeChange(ViewMode.CARDS) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        icon = { Icon(Icons.Rounded.ViewAgenda, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("view_mode_cards_button")
                    ) {
                        Text("کارتی (تایم‌لاین)")
                    }
                }

                // Category & Status Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        FilterChip(
                            selected = uiState.filter.categoryId == "all",
                            onClick = { onCategoryFilterChange("all") },
                            label = { Text("همه دسته‌ها") }
                        )
                    }
                    items(uiState.categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = uiState.filter.categoryId == cat.id,
                            onClick = {
                                onCategoryFilterChange(
                                    if (uiState.filter.categoryId == cat.id) "all" else cat.id
                                )
                            },
                            label = { Text(cat.name) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filter.status == "pending",
                            onClick = {
                                onStatusFilterChange(
                                    if (uiState.filter.status == "pending") "all" else "pending"
                                )
                            },
                            label = { Text("در انتظار") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filter.status == "completed",
                            onClick = {
                                onStatusFilterChange(
                                    if (uiState.filter.status == "completed") "all" else "completed"
                                )
                            },
                            label = { Text("انجام‌شده") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = uiState.filter.priority == "high",
                            onClick = {
                                onPriorityFilterChange(
                                    if (uiState.filter.priority == "high") "all" else "high"
                                )
                            },
                            label = { Text("اولویت فوری") }
                        )
                    }
                    if (hasActiveFilter) {
                        item {
                            TextButton(onClick = onResetFilters) {
                                Icon(Icons.Rounded.FilterListOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("حذف فیلتر")
                            }
                        }
                    }
                }
            }

            if (uiState.settings.viewMode == ViewMode.CARDS) {
                // Day Selector Strip (also acts as Drag & Drop target)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orderedDays, key = { it }) { dayKey ->
                        val dayFa = PersianUtils.DAYS_MAP[dayKey]?.fa ?: dayKey
                        val count = uiState.filteredTasks.count { it.day == dayKey }
                        val selected = uiState.selectedDayId == dayKey
                        val dropKey = DragDropZoneKey(dayKey, null)
                        val isDragHover = hoveredDropZone == dropKey

                        Column(
                            modifier = Modifier
                                .onGloballyPositioned { coords ->
                                    dropZoneBounds[dropKey] = coords.boundsInRoot()
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        isDragHover -> palette.accentSecondary.copy(alpha = 0.25f)
                                        selected -> palette.accentPrimary
                                        else -> palette.surfaceCard
                                    }
                                )
                                .border(
                                    width = if (isDragHover) 2.dp else 1.dp,
                                    color = when {
                                        isDragHover -> palette.accentSecondary
                                        selected -> palette.accentPrimary
                                        else -> palette.borderColor
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectDay(dayKey) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("day_tab_$dayKey"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayFa,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected && !isDragHover) Color.White else palette.textHeading,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = PersianUtils.formatNumber("$count کار", usePersian),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected && !isDragHover) Color.White.copy(alpha = 0.9f) else palette.textMuted
                            )
                        }
                    }
                }

                // Selected Day Cards Timeline
                val dayTasks = remember(uiState.filteredTasks, uiState.selectedDayId) {
                    uiState.filteredTasks
                        .filter { it.day == uiState.selectedDayId }
                        .sortedBy { PersianUtils.parseTimeMinutes(it.time) }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (dayTasks.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = palette.surfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(22.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "برای این روز موردی یافت نشد",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = palette.textHeading
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onAddTaskInSlot(uiState.selectedDayId, null) }) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("افزودن کار")
                                        }
                                        if (uiState.tasks.isEmpty()) {
                                            OutlinedButton(onClick = { showRestoreConfirm = true }) {
                                                Text("بازیابی برنامه ترم ۵")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "نکته: برای جابجایی، کارت را لمس و نگه دارید و روی نام روز رها کنید، یا دکمه انتقال (⇄) را بزنید.",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted
                            )
                        }
                        items(dayTasks, key = { it.id }) { task ->
                            var cardTopLeft by remember { mutableStateOf(Offset.Zero) }
                            Box(
                                modifier = Modifier
                                    .onGloballyPositioned { coords ->
                                        cardTopLeft = coords.boundsInRoot().topLeft
                                    }
                                    .pointerInput(task.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { localOffset ->
                                                draggingTask = task
                                                dragPointerRoot = cardTopLeft + localOffset
                                                hoveredDropZone = null
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragPointerRoot += dragAmount
                                                hoveredDropZone = dropZoneBounds.entries
                                                    .firstOrNull { it.value.contains(dragPointerRoot) }
                                                    ?.key
                                            },
                                            onDragEnd = {
                                                val target = hoveredDropZone
                                                val moved = draggingTask
                                                if (target != null && moved != null && target.dayId != moved.day) {
                                                    onDropMoveTask(moved.id, target.dayId, target.slotId)
                                                }
                                                draggingTask = null
                                                hoveredDropZone = null
                                            },
                                            onDragCancel = {
                                                draggingTask = null
                                                hoveredDropZone = null
                                            }
                                        )
                                    }
                            ) {
                                TaskCard(
                                    task = task,
                                    categories = uiState.categories,
                                    usePersianDigits = usePersian,
                                    compactMode = uiState.settings.compactView,
                                    showDragHandle = true,
                                    onToggleComplete = { onToggleTask(task.id) },
                                    onEdit = { onEditTask(task) },
                                    onMove = { onOpenMoveDialog(task) },
                                    onDelete = { onDeleteTask(task) }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(74.dp)) }
                }
            } else {
                // MATRIX TABLE VIEW (9 Time Slots x 7 Days)
                MatrixScheduleView(
                    tasks = uiState.filteredTasks,
                    categories = uiState.categories,
                    orderedDays = orderedDays,
                    usePersianDigits = usePersian,
                    dropZoneBounds = dropZoneBounds,
                    hoveredDropZone = hoveredDropZone,
                    onStartDrag = { task, rootPos ->
                        draggingTask = task
                        dragPointerRoot = rootPos
                        hoveredDropZone = null
                    },
                    onUpdateDrag = { delta ->
                        dragPointerRoot += delta
                        hoveredDropZone = dropZoneBounds.entries
                            .firstOrNull { it.value.contains(dragPointerRoot) }
                            ?.key
                    },
                    onEndDrag = {
                        val target = hoveredDropZone
                        val moved = draggingTask
                        if (target != null && moved != null) {
                            onDropMoveTask(moved.id, target.dayId, target.slotId)
                        }
                        draggingTask = null
                        hoveredDropZone = null
                    },
                    onCancelDrag = {
                        draggingTask = null
                        hoveredDropZone = null
                    },
                    onToggleTask = onToggleTask,
                    onEditTask = onEditTask,
                    onAddTaskInSlot = onAddTaskInSlot,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Floating dragged task chip overlay
        draggingTask?.let { dragged ->
            val relOffset = dragPointerRoot - rootOrigin
            Surface(
                modifier = Modifier
                    .zIndex(10f)
                    .offset {
                        IntOffset(
                            x = (relOffset.x - 80f).roundToInt(),
                            y = (relOffset.y - 40f).roundToInt()
                        )
                    },
                shape = RoundedCornerShape(14.dp),
                color = palette.accentPrimary,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = dragged.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text("بازگردانی برنامه جامع ترم ۵ دندانپزشکی") },
            text = {
                Text("برنامه فعلی ابتدا به‌طور خودکار در آرشیو ذخیره می‌شود و سپس برنامه ۶۳ وظیفه‌ای ترم ۵ (شامل تایم ناهار ۱۲ تا ۱۳، بیدارباش ۶ صبح و ۳ جلسه ورزش) بارگذاری می‌گردد.")
            },
            confirmButton = {
                Button(onClick = {
                    onRestoreDentalSchedule()
                    showRestoreConfirm = false
                }) {
                    Text("بله، بازیابی شود")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun MatrixScheduleView(
    tasks: List<TaskItem>,
    categories: List<CategoryItem>,
    orderedDays: List<String>,
    usePersianDigits: Boolean,
    dropZoneBounds: MutableMap<DragDropZoneKey, Rect>,
    hoveredDropZone: DragDropZoneKey?,
    onStartDrag: (TaskItem, Offset) -> Unit,
    onUpdateDrag: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onCancelDrag: () -> Unit,
    onToggleTask: (String) -> Unit,
    onEditTask: (TaskItem) -> Unit,
    onAddTaskInSlot: (dayId: String, slotId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalPlannerPalette.current
    val horizontalScroll = rememberScrollState()
    val verticalScroll = rememberScrollState()
    val slotColWidth = 112.dp
    val dayColWidth = 148.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(verticalScroll)
            .padding(bottom = 78.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(horizontalScroll)
                .padding(horizontal = 10.dp)
        ) {
            Column {
                // Header Row
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(palette.surfaceWarmAlt)
                        .border(1.dp, palette.borderColor, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .width(slotColWidth)
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بازه زمانی",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textHeading,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    orderedDays.forEach { dayKey ->
                        Box(
                            modifier = Modifier
                                .width(dayColWidth)
                                .border(0.5.dp, palette.borderColor)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = PersianUtils.DAYS_MAP[dayKey]?.fa ?: dayKey,
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textHeading,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 9 Time Slot Rows
                PersianUtils.TIME_SLOTS.forEach { slot ->
                    Row(
                        modifier = Modifier
                            .background(
                                if (slot.isLunch) palette.personalBg.copy(alpha = 0.65f)
                                else palette.surfaceCard
                            )
                            .border(0.5.dp, palette.borderColor)
                    ) {
                        // Slot Label Cell
                        Column(
                            modifier = Modifier
                                .width(slotColWidth)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = slot.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(slot.colorHex),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = PersianUtils.formatNumber(slot.timeRange, usePersianDigits),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textMuted
                            )
                        }

                        // 7 Day Cells
                        orderedDays.forEach { dayKey ->
                            val cellTasks = tasks.filter {
                                it.day == dayKey && PersianUtils.getTaskSlot(it) == slot.id
                            }.sortedBy { PersianUtils.parseTimeMinutes(it.time) }

                            val dropKey = DragDropZoneKey(dayKey, slot.id)
                            val isHover = hoveredDropZone == dropKey

                            Column(
                                modifier = Modifier
                                    .width(dayColWidth)
                                    .onGloballyPositioned { coords ->
                                        dropZoneBounds[dropKey] = coords.boundsInRoot()
                                    }
                                    .background(
                                        when {
                                            isHover -> palette.accentSecondary.copy(alpha = 0.18f)
                                            slot.isLunch -> palette.personalBg.copy(alpha = 0.45f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isHover) 1.5.dp else 0.5.dp,
                                        color = if (isHover) palette.accentSecondary else palette.borderColor
                                    )
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                cellTasks.forEach { t ->
                                    val catStyle = resolveCategoryStyle(t.category, categories, palette)
                                    var chipTopLeft by remember { mutableStateOf(Offset.Zero) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coords ->
                                                chipTopLeft = coords.boundsInRoot().topLeft
                                            }
                                            .pointerInput(t.id) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = { localOffset ->
                                                        onStartDrag(t, chipTopLeft + localOffset)
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        onUpdateDrag(dragAmount)
                                                    },
                                                    onDragEnd = onEndDrag,
                                                    onDragCancel = onCancelDrag
                                                )
                                            }
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(catStyle.bg)
                                            .border(1.dp, catStyle.border, RoundedCornerShape(10.dp))
                                            .clickable { onEditTask(t) }
                                            .padding(horizontal = 7.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (t.completed) palette.accentSecondary
                                                    else Color.White
                                                )
                                                .border(1.5.dp, catStyle.accent, CircleShape)
                                                .clickable { onToggleTask(t.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (t.completed) {
                                                Icon(
                                                    Icons.Rounded.Check,
                                                    contentDescription = "تکمیل‌شده",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = PersianUtils.formatNumber(t.title, usePersianDigits),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = catStyle.text,
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = if (t.completed) TextDecoration.LineThrough else TextDecoration.None,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (t.time.isNotBlank()) {
                                                Text(
                                                    text = PersianUtils.formatNumber(t.time, usePersianDigits),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.textMuted
                                                )
                                            }
                                        }
                                    }
                                }

                                // Quick add button in cell
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onAddTaskInSlot(dayKey, slot.id) }
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "افزودن کار به این بازه",
                                        tint = palette.textMuted.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
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
