import seedPayloadRaw from '../data/weeklyData.json';
import type {
  AiModelId,
  CategoryItem,
  ChatMessage,
  CheerfulThemeId,
  PlannerBackupPayload,
  PlannerSettings,
  ReflectionData,
  TaskItem,
  ViewMode,
  WeekArchiveItem,
  WeekStartOption,
} from '../types/planner';
import { nowIsoTimestamp, resolveSlotFromTime } from './persian';

const STORAGE_KEYS = {
  SEED_DONE: 'dental_planner_seed_done_v1',
  TASKS: 'dental_planner_tasks_v1',
  CATEGORIES: 'dental_planner_categories_v1',
  REFLECTION: 'dental_planner_reflection_v1',
  ARCHIVES: 'dental_planner_archives_v1',
  SETTINGS: 'dental_planner_settings_v1',
  CHAT: 'dental_planner_chat_v1',
} as const;

export interface PersistedPlannerState {
  tasks: TaskItem[];
  categories: CategoryItem[];
  reflection: ReflectionData;
  archives: WeekArchiveItem[];
  settings: PlannerSettings;
  chatMessages: ChatMessage[];
}

const VALID_THEMES: CheerfulThemeId[] = [
  'theme-energetic-light',
  'theme-spring-blossom',
  'theme-ocean-mint',
  'theme-sunshine-warm',
];

function normalizeTheme(id?: string): CheerfulThemeId {
  return VALID_THEMES.includes(id as CheerfulThemeId)
    ? (id as CheerfulThemeId)
    : 'theme-energetic-light';
}

function normalizeWeekStart(id?: string): WeekStartOption {
  if (id === 'sunday' || id === 'monday' || id === 'saturday') return id;
  return 'saturday';
}

function normalizeViewMode(id?: string): ViewMode {
  return id === 'cards' ? 'cards' : 'table';
}

function normalizeAiModel(id?: string): AiModelId {
  if (id === 'gemini-3.1-flash-lite' || id === 'gemini-4-hydrogen-deep') return id;
  return 'gemini-4-hydrogen';
}

function safeJsonParse<T>(raw: string | null, fallback: T): T {
  if (!raw) return fallback;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

export function getSeedPayload(): PlannerBackupPayload {
  return seedPayloadRaw as PlannerBackupPayload;
}

export function loadInitialPlannerState(): PersistedPlannerState {
  const seed = getSeedPayload();
  const seedDone = localStorage.getItem(STORAGE_KEYS.SEED_DONE) === 'true';

  if (!seedDone) {
    const initialSettings: PlannerSettings = {
      theme: normalizeTheme(seed.settings?.theme),
      currentWeekTitle: seed.currentWeekTitle || 'برنامه هفتگی ترم ۵ دندانپزشکی',
      weekStart: normalizeWeekStart(seed.settings?.weekStart),
      soundEnabled: seed.settings?.soundEnabled ?? true,
      persianNumbers: seed.settings?.persianNumbers ?? true,
      compactView: seed.settings?.compactView ?? false,
      viewMode: normalizeViewMode(seed.settings?.viewMode),
      aiModel: 'gemini-4-hydrogen',
    };
    const state: PersistedPlannerState = {
      tasks: seed.tasks ?? [],
      categories: seed.categories ?? [],
      reflection: seed.reflection ?? { wins: '', challenges: '', improvements: '', rating: 5 },
      archives: seed.archives ?? [],
      settings: initialSettings,
      chatMessages: [],
    };
    saveAllPlannerState(state);
    localStorage.setItem(STORAGE_KEYS.SEED_DONE, 'true');
    return state;
  }

  const defaultSettings: PlannerSettings = {
    theme: normalizeTheme(seed.settings?.theme),
    currentWeekTitle: seed.currentWeekTitle || 'برنامه هفتگی ترم ۵ دندانپزشکی',
    weekStart: normalizeWeekStart(seed.settings?.weekStart),
    soundEnabled: seed.settings?.soundEnabled ?? true,
    persianNumbers: seed.settings?.persianNumbers ?? true,
    compactView: seed.settings?.compactView ?? false,
    viewMode: normalizeViewMode(seed.settings?.viewMode),
    aiModel: 'gemini-4-hydrogen',
  };

  const savedSettings = safeJsonParse<Partial<PlannerSettings>>(
    localStorage.getItem(STORAGE_KEYS.SETTINGS),
    {}
  );

  return {
    tasks: safeJsonParse<TaskItem[]>(localStorage.getItem(STORAGE_KEYS.TASKS), seed.tasks ?? []),
    categories: safeJsonParse<CategoryItem[]>(
      localStorage.getItem(STORAGE_KEYS.CATEGORIES),
      seed.categories ?? []
    ),
    reflection: safeJsonParse<ReflectionData>(localStorage.getItem(STORAGE_KEYS.REFLECTION), {
      wins: '',
      challenges: '',
      improvements: '',
      rating: 5,
    }),
    archives: safeJsonParse<WeekArchiveItem[]>(localStorage.getItem(STORAGE_KEYS.ARCHIVES), []),
    settings: {
      ...defaultSettings,
      ...savedSettings,
      theme: normalizeTheme(savedSettings.theme ?? defaultSettings.theme),
      weekStart: normalizeWeekStart(savedSettings.weekStart ?? defaultSettings.weekStart),
      viewMode: normalizeViewMode(savedSettings.viewMode ?? defaultSettings.viewMode),
      aiModel: normalizeAiModel(savedSettings.aiModel ?? defaultSettings.aiModel),
    },
    chatMessages: safeJsonParse<ChatMessage[]>(localStorage.getItem(STORAGE_KEYS.CHAT), []),
  };
}

export function saveAllPlannerState(state: PersistedPlannerState): void {
  localStorage.setItem(STORAGE_KEYS.TASKS, JSON.stringify(state.tasks));
  localStorage.setItem(STORAGE_KEYS.CATEGORIES, JSON.stringify(state.categories));
  localStorage.setItem(STORAGE_KEYS.REFLECTION, JSON.stringify(state.reflection));
  localStorage.setItem(STORAGE_KEYS.ARCHIVES, JSON.stringify(state.archives));
  localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(state.settings));
  localStorage.setItem(STORAGE_KEYS.CHAT, JSON.stringify(state.chatMessages));
}

