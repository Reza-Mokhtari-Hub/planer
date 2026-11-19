package com.example.ai

import android.util.Log
import com.example.model.AiModelOption
import com.example.model.ChatMessage
import com.example.model.ReflectionData
import com.example.model.TaskItem
import com.example.util.PersianUtils
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed interface AiStreamEvent {
    data class Chunk(val accumulatedText: String) : AiStreamEvent
    data class Completed(val finalText: String) : AiStreamEvent
    data class Error(val message: String) : AiStreamEvent
}

@Serializable
private data class AiDayTaskSummary(
    val time: String,
    val title: String,
    val category: String,
    val priority: String,
    val difficulty: String,
    val completed: Boolean
)

@Serializable
private data class AiScheduleSnapshot(
    val weekTitle: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRatePercent: Int,
    val sportTasksCount: Int,
    val studyTasksCount: Int,
    val uniTasksCount: Int,
    val restTasksCount: Int,
    val reflection: ReflectionData,
    val daysSummary: Map<String, List<AiDayTaskSummary>>
)

class DentalAiRepository {

    private val json = Json { prettyPrint = true }

    companion object {
        private const val TAG = "DentalAiRepository"

        private val SYSTEM_INSTRUCTION = """
شما یک مشاور و منتور ارشد فوق‌تخصصی تحصیلی برای دانشجویان پزشکی و دندانپزشکی (به‌ویژه ترم ۵ دندانپزشکی) و متخصص ارگونومی، برنامه‌ریزی عصبی-شناختی و مدیریت زمان هستید.

وظیفه شما:
تحلیل، بهینه‌سازی و پیشنهاد استراتژی‌های عملی و واقع‌گرایانه برای دانشجوی ترم ۵ دندانپزشکی بر اساس اطلاعات واقعی برنامه هفتگی ارائه‌شده.

اصول کلیدی برنامه دانشجوی دندانپزشکی:
۱. بیدارباش و صبحانه: ساعت ۰۶:۰۰ صبح شنبه تا چهارشنبه برای تغذیه مقوی، شادابی و حضور به‌موقع در کلاس ۰۷:۳۰ دانشگاه.
۲. دروس سنگین ترم ۵: فارماکولوژی، ترمیم ۱ و عملی ترمیم، پروتز کامل نظری و عملی، اندودانتیکس، رادیولوژی دهان، ارگونومی و بیماری‌های دهان و سیستمیک.
۳. تایم ثابت ناهار و نماز: همواره ساعت ۱۲:۰۰ تا ۱۳:۰۰ در تمامی روزها.
۴. ۳ جلسه ورزش هفتگی: روزهای جمعه، یک‌شنبه و سه‌شنبه (ساعت ۱۸:۰۰ به بعد) جهت کاهش استرس، تقویت عضلات کمر و گردن دندانپزشک و شادابی ذهنی.
۵. تایم ثابت شام و استراحت: همواره ساعت ۲۱:۳۰ تا ۲۳:۰۰ در تمام روزها و خواب منظم ساعت ۲۳:۰۰.
۶. خستگی بعد از کلینیک و پری‌کلینیک: در نظر گرفتن تایم ریکاوری بعدازظهر و اختصاص بازه شب به دروس نیازمند تمرکز بالا با تکنیک‌های فاصله‌گذاری و مرور فعال (Active Recall & Spaced Repetition).

قوانین مهم:
- پاسخ‌ها باید به زبان فارسی بسیار شیوا، صمیمی، علمی، انگیزه‌بخش، شاد و کاملاً ساختاریافته (با تیترهای Markdown، بولت‌پوینت‌های دقیق و ایموجی‌های مرتبط) ارائه شود.
- شما اجازه ندارید برنامه کاربر را به‌طور خودکار تغییر دهید؛ تمام پیشنهادها را به‌صورت شفاف و گام‌به‌گام به کاربر ارائه دهید تا در صورت تمایل خودش اعمال کند.
        """.trimIndent()
    }

    private val modelsCache = mutableMapOf<String, GenerativeModel>()

