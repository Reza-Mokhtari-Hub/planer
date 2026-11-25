import { useMemo, useState } from 'react';
import {
  AlertCircle,
  Calendar,
  CheckCircle2,
  Clock,
  FileText,
  Palette,
  Pencil,
  Plus,
  Sparkles,
  Star,
} from 'lucide-react';
import { TaskCard } from '../components/TaskCard';
import {
  CHEERFUL_THEMES,
  type CategoryItem,
  type PlannerSettings,
  type ReflectionData,
  type TaskItem,
} from '../types/planner';
import {
  DAYS_MAP,
  MOTIVATIONAL_QUOTES,
  formatJalaliDate,
  formatNumber,
  getCurrentDayId,
  getDayOfYear,
  getOrderedDays,
  parseTimeMinutes,
} from '../utils/persian';
import type { PlannerPalette } from '../utils/theme';

interface DashboardScreenProps {
  tasks: TaskItem[];
  categories: CategoryItem[];
  reflection: ReflectionData;
  settings: PlannerSettings;
  palette: PlannerPalette;
  onToggleTask: (taskId: string) => void;
  onEditTask: (task: TaskItem) => void;
  onMoveTask: (task: TaskItem) => void;
  onDeleteTask: (task: TaskItem) => void;
  onAddTaskToday: (dayId: string) => void;
  onUpdateReflection: (reflection: ReflectionData) => void;
  onUpdateWeekTitle: (title: string) => void;
  onCycleTheme: () => void;
  onOpenExportCenter: () => void;
  onOpenAiMentor: () => void;
}

