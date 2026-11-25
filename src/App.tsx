import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  Archive,
  BarChart3,
  CalendarDays,
  LayoutDashboard,
  Plus,
  Settings as SettingsIcon,
  Sparkles,
} from 'lucide-react';
import { ConfirmDialog } from './components/ConfirmDialog';
import { ConfettiOverlay } from './components/ConfettiOverlay';
import { ExportCenterSheet } from './components/ExportCenterSheet';
import { MoveTaskDialog } from './components/MoveTaskDialog';
import { TaskEditorSheet } from './components/TaskEditorSheet';
import { ArchiveScreen } from './screens/ArchiveScreen';
import { DashboardScreen } from './screens/DashboardScreen';
import { GeminiMentorScreen } from './screens/GeminiMentorScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { StatsScreen } from './screens/StatsScreen';
import { WeeklyScheduleScreen } from './screens/WeeklyScheduleScreen';
import {
  CHEERFUL_THEMES,
  type AiModelId,
  type ChatMessage,
  type CheerfulThemeId,
  type NavTabId,
  type ReflectionData,
  type TaskFilterState,
  type TaskItem,
  type ViewMode,
  type WeekArchiveItem,
  type WeekStartOption,
} from './types/planner';
import {
  DAYS_MAP,
  TIME_SLOTS,
  getCurrentDayId,
  nowIsoTimestamp,
  resolveSlotFromTime,
} from './utils/persian';
import { playCompleteChime, playDrop, playPop } from './utils/sound';
import {
  buildJsonExportPayload,
  getSeedPayload,
  loadInitialPlannerState,
  parseAndValidateBackupJson,
  saveAllPlannerState,
  type PersistedPlannerState,
} from './utils/storage';
import { paletteForTheme } from './utils/theme';

const NAV_ITEMS: { id: NavTabId; label: string; icon: typeof LayoutDashboard }[] = [
  { id: 'dashboard', label: 'داشبورد', icon: LayoutDashboard },
  { id: 'weekly', label: 'هفتگی', icon: CalendarDays },
  { id: 'stats', label: 'آمار', icon: BarChart3 },
  { id: 'archive', label: 'آرشیو', icon: Archive },
  { id: 'gemini', label: 'هوشمند', icon: Sparkles },
  { id: 'settings', label: 'تنظیمات', icon: SettingsIcon },
];

