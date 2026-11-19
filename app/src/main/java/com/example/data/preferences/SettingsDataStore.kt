package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AiModelOption
import com.example.model.CheerfulTheme
import com.example.model.PlannerSettings
import com.example.model.ViewMode
import com.example.model.WeekStartOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "planner_settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val WEEK_TITLE = stringPreferencesKey("current_week_title")
        val WEEK_START = stringPreferencesKey("week_start")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val PERSIAN_NUMBERS = booleanPreferencesKey("persian_numbers")
        val COMPACT_VIEW = booleanPreferencesKey("compact_view")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val AI_MODEL = stringPreferencesKey("ai_model")
        val INITIAL_SEED_DONE = booleanPreferencesKey("initial_seed_done")
    }

    val settingsFlow: Flow<PlannerSettings> = context.settingsDataStore.data.map { prefs ->
        PlannerSettings(
            theme = CheerfulTheme.fromId(prefs[Keys.THEME] ?: CheerfulTheme.ENERGETIC_LIGHT.id),
            currentWeekTitle = prefs[Keys.WEEK_TITLE]
                ?: "برنامه کلاسی، مطالعاتی و ورزشی ترم ۵ دندانپزشکی — نسخه متعادل و کم‌فشار",
            weekStart = WeekStartOption.fromId(prefs[Keys.WEEK_START] ?: WeekStartOption.SATURDAY.id),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            persianNumbers = prefs[Keys.PERSIAN_NUMBERS] ?: true,
            compactView = prefs[Keys.COMPACT_VIEW] ?: false,
            viewMode = ViewMode.fromId(prefs[Keys.VIEW_MODE] ?: ViewMode.TABLE.id),
            aiModel = AiModelOption.fromId(prefs[Keys.AI_MODEL] ?: AiModelOption.GEMINI_3_8_FLASH.modelId)
        )
    }

    suspend fun isInitialSeedDone(): Boolean {
        return context.settingsDataStore.data.map { it[Keys.INITIAL_SEED_DONE] ?: false }.first()
    }

    suspend fun markInitialSeedDone() {
        context.settingsDataStore.edit { it[Keys.INITIAL_SEED_DONE] = true }
    }

    suspend fun setTheme(theme: CheerfulTheme) {
        context.settingsDataStore.edit { it[Keys.THEME] = theme.id }
    }

    suspend fun setCurrentWeekTitle(title: String) {
        context.settingsDataStore.edit { it[Keys.WEEK_TITLE] = title }
    }

    suspend fun setWeekStart(option: WeekStartOption) {
        context.settingsDataStore.edit { it[Keys.WEEK_START] = option.id }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setPersianNumbers(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.PERSIAN_NUMBERS] = enabled }
    }

    suspend fun setCompactView(compact: Boolean) {
        context.settingsDataStore.edit { it[Keys.COMPACT_VIEW] = compact }
    }

    suspend fun setViewMode(mode: ViewMode) {
        context.settingsDataStore.edit { it[Keys.VIEW_MODE] = mode.id }
    }

    suspend fun setAiModel(model: AiModelOption) {
        context.settingsDataStore.edit { it[Keys.AI_MODEL] = model.modelId }
    }

    suspend fun applyAllSettings(settings: PlannerSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.THEME] = settings.theme.id
            prefs[Keys.WEEK_TITLE] = settings.currentWeekTitle
            prefs[Keys.WEEK_START] = settings.weekStart.id
            prefs[Keys.SOUND_ENABLED] = settings.soundEnabled
            prefs[Keys.PERSIAN_NUMBERS] = settings.persianNumbers
            prefs[Keys.COMPACT_VIEW] = settings.compactView
            prefs[Keys.VIEW_MODE] = settings.viewMode.id
            prefs[Keys.AI_MODEL] = settings.aiModel.modelId
        }
    }
}