export function DashboardScreen({
  tasks,
  categories,
  reflection,
  settings,
  palette,
  onToggleTask,
  onEditTask,
  onMoveTask,
  onDeleteTask,
  onAddTaskToday,
  onUpdateReflection,
  onUpdateWeekTitle,
  onCycleTheme,
  onOpenExportCenter,
  onOpenAiMentor,
}: DashboardScreenProps) {
  const usePersian = settings.persianNumbers;
  const total = tasks.length;
  const completed = tasks.filter((t) => t.completed).length;
  const pending = total - completed;
  const highPriorityPending = tasks.filter((t) => !t.completed && t.priority === 'high').length;
  const percent = total === 0 ? 0 : Math.round((completed * 100) / total);

  const todayId = useMemo(() => getCurrentDayId(), []);
  const todayTasks = useMemo(
    () =>
      tasks
        .filter((t) => t.day === todayId)
        .sort((a, b) => parseTimeMinutes(a.time) - parseTimeMinutes(b.time)),
    [tasks, todayId]
  );

  const quoteIndex = useMemo(
    () => getDayOfYear() % MOTIVATIONAL_QUOTES.length,
    []
  );

  const orderedDays = useMemo(() => getOrderedDays(settings.weekStart), [settings.weekStart]);
  const dayStats = useMemo(
    () =>
      orderedDays.map((dayKey) => {
        const list = tasks.filter((t) => t.day === dayKey);
        return {
          dayKey,
          shortFa: DAYS_MAP[dayKey]?.shortFa ?? dayKey,
          done: list.filter((t) => t.completed).length,
          total: list.length,
        };
      }),
    [tasks, orderedDays]
  );
  const maxDayTotal = Math.max(1, ...dayStats.map((d) => d.total));

  const themeInfo = CHEERFUL_THEMES.find((t) => t.id === settings.theme) ?? CHEERFUL_THEMES[0];
  const [editTitleOpen, setEditTitleOpen] = useState(false);
  const [titleDraft, setTitleDraft] = useState(settings.currentWeekTitle);

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-4 px-4 py-4 pb-24 md:pb-8">
      {/* Hero Header Card */}
      <section
        className="rounded-3xl border p-5 shadow-sm"
        style={{
          background: `linear-gradient(135deg, ${palette.surfaceCard} 0%, ${palette.surfaceWarmAlt} 100%)`,
          borderColor: palette.borderColor,
        }}
      >
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2">
              <h1
                className="text-xl font-extrabold tracking-tight sm:text-2xl"
                style={{ color: palette.textHeading }}
              >
                برنامه‌ریزی هفتگی هوشمند
              </h1>
              <span className="text-xl" aria-hidden="true">
                {themeInfo.emoji}
              </span>
            </div>
            <button
              type="button"
              onClick={() => {
                setTitleDraft(settings.currentWeekTitle);
                setEditTitleOpen(true);
              }}
              className="mt-1 inline-flex items-center gap-1.5 text-right text-sm font-bold transition-opacity hover:opacity-80"
              style={{ color: palette.accentPrimary }}
            >
              <span>{settings.currentWeekTitle}</span>
              <Pencil size={14} className="shrink-0" />
            </button>
          </div>

          <div className="flex shrink-0 items-center gap-1">
            <button
              type="button"
              onClick={onCycleTheme}
              title="تغییر پوسته رنگی"
              aria-label="تغییر پوسته رنگی"
              className="flex h-10 w-10 items-center justify-center rounded-xl transition-colors hover:bg-black/5"
              style={{ color: palette.accentPrimary }}
            >
              <Palette size={20} />
            </button>
            <button
              type="button"
              onClick={onOpenExportCenter}
              title="خروجی PDF و JSON"
              aria-label="خروجی PDF و JSON"
              className="flex h-10 w-10 items-center justify-center rounded-xl transition-colors hover:bg-black/5"
              style={{ color: palette.accentSecondary }}
            >
              <FileText size={20} />
            </button>
          </div>
        </div>

        {/* Date + Lunch/Dinner bar */}
        <div
          className="mt-3 flex flex-wrap items-center justify-between gap-2 rounded-2xl border px-3.5 py-2.5"
          style={{
            backgroundColor: palette.surfaceCard,
            borderColor: palette.borderColor,
          }}
        >
          <div className="flex items-center gap-2">
            <Calendar size={16} style={{ color: palette.accentPrimary }} />
            <span className="text-xs font-bold tabular-nums" style={{ color: palette.textHeading }}>
              {formatJalaliDate(new Date(), usePersian)}
            </span>
          </div>
          <span className="text-xs font-medium tabular-nums" style={{ color: palette.textMuted }}>
            {formatNumber('ناهار: ۱۲ تا ۱۳ | شام: ۲۱:۳۰', usePersian)}
          </span>
        </div>

        <p className="mt-3 text-xs leading-relaxed" style={{ color: palette.textMain }}>
          «{MOTIVATIONAL_QUOTES[quoteIndex]}»
        </p>

        <div className="mt-3.5 grid grid-cols-2 gap-2.5">
          <button
            type="button"
            onClick={() => onAddTaskToday(todayId)}
            className="flex items-center justify-center gap-1.5 whitespace-nowrap rounded-xl px-4 py-2.5 text-sm font-bold text-white shadow-sm transition-opacity hover:opacity-95"
            style={{ backgroundColor: palette.accentPrimary }}
          >
            <Plus size={18} />
            کار جدید
          </button>
          <button
            type="button"
            onClick={onOpenAiMentor}
            className="flex items-center justify-center gap-1.5 whitespace-nowrap rounded-xl px-4 py-2.5 text-sm font-bold text-white shadow-sm transition-opacity hover:opacity-95"
            style={{ backgroundColor: palette.accentSecondary }}
          >
            <Sparkles size={18} />
            تحلیل Gemini
          </button>
        </div>
      </section>

      {/* Progress & KPIs */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <div className="flex items-center justify-between">
          <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
            پیشرفت کلی هفته
          </h2>
          <span
            className="text-xl font-extrabold tabular-nums"
            style={{ color: palette.accentSecondary }}
          >
            {formatNumber(`${percent}%`, usePersian)}
          </span>
        </div>

        <div
          className="mt-3 h-2.5 w-full overflow-hidden rounded-full"
          style={{ backgroundColor: palette.surfaceWarmAlt }}
        >
          <div
            className="h-full rounded-full transition-all duration-500"
            style={{
              width: `${percent}%`,
              backgroundColor: palette.accentSecondary,
            }}
          />
        </div>

        <div className="mt-3.5 grid grid-cols-2 gap-2.5 sm:grid-cols-4">
          {[
            {
              label: 'کل برنامه',
              value: formatNumber(total, usePersian),
              icon: Calendar,
              accent: palette.accentPrimary,
            },
            {
              label: 'تکمیل‌شده',
              value: formatNumber(completed, usePersian),
              icon: CheckCircle2,
              accent: palette.accentSecondary,
            },
            {
              label: 'باقیمانده',
              value: formatNumber(pending, usePersian),
              icon: Clock,
              accent: '#0284C7',
            },
            {
              label: 'فوری مانده',
              value: formatNumber(highPriorityPending, usePersian),
              icon: AlertCircle,
              accent: '#E11D48',
            },
          ].map((kpi) => {
            const IconComp = kpi.icon;
            return (
              <div
                key={kpi.label}
                className="flex flex-col items-center gap-1 rounded-2xl border p-3"
                style={{
                  backgroundColor: palette.surfaceWarmAlt,
                  borderColor: palette.borderColor,
                }}
              >
                <div
                  className="flex h-7 w-7 items-center justify-center rounded-full"
                  style={{ backgroundColor: `${kpi.accent}24` }}
                >
                  <IconComp size={15} style={{ color: kpi.accent }} />
                </div>
                <span
                  className="text-lg font-extrabold tabular-nums"
                  style={{ color: palette.textHeading }}
                >
                  {kpi.value}
                </span>
                <span className="text-[11px] font-medium" style={{ color: palette.textMuted }}>
                  {kpi.label}
                </span>
              </div>
            );
          })}
        </div>
      </section>

      {/* 7-Day Productivity Mini-Chart */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
            نمودار بهره‌وری ۷ روز هفته
          </h2>
          <div className="flex items-center gap-3 text-xs" style={{ color: palette.textMuted }}>
            <span className="inline-flex items-center gap-1.5">
              <span
                className="inline-block h-2.5 w-2.5 rounded-full"
                style={{ backgroundColor: palette.accentSecondary }}
              />
              انجام‌شده
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span
                className="inline-block h-2.5 w-2.5 rounded-full"
                style={{ backgroundColor: `${palette.accentPrimary}66` }}
              />
              کل برنامه
            </span>
          </div>
        </div>

        <div className="mt-4 grid grid-cols-7 items-end gap-2 pt-2">
          {dayStats.map((d) => {
            const totalPct = Math.max(6, Math.round((d.total / maxDayTotal) * 100));
            const donePct = d.total === 0 ? 0 : Math.round((d.done / maxDayTotal) * 100);
            return (
              <div key={d.dayKey} className="flex flex-col items-center gap-1.5">
                <div className="relative flex h-28 w-full max-w-[32px] items-end justify-center">
                  <div
                    className="w-full rounded-t-lg transition-all duration-300"
                    style={{
                      height: `${totalPct}%`,
                      backgroundColor: `${palette.accentPrimary}38`,
                    }}
                  />
                  {d.done > 0 && (
                    <div
                      className="absolute bottom-0 w-full rounded-t-lg transition-all duration-300"
                      style={{
                        height: `${Math.max(6, donePct)}%`,
                        backgroundColor: palette.accentSecondary,
                      }}
                    />
                  )}
                </div>
                <span className="text-xs font-bold" style={{ color: palette.textHeading }}>
                  {d.shortFa}
                </span>
                <span className="text-[11px] tabular-nums" style={{ color: palette.textMuted }}>
                  {formatNumber(`${d.done}/${d.total}`, usePersian)}
                </span>
              </div>
            );
          })}
        </div>
      </section>

      {/* Today's Tasks */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
            برنامه امروز ({DAYS_MAP[todayId]?.fa ?? 'امروز'})
          </h2>
          <span className="text-xs font-bold tabular-nums" style={{ color: palette.accentPrimary }}>
            {formatNumber(
              `${todayTasks.filter((t) => t.completed).length} از ${todayTasks.length} انجام‌شده`,
              usePersian
            )}
          </span>
        </div>

        {todayTasks.length === 0 ? (
          <div
            className="flex flex-col items-center gap-2 rounded-2xl border p-6 text-center"
            style={{
              backgroundColor: palette.surfaceCard,
              borderColor: palette.borderColor,
            }}
          >
            <p className="text-sm font-bold" style={{ color: palette.textHeading }}>
              امروز کاری ثبت نشده است!
            </p>
            <p className="text-xs" style={{ color: palette.textMuted }}>
              از دکمه بالا یک فعالیت برای امروز اضافه کنید.
            </p>
          </div>
        ) : (
          <div className="flex flex-col gap-2.5">
            {todayTasks.map((task) => (
              <TaskCard
                key={task.id}
                task={task}
                categories={categories}
                palette={palette}
                usePersianDigits={usePersian}
                compactMode={settings.compactView}
                onToggleComplete={() => onToggleTask(task.id)}
                onEdit={() => onEditTask(task)}
                onMove={() => onMoveTask(task)}
                onDelete={() => onDeleteTask(task)}
              />
            ))}
          </div>
        )}
      </section>

      {/* Weekly Reflection Card */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
          بازتاب و جمع‌بندی هفتگی
        </h2>

        <div className="mt-3 flex flex-wrap items-center justify-between gap-2">
          <span className="text-sm" style={{ color: palette.textMain }}>
            امتیاز رضایت از هفته:
          </span>
          <div className="flex items-center gap-1">
            {[1, 2, 3, 4, 5].map((star) => {
              const active = star <= reflection.rating;
              return (
                <button
                  key={star}
                  type="button"
                  onClick={() => onUpdateReflection({ ...reflection, rating: star })}
                  aria-label={`امتیاز ${star} از ۵`}
                  className="flex h-9 w-9 items-center justify-center rounded-lg transition-transform hover:scale-110"
                >
                  <Star
                    size={22}
                    fill={active ? '#F59E0B' : 'transparent'}
                    className="text-amber-500"
                  />
                </button>
              );
            })}
          </div>
        </div>

        <div className="mt-3 flex flex-col gap-3">
          <div>
            <label className="mb-1 block text-xs font-bold" style={{ color: palette.textHeading }}>
              موفقیت‌ها و نقاط قوت این هفته
            </label>
            <textarea
              rows={2}
              value={reflection.wins}
              onChange={(e) => onUpdateReflection({ ...reflection, wins: e.target.value })}
              className="w-full rounded-xl border px-3 py-2 text-sm outline-none"
              style={{ borderColor: palette.borderColor, color: palette.textMain }}
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-bold" style={{ color: palette.textHeading }}>
              چالش‌ها و موانع
            </label>
            <textarea
              rows={2}
              value={reflection.challenges}
              onChange={(e) => onUpdateReflection({ ...reflection, challenges: e.target.value })}
              className="w-full rounded-xl border px-3 py-2 text-sm outline-none"
              style={{ borderColor: palette.borderColor, color: palette.textMain }}
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-bold" style={{ color: palette.textHeading }}>
              برنامه بهبود برای هفته آینده
            </label>
            <textarea
              rows={2}
              value={reflection.improvements}
              onChange={(e) =>
                onUpdateReflection({ ...reflection, improvements: e.target.value })
              }
              className="w-full rounded-xl border px-3 py-2 text-sm outline-none"
              style={{ borderColor: palette.borderColor, color: palette.textMain }}
            />
          </div>
        </div>
      </section>

      {/* Edit Week Title Dialog */}
      {editTitleOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-[2px]"
          role="dialog"
          aria-modal="true"
          onClick={() => setEditTitleOpen(false)}
        >
          <div
            className="w-full max-w-md rounded-3xl border p-5 shadow-2xl"
            style={{
              backgroundColor: palette.surfaceCard,
              borderColor: palette.borderColor,
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
              ویرایش عنوان هفته
            </h3>
            <input
              type="text"
              value={titleDraft}
              onChange={(e) => setTitleDraft(e.target.value)}
              className="mt-3 w-full rounded-xl border px-3.5 py-2.5 text-sm outline-none"
              style={{ borderColor: palette.borderColor, color: palette.textMain }}
            />
            <div className="mt-4 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setEditTitleOpen(false)}
                className="rounded-xl border px-4 py-2 text-xs font-semibold"
                style={{ borderColor: palette.borderColor, color: palette.textMain }}
              >
                انصراف
              </button>
              <button
                type="button"
                onClick={() => {
                  if (titleDraft.trim()) onUpdateWeekTitle(titleDraft.trim());
                  setEditTitleOpen(false);
                }}
                className="rounded-xl px-4 py-2 text-xs font-bold text-white"
                style={{ backgroundColor: palette.accentPrimary }}
              >
                ذخیره
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