    private val defaultModel by lazy {
        createModel(AiModelOption.GEMINI_3_8_FLASH.modelId)
    }

    private fun getModel(modelOption: AiModelOption): GenerativeModel {
        if (modelOption == AiModelOption.GEMINI_3_8_FLASH) return defaultModel
        return modelsCache.getOrPut(modelOption.modelId) {
            createModel(modelOption.modelId)
        }
    }

    private fun createModel(modelId: String): GenerativeModel {
        return Firebase.ai.generativeModel(
            modelName = modelId,
            generationConfig = generationConfig {
                temperature = 0.7f
            },
            systemInstruction = content {
                text(SYSTEM_INSTRUCTION)
            }
        )
    }

    fun buildScheduleContextJson(
        weekTitle: String,
        tasks: List<TaskItem>,
        reflection: ReflectionData
    ): String {
        val total = tasks.size
        val done = tasks.count { it.completed }
        val daysSummary = linkedMapOf<String, List<AiDayTaskSummary>>()
        PersianUtils.DEFAULT_DAYS_ORDER.forEach { dayKey ->
            val dayName = PersianUtils.DAYS_MAP[dayKey]?.fa ?: dayKey
            daysSummary[dayName] = tasks.filter { it.day == dayKey }
                .sortedBy { PersianUtils.parseTimeMinutes(it.time) }
                .map { t ->
                    AiDayTaskSummary(
                        time = t.time,
                        title = t.title,
                        category = t.category,
                        priority = t.priority,
                        difficulty = t.difficulty,
                        completed = t.completed
                    )
                }
        }
        val snapshot = AiScheduleSnapshot(
            weekTitle = weekTitle,
            totalTasks = total,
            completedTasks = done,
            completionRatePercent = if (total == 0) 0 else ((done * 100f) / total).toInt(),
            sportTasksCount = tasks.count { it.category == "sport" },
            studyTasksCount = tasks.count { it.category == "study" },
            uniTasksCount = tasks.count { it.category == "uni" },
            restTasksCount = tasks.count { it.category == "rest" },
            reflection = reflection,
            daysSummary = daysSummary
        )
        return json.encodeToString(snapshot)
    }

    fun streamAnalysisOrChat(
        modelOption: AiModelOption,
        userPrompt: String,
        weekTitle: String,
        tasks: List<TaskItem>,
        reflection: ReflectionData,
        chatHistory: List<ChatMessage>
    ): Flow<AiStreamEvent> = flow {
        val model = getModel(modelOption)
        val scheduleJson = buildScheduleContextJson(weekTitle, tasks, reflection)

        val historyContents = chatHistory
            .filter { it.role == "user" || it.role == "model" }
            .takeLast(10)
            .map { msg ->
                content(role = msg.role) {
                    text(msg.content)
                }
            }

        val fullPrompt = """
اطلاعات واقعی برنامه هفتگی جاری کاربر:
```json
$scheduleJson
```

پرسش یا درخواست کاربر:
$userPrompt
        """.trimIndent()

        val accumulated = StringBuilder()
        try {
            withTimeout(60_000L) {
                val chat = model.startChat(history = historyContents)
                chat.sendMessageStream(fullPrompt).collect { chunk ->
                    val piece = chunk.text.orEmpty()
                    if (piece.isNotEmpty()) {
                        accumulated.append(piece)
                        emit(AiStreamEvent.Chunk(accumulated.toString()))
                    }
                }
            }
            if (accumulated.isNotBlank()) {
                emit(AiStreamEvent.Completed(accumulated.toString()))
            } else {
                emit(AiStreamEvent.Error("پاسخی از مدل هوش مصنوعی دریافت نشد."))
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini streaming timed out", e)
            emit(AiStreamEvent.Error("زمان پاسخگویی هوش مصنوعی به پایان رسید (بیش از ۶۰ ثانیه). لطفاً دوباره تلاش کنید."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed", e)
            val detail = e.localizedMessage ?: "خطای نامشخص در ارتباط با Firebase AI Logic"
            emit(AiStreamEvent.Error("خطا در ارتباط امن با Gemini: $detail"))
        }
    }
}
