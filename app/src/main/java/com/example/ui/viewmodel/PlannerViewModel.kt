package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiStreamEvent
import com.example.ai.DentalAiRepository
import com.example.data.PlannerRepository
import com.example.data.local.PlannerDatabase
import com.example.data.preferences.SettingsDataStore
import com.example.model.AiModelOption
import com.example.model.CategoryItem
import com.example.model.ChatMessage
import com.example.model.CheerfulTheme
import com.example.model.PdfOrientation
import com.example.model.PdfTemplateType
import com.example.model.PlannerSettings
import com.example.model.ReflectionData
import com.example.model.TaskItem
import com.example.model.ViewMode
import com.example.model.WeekArchiveItem
import com.example.model.WeekStartOption
import com.example.util.ExportManager
import com.example.util.PersianUtils
import com.example.util.SoundFeedbackManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TaskFilterState(
    val query: String = "",
    val categoryId: String = "all",
    val priority: String = "all",
    val status: String = "all"
)

data class PlannerUiState(
    val isLoading: Boolean = true,
    val tasks: List<TaskItem> = emptyList(),
    val filteredTasks: List<TaskItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val reflection: ReflectionData = ReflectionData(),
    val archives: List<WeekArchiveItem> = emptyList(),
    val settings: PlannerSettings = PlannerSettings(),
    val filter: TaskFilterState = TaskFilterState(),
    val selectedDayId: String = PersianUtils.getCurrentDayId(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val streamingAiText: String? = null,
    val isAiGenerating: Boolean = false
)

sealed interface PlannerEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : PlannerEvent
    data class TriggerConfetti(val grandCelebration: Boolean = false) : PlannerEvent
}

class PlannerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PlannerDatabase.getInstance(application)
    private val settingsStore = SettingsDataStore(application)
    private val repository = PlannerRepository(application, database.plannerDao(), settingsStore)
    private val aiRepository = DentalAiRepository()
    private val soundManager = SoundFeedbackManager()
    val exportManager = ExportManager(application)

    private val filterFlow = MutableStateFlow(TaskFilterState())
    private val selectedDayFlow = MutableStateFlow(PersianUtils.getCurrentDayId())
    private val isLoadingFlow = MutableStateFlow(true)
    private val streamingAiTextFlow = MutableStateFlow<String?>(null)
    private val isAiGeneratingFlow = MutableStateFlow(false)

    private val _events = MutableSharedFlow<PlannerEvent>()
    val events = _events.asSharedFlow()

    private var aiJob: Job? = null

    val uiState: StateFlow<PlannerUiState> = combine(
        repository.tasksFlow,
        repository.categoriesFlow,
        repository.reflectionFlow,
        repository.archivesFlow,
        repository.settingsFlow,
        filterFlow,
        selectedDayFlow,
        repository.chatMessagesFlow,
        streamingAiTextFlow,
        isAiGeneratingFlow,
        isLoadingFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val tasks = values[0] as List<TaskItem>
        @Suppress("UNCHECKED_CAST")
        val categories = values[1] as List<CategoryItem>
        val reflection = values[2] as ReflectionData
        @Suppress("UNCHECKED_CAST")
        val archives = values[3] as List<WeekArchiveItem>
        val settings = values[4] as PlannerSettings
        val filter = values[5] as TaskFilterState
        val selectedDay = values[6] as String
        @Suppress("UNCHECKED_CAST")
        val chatMessages = values[7] as List<ChatMessage>
        val streamingText = values[8] as String?
        val isAiGenerating = values[9] as Boolean
        val isLoading = values[10] as Boolean

        val filtered = applyFilter(tasks, categories, filter)

        PlannerUiState(
            isLoading = isLoading,
            tasks = tasks,
            filteredTasks = filtered,
            categories = categories,
            reflection = reflection,
            archives = archives,
            settings = settings,
            filter = filter,
            selectedDayId = selectedDay,
            chatMessages = chatMessages,
            streamingAiText = streamingText,
            isAiGenerating = isAiGenerating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PlannerUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureInitialSeedIfNeeded()
            isLoadingFlow.value = false
        }
    }

    private fun applyFilter(
        tasks: List<TaskItem>,
        categories: List<CategoryItem>,
        filter: TaskFilterState
    ): List<TaskItem> {
        val q = filter.query.trim().lowercase()
        return tasks.filter { t ->
            if (q.isNotEmpty()) {
                val catName = categories.find { it.id == t.category }?.name?.lowercase().orEmpty()
                val matches = t.title.lowercase().contains(q) ||
                    t.description.lowercase().contains(q) ||
                    catName.contains(q)
                if (!matches) return@filter false
            }
            if (filter.categoryId != "all" && t.category != filter.categoryId) return@filter false
            if (filter.priority != "all" && t.priority != filter.priority) return@filter false
            if (filter.status == "pending" && t.completed) return@filter false
            if (filter.status == "completed" && !t.completed) return@filter false
            true
        }
    }

    fun selectDay(dayId: String) {
        selectedDayFlow.value = dayId
    }

    fun updateSearchQuery(query: String) {
        filterFlow.update { it.copy(query = query) }
    }

    fun updateCategoryFilter(categoryId: String) {
        filterFlow.update { it.copy(categoryId = categoryId) }
    }

    fun updatePriorityFilter(priority: String) {
        filterFlow.update { it.copy(priority = priority) }
    }

    fun updateStatusFilter(status: String) {
        filterFlow.update { it.copy(status = status) }
    }

    fun resetFilters() {
        filterFlow.value = TaskFilterState()
    }

    fun toggleTaskCompletion(taskId: String) {
        viewModelScope.launch {
            val nowCompleted = repository.toggleTaskCompletion(taskId)
            val soundOn = uiState.value.settings.soundEnabled
            if (nowCompleted) {
                soundManager.playCompleteChime(soundOn)
                val updatedTasks = uiState.value.tasks.map {
                    if (it.id == taskId) it.copy(completed = true) else it
                }
                val allDone = updatedTasks.isNotEmpty() && updatedTasks.all { it.completed }
                _events.emit(PlannerEvent.TriggerConfetti(grandCelebration = allDone))
                _events.emit(
                    PlannerEvent.ShowMessage(
                        if (allDone) "آفرین! تمام کارهای این هفته تکمیل شدند!" else "کار با موفقیت تکمیل شد!"
                    )
                )
            } else {
                soundManager.playPop(soundOn)
            }
        }
    }

    fun saveTask(task: TaskItem, isNew: Boolean) {
        viewModelScope.launch {
            repository.upsertTask(task)
            soundManager.playPop(uiState.value.settings.soundEnabled)
            _events.emit(
                PlannerEvent.ShowMessage(
                    if (isNew) "کار جدید به برنامه اضافه شد" else "تغییرات کار ذخیره شد"
                )
            )
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            soundManager.playPop(uiState.value.settings.soundEnabled)
            _events.emit(PlannerEvent.ShowMessage("کار از برنامه حذف شد"))
        }
    }

    fun moveTask(taskId: String, newDay: String, newSlotId: String? = null) {
        viewModelScope.launch {
            repository.moveTask(taskId, newDay, newSlotId)
            soundManager.playDrop(uiState.value.settings.soundEnabled)
            val dayName = PersianUtils.DAYS_MAP[newDay]?.fa ?: newDay
            val slotTitle = newSlotId?.let { sId ->
                PersianUtils.TIME_SLOTS.find { it.id == sId }?.title
            }
            val msg = if (slotTitle != null) {
                "کار به «$dayName - $slotTitle» منتقل شد"
            } else {
                "کار به روز $dayName منتقل شد"
            }
            _events.emit(PlannerEvent.ShowMessage(msg))
        }
    }

    fun updateReflection(reflection: ReflectionData) {
        viewModelScope.launch {
            repository.updateReflection(reflection)
        }
    }

    fun setTheme(theme: CheerfulTheme) {
        viewModelScope.launch {
            settingsStore.setTheme(theme)
            _events.emit(PlannerEvent.ShowMessage("پوسته «${theme.titleFa}» فعال شد"))
        }
    }

    fun cycleTheme() {
        val current = uiState.value.settings.theme
        val entries = CheerfulTheme.entries
        val next = entries[(entries.indexOf(current) + 1) % entries.size]
        setTheme(next)
    }

    fun setWeekTitle(title: String) {
        viewModelScope.launch { settingsStore.setCurrentWeekTitle(title) }
    }

    fun setWeekStart(option: WeekStartOption) {
        viewModelScope.launch {
            settingsStore.setWeekStart(option)
            _events.emit(PlannerEvent.ShowMessage("روز شروع هفته تغییر یافت"))
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsStore.setSoundEnabled(enabled)
            if (enabled) soundManager.playCompleteChime(true)
        }
    }

    fun setPersianNumbers(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setPersianNumbers(enabled) }
    }

    fun setCompactView(compact: Boolean) {
        viewModelScope.launch { settingsStore.setCompactView(compact) }
    }

    fun setViewMode(mode: ViewMode) {
        viewModelScope.launch { settingsStore.setViewMode(mode) }
    }

    fun setAiModel(model: AiModelOption) {
        viewModelScope.launch { settingsStore.setAiModel(model) }
    }

    fun addCategory(name: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.addCategory(name, colorHex)
            _events.emit(PlannerEvent.ShowMessage("دسته‌بندی «$name» اضافه شد"))
        }
    }

    fun deleteCategory(catId: String) {
        viewModelScope.launch {
            val ok = repository.deleteCategory(catId)
            _events.emit(
                if (ok) PlannerEvent.ShowMessage("دسته‌بندی حذف شد")
                else PlannerEvent.ShowMessage("حداقل یک دسته‌بندی باید فعال بماند", isError = true)
            )
        }
    }

    fun restoreDentalSchedule() {
        viewModelScope.launch {
            repository.restoreDentalScheduleWithBackup()
            soundManager.playCompleteChime(uiState.value.settings.soundEnabled)
            _events.emit(PlannerEvent.ShowMessage("برنامه جامع ترم ۵ دندانپزشکی بازیابی شد (نسخه قبلی آرشیو گردید)"))
        }
    }

    fun archiveCurrentWeek() {
        viewModelScope.launch {
            repository.archiveCurrentWeek()
            soundManager.playPop(uiState.value.settings.soundEnabled)
            _events.emit(PlannerEvent.ShowMessage("وضعیت هفته جاری در آرشیو ثبت شد"))
        }
    }

    fun restoreArchive(archiveId: String) {
        viewModelScope.launch {
            repository.restoreFromArchive(archiveId)
            _events.emit(PlannerEvent.ShowMessage("برنامه هفته انتخاب‌شده بازیابی شد"))
        }
    }

    fun repeatFromArchive(
        archiveId: String,
        archiveCurrentFirst: Boolean,
        resetCompleted: Boolean,
        resetReflection: Boolean
    ) {
        viewModelScope.launch {
            repository.repeatFromArchive(archiveId, archiveCurrentFirst, resetCompleted, resetReflection)
            soundManager.playPop(uiState.value.settings.soundEnabled)
            _events.emit(PlannerEvent.ShowMessage("برنامه برای هفته جدید تکرار شد"))
        }
    }

    fun repeatCurrentWeek(
        archiveCurrentFirst: Boolean,
        resetCompleted: Boolean,
        resetReflection: Boolean
    ) {
        viewModelScope.launch {
            repository.repeatCurrentWeek(archiveCurrentFirst, resetCompleted, resetReflection)
            soundManager.playPop(uiState.value.settings.soundEnabled)
            _events.emit(PlannerEvent.ShowMessage("کارهای جاری برای هفته جدید تکرار شدند"))
        }
    }

    fun deleteArchive(archiveId: String) {
        viewModelScope.launch {
            repository.deleteArchive(archiveId)
            _events.emit(PlannerEvent.ShowMessage("مورد از آرشیو حذف شد"))
        }
    }

    fun uncheckAllTasks() {
        viewModelScope.launch {
            repository.uncheckAllTasks()
            _events.emit(PlannerEvent.ShowMessage("تمام تیک‌ها به حالت در انتظار برگشتند"))
        }
    }

    fun archiveAndClearCompleted() {
        viewModelScope.launch {
            repository.archiveAndClearCompleted()
            _events.emit(PlannerEvent.ShowMessage("هفته آرشیو و کارهای انجام‌شده پاک‌سازی شدند"))
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
            _events.emit(PlannerEvent.ShowMessage("تمام کارهای هفته پاک شدند"))
        }
    }

    // ==================== JSON BACKUP & EXPORT ====================

    suspend fun buildJsonBackupString(): String = repository.exportToJsonString()

    fun saveJsonToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val raw = repository.exportToJsonString()
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(raw.toByteArray(Charsets.UTF_8))
                    } ?: error("دسترسی به مسیر ذخیره‌سازی ممکن نیست")
                }
                _events.emit(PlannerEvent.ShowMessage("فایل پشتیبان JSON با موفقیت ذخیره شد"))
            } catch (e: Exception) {
                _events.emit(PlannerEvent.ShowMessage("خطا در ذخیره JSON: ${e.localizedMessage}", isError = true))
            }
        }
    }

    fun shareJsonBackup() {
        viewModelScope.launch {
            try {
                val raw = repository.exportToJsonString()
                val uri = withContext(Dispatchers.IO) {
                    exportManager.createShareableExportFile("weekly-planner-backup.json") { out ->
                        out.write(raw.toByteArray(Charsets.UTF_8))
                    }
                }
                exportManager.shareFileUri(uri, "application/json", "اشتراک‌گذاری پشتیبان JSON")
            } catch (e: Exception) {
                _events.emit(PlannerEvent.ShowMessage("خطا در اشتراک‌گذاری JSON: ${e.localizedMessage}", isError = true))
            }
        }
    }

    fun importJsonFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val raw = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader()
                        ?.use { it.readText() } ?: ""
                }
                val result = repository.importFromJsonString(raw)
                result.onSuccess { count ->
                    soundManager.playCompleteChime(uiState.value.settings.soundEnabled)
                    _events.emit(PlannerEvent.ShowMessage("$count وظیفه با موفقیت از فایل JSON وارد شد"))
                }.onFailure { err ->
                    _events.emit(PlannerEvent.ShowMessage(err.message ?: "فایل JSON نامعتبر است", isError = true))
                }
            } catch (e: Exception) {
                _events.emit(PlannerEvent.ShowMessage("خطا در خواندن فایل: ${e.localizedMessage}", isError = true))
            }
        }
    }

    // ==================== PDF & PNG EXPORT ====================

    suspend fun generatePreviewBitmap(
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean
    ): Bitmap = withContext(Dispatchers.Default) {
        val state = uiState.value
        exportManager.renderScheduleBitmap(
            weekTitle = state.settings.currentWeekTitle,
            tasks = state.tasks,
            weekStart = state.settings.weekStart,
            usePersianDigits = state.settings.persianNumbers,
            template = template,
            orientation = orientation,
            includeStats = includeStats,
            includeNotes = includeNotes,
            scaleFactor = 1.1f
        )
    }

    fun savePdfToUri(
        uri: Uri,
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean
    ) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        exportManager.writeSchedulePdf(
                            outputStream = out,
                            weekTitle = state.settings.currentWeekTitle,
                            tasks = state.tasks,
                            weekStart = state.settings.weekStart,
                            usePersianDigits = state.settings.persianNumbers,
                            template = template,
                            orientation = orientation,
                            includeStats = includeStats,
                            includeNotes = includeNotes
                        )
                    } ?: error("خطا در باز کردن فایل مقصد")
                }
                soundManager.playCompleteChime(state.settings.soundEnabled)
                _events.emit(PlannerEvent.TriggerConfetti(grandCelebration = false))
                _events.emit(PlannerEvent.ShowMessage("فایل PDF استاندارد با موفقیت ذخیره شد"))
            } catch (e: Exception) {
                _events.emit(PlannerEvent.ShowMessage("خطا در ساخت PDF: ${e.localizedMessage}", isError = true))
            }
        }
    }

    fun sharePdf(
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean
    ) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val uri = withContext(Dispatchers.IO) {
                    exportManager.createShareableExportFile("weekly-schedule-dental-term5.pdf") { out ->
                        exportManager.writeSchedulePdf(
                            outputStream = out,
                            weekTitle = state.settings.currentWeekTitle,
                            tasks = state.tasks,
                            weekStart = state.settings.weekStart,
                            usePersianDigits = state.settings.persianNumbers,
                            template = template,
                            orientation = orientation,
                            includeStats = includeStats,
                            includeNotes = includeNotes
                        )
                    }
                }
                exportManager.shareFileUri(uri, "application/pdf", "اشتراک‌گذاری فایل PDF برنامه")
            } catch (e: Exception) {
                _events.emit(PlannerEvent.ShowMessage("خطا در اشتراک‌گذاری PDF: ${e.localizedMessage}", isError = true))
            }
        }
    }

    fun savePngToUri(
        uri: Uri,
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean
    ) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                withContext(Dispatchers.IO) {
                    val bmp = exportManager.renderScheduleBitmap(
                        weekTitle = state.settings.currentWeekTitle,
                        tasks = state.tasks,
                        weekStart = state.settings.weekStart,
                        usePersianDigits = state.settings.persianNumbers,
                        template = template,
                        orientation = orientation,
                        includeStats = includeStats,
                        includeNotes = includeNotes,
                        scaleFactor = 1.8f
                    )
                    getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                        exportManager.writeBitmapPng(bmp, out)
                    } ?: error("خطا در ذخیره‌سازی تصویر")
                    bmp.recycle()
                }
                soundManager.playCompleteChime(state.settings.soundEnabled)
                _events.emit(PlannerEvent.ShowMessage("تصویر باکیفیت PNG با موفقیت ذخیره شد"))
            } catch (e: Exception) {
                _events.emit(PlannerEvent.ShowMessage("خطا در ساخت PNG: ${e.localizedMessage}", isError = true))
            }
        }
    }

    fun sharePng(
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean
    ) {
        viewModelScope.launch {
            try {
                val state = uiState.value
                val uri = withContext(Dispatchers.IO) {
                    val bmp = exportManager.renderScheduleBitmap(
                        weekTitle = state.settings.currentWeekTitle,
                        tasks = state.tasks,
                        weekStart = state.settings.weekStart,
                        usePersianDigits = state.settings.persianNumbers,
                        template = template,
                        orientation = orientation,
                        includeStats = includeStats,
                        includeNotes = includeNotes,
                        scaleFactor = 1.8f
                    )
                    val fileUri = exportManager.createShareableExportFile("weekly-schedule-dental-term5.png") { out ->
                        exportManager.writeBitmapPng(bmp, out)
                    }
                    bmp.recycle()
                    fileUri
                }
                exportManager.shareFileUri(uri, "image/png", "اشتراک‌گذاری تصویر برنامه")
            } catch (e: Exception) {
                _events.emit(PlannerEvent.ShowMessage("خطا در اشتراک‌گذاری PNG: ${e.localizedMessage}", isError = true))
            }
        }
    }

    // ==================== GEMINI AI MENTOR ====================

    fun sendAiPreset(presetKey: String) {
        val (title, prompt) = when (presetKey) {
            "comprehensive" -> "تحلیل توازن درسی و سلامت" to
                "یک تحلیل جامع، فوق‌حرفه‌ای و انگیزشی از برنامه هفتگی من به عنوان دانشجوی ترم ۵ دندانپزشکی ارائه بده. تناسب ساعات مطالعه دروس سنگین (فارماکولوژی، ترمیم ۱، اندودانتیکس، پروتز کامل)، پوشش دقیق ۳ جلسه ورزش هفتگی (جمعه، یک‌شنبه، سه‌شنبه)، تایم ثابت ناهار ۱۲ تا ۱۳ و تایم شام ۲۱:۳۰ را بسنج و نقاط قوت و راهکارهای ارتقای عملکردم را بگو."
            "exams" -> "استراتژی امتحانات و کوئیزها" to
                "برنامه مرور فعال و تست‌زنی هوشمند برای موفقیت در امتحانات تئوری و پره‌کلینیک ترم ۵ (به‌ویژه فارماکولوژی، اندو، جراحی و ترمیم) تدوین کن و راهکارهای جمع‌بندی آخر هفته در پنج‌شنبه و جمعه را شرح بده."
            "recovery" -> "ارگونومی، ۳ جلسه ورزش و خواب" to
                "با توجه به ارگونومی کار دندانپزشکی، خستگی عضلات گردن و کمر در پری‌کلینیک و لزوم شادابی، نحوه اجرای ۳ جلسه ورزش هفتگی (جمعه، یک‌شنبه و سه‌شنبه) و رعایت تایم شام و خواب ۲۳:۰۰ را بررسی و توصیه‌های کاربردی ارائه کن."
            "optimize" -> "بهینه‌سازی هوشمند و گلوگاه‌ها" to
                "چیدمان فعلی فعالیت‌ها در روزهای هفته را بررسی کن، گلوگاه‌های زمانی پرفشار و خسته‌کننده را شناسایی کن و پیشنهادهای عملی برای چیدمان سبک‌تر با بالاترین بازدهی بده (بدون تغییر خودکار برنامه)."
            else -> "تحلیل برنامه" to "برنامه هفتگی من را تحلیل کن."
        }
        sendAiPrompt(displayLabel = title, prompt = prompt, presetTag = presetKey)
    }

    fun sendAiPrompt(displayLabel: String, prompt: String, presetTag: String? = null) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || isAiGeneratingFlow.value) return

        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            val state = uiState.value
            val historySnapshot = state.chatMessages
            repository.saveChatMessage(role = "user", content = displayLabel, presetTag = presetTag)

            isAiGeneratingFlow.value = true
            streamingAiTextFlow.value = ""

            aiRepository.streamAnalysisOrChat(
                modelOption = state.settings.aiModel,
                userPrompt = trimmed,
                weekTitle = state.settings.currentWeekTitle,
                tasks = state.tasks,
                reflection = state.reflection,
                chatHistory = historySnapshot
            ).collect { event ->
                when (event) {
                    is AiStreamEvent.Chunk -> {
                        streamingAiTextFlow.value = event.accumulatedText
                    }
                    is AiStreamEvent.Completed -> {
                        streamingAiTextFlow.value = null
                        isAiGeneratingFlow.value = false
                        repository.saveChatMessage(role = "model", content = event.finalText, presetTag = presetTag)
                        soundManager.playCompleteChime(uiState.value.settings.soundEnabled)
                    }
                    is AiStreamEvent.Error -> {
                        streamingAiTextFlow.value = null
                        isAiGeneratingFlow.value = false
                        repository.saveChatMessage(role = "error", content = event.message, presetTag = presetTag)
                        _events.emit(PlannerEvent.ShowMessage("خطا در دریافت پاسخ Gemini", isError = true))
                    }
                }
            }
        }
    }

    fun clearAiChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            _events.emit(PlannerEvent.ShowMessage("تاریخچه گفتگوی Gemini پاک شد"))
        }
    }
}