export function buildJsonExportPayload(state: PersistedPlannerState): PlannerBackupPayload {
  return {
    version: '2.0',
    appName: 'برنامه‌ریزی و ردیابی هفتگی دندانپزشکی',
    exportedAt: nowIsoTimestamp(),
    currentWeekTitle: state.settings.currentWeekTitle,
    scheduleVersion: 'dental-term5-v6.1-web',
    settings: {
      theme: state.settings.theme,
      weekStart: state.settings.weekStart,
      soundEnabled: state.settings.soundEnabled,
      persianNumbers: state.settings.persianNumbers,
      compactView: state.settings.compactView,
      viewMode: state.settings.viewMode,
    },
    categories: state.categories,
    tasks: state.tasks,
    reflection: state.reflection,
    archives: state.archives,
  };
}

export function parseAndValidateBackupJson(
  rawJson: string,
  currentAiModel: AiModelId
): { ok: true; data: Omit<PersistedPlannerState, 'chatMessages'> } | { ok: false; error: string } {
  try {
    const payload = JSON.parse(rawJson) as Partial<PlannerBackupPayload>;
    const tasks = Array.isArray(payload.tasks) ? payload.tasks : [];
    const categories = Array.isArray(payload.categories) ? payload.categories : [];
    if (tasks.length === 0 && categories.length === 0) {
      return { ok: false, error: 'فایل پشتیبان خالی یا نامعتبر است.' };
    }
    const validDays = new Set(['sat', 'sun', 'mon', 'tue', 'wed', 'thu', 'fri']);
    const hasInvalid = tasks.some(
      (t) => !t || typeof t.title !== 'string' || !t.title.trim() || !validDays.has(t.day)
    );
    if (hasInvalid) {
      return { ok: false, error: 'ساختار برخی وظایف در فایل JSON معتبر نیست.' };
    }

    const normalizedTasks: TaskItem[] = tasks.map((t, idx) => ({
      id: t.id?.trim() || `imp_${Date.now()}_${idx}`,
      day: t.day,
      time: t.time || '',
      title: t.title.trim(),
      description: t.description || '',
      category: t.category || 'personal',
      priority: t.priority || 'medium',
      completed: Boolean(t.completed),
      difficulty: t.difficulty || '',
      timeSlot: t.timeSlot?.trim() || resolveSlotFromTime(t.time || ''),
      createdAt: t.createdAt || nowIsoTimestamp(),
    }));

    const reflection: ReflectionData = {
      wins: payload.reflection?.wins ?? '',
      challenges: payload.reflection?.challenges ?? '',
      improvements: payload.reflection?.improvements ?? '',
      rating: Math.min(5, Math.max(1, Number(payload.reflection?.rating ?? 5))),
    };

    const settings: PlannerSettings = {
      theme: normalizeTheme(payload.settings?.theme),
      currentWeekTitle: payload.currentWeekTitle?.trim() || 'برنامه بازیابی‌شده',
      weekStart: normalizeWeekStart(payload.settings?.weekStart),
      soundEnabled: payload.settings?.soundEnabled ?? true,
      persianNumbers: payload.settings?.persianNumbers ?? true,
      compactView: payload.settings?.compactView ?? false,
      viewMode: normalizeViewMode(payload.settings?.viewMode),
      aiModel: currentAiModel,
    };

    return {
      ok: true,
      data: {
        tasks: normalizedTasks,
        categories,
        reflection,
        archives: Array.isArray(payload.archives) ? payload.archives : [],
        settings,
      },
    };
  } catch (err) {
    const msg = err instanceof Error ? err.message : 'خطای خواندن';
    return { ok: false, error: `فرمت فایل JSON صحیح نیست: ${msg}` };
  }
}
