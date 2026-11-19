package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.TaskItem
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.ExportCenterSheet
import com.example.ui.components.MoveTaskDialog
import com.example.ui.components.PlannerAmbientBackground
import com.example.ui.components.TaskEditorSheet
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeminiMentorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.WeeklyScheduleScreen
import com.example.ui.theme.LocalPlannerPalette
import com.example.ui.viewmodel.PlannerEvent
import com.example.ui.viewmodel.PlannerUiState
import com.example.ui.viewmodel.PlannerViewModel
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute
@Serializable data object WeeklyRoute
@Serializable data object StatsRoute
@Serializable data object ArchiveRoute
@Serializable data object GeminiRoute
@Serializable data object SettingsRoute

private data class NavDestinationItem(
    val route: Any,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private val NAV_ITEMS = listOf(
    NavDestinationItem(DashboardRoute, "داشبورد", Icons.Rounded.Dashboard, "nav_dashboard"),
    NavDestinationItem(WeeklyRoute, "هفتگی", Icons.Rounded.CalendarMonth, "nav_weekly"),
    NavDestinationItem(StatsRoute, "آمار", Icons.Rounded.BarChart, "nav_stats"),
    NavDestinationItem(ArchiveRoute, "آرشیو", Icons.Rounded.Archive, "nav_archive"),
    NavDestinationItem(GeminiRoute, "هوشمند", Icons.Rounded.AutoAwesome, "nav_gemini"),
    NavDestinationItem(SettingsRoute, "تنظیمات", Icons.Rounded.Settings, "nav_settings")
)

@Composable
fun PlannerRootApp(
    uiState: PlannerUiState,
    viewModel: PlannerViewModel,
    windowSizeClass: WindowSizeClass
) {
    val palette = LocalPlannerPalette.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }

    var confettiKey by remember { mutableIntStateOf(0) }
    var confettiGrand by remember { mutableStateOf(false) }

    var editorVisible by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }
    var editorDefaultDay by remember { mutableStateOf(uiState.selectedDayId) }
    var editorDefaultSlot by remember { mutableStateOf<String?>(null) }

    var moveDialogTask by remember { mutableStateOf<TaskItem?>(null) }
    var deleteConfirmTask by remember { mutableStateOf<TaskItem?>(null) }
    var showExportCenter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PlannerEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is PlannerEvent.TriggerConfetti -> {
                    confettiGrand = event.grandCelebration
                    confettiKey++
                }
            }
        }
    }

    val useNavigationRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    val showFab = currentDest?.hasRoute(DashboardRoute::class) == true ||
        currentDest?.hasRoute(WeeklyRoute::class) == true

    fun navigateTo(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    PlannerAmbientBackground {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = palette.accentPrimary)
            }
        } else {
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                floatingActionButton = {
                    if (showFab) {
                        FloatingActionButton(
                            onClick = {
                                editingTask = null
                                editorDefaultDay = uiState.selectedDayId
                                editorDefaultSlot = null
                                editorVisible = true
                            },
                            containerColor = palette.accentPrimary,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("global_add_task_fab")
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "افزودن کار جدید")
                        }
                    }
                },
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = palette.surfaceCard.copy(alpha = 0.96f)
                        ) {
                            NAV_ITEMS.forEach { item ->
                                val selected = currentDest?.hasRoute(item.route::class) == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    alwaysShowLabel = true,
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(
                            containerColor = palette.surfaceCard.copy(alpha = 0.92f)
                        ) {
                            NAV_ITEMS.forEach { item ->
                                val selected = currentDest?.hasRoute(item.route::class) == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTo(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label, maxLines = 1) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = DashboardRoute,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable<DashboardRoute> {
                            DashboardScreen(
                                uiState = uiState,
                                onToggleTask = viewModel::toggleTaskCompletion,
                                onEditTask = { task ->
                                    editingTask = task
                                    editorDefaultDay = task.day
                                    editorDefaultSlot = task.timeSlot.ifBlank { null }
                                    editorVisible = true
                                },
                                onMoveTask = { task -> moveDialogTask = task },
                                onDeleteTask = { task -> deleteConfirmTask = task },
                                onAddTaskToday = { dayId ->
                                    editingTask = null
                                    editorDefaultDay = dayId
                                    editorDefaultSlot = null
                                    editorVisible = true
                                },
                                onUpdateReflection = viewModel::updateReflection,
                                onUpdateWeekTitle = viewModel::setWeekTitle,
                                onCycleTheme = viewModel::cycleTheme,
                                onOpenExportCenter = { showExportCenter = true },
                                onOpenAiMentor = { navigateTo(GeminiRoute) }
                            )
                        }

                        composable<WeeklyRoute> {
                            WeeklyScheduleScreen(
                                uiState = uiState,
                                onSelectDay = viewModel::selectDay,
                                onViewModeChange = viewModel::setViewMode,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onCategoryFilterChange = viewModel::updateCategoryFilter,
                                onPriorityFilterChange = viewModel::updatePriorityFilter,
                                onStatusFilterChange = viewModel::updateStatusFilter,
                                onResetFilters = viewModel::resetFilters,
                                onToggleTask = viewModel::toggleTaskCompletion,
                                onEditTask = { task ->
                                    editingTask = task
                                    editorDefaultDay = task.day
                                    editorDefaultSlot = task.timeSlot.ifBlank { null }
                                    editorVisible = true
                                },
                                onOpenMoveDialog = { task -> moveDialogTask = task },
                                onDropMoveTask = viewModel::moveTask,
                                onDeleteTask = { task -> deleteConfirmTask = task },
                                onAddTaskInSlot = { dayId, slotId ->
                                    editingTask = null
                                    editorDefaultDay = dayId
                                    editorDefaultSlot = slotId
                                    editorVisible = true
                                },
                                onRestoreDentalSchedule = viewModel::restoreDentalSchedule
                            )
                        }

                        composable<StatsRoute> {
                            StatsScreen(uiState = uiState)
                        }

                        composable<ArchiveRoute> {
                            ArchiveScreen(
                                uiState = uiState,
                                onArchiveCurrentWeek = viewModel::archiveCurrentWeek,
                                onRestoreArchive = viewModel::restoreArchive,
                                onRepeatFromArchive = viewModel::repeatFromArchive,
                                onRepeatCurrentWeek = viewModel::repeatCurrentWeek,
                                onDeleteArchive = viewModel::deleteArchive,
                                onUncheckAllTasks = viewModel::uncheckAllTasks,
                                onArchiveAndClearCompleted = viewModel::archiveAndClearCompleted,
                                onClearAllTasks = viewModel::clearAllTasks
                            )
                        }

                        composable<GeminiRoute> {
                            GeminiMentorScreen(
                                uiState = uiState,
                                onSendPreset = viewModel::sendAiPreset,
                                onSendPrompt = { prompt ->
                                    viewModel.sendAiPrompt(displayLabel = prompt, prompt = prompt)
                                },
                                onSelectModel = viewModel::setAiModel,
                                onClearChat = viewModel::clearAiChatHistory
                            )
                        }

                        composable<SettingsRoute> {
                            SettingsScreen(
                                uiState = uiState,
                                onSelectTheme = viewModel::setTheme,
                                onUpdateWeekTitle = viewModel::setWeekTitle,
                                onSelectWeekStart = viewModel::setWeekStart,
                                onSelectViewMode = viewModel::setViewMode,
                                onSetSoundEnabled = viewModel::setSoundEnabled,
                                onSetPersianNumbers = viewModel::setPersianNumbers,
                                onSetCompactView = viewModel::setCompactView,
                                onSelectAiModel = viewModel::setAiModel,
                                onAddCategory = viewModel::addCategory,
                                onDeleteCategory = viewModel::deleteCategory,
                                onOpenExportCenter = { showExportCenter = true },
                                onRestoreDentalSchedule = viewModel::restoreDentalSchedule
                            )
                        }
                    }
                }
            }
        }

        // Confetti Celebration Overlay
        ConfettiOverlay(
            triggerKey = confettiKey,
            grandCelebration = confettiGrand
        )
    }

    if (editorVisible) {
        TaskEditorSheet(
            initialTask = editingTask,
            defaultDayId = editorDefaultDay,
            defaultSlotId = editorDefaultSlot,
            categories = uiState.categories,
            usePersianDigits = uiState.settings.persianNumbers,
            onDismiss = { editorVisible = false },
            onSave = { task, isNew -> viewModel.saveTask(task, isNew) }
        )
    }

    moveDialogTask?.let { task ->
        MoveTaskDialog(
            task = task,
            usePersianDigits = uiState.settings.persianNumbers,
            onDismiss = { moveDialogTask = null },
            onConfirmMove = { dayId, slotId ->
                viewModel.moveTask(task.id, dayId, slotId)
            }
        )
    }

    deleteConfirmTask?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTask = null },
            title = { Text("حذف فعالیت") },
            text = { Text("آیا «${task.title}» از برنامه هفتگی حذف شود؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(task.id)
                        deleteConfirmTask = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmTask = null }) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showExportCenter) {
        ExportCenterSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showExportCenter = false }
        )
    }
}
