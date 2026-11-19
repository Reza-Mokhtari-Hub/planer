package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.local.ArchiveEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.PlannerDao
import com.example.data.local.ReflectionEntity
import com.example.data.local.TaskEntity
import com.example.data.preferences.SettingsDataStore
import com.example.model.BackupSettings
import com.example.model.CategoryItem
import com.example.model.ChatMessage
import com.example.model.CheerfulTheme
import com.example.model.PlannerBackupPayload
import com.example.model.PlannerSettings
import com.example.model.ReflectionData
import com.example.model.TaskItem
import com.example.model.ViewMode
import com.example.model.WeekArchiveItem
import com.example.model.WeekStartOption
import com.example.util.PersianUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

class PlannerRepository(
    private val context: Context,
    private val dao: PlannerDao,
    private val settingsStore: SettingsDataStore
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    val tasksFlow: Flow<List<TaskItem>> = dao.observeTasks().map { list ->
        list.map { it.toModel() }
    }

    val categoriesFlow: Flow<List<CategoryItem>> = dao.observeCategories().map { list ->
        list.map { it.toModel() }
    }

    val reflectionFlow: Flow<ReflectionData> = dao.observeReflection().map { entity ->
        entity?.toModel() ?: ReflectionData()
    }

    val archivesFlow: Flow<List<WeekArchiveItem>> = dao.observeArchives().map { list ->
        list.map { it.toModel() }
    }

    val chatMessagesFlow: Flow<List<ChatMessage>> = dao.observeChatMessages().map { list ->
        list.map { it.toModel() }
    }

    val settingsFlow: Flow<PlannerSettings> = settingsStore.settingsFlow

    /**
     * Seeds the Room database from assets/weekly_data.json ONLY on the very first run.
     * Subsequent runs preserve all user edits, completions, and archives.
     */
    suspend fun ensureInitialSeedIfNeeded() = withContext(Dispatchers.IO) {
        if (settingsStore.isInitialSeedDone()) return@withContext
        val existingCount = dao.getTaskCount()
        if (existingCount > 0) {
            settingsStore.markInitialSeedDone()
            return@withContext
        }

        val payload = readSeedPayloadFromAssets()
        if (payload != null) {
            dao.replaceFullBackup(
                tasks = payload.tasks.mapIndexed { idx, t -> t.toEntity(sortOrder = idx) },
                categories = payload.categories.map { it.toEntity() },
                reflection = payload.reflection.toEntity(),
                archives = payload.archives.map { it.toEntity() }
            )
            settingsStore.applyAllSettings(
                PlannerSettings(
                    theme = CheerfulTheme.fromId(payload.settings.theme),
                    currentWeekTitle = payload.currentWeekTitle,
                    weekStart = WeekStartOption.fromId(payload.settings.weekStart),
                    soundEnabled = payload.settings.soundEnabled,
                    persianNumbers = payload.settings.persianNumbers,
                    compactView = payload.settings.compactView,
                    viewMode = ViewMode.fromId(payload.settings.viewMode)
                )
            )
            settingsStore.markInitialSeedDone()
        }
    }

    private fun readSeedPayloadFromAssets(): PlannerBackupPayload? {
        return try {
            val raw = context.assets.open("weekly_data.json").bufferedReader().use { it.readText() }
            json.decodeFromString<PlannerBackupPayload>(raw)
        } catch (e: Exception) {
            Log.e("PlannerRepository", "Failed to read weekly_data.json asset", e)
            null
        }
    }

    suspend fun restoreDentalScheduleWithBackup() = withContext(Dispatchers.IO) {
        archiveCurrentWeekInternal("پشتیبان خودکار قبل از بازگردانی برنامه دندانپزشکی")
        val payload = readSeedPayloadFromAssets() ?: return@withContext
        dao.replaceAllTasks(payload.tasks.mapIndexed { idx, t -> t.toEntity(sortOrder = idx) })
        if (payload.categories.isNotEmpty()) {
            dao.insertCategories(payload.categories.map { it.toEntity() })
        }
        settingsStore.setCurrentWeekTitle(payload.currentWeekTitle)
    }

    suspend fun toggleTaskCompletion(taskId: String): Boolean = withContext(Dispatchers.IO) {
        val current = dao.getAllTasksOnce().find { it.id == taskId } ?: return@withContext false
        val newCompleted = !current.completed
        dao.updateTask(current.copy(completed = newCompleted))
        newCompleted
    }

    suspend fun upsertTask(task: TaskItem) = withContext(Dispatchers.IO) {
        val existing = dao.getAllTasksOnce().find { it.id == task.id }
        val resolvedSlot = task.timeSlot.ifBlank { PersianUtils.resolveSlotFromTime(task.time) }
        val finalTask = task.copy(
            id = task.id.ifBlank { "task_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}" },
            timeSlot = resolvedSlot,
            createdAt = task.createdAt.ifBlank { PersianUtils.nowIsoTimestamp() }
        )
        dao.insertTask(finalTask.toEntity(sortOrder = existing?.sortOrder ?: 999))
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        dao.deleteTaskById(taskId)
    }

    suspend fun moveTask(taskId: String, newDay: String, newSlotId: String? = null) = withContext(Dispatchers.IO) {
        val current = dao.getAllTasksOnce().find { it.id == taskId } ?: return@withContext
        val slotInfo = newSlotId?.let { sId -> PersianUtils.TIME_SLOTS.find { it.id == sId } }
        val updated = current.copy(
            day = newDay,
            timeSlot = newSlotId ?: current.timeSlot,
            time = slotInfo?.defaultStartTime ?: current.time
        )
        dao.updateTask(updated)
    }

    suspend fun updateReflection(reflection: ReflectionData) = withContext(Dispatchers.IO) {
        dao.upsertReflection(reflection.toEntity())
    }

    suspend fun addCategory(name: String, colorHex: String) = withContext(Dispatchers.IO) {
        val cat = CategoryEntity(
            id = "cat_${System.currentTimeMillis()}",
            name = name.trim(),
            color = colorHex,
            icon = "fa-tag"
        )
        dao.insertCategory(cat)
    }

    suspend fun deleteCategory(catId: String): Boolean = withContext(Dispatchers.IO) {
        val current = dao.getCategoriesOnce()
        if (current.size <= 1) return@withContext false
        dao.deleteCategoryById(catId)
        true
    }

    suspend fun archiveCurrentWeek(customTitle: String? = null) = withContext(Dispatchers.IO) {
        val settings = settingsStore.settingsFlow.first()
        archiveCurrentWeekInternal(customTitle ?: settings.currentWeekTitle)
    }

    private suspend fun archiveCurrentWeekInternal(title: String) {
        val tasks = dao.getAllTasksOnce().map { it.toModel() }
        if (tasks.isEmpty()) return
        val reflection = dao.getReflectionOnce()?.toModel() ?: ReflectionData()
        val total = tasks.size
        val done = tasks.count { it.completed }
        val pct = if (total == 0) 0 else ((done * 100f) / total).toInt()
        val archive = WeekArchiveItem(
            id = "arch_${System.currentTimeMillis()}",
            date = PersianUtils.nowIsoTimestamp(),
            title = title,
            totalTasks = total,
            completedTasks = done,
            rate = pct,
            tasksSnapshot = tasks,
            reflection = reflection
        )
        dao.insertArchive(archive.toEntity())
    }

    suspend fun restoreFromArchive(archiveId: String) = withContext(Dispatchers.IO) {
        val archive = dao.getArchivesOnce().find { it.id == archiveId }?.toModel() ?: return@withContext
        dao.replaceAllTasks(archive.tasksSnapshot.mapIndexed { idx, t -> t.toEntity(sortOrder = idx) })
        archive.reflection?.let { dao.upsertReflection(it.toEntity()) }
    }

    suspend fun repeatFromArchive(
        archiveId: String,
        archiveCurrentFirst: Boolean,
        resetCompleted: Boolean,
        resetReflection: Boolean
    ) = withContext(Dispatchers.IO) {
        val archive = dao.getArchivesOnce().find { it.id == archiveId }?.toModel() ?: return@withContext
        if (archiveCurrentFirst) {
            val title = settingsStore.settingsFlow.first().currentWeekTitle
            archiveCurrentWeekInternal(title)
        }
        val cloned = archive.tasksSnapshot.mapIndexed { idx, t ->
            t.copy(
                id = "task_${System.currentTimeMillis()}_${idx}_${UUID.randomUUID().toString().take(4)}",
                completed = if (resetCompleted) false else t.completed,
                createdAt = PersianUtils.nowIsoTimestamp()
            ).toEntity(sortOrder = idx)
        }
        dao.replaceAllTasks(cloned)
        if (resetReflection) {
            dao.upsertReflection(ReflectionData("", "", "", 5).toEntity())
        }
    }

    suspend fun repeatCurrentWeek(
        archiveCurrentFirst: Boolean,
        resetCompleted: Boolean,
        resetReflection: Boolean
    ) = withContext(Dispatchers.IO) {
        val currentTasks = dao.getAllTasksOnce().map { it.toModel() }
        if (currentTasks.isEmpty()) return@withContext
        if (archiveCurrentFirst) {
            val title = settingsStore.settingsFlow.first().currentWeekTitle
            archiveCurrentWeekInternal(title)
        }
        val cloned = currentTasks.mapIndexed { idx, t ->
            t.copy(
                id = "task_${System.currentTimeMillis()}_${idx}_${UUID.randomUUID().toString().take(4)}",
                completed = if (resetCompleted) false else t.completed,
                createdAt = PersianUtils.nowIsoTimestamp()
            ).toEntity(sortOrder = idx)
        }
        dao.replaceAllTasks(cloned)
        if (resetReflection) {
            dao.upsertReflection(ReflectionData("", "", "", 5).toEntity())
        }
    }

    suspend fun deleteArchive(archiveId: String) = withContext(Dispatchers.IO) {
        dao.deleteArchiveById(archiveId)
    }

    suspend fun uncheckAllTasks() = withContext(Dispatchers.IO) {
        dao.uncheckAllTasks()
    }

    suspend fun archiveAndClearCompleted() = withContext(Dispatchers.IO) {
        val title = settingsStore.settingsFlow.first().currentWeekTitle
        archiveCurrentWeekInternal(title)
        dao.deleteCompletedTasks()
    }

    suspend fun clearAllTasks() = withContext(Dispatchers.IO) {
        dao.deleteAllTasks()
    }

    suspend fun exportToJsonString(): String = withContext(Dispatchers.IO) {
        val settings = settingsStore.settingsFlow.first()
        val tasks = dao.getAllTasksOnce().map { it.toModel() }
        val categories = dao.getCategoriesOnce().map { it.toModel() }
        val reflection = dao.getReflectionOnce()?.toModel() ?: ReflectionData()
        val archives = dao.getArchivesOnce().map { it.toModel() }

        val payload = PlannerBackupPayload(
            version = "2.0",
            appName = "برنامه‌ریزی و ردیابی هفتگی دندانپزشکی",
            exportedAt = PersianUtils.nowIsoTimestamp(),
            currentWeekTitle = settings.currentWeekTitle,
            scheduleVersion = "dental-term5-v6.1-native",
            settings = BackupSettings(
                theme = settings.theme.id,
                weekStart = settings.weekStart.id,
                soundEnabled = settings.soundEnabled,
                persianNumbers = settings.persianNumbers,
                compactView = settings.compactView,
                viewMode = settings.viewMode.id
            ),
            categories = categories,
            tasks = tasks,
            reflection = reflection,
            archives = archives
        )
        json.encodeToString(payload)
    }

    /**
     * Validates the JSON backup strictly BEFORE modifying existing database records.
     * Returns Result.success(importedTasksCount) or Result.failure(errorMessage).
     */
    suspend fun importFromJsonString(rawJson: String): Result<Int> = withContext(Dispatchers.IO) {
        return@withContext try {
            val payload = json.decodeFromString<PlannerBackupPayload>(rawJson)
            if (payload.tasks.isEmpty() && payload.categories.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("فایل پشتیبان خالی یا نامعتبر است."))
            }
            val validDays = setOf("sat", "sun", "mon", "tue", "wed", "thu", "fri")
            val invalidTask = payload.tasks.any { it.title.isBlank() || it.day !in validDays }
            if (invalidTask) {
                return@withContext Result.failure(IllegalArgumentException("ساختار برخی وظایف در فایل JSON معتبر نیست."))
            }

            dao.replaceFullBackup(
                tasks = payload.tasks.mapIndexed { idx, t ->
                    t.copy(
                        id = t.id.ifBlank { "imp_${System.currentTimeMillis()}_$idx" },
                        timeSlot = t.timeSlot.ifBlank { PersianUtils.resolveSlotFromTime(t.time) }
                    ).toEntity(sortOrder = idx)
                },
                categories = payload.categories.map { it.toEntity() },
                reflection = payload.reflection.toEntity(),
                archives = payload.archives.map { it.toEntity() }
            )
            settingsStore.applyAllSettings(
                PlannerSettings(
                    theme = CheerfulTheme.fromId(payload.settings.theme),
                    currentWeekTitle = payload.currentWeekTitle.ifBlank { "برنامه بازیابی‌شده" },
                    weekStart = WeekStartOption.fromId(payload.settings.weekStart),
                    soundEnabled = payload.settings.soundEnabled,
                    persianNumbers = payload.settings.persianNumbers,
                    compactView = payload.settings.compactView,
                    viewMode = ViewMode.fromId(payload.settings.viewMode)
                )
            )
            Result.success(payload.tasks.size)
        } catch (e: Exception) {
            Log.e("PlannerRepository", "JSON import validation failed", e)
            Result.failure(IllegalArgumentException("فرمت فایل JSON صحیح نیست: ${e.localizedMessage ?: "خطای خواندن"}"))
        }
    }

    suspend fun saveChatMessage(role: String, content: String, presetTag: String? = null) =
        withContext(Dispatchers.IO) {
            dao.insertChatMessage(
                ChatMessageEntity(
                    role = role,
                    content = content,
                    presetTag = presetTag,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        dao.clearChatMessages()
    }

    private fun TaskEntity.toModel() = TaskItem(
        id = id, day = day, time = time, title = title, description = description,
        category = category, priority = priority, completed = completed,
        difficulty = difficulty, timeSlot = timeSlot, createdAt = createdAt
    )

    private fun TaskItem.toEntity(sortOrder: Int) = TaskEntity(
        id = id, day = day, time = time, title = title, description = description,
        category = category, priority = priority, completed = completed,
        difficulty = difficulty, timeSlot = timeSlot, createdAt = createdAt, sortOrder = sortOrder
    )

    private fun CategoryEntity.toModel() = CategoryItem(id = id, name = name, color = color, icon = icon)
    private fun CategoryItem.toEntity() = CategoryEntity(id = id, name = name, color = color, icon = icon)

    private fun ReflectionEntity.toModel() = ReflectionData(
        wins = wins, challenges = challenges, improvements = improvements, rating = rating
    )

    private fun ReflectionData.toEntity() = ReflectionEntity(
        id = 1, wins = wins, challenges = challenges, improvements = improvements, rating = rating.coerceIn(1, 5)
    )

    private fun WeekArchiveItem.toEntity() = ArchiveEntity(
        id = id,
        date = date,
        title = title,
        totalTasks = totalTasks,
        completedTasks = completedTasks,
        rate = rate,
        tasksSnapshotJson = json.encodeToString(tasksSnapshot),
        reflectionJson = reflection?.let { json.encodeToString(it) }
    )

    private fun ArchiveEntity.toModel(): WeekArchiveItem {
        val tasks = try {
            json.decodeFromString<List<TaskItem>>(tasksSnapshotJson)
        } catch (_: Exception) {
            emptyList()
        }
        val ref = reflectionJson?.let {
            try { json.decodeFromString<ReflectionData>(it) } catch (_: Exception) { null }
        }
        return WeekArchiveItem(
            id = id, date = date, title = title, totalTasks = totalTasks,
            completedTasks = completedTasks, rate = rate, tasksSnapshot = tasks, reflection = ref
        )
    }

    private fun ChatMessageEntity.toModel() = ChatMessage(
        id = id, role = role, content = content, presetTag = presetTag, timestampMillis = timestampMillis
    )
}
