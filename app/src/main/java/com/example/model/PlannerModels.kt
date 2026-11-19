package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskItem(
    val id: String,
    val day: String,
    val time: String = "",
    val title: String,
    val description: String = "",
    val category: String = "personal",
    val priority: String = "medium",
    val completed: Boolean = false,
    val difficulty: String = "",
    val timeSlot: String = "",
    val createdAt: String = ""
)

@Serializable
data class CategoryItem(
    val id: String,
    val name: String,
    val color: String,
    val icon: String = "fa-tag"
)

@Serializable
data class ReflectionData(
    val wins: String = "",
    val challenges: String = "",
    val improvements: String = "",
    val rating: Int = 5
)

@Serializable
data class WeekArchiveItem(
    val id: String,
    val date: String,
    val title: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val rate: Int,
    val tasksSnapshot: List<TaskItem> = emptyList(),
    val reflection: ReflectionData? = null
)

@Serializable
data class BackupSettings(
    val theme: String = "theme-energetic-light",
    val weekStart: String = "saturday",
    val soundEnabled: Boolean = true,
    val persianNumbers: Boolean = true,
    val compactView: Boolean = false,
    val viewMode: String = "table"
)

@Serializable
data class PlannerBackupPayload(
    val version: String = "2.0",
    val appName: String = "برنامه‌ریزی و ردیابی هفتگی دندانپزشکی",
    val exportedAt: String = "",
    val currentWeekTitle: String = "برنامه هفتگی ترم ۵ دندانپزشکی",
    val scheduleVersion: String? = "dental-term5-v6.1-sunday-adjusted",
    val settings: BackupSettings = BackupSettings(),
    val categories: List<CategoryItem> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val reflection: ReflectionData = ReflectionData(),
    val archives: List<WeekArchiveItem> = emptyList()
)

enum class CheerfulTheme(
    val id: String,
    val titleFa: String,
    val subtitleFa: String,
    val emoji: String
) {
    ENERGETIC_LIGHT("theme-energetic-light", "شاد و آفتابی", "پرانرژی و سرزنده", "☀️"),
    SPRING_BLOSSOM("theme-spring-blossom", "بهاری و گل‌بهی", "پاستلی شاداب", "🌸"),
    OCEAN_MINT("theme-ocean-mint", "اقیانوس و نعنا", "خنک و باطراوت", "🌊"),
    SUNSHINE_WARM("theme-sunshine-warm", "پرتقالی فوق‌انرژی", "سوپر شارژ و گرم", "⚡");

    companion object {
        fun fromId(id: String): CheerfulTheme = entries.find { it.id == id } ?: ENERGETIC_LIGHT
    }
}

enum class WeekStartOption(val id: String, val labelFa: String) {
    SATURDAY("saturday", "شنبه (تقویم رسمی ایران)"),
    SUNDAY("sunday", "یکشنبه (تقویم غربی)"),
    MONDAY("monday", "دوشنبه (ISO بین‌المللی)");

    companion object {
        fun fromId(id: String): WeekStartOption = entries.find { it.id == id } ?: SATURDAY
    }
}

enum class ViewMode(val id: String, val labelFa: String) {
    TABLE("table", "نمای جدولی (ماتریس)"),
    CARDS("cards", "نمای کارتی (تایم‌لاین)");

    companion object {
        fun fromId(id: String): ViewMode = entries.find { it.id == id } ?: TABLE
    }
}

enum class AiModelOption(val modelId: String, val labelFa: String, val descriptionFa: String) {
    GEMINI_3_8_FLASH("gemini-3.8-flash", "Gemini 3.8 Flash", "پیشنهادی، سریع و دقیق"),
    GEMINI_3_5_FLASH_LITE("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "فوق‌سریع و سبک"),
    GEMINI_4_ARGON("gemini-4-argon", "Gemini 4 Argon", "تحلیل عمیق و استدلال پیچیده");

    companion object {
        fun fromId(id: String): AiModelOption = entries.find { it.modelId == id } ?: GEMINI_3_8_FLASH
    }
}

enum class PdfTemplateType(val labelFa: String, val descFa: String) {
    MATRIX("جدول ماتریسی کامل هفتگی", "تمامی ۷ روز و ۹ بازه زمانی با تفکیک رنگی و ساعات ثابت"),
    CHECKLIST("چک‌لیست ستونی ۷ روزه", "۷ کارت مجزا با کادر تیک‌زدن فیزیکی و اولویت‌ها")
}

enum class PdfOrientation(val labelFa: String) {
    LANDSCAPE("افقی (Landscape)"),
    PORTRAIT("عمودی (Portrait)")
}

data class DayInfo(
    val id: String,
    val fa: String,
    val shortFa: String,
    val en: String,
    val order: Int
)

data class TimeSlotInfo(
    val id: String,
    val title: String,
    val timeRange: String,
    val defaultStartTime: String,
    val colorHex: Long,
    val desc: String,
    val isLunch: Boolean = false
)

data class PlannerSettings(
    val theme: CheerfulTheme = CheerfulTheme.ENERGETIC_LIGHT,
    val currentWeekTitle: String = "برنامه کلاسی، مطالعاتی و ورزشی ترم ۵ دندانپزشکی",
    val weekStart: WeekStartOption = WeekStartOption.SATURDAY,
    val soundEnabled: Boolean = true,
    val persianNumbers: Boolean = true,
    val compactView: Boolean = false,
    val viewMode: ViewMode = ViewMode.TABLE,
    val aiModel: AiModelOption = AiModelOption.GEMINI_3_8_FLASH
)

data class ChatMessage(
    val id: Long = 0,
    val role: String, // "user" or "model" or "error"
    val content: String,
    val presetTag: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)

enum class TaskPriority(val id: String, val labelFa: String) {
    HIGH("high", "بالا (فوری)"),
    MEDIUM("medium", "متوسط"),
    LOW("low", "پایین");

    companion object {
        fun fromId(id: String): TaskPriority = entries.find { it.id == id } ?: MEDIUM
    }
}
