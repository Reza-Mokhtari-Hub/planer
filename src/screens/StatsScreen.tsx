import { useMemo } from 'react';
import { Dumbbell, Trophy, Utensils } from 'lucide-react';
import { CategoryIcon } from '../components/CategoryIcon';
import type { CategoryItem, PlannerSettings, TaskItem } from '../types/planner';
import { DAYS_MAP, formatNumber, getOrderedDays, getTaskSlot } from '../utils/persian';
import { resolveCategoryStyle, type PlannerPalette } from '../utils/theme';

interface StatsScreenProps {
  tasks: TaskItem[];
  categories: CategoryItem[];
  settings: PlannerSettings;
  palette: PlannerPalette;
}

export function StatsScreen({ tasks, categories, settings, palette }: StatsScreenProps) {
  const usePersian = settings.persianNumbers;
  const orderedDays = useMemo(() => getOrderedDays(settings.weekStart), [settings.weekStart]);

  const dayMetrics = useMemo(
    () =>
      orderedDays.map((dayKey) => {
        const list = tasks.filter((t) => t.day === dayKey);
        const done = list.filter((t) => t.completed).length;
        const pending = list.length - done;
        const rate = list.length === 0 ? 0 : Math.round((done * 100) / list.length);
        return {
          dayId: dayKey,
          labelFa: DAYS_MAP[dayKey]?.fa ?? dayKey,
          shortFa: DAYS_MAP[dayKey]?.shortFa ?? dayKey,
          done,
          pending,
          total: list.length,
          rate,
        };
      }),
    [tasks, orderedDays]
  );

  const bestDay = useMemo(() => {
    const withTasks = dayMetrics.filter((d) => d.total > 0);
    if (withTasks.length === 0) return null;
    return withTasks.reduce((best, curr) => {
      if (curr.rate > best.rate) return curr;
      if (curr.rate === best.rate && curr.done > best.done) return curr;
      return best;
    });
  }, [dayMetrics]);

  const maxGrouped = Math.max(1, ...dayMetrics.map((d) => Math.max(d.done, d.pending)));
  const sportTasks = useMemo(() => tasks.filter((t) => t.category === 'sport'), [tasks]);
  const lunchTasks = useMemo(
    () => tasks.filter((t) => getTaskSlot(t) === 'lunch_uni'),
    [tasks]
  );

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-4 px-4 py-4 pb-24 md:pb-8">
      <h1 className="text-xl font-extrabold" style={{ color: palette.textHeading }}>
        آمار و تحلیل عملکرد هفتگی
      </h1>

      {/* Most Productive Day */}
      {bestDay && (
        <section
          className="flex items-center gap-3 rounded-3xl border p-4"
          style={{
            backgroundColor: palette.surfaceWarmAlt,
            borderColor: palette.accentPrimary,
          }}
        >
          <div
            className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full"
            style={{ backgroundColor: `${palette.accentPrimary}29` }}
          >
            <Trophy size={24} style={{ color: palette.accentPrimary }} />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="text-base font-bold" style={{ color: palette.textHeading }}>
              پربازده‌ترین روز هفته: {bestDay.labelFa}
            </h2>
            <p className="mt-0.5 text-xs tabular-nums" style={{ color: palette.textMain }}>
              {formatNumber(
                `${bestDay.done} از ${bestDay.total} فعالیت تکمیل شده (${bestDay.rate}%)`,
                usePersian
              )}
            </p>
          </div>
        </section>
      )}

      {/* Grouped Bar Chart (Completed vs Pending) */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
          نمودار مقایسه‌ای انجام‌شده در برابر باقیمانده
        </h2>
        <div className="mt-2 flex items-center gap-4 text-xs" style={{ color: palette.textMuted }}>
          <span className="inline-flex items-center gap-1.5">
            <span
              className="inline-block h-2.5 w-2.5 rounded-full"
              style={{ backgroundColor: palette.accentSecondary }}
            />
            تکمیل‌شده
          </span>
          <span className="inline-flex items-center gap-1.5">
            <span className="inline-block h-2.5 w-2.5 rounded-full bg-amber-500" />
            در انتظار
          </span>
        </div>

        <div className="mt-4 grid grid-cols-7 items-end gap-2">
          {dayMetrics.map((m) => {
            const donePct = Math.max(4, Math.round((m.done / maxGrouped) * 100));
            const pendPct = Math.max(4, Math.round((m.pending / maxGrouped) * 100));
            return (
              <div key={m.dayId} className="flex flex-col items-center gap-1.5">
                <div className="flex h-36 items-end justify-center gap-1">
                  <div
                    className="w-3.5 rounded-t-md transition-all duration-300"
                    style={{
                      height: `${donePct}%`,
                      backgroundColor: palette.accentSecondary,
                    }}
                    title={`تکمیل‌شده: ${m.done}`}
                  />
                  <div
                    className="w-3.5 rounded-t-md bg-amber-500 transition-all duration-300"
                    style={{ height: `${pendPct}%` }}
                    title={`در انتظار: ${m.pending}`}
                  />
                </div>
                <span className="text-xs font-medium" style={{ color: palette.textMuted }}>
                  {m.shortFa}
                </span>
              </div>
            );
          })}
        </div>
      </section>

      {/* Daily Progress List */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          روند روزانه تکمیل کارها
        </h2>
        <div className="flex flex-col gap-3">
          {dayMetrics.map((m) => (
            <div key={m.dayId} className="flex flex-col gap-1">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold" style={{ color: palette.textHeading }}>
                  {m.labelFa}
                </span>
                <span
                  className="font-bold tabular-nums"
                  style={{ color: palette.accentSecondary }}
                >
                  {formatNumber(`${m.done}/${m.total} (${m.rate}%)`, usePersian)}
                </span>
              </div>
              <div
                className="h-2 w-full overflow-hidden rounded-full"
                style={{ backgroundColor: palette.surfaceWarmAlt }}
              >
                <div
                  className="h-full rounded-full transition-all duration-300"
                  style={{
                    width: `${m.rate}%`,
                    backgroundColor: palette.accentSecondary,
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Category Distribution */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          توزیع دسته‌بندی فعالیت‌ها
        </h2>
        <div className="flex flex-col gap-2">
          {categories.map((cat) => {
            const catTasks = tasks.filter((t) => t.category === cat.id);
            const catDone = catTasks.filter((t) => t.completed).length;
            const style = resolveCategoryStyle(cat.id, categories, palette);
            return (
              <div
                key={cat.id}
                className="flex items-center justify-between rounded-2xl border px-3.5 py-2.5"
                style={{
                  backgroundColor: style.bg,
                  borderColor: style.border,
                }}
              >
                <div className="flex items-center gap-2">
                  <CategoryIcon iconKey={style.iconKey} color={style.accent} size={18} />
                  <span className="text-sm font-bold" style={{ color: style.text }}>
                    {cat.name}
                  </span>
                </div>
                <span
                  className="text-xs font-extrabold tabular-nums"
                  style={{ color: style.accent }}
                >
                  {formatNumber(`${catDone} از ${catTasks.length}`, usePersian)}
                </span>
              </div>
            );
          })}
        </div>
      </section>

      {/* Dental Term 5 Habits */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          پایش عادت‌های کلیدی ترم ۵ دندانپزشکی
        </h2>
        <div className="flex flex-col gap-2.5">
          <div
            className="flex items-center justify-between gap-3 rounded-2xl border p-3.5"
            style={{
              backgroundColor: palette.sportBg,
              borderColor: palette.sportBorder,
            }}
          >
            <div className="flex items-center gap-2.5">
              <Dumbbell size={20} style={{ color: palette.sportText }} />
              <div>
                <p className="text-xs font-bold" style={{ color: palette.sportText }}>
                  ۳ جلسه ورزش هفتگی (جمعه، یک‌شنبه، سه‌شنبه)
                </p>
                <p className="text-[11px]" style={{ color: palette.textMuted }}>
                  حفظ ارگونومی و سلامت عضلانی دندانپزشک
                </p>
              </div>
            </div>
            <span
              className="text-sm font-extrabold tabular-nums"
              style={{ color: palette.sportText }}
            >
              {formatNumber(
                `${sportTasks.filter((t) => t.completed).length}/${sportTasks.length}`,
                usePersian
              )}
            </span>
          </div>

          <div
            className="flex items-center justify-between gap-3 rounded-2xl border p-3.5"
            style={{
              backgroundColor: palette.personalBg,
              borderColor: palette.personalBorder,
            }}
          >
            <div className="flex items-center gap-2.5">
              <Utensils size={20} style={{ color: palette.personalText }} />
              <div>
                <p className="text-xs font-bold" style={{ color: palette.personalText }}>
                  {formatNumber('تایم ثابت ناهار ۱۲ تا ۱۳', usePersian)}
                </p>
                <p className="text-[11px]" style={{ color: palette.textMuted }}>
                  {formatNumber('تایم استاندارد شام ۲۱:۳۰ و خواب ۲۳:۰۰', usePersian)}
                </p>
              </div>
            </div>
            <span
              className="text-sm font-extrabold tabular-nums"
              style={{ color: palette.personalText }}
            >
              {formatNumber(
                `${lunchTasks.filter((t) => t.completed).length}/${lunchTasks.length}`,
                usePersian
              )}
            </span>
          </div>
        </div>
      </section>
    </div>
  );
}