export function App() {
  const [store, setStore] = useState<PersistedPlannerState>(() => loadInitialPlannerState());
  const [activeTab, setActiveTab] = useState<NavTabId>('dashboard');
  const [selectedDayId, setSelectedDayId] = useState<string>(() => getCurrentDayId());
  const [filter, setFilter] = useState<TaskFilterState>({
    query: '',
    categoryId: 'all',
    priority: 'all',
    status: 'all',
  });

  const [streamingAiText, setStreamingAiText] = useState<string | null>(null);
  const [isAiGenerating, setIsAiGenerating] = useState(false);
  const aiAbortRef = useRef<AbortController | null>(null);

  const [confettiKey, setConfettiKey] = useState(0);
  const [confettiGrand, setConfettiGrand] = useState(false);
  const [toast, setToast] = useState<{ message: string; isError: boolean } | null>(null);
  const toastTimerRef = useRef<number | null>(null);

  // Modals
  const [editorState, setEditorState] = useState<{
    visible: boolean;
    task: TaskItem | null;
    defaultDay: string;
    defaultSlot: string | null;
  }>({
    visible: false,
    task: null,
    defaultDay: getCurrentDayId(),
    defaultSlot: null,
  });
  const [moveDialogTask, setMoveDialogTask] = useState<TaskItem | null>(null);
  const [deleteConfirmTask, setDeleteConfirmTask] = useState<TaskItem | null>(null);
  const [showExportCenter, setShowExportCenter] = useState(false);

  useEffect(() => {
    saveAllPlannerState(store);
  }, [store]);

  const palette = useMemo(() => paletteForTheme(store.settings.theme), [store.settings.theme]);

  const showToast = useCallback((message: string, isError = false) => {
    setToast({ message, isError });
    if (toastTimerRef.current) window.clearTimeout(toastTimerRef.current);
    toastTimerRef.current = window.setTimeout(() => setToast(null), 3200);
  }, []);

  const triggerConfetti = useCallback((grand = false) => {
    setConfettiGrand(grand);
    setConfettiKey((k) => k + 1);
  }, []);

  const filteredTasks = useMemo(() => {
    const q = filter.query.trim().toLowerCase();
    return store.tasks.filter((t) => {
      if (q) {
        const catName =
          store.categories.find((c) => c.id === t.category)?.name.toLowerCase() ?? '';
        const matches =
          t.title.toLowerCase().includes(q) ||
          t.description.toLowerCase().includes(q) ||
          catName.includes(q);
        if (!matches) return false;
      }
      if (filter.categoryId !== 'all' && t.category !== filter.categoryId) return false;
      if (filter.priority !== 'all' && t.priority !== filter.priority) return false;
      if (filter.status === 'pending' && t.completed) return false;
      if (filter.status === 'completed' && !t.completed) return false;
      return true;
    });
  }, [store.tasks, store.categories, filter]);

  // Task Actions
  const handleToggleTask = (taskId: string) => {
    const target = store.tasks.find((t) => t.id === taskId);
    if (!target) return;
    const nowCompleted = !target.completed;
    const updatedTasks = store.tasks.map((t) =>
      t.id === taskId ? { ...t, completed: nowCompleted } : t
    );
    setStore((prev) => ({ ...prev, tasks: updatedTasks }));

    if (nowCompleted) {
      playCompleteChime(store.settings.soundEnabled);
      const allDone = updatedTasks.length > 0 && updatedTasks.every((t) => t.completed);
      triggerConfetti(allDone);
      showToast(
        allDone ? 'آفرین! تمام کارهای این هفته تکمیل شدند!' : 'کار با موفقیت تکمیل شد!'
      );
    } else {
      playPop(store.settings.soundEnabled);
    }
  };

  const handleSaveTask = (task: TaskItem, isNew: boolean) => {
    const resolvedSlot = task.timeSlot.trim() || resolveSlotFromTime(task.time);
    const finalTask: TaskItem = {
      ...task,
      id:
        task.id.trim() ||
        `task_${Date.now()}_${Math.random().toString(36).slice(2, 7)}`,
      timeSlot: resolvedSlot,
      createdAt: task.createdAt.trim() || nowIsoTimestamp(),
    };
    setStore((prev) => {
      const exists = prev.tasks.some((t) => t.id === finalTask.id);
      const nextTasks = exists
        ? prev.tasks.map((t) => (t.id === finalTask.id ? finalTask : t))
        : [...prev.tasks, finalTask];
      return { ...prev, tasks: nextTasks };
    });
    playPop(store.settings.soundEnabled);
    showToast(isNew ? 'کار جدید به برنامه اضافه شد' : 'تغییرات کار ذخیره شد');
  };

  const handleDeleteTask = (taskId: string) => {
    setStore((prev) => ({
      ...prev,
      tasks: prev.tasks.filter((t) => t.id !== taskId),
    }));
    playPop(store.settings.soundEnabled);
    showToast('کار از برنامه حذف شد');
  };

  const handleMoveTask = (
    taskId: string,
    newDay: string,
    newSlotId?: string | null
  ) => {
    const slotInfo = newSlotId ? TIME_SLOTS.find((s) => s.id === newSlotId) : undefined;
    setStore((prev) => ({
      ...prev,
      tasks: prev.tasks.map((t) => {
        if (t.id !== taskId) return t;
        return {
          ...t,
          day: newDay,
          timeSlot: newSlotId || t.timeSlot,
          time: slotInfo?.defaultStartTime ?? t.time,
        };
      }),
    }));
    playDrop(store.settings.soundEnabled);
    const dayName = DAYS_MAP[newDay]?.fa ?? newDay;
    const msg = slotInfo
      ? `کار به «${dayName} - ${slotInfo.title}» منتقل شد`
      : `کار به روز ${dayName} منتقل شد`;
    showToast(msg);
  };

  const handleUpdateReflection = (reflection: ReflectionData) => {
    setStore((prev) => ({ ...prev, reflection }));
  };

  const handleUpdateWeekTitle = (title: string) => {
    setStore((prev) => ({
      ...prev,
      settings: { ...prev.settings, currentWeekTitle: title },
    }));
  };

  const handleSelectTheme = (theme: CheerfulThemeId) => {
    setStore((prev) => ({ ...prev, settings: { ...prev.settings, theme } }));
    const info = CHEERFUL_THEMES.find((t) => t.id === theme);
    if (info) showToast(`پوسته «${info.titleFa}» فعال شد`);
  };

  const handleCycleTheme = () => {
    const idx = CHEERFUL_THEMES.findIndex((t) => t.id === store.settings.theme);
    const next = CHEERFUL_THEMES[(idx + 1) % CHEERFUL_THEMES.length];
    handleSelectTheme(next.id);
  };

  const handleSelectWeekStart = (weekStart: WeekStartOption) => {
    setStore((prev) => ({ ...prev, settings: { ...prev.settings, weekStart } }));
    showToast('روز شروع هفته تغییر یافت');
  };

  const handleSetSoundEnabled = (soundEnabled: boolean) => {
    setStore((prev) => ({ ...prev, settings: { ...prev.settings, soundEnabled } }));
    if (soundEnabled) playCompleteChime(true);
  };

  const handleSetPersianNumbers = (persianNumbers: boolean) => {
    setStore((prev) => ({ ...prev, settings: { ...prev.settings, persianNumbers } }));
  };

  const handleSetCompactView = (compactView: boolean) => {
    setStore((prev) => ({ ...prev, settings: { ...prev.settings, compactView } }));
  };

  const handleSelectViewMode = (viewMode: ViewMode) => {
    setStore((prev) => ({ ...prev, settings: { ...prev.settings, viewMode } }));
  };

  const handleSelectAiModel = (aiModel: AiModelId) => {
    setStore((prev) => ({ ...prev, settings: { ...prev.settings, aiModel } }));
  };

  const handleAddCategory = (name: string, colorHex: string) => {
    const trimmed = name.trim();
    if (!trimmed) return;
    setStore((prev) => ({
      ...prev,
      categories: [
        ...prev.categories,
        { id: `cat_${Date.now()}`, name: trimmed, color: colorHex, icon: 'fa-tag' },
      ],
    }));
    showToast(`دسته‌بندی «${trimmed}» اضافه شد`);
  };

  const handleDeleteCategory = (catId: string) => {
    if (store.categories.length <= 1) {
      showToast('حداقل یک دسته‌بندی باید فعال بماند', true);
      return;
    }
    setStore((prev) => ({
      ...prev,
      categories: prev.categories.filter((c) => c.id !== catId),
    }));
    showToast('دسته‌بندی حذف شد');
  };

  const createArchiveSnapshot = (
    state: PersistedPlannerState,
    customTitle?: string
  ): WeekArchiveItem | null => {
    if (state.tasks.length === 0) return null;
    const total = state.tasks.length;
    const done = state.tasks.filter((t) => t.completed).length;
    const rate = total === 0 ? 0 : Math.round((done * 100) / total);
    return {
      id: `arch_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
      date: nowIsoTimestamp(),
      title: customTitle ?? state.settings.currentWeekTitle,
      totalTasks: total,
      completedTasks: done,
      rate,
      tasksSnapshot: state.tasks,
      reflection: state.reflection,
    };
  };

  const handleRestoreDentalSchedule = () => {
    const seed = getSeedPayload();
    setStore((prev) => {
      const backup = createArchiveSnapshot(
        prev,
        'پشتیبان خودکار قبل از بازگردانی برنامه دندانپزشکی'
      );
      return {
        ...prev,
        tasks: seed.tasks ?? [],
        categories: seed.categories?.length ? seed.categories : prev.categories,
        archives: backup ? [backup, ...prev.archives] : prev.archives,
        settings: {
          ...prev.settings,
          currentWeekTitle: seed.currentWeekTitle || prev.settings.currentWeekTitle,
        },
      };
    });
    playCompleteChime(store.settings.soundEnabled);
    showToast('برنامه جامع ترم ۵ دندانپزشکی بازیابی شد (نسخه قبلی آرشیو گردید)');
  };

  const handleArchiveCurrentWeek = () => {
    setStore((prev) => {
      const snap = createArchiveSnapshot(prev);
      if (!snap) return prev;
      return { ...prev, archives: [snap, ...prev.archives] };
    });
    playPop(store.settings.soundEnabled);
    showToast('وضعیت هفته جاری در آرشیو ثبت شد');
  };

  const handleRestoreArchive = (archiveId: string) => {
    setStore((prev) => {
      const arch = prev.archives.find((a) => a.id === archiveId);
      if (!arch) return prev;
      return {
        ...prev,
        tasks: arch.tasksSnapshot,
        reflection: arch.reflection ?? prev.reflection,
      };
    });
    showToast('برنامه هفته انتخاب‌شده بازیابی شد');
  };

  const handleRepeatFromArchive = (
    archiveId: string,
    archiveCurrentFirst: boolean,
    resetCompleted: boolean,
    resetReflection: boolean
  ) => {
    setStore((prev) => {
      const arch = prev.archives.find((a) => a.id === archiveId);
      if (!arch) return prev;
      const snap = archiveCurrentFirst ? createArchiveSnapshot(prev) : null;
      const now = Date.now();
      const cloned = arch.tasksSnapshot.map((t, idx) => ({
        ...t,
        id: `task_${now}_${idx}_${Math.random().toString(36).slice(2, 6)}`,
        completed: resetCompleted ? false : t.completed,
        createdAt: nowIsoTimestamp(),
      }));
      return {
        ...prev,
        tasks: cloned,
        archives: snap ? [snap, ...prev.archives] : prev.archives,
        reflection: resetReflection
          ? { wins: '', challenges: '', improvements: '', rating: 5 }
          : prev.reflection,
      };
    });
    playPop(store.settings.soundEnabled);
    showToast('برنامه برای هفته جدید تکرار شد');
  };

  const handleRepeatCurrentWeek = (
    archiveCurrentFirst: boolean,
    resetCompleted: boolean,
    resetReflection: boolean
  ) => {
    setStore((prev) => {
      if (prev.tasks.length === 0) return prev;
      const snap = archiveCurrentFirst ? createArchiveSnapshot(prev) : null;
      const now = Date.now();
      const cloned = prev.tasks.map((t, idx) => ({
        ...t,
        id: `task_${now}_${idx}_${Math.random().toString(36).slice(2, 6)}`,
        completed: resetCompleted ? false : t.completed,
        createdAt: nowIsoTimestamp(),
      }));
      return {
        ...prev,
        tasks: cloned,
        archives: snap ? [snap, ...prev.archives] : prev.archives,
        reflection: resetReflection
          ? { wins: '', challenges: '', improvements: '', rating: 5 }
          : prev.reflection,
      };
    });
    playPop(store.settings.soundEnabled);
    showToast('کارهای جاری برای هفته جدید تکرار شدند');
  };

  const handleDeleteArchive = (archiveId: string) => {
    setStore((prev) => ({
      ...prev,
      archives: prev.archives.filter((a) => a.id !== archiveId),
    }));
    showToast('مورد از آرشیو حذف شد');
  };

  const handleUncheckAllTasks = () => {
    setStore((prev) => ({
      ...prev,
      tasks: prev.tasks.map((t) => ({ ...t, completed: false })),
    }));
    showToast('تمام تیک‌ها به حالت در انتظار برگشتند');
  };

  const handleArchiveAndClearCompleted = () => {
    setStore((prev) => {
      const snap = createArchiveSnapshot(prev);
      return {
        ...prev,
        tasks: prev.tasks.filter((t) => !t.completed),
        archives: snap ? [snap, ...prev.archives] : prev.archives,
      };
    });
    showToast('هفته آرشیو و کارهای انجام‌شده پاک‌سازی شدند');
  };

  const handleClearAllTasks = () => {
    setStore((prev) => ({ ...prev, tasks: [] }));
    showToast('تمام کارهای هفته پاک شدند');
  };

  const handleImportJsonString = (raw: string): boolean => {
    const result = parseAndValidateBackupJson(raw, store.settings.aiModel);
    if (!result.ok) {
      showToast(result.error, true);
      return false;
    }
    setStore((prev) => ({
      ...prev,
      ...result.data,
    }));
    playCompleteChime(store.settings.soundEnabled);
    showToast(`${result.data.tasks.length} وظیفه با موفقیت از فایل JSON وارد شد`);
    return true;
  };

  // Gemini AI Mentor
  const sendAiPrompt = async (displayLabel: string, prompt: string, presetTag?: string) => {
    const trimmed = prompt.trim();
    if (!trimmed || isAiGenerating) return;

    aiAbortRef.current?.abort();
    const controller = new AbortController();
    aiAbortRef.current = controller;

    const historySnapshot = store.chatMessages;
    const userMsg: ChatMessage = {
      id: `msg_${Date.now()}_u`,
      role: 'user',
      content: displayLabel,
      presetTag,
      timestampMillis: Date.now(),
    };

    setStore((prev) => ({
      ...prev,
      chatMessages: [...prev.chatMessages, userMsg],
    }));
    setIsAiGenerating(true);
    setStreamingAiText('');

    try {
      const response = await fetch('/api/ai/stream', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        signal: controller.signal,
        body: JSON.stringify({
          modelId: store.settings.aiModel,
          userPrompt: trimmed,
          weekTitle: store.settings.currentWeekTitle,
          tasks: store.tasks,
          reflection: store.reflection,
          chatHistory: historySnapshot,
        }),
      });

      if (!response.ok || !response.body) {
        throw new Error(`خطای سرور (${response.status})`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder('utf-8');
      let buffer = '';
      let finalReceived = false;

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const events = buffer.split('\n\n');
        buffer = events.pop() ?? '';

        for (const evt of events) {
          const line = evt.split('\n').find((l) => l.startsWith('data: '));
          if (!line) continue;
          const payload = JSON.parse(line.slice(6)) as {
            type: 'chunk' | 'done' | 'error';
            text?: string;
            finalText?: string;
            message?: string;
          };

          if (payload.type === 'chunk' && typeof payload.text === 'string') {
            setStreamingAiText(payload.text);
          } else if (payload.type === 'done' && typeof payload.finalText === 'string') {
            finalReceived = true;
            setStreamingAiText(null);
            setIsAiGenerating(false);
            const modelMsg: ChatMessage = {
              id: `msg_${Date.now()}_m`,
              role: 'model',
              content: payload.finalText,
              presetTag,
              timestampMillis: Date.now(),
            };
            setStore((prev) => ({
              ...prev,
              chatMessages: [...prev.chatMessages, modelMsg],
            }));
            playCompleteChime(store.settings.soundEnabled);
          } else if (payload.type === 'error') {
            finalReceived = true;
            setStreamingAiText(null);
            setIsAiGenerating(false);
            const errMsg: ChatMessage = {
              id: `msg_${Date.now()}_e`,
              role: 'error',
              content: payload.message || 'خطا در ارتباط با Gemini',
              presetTag,
              timestampMillis: Date.now(),
            };
            setStore((prev) => ({
              ...prev,
              chatMessages: [...prev.chatMessages, errMsg],
            }));
            showToast('خطا در دریافت پاسخ Gemini', true);
          }
        }
      }

      if (!finalReceived) {
        setStreamingAiText(null);
        setIsAiGenerating(false);
      }
    } catch (err) {
      if ((err as Error).name === 'AbortError') return;
      setStreamingAiText(null);
      setIsAiGenerating(false);
      const errMsg: ChatMessage = {
        id: `msg_${Date.now()}_e`,
        role: 'error',
        content:
          err instanceof Error
            ? `خطا در ارتباط امن با Gemini: ${err.message}`
            : 'خطا در ارتباط امن با Gemini',
        presetTag,
        timestampMillis: Date.now(),
      };
      setStore((prev) => ({
        ...prev,
        chatMessages: [...prev.chatMessages, errMsg],
      }));
      showToast('خطا در دریافت پاسخ Gemini', true);
    }
  };

  const handleSendPreset = (presetKey: string) => {
    const presetMap: Record<string, [string, string]> = {
      comprehensive: [
        'تحلیل توازن درسی و سلامت',
        'یک تحلیل جامع، فوق‌حرفه‌ای و انگیزشی از برنامه هفتگی من به عنوان دانشجوی ترم ۵ دندانپزشکی ارائه بده. تناسب ساعات مطالعه دروس سنگین (فارماکولوژی، ترمیم ۱، اندودانتیکس، پروتز کامل)، پوشش دقیق ۳ جلسه ورزش هفتگی (جمعه، یک‌شنبه، سه‌شنبه)، تایم ثابت ناهار ۱۲ تا ۱۳ و تایم شام ۲۱:۳۰ را بسنج و نقاط قوت و راهکارهای ارتقای عملکردم را بگو.',
      ],
      exams: [
        'استراتژی امتحانات و کوئیزها',
        'برنامه مرور فعال و تست‌زنی هوشمند برای موفقیت در امتحانات تئوری و پره‌کلینیک ترم ۵ (به‌ویژه فارماکولوژی، اندو، جراحی و ترمیم) تدوین کن و راهکارهای جمع‌بندی آخر هفته در پنج‌شنبه و جمعه را شرح بده.',
      ],
      recovery: [
        'ارگونومی، ۳ جلسه ورزش و خواب',
        'با توجه به ارگونومی کار دندانپزشکی، خستگی عضلات گردن و کمر در پری‌کلینیک و لزوم شادابی، نحوه اجرای ۳ جلسه ورزش هفتگی (جمعه، یک‌شنبه و سه‌شنبه) و رعایت تایم شام و خواب ۲۳:۰۰ را بررسی و توصیه‌های کاربردی ارائه کن.',
      ],
      optimize: [
        'بهینه‌سازی هوشمند و گلوگاه‌ها',
        'چیدمان فعلی فعالیت‌ها در روزهای هفته را بررسی کن، گلوگاه‌های زمانی پرفشار و خسته‌کننده را شناسایی کن و پیشنهادهای عملی برای چیدمان سبک‌تر با بالاترین بازدهی بده (بدون تغییر خودکار برنامه).',
      ],
    };
    const [title, prompt] = presetMap[presetKey] ?? [
      'تحلیل برنامه',
      'برنامه هفتگی من را تحلیل کن.',
    ];
    sendAiPrompt(title, prompt, presetKey);
  };

  const handleClearChat = () => {
    setStore((prev) => ({ ...prev, chatMessages: [] }));
    showToast('تاریخچه گفتگوی Gemini پاک شد');
  };

  const showFab = activeTab === 'dashboard' || activeTab === 'weekly';

  return (
    <div
      className="relative min-h-screen w-full overflow-x-hidden transition-colors duration-300"
      style={{ backgroundColor: palette.backgroundMain, color: palette.textMain }}
    >
      {/* Ambient Radial Glow Background */}
      <div className="pointer-events-none fixed inset-0 overflow-hidden" aria-hidden="true">
        <div
          className="absolute -top-40 right-[-10%] h-[520px] w-[520px] rounded-full blur-3xl"
          style={{ background: `radial-gradient(circle, ${palette.glowTopStart}, transparent 70%)` }}
        />
        <div
          className="absolute top-24 left-[-8%] h-[440px] w-[440px] rounded-full blur-3xl"
          style={{ background: `radial-gradient(circle, ${palette.glowTopEnd}, transparent 70%)` }}
        />
        <div
          className="absolute -bottom-44 left-1/2 h-[480px] w-[480px] -translate-x-1/2 rounded-full blur-3xl"
          style={{ background: `radial-gradient(circle, ${palette.glowBottom}, transparent 70%)` }}
        />
      </div>

      {/* Main Layout */}
      <div className="relative z-10 flex min-h-screen">
        {/* Desktop Navigation Rail (RTL right side) */}
        <aside
          className="hidden w-24 shrink-0 flex-col items-center gap-2 border-l py-5 md:flex lg:w-52 lg:items-stretch lg:px-3"
          style={{
            backgroundColor: `${palette.surfaceCard}EB`,
            borderColor: palette.borderColor,
          }}
        >
          <div className="mb-3 hidden px-2 lg:block">
            <p className="text-sm font-extrabold" style={{ color: palette.textHeading }}>
              برنامه‌ریزی هفتگی
            </p>
            <p className="mt-0.5 text-[11px]" style={{ color: palette.textMuted }}>
              ترم ۵ دندانپزشکی
            </p>
          </div>

          {NAV_ITEMS.map((item) => {
            const IconComp = item.icon;
            const selected = activeTab === item.id;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => setActiveTab(item.id)}
                className="flex flex-col items-center justify-center gap-1 rounded-2xl px-2 py-2.5 text-xs font-bold transition-colors lg:flex-row lg:justify-start lg:gap-3 lg:px-3.5"
                style={{
                  backgroundColor: selected ? `${palette.accentPrimary}1F` : 'transparent',
                  color: selected ? palette.accentPrimary : palette.textMuted,
                }}
              >
                <IconComp size={20} />
                <span className="whitespace-nowrap">{item.label}</span>
              </button>
            );
          })}
        </aside>

        {/* Active Screen Viewport */}
        <main className="min-w-0 flex-1">
          {activeTab === 'dashboard' && (
            <DashboardScreen
              tasks={store.tasks}
              categories={store.categories}
              reflection={store.reflection}
              settings={store.settings}
              palette={palette}
              onToggleTask={handleToggleTask}
              onEditTask={(task) =>
                setEditorState({
                  visible: true,
                  task,
                  defaultDay: task.day,
                  defaultSlot: task.timeSlot || null,
                })
              }
              onMoveTask={(task) => setMoveDialogTask(task)}
              onDeleteTask={(task) => setDeleteConfirmTask(task)}
              onAddTaskToday={(dayId) =>
                setEditorState({
                  visible: true,
                  task: null,
                  defaultDay: dayId,
                  defaultSlot: null,
                })
              }
              onUpdateReflection={handleUpdateReflection}
              onUpdateWeekTitle={handleUpdateWeekTitle}
              onCycleTheme={handleCycleTheme}
              onOpenExportCenter={() => setShowExportCenter(true)}
              onOpenAiMentor={() => setActiveTab('gemini')}
            />
          )}

          {activeTab === 'weekly' && (
            <WeeklyScheduleScreen
              tasks={store.tasks}
              filteredTasks={filteredTasks}
              categories={store.categories}
              settings={store.settings}
              filter={filter}
              selectedDayId={selectedDayId}
              palette={palette}
              onSelectDay={setSelectedDayId}
              onViewModeChange={handleSelectViewMode}
              onSearchQueryChange={(q) => setFilter((f) => ({ ...f, query: q }))}
              onCategoryFilterChange={(categoryId) =>
                setFilter((f) => ({ ...f, categoryId }))
              }
              onPriorityFilterChange={(priority) => setFilter((f) => ({ ...f, priority }))}
              onStatusFilterChange={(status) => setFilter((f) => ({ ...f, status }))}
              onResetFilters={() =>
                setFilter({ query: '', categoryId: 'all', priority: 'all', status: 'all' })
              }
              onToggleTask={handleToggleTask}
              onEditTask={(task) =>
                setEditorState({
                  visible: true,
                  task,
                  defaultDay: task.day,
                  defaultSlot: task.timeSlot || null,
                })
              }
              onOpenMoveDialog={(task) => setMoveDialogTask(task)}
              onDropMoveTask={handleMoveTask}
              onDeleteTask={(task) => setDeleteConfirmTask(task)}
              onAddTaskInSlot={(dayId, slotId) =>
                setEditorState({
                  visible: true,
                  task: null,
                  defaultDay: dayId,
                  defaultSlot: slotId,
                })
              }
              onRestoreDentalSchedule={handleRestoreDentalSchedule}
            />
          )}

          {activeTab === 'stats' && (
            <StatsScreen
              tasks={store.tasks}
              categories={store.categories}
              settings={store.settings}
              palette={palette}
            />
          )}

          {activeTab === 'archive' && (
            <ArchiveScreen
              archives={store.archives}
              settings={store.settings}
              palette={palette}
              onArchiveCurrentWeek={handleArchiveCurrentWeek}
              onRestoreArchive={handleRestoreArchive}
              onRepeatFromArchive={handleRepeatFromArchive}
              onRepeatCurrentWeek={handleRepeatCurrentWeek}
              onDeleteArchive={handleDeleteArchive}
              onUncheckAllTasks={handleUncheckAllTasks}
              onArchiveAndClearCompleted={handleArchiveAndClearCompleted}
              onClearAllTasks={handleClearAllTasks}
            />
          )}

          {activeTab === 'gemini' && (
            <GeminiMentorScreen
              chatMessages={store.chatMessages}
              streamingAiText={streamingAiText}
              isAiGenerating={isAiGenerating}
              settings={store.settings}
              palette={palette}
              onSendPreset={handleSendPreset}
              onSendPrompt={(p) => sendAiPrompt(p, p)}
              onSelectModel={handleSelectAiModel}
              onClearChat={handleClearChat}
              onCopySuccess={() => showToast('متن پاسخ کپی شد')}
            />
          )}

          {activeTab === 'settings' && (
            <SettingsScreen
              settings={store.settings}
              categories={store.categories}
              palette={palette}
              onSelectTheme={handleSelectTheme}
              onUpdateWeekTitle={handleUpdateWeekTitle}
              onSelectWeekStart={handleSelectWeekStart}
              onSelectViewMode={handleSelectViewMode}
              onSetSoundEnabled={handleSetSoundEnabled}
              onSetPersianNumbers={handleSetPersianNumbers}
              onSetCompactView={handleSetCompactView}
              onSelectAiModel={handleSelectAiModel}
              onAddCategory={handleAddCategory}
              onDeleteCategory={handleDeleteCategory}
              onOpenExportCenter={() => setShowExportCenter(true)}
              onRestoreDentalSchedule={handleRestoreDentalSchedule}
            />
          )}
        </main>
      </div>

      {/* Mobile Bottom Navigation */}
      <nav
        className="fixed right-0 bottom-0 left-0 z-30 grid grid-cols-6 border-t py-1.5 backdrop-blur-md md:hidden"
        style={{
          backgroundColor: `${palette.surfaceCard}F2`,
          borderColor: palette.borderColor,
        }}
        aria-label="منوی اصلی"
      >
        {NAV_ITEMS.map((item) => {
          const IconComp = item.icon;
          const selected = activeTab === item.id;
          return (
            <button
              key={item.id}
              type="button"
              onClick={() => setActiveTab(item.id)}
              className="flex flex-col items-center justify-center gap-0.5 py-1 text-[10px] font-bold"
              style={{ color: selected ? palette.accentPrimary : palette.textMuted }}
            >
              <IconComp size={19} />
              <span className="truncate">{item.label}</span>
            </button>
          );
        })}
      </nav>

      {/* Floating Action Button */}
      {showFab && (
        <button
          type="button"
          onClick={() =>
            setEditorState({
              visible: true,
              task: null,
              defaultDay: selectedDayId,
              defaultSlot: null,
            })
          }
          title="افزودن کار جدید"
          aria-label="افزودن کار جدید"
          className="fixed bottom-20 left-5 z-30 flex h-14 w-14 items-center justify-center rounded-2xl text-white shadow-lg transition-transform hover:scale-105 active:scale-95 md:bottom-6 md:left-6"
          style={{ backgroundColor: palette.accentPrimary }}
        >
          <Plus size={26} />
        </button>
      )}

      {/* Toast Notification */}
      {toast && (
        <div
          role="status"
          className="fixed bottom-24 left-1/2 z-50 -translate-x-1/2 whitespace-nowrap rounded-2xl px-4 py-2.5 text-xs font-bold text-white shadow-xl md:bottom-8"
          style={{ backgroundColor: toast.isError ? '#E11D48' : '#0F172A' }}
        >
          {toast.message}
        </div>
      )}

      {/* Confetti */}
      <ConfettiOverlay triggerKey={confettiKey} grandCelebration={confettiGrand} />

      {/* Modals */}
      {editorState.visible && (
        <TaskEditorSheet
          initialTask={editorState.task}
          defaultDayId={editorState.defaultDay}
          defaultSlotId={editorState.defaultSlot}
          categories={store.categories}
          palette={palette}
          usePersianDigits={store.settings.persianNumbers}
          onDismiss={() => setEditorState((s) => ({ ...s, visible: false }))}
          onSave={handleSaveTask}
        />
      )}

      {moveDialogTask && (
        <MoveTaskDialog
          task={moveDialogTask}
          palette={palette}
          usePersianDigits={store.settings.persianNumbers}
          onDismiss={() => setMoveDialogTask(null)}
          onConfirmMove={(dayId, slotId) =>
            handleMoveTask(moveDialogTask.id, dayId, slotId)
          }
        />
      )}

      {deleteConfirmTask && (
        <ConfirmDialog
          title="حذف فعالیت"
          message={`آیا «${deleteConfirmTask.title}» از برنامه هفتگی حذف شود؟`}
          confirmLabel="حذف"
          destructive
          palette={palette}
          onConfirm={() => {
            handleDeleteTask(deleteConfirmTask.id);
            setDeleteConfirmTask(null);
          }}
          onCancel={() => setDeleteConfirmTask(null)}
        />
      )}

      {showExportCenter && (
        <ExportCenterSheet
          tasks={store.tasks}
          settings={store.settings}
          palette={palette}
          buildJsonBackupString={() =>
            JSON.stringify(buildJsonExportPayload(store), null, 2)
          }
          onImportJsonString={handleImportJsonString}
          onExportSuccess={(msg) => {
            playCompleteChime(store.settings.soundEnabled);
            triggerConfetti(false);
            showToast(msg);
          }}
          onExportError={(msg) => showToast(msg, true)}
          onDismiss={() => setShowExportCenter(false)}
        />
      )}
    </div>
  );
}

export default App;
