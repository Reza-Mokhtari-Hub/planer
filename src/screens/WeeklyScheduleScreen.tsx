import { useMemo, useState } from 'react';
import {
  Check,
  FilterX,
  LayoutGrid,
  List,
  Plus,
  RotateCcw,
  Search,
  X,
} from 'lucide-react';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { TaskCard } from '../components/TaskCard';
import type {
  CategoryItem,
  PlannerSettings,
  TaskFilterState,
  TaskItem,
  ViewMode,
} from '../types/planner';
import {
  DAYS_MAP,
  TIME_SLOTS,
  formatNumber,
  getOrderedDays,
  getTaskSlot,
  parseTimeMinutes,
} from '../utils/persian';
import { resolveCategoryStyle, type PlannerPalette } from '../utils/theme';

interface WeeklyScheduleScreenProps {
  tasks: TaskItem[];
  filteredTasks: TaskItem[];
  categories: CategoryItem[];
  settings: PlannerSettings;
  filter: TaskFilterState;
  selectedDayId: string;
  palette: PlannerPalette;
  onSelectDay: (dayId: string) => void;
  onViewModeChange: (mode: ViewMode) => void;
  onSearchQueryChange: (q: string) => void;
  onCategoryFilterChange: (catId: string) => void;
  onPriorityFilterChange: (priority: string) => void;
  onStatusFilterChange: (status: string) => void;
  onResetFilters: () => void;
  onToggleTask: (taskId: string) => void;
  onEditTask: (task: TaskItem) => void;
  onOpenMoveDialog: (task: TaskItem) => void;
  onDropMoveTask: (taskId: string, newDay: string, newSlotId?: string | null) => void;
  onDeleteTask: (task: TaskItem) => void;
  onAddTaskInSlot: (dayId: string, slotId: string | null) => void;
  onRestoreDentalSchedule: () => void;
}

export function WeeklyScheduleScreen({
  tasks,
  filteredTasks,
  categories,
  settings,
  filter,
  selectedDayId,
  palette,
  onSelectDay,
  onViewModeChange,
  onSearchQueryChange,
  onCategoryFilterChange,
  onPriorityFilterChange,
  onStatusFilterChange,
  onResetFilters,
  onToggleTask,
  onEditTask,
  onOpenMoveDialog,
  onDropMoveTask,
  onDeleteTask,
  onAddTaskInSlot,
  onRestoreDentalSchedule,
}: WeeklyScheduleScreenProps) {
  const usePersian = settings.persianNumbers;
  const orderedDays = useMemo(() => getOrderedDays(settings.weekStart), [settings.weekStart]);

  const [showRestoreConfirm, setShowRestoreConfirm] = useState(false);
  const [draggingTaskId, setDraggingTaskId] = useState<string | null>(null);
  const [hoverZoneKey, setHoverZoneKey] = useState<string | null>(null);

  const hasActiveFilter =
    filter.query.trim().length > 0 ||
    filter.categoryId !== 'all' ||
    filter.priority !== 'all' ||
    filter.status !== 'all';

  const dayTasks = useMemo(
    () =>
      filteredTasks
        .filter((t) => t.day === selectedDayId)
        .sort((a, b) => parseTimeMinutes(a.time) - parseTimeMinutes(b.time)),
    [filteredTasks, selectedDayId]
  );

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-3 px-4 py-3 pb-24 md:pb-8">
      {/* Top Controls */}
      <div className="flex flex-col gap-2.5">
        <div className="flex items-center gap-2">
          <div
            className="flex flex-1 items-center gap-2 rounded-2xl border px-3 py-2"
            style={{
              backgroundColor: palette.surfaceCard,
              borderColor: palette.borderColor,
            }}
          >
            <Search size={18} style={{ color: palette.textMuted }} />
            <input
              type="text"
              value={filter.query}
              onChange={(e) => onSearchQueryChange(e.target.value)}
              placeholder="جستجو در دروس، توضیحات..."
              className="w-full bg-transparent text-sm outline-none"
              style={{ color: palette.textMain }}
            />
            {filter.query && (
              <button
                type="button"
                onClick={() => onSearchQueryChange('')}
                aria-label="پاک کردن جستجو"
                className="flex h-7 w-7 items-center justify-center rounded-lg hover:bg-black/5"
                style={{ color: palette.textMuted }}
              >
                <X size={16} />
              </button>
            )}
          </div>

          <button
            type="button"
            onClick={() => setShowRestoreConfirm(true)}
            title="بازگردانی برنامه ترم ۵ دندانپزشکی"
            aria-label="بازگردانی برنامه ترم ۵ دندانپزشکی"
            className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border transition-colors hover:opacity-90"
            style={{
              backgroundColor: palette.surfaceWarmAlt,
              borderColor: palette.borderColor,
              color: palette.accentPrimary,
            }}
          >
            <RotateCcw size={19} />
          </button>
        </div>

        {/* Segmented ViewMode Toggle */}
        <div
          className="grid grid-cols-2 gap-1 rounded-2xl border p-1"
          style={{
            backgroundColor: palette.surfaceWarmAlt,
            borderColor: palette.borderColor,
          }}
        >
          <button
            type="button"
            onClick={() => onViewModeChange('table')}
            className="flex items-center justify-center gap-1.5 whitespace-nowrap rounded-xl px-3 py-2 text-xs font-bold transition-colors"
            style={{
              backgroundColor: settings.viewMode === 'table' ? palette.surfaceCard : 'transparent',
              color: settings.viewMode === 'table' ? palette.accentPrimary : palette.textMuted,
              boxShadow:
                settings.viewMode === 'table' ? '0 1px 4px rgba(15,23,42,0.08)' : 'none',
            }}
          >
            <LayoutGrid size={16} />
            جدولی (ماتریس)
          </button>
          <button
            type="button"
            onClick={() => onViewModeChange('cards')}
            className="flex items-center justify-center gap-1.5 whitespace-nowrap rounded-xl px-3 py-2 text-xs font-bold transition-colors"
            style={{
              backgroundColor: settings.viewMode === 'cards' ? palette.surfaceCard : 'transparent',
              color: settings.viewMode === 'cards' ? palette.accentPrimary : palette.textMuted,
              boxShadow:
                settings.viewMode === 'cards' ? '0 1px 4px rgba(15,23,42,0.08)' : 'none',
            }}
          >
            <List size={16} />
            کارتی (تایم‌لاین)
          </button>
        </div>

        {/* Filter Chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1">
          <button
            type="button"
            onClick={() => onCategoryFilterChange('all')}
            className="whitespace-nowrap shrink-0 rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
            style={{
              backgroundColor:
                filter.categoryId === 'all' ? palette.accentPrimary : palette.surfaceCard,
              borderColor:
                filter.categoryId === 'all' ? palette.accentPrimary : palette.borderColor,
              color: filter.categoryId === 'all' ? '#FFFFFF' : palette.textMain,
            }}
          >
            همه دسته‌ها
          </button>

          {categories.map((cat) => {
            const active = filter.categoryId === cat.id;
            return (
              <button
                key={cat.id}
                type="button"
                onClick={() => onCategoryFilterChange(active ? 'all' : cat.id)}
                className="whitespace-nowrap shrink-0 rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
                style={{
                  backgroundColor: active ? palette.accentPrimary : palette.surfaceCard,
                  borderColor: active ? palette.accentPrimary : palette.borderColor,
                  color: active ? '#FFFFFF' : palette.textMain,
                }}
              >
                {cat.name}
              </button>
            );
          })}

          <button
            type="button"
            onClick={() =>
              onStatusFilterChange(filter.status === 'pending' ? 'all' : 'pending')
            }
            className="whitespace-nowrap shrink-0 rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
            style={{
              backgroundColor:
                filter.status === 'pending' ? palette.accentSecondary : palette.surfaceCard,
              borderColor:
                filter.status === 'pending' ? palette.accentSecondary : palette.borderColor,
              color: filter.status === 'pending' ? '#FFFFFF' : palette.textMain,
            }}
          >
            در انتظار
          </button>

          <button
            type="button"
            onClick={() =>
              onStatusFilterChange(filter.status === 'completed' ? 'all' : 'completed')
            }
            className="whitespace-nowrap shrink-0 rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
            style={{
              backgroundColor:
                filter.status === 'completed' ? palette.accentSecondary : palette.surfaceCard,
              borderColor:
                filter.status === 'completed' ? palette.accentSecondary : palette.borderColor,
              color: filter.status === 'completed' ? '#FFFFFF' : palette.textMain,
            }}
          >
            انجام‌شده
          </button>

          <button
            type="button"
            onClick={() =>
              onPriorityFilterChange(filter.priority === 'high' ? 'all' : 'high')
            }
            className="whitespace-nowrap shrink-0 rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
            style={{
              backgroundColor:
                filter.priority === 'high' ? '#E11D48' : palette.surfaceCard,
              borderColor: filter.priority === 'high' ? '#E11D48' : palette.borderColor,
              color: filter.priority === 'high' ? '#FFFFFF' : palette.textMain,
            }}
          >
            اولویت فوری
          </button>

          {hasActiveFilter && (
            <button
              type="button"
              onClick={onResetFilters}
              className="inline-flex shrink-0 items-center gap-1 whitespace-nowrap rounded-xl px-2.5 py-1.5 text-xs font-bold text-rose-600 hover:bg-rose-50"
            >
              <FilterX size={15} />
              حذف فیلتر
            </button>
          )}
        </div>
      </div>

      {settings.viewMode === 'cards' ? (
        <>
          {/* Day Selector Strip (Drop Targets) */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            {orderedDays.map((dayKey) => {
              const dayFa = DAYS_MAP[dayKey]?.fa ?? dayKey;
              const count = filteredTasks.filter((t) => t.day === dayKey).length;
              const selected = selectedDayId === dayKey;
              const isHover = hoverZoneKey === `day:${dayKey}`;

              return (
                <button
                  key={dayKey}
                  type="button"
                  onClick={() => onSelectDay(dayKey)}
                  onDragOver={(e) => {
                    e.preventDefault();
                    setHoverZoneKey(`day:${dayKey}`);
                  }}
                  onDragLeave={() => {
                    if (hoverZoneKey === `day:${dayKey}`) setHoverZoneKey(null);
                  }}
                  onDrop={(e) => {
                    e.preventDefault();
                    const droppedId = e.dataTransfer.getData('text/plain') || draggingTaskId;
                    setHoverZoneKey(null);
                    setDraggingTaskId(null);
                    if (droppedId) {
                      onDropMoveTask(droppedId, dayKey, null);
                    }
                  }}
                  className="flex shrink-0 flex-col items-center rounded-2xl border px-4 py-2 transition-colors"
                  style={{
                    backgroundColor: isHover
                      ? `${palette.accentSecondary}33`
                      : selected
                        ? palette.accentPrimary
                        : palette.surfaceCard,
                    borderColor: isHover
                      ? palette.accentSecondary
                      : selected
                        ? palette.accentPrimary
                        : palette.borderColor,
                    borderWidth: isHover ? '2px' : '1px',
                  }}
                >
                  <span
                    className="text-xs font-bold whitespace-nowrap"
                    style={{
                      color: selected && !isHover ? '#FFFFFF' : palette.textHeading,
                    }}
                  >
                    {dayFa}
                  </span>
                  <span
                    className="text-[11px] tabular-nums whitespace-nowrap"
                    style={{
                      color:
                        selected && !isHover ? 'rgba(255,255,255,0.9)' : palette.textMuted,
                    }}
                  >
                    {formatNumber(`${count} کار`, usePersian)}
                  </span>
                </button>
              );
            })}
          </div>

          {dayTasks.length === 0 ? (
            <div
              className="flex flex-col items-center gap-3 rounded-2xl border p-6 text-center"
              style={{
                backgroundColor: palette.surfaceCard,
                borderColor: palette.borderColor,
              }}
            >
              <p className="text-sm font-bold" style={{ color: palette.textHeading }}>
                برای این روز موردی یافت نشد
              </p>
              <div className="flex flex-wrap justify-center gap-2">
                <button
                  type="button"
                  onClick={() => onAddTaskInSlot(selectedDayId, null)}
                  className="inline-flex items-center gap-1.5 rounded-xl px-4 py-2 text-xs font-bold text-white"
                  style={{ backgroundColor: palette.accentPrimary }}
                >
                  <Plus size={15} />
                  افزودن کار
                </button>
                {tasks.length === 0 && (
                  <button
                    type="button"
                    onClick={() => setShowRestoreConfirm(true)}
                    className="rounded-xl border px-4 py-2 text-xs font-bold"
                    style={{ borderColor: palette.borderColor, color: palette.textHeading }}
                  >
                    بازیابی برنامه ترم ۵
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="flex flex-col gap-2.5">
              <p className="text-[11px]" style={{ color: palette.textMuted }}>
                نکته: برای جابجایی، کارت را بکشید و روی نام روز رها کنید، یا دکمه انتقال (⇄) را بزنید.
              </p>
              {dayTasks.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  categories={categories}
                  palette={palette}
                  usePersianDigits={usePersian}
                  compactMode={settings.compactView}
                  showDragHandle
                  draggable
                  onDragStart={(t) => setDraggingTaskId(t.id)}
                  onDragEnd={() => {
                    setDraggingTaskId(null);
                    setHoverZoneKey(null);
                  }}
                  onToggleComplete={() => onToggleTask(task.id)}
                  onEdit={() => onEditTask(task)}
                  onMove={() => onOpenMoveDialog(task)}
                  onDelete={() => onDeleteTask(task)}
                />
              ))}
            </div>
          )}
        </>
      ) : (
        /* MATRIX TABLE VIEW (9 Time Slots x 7 Days) */
        <div
          className="overflow-x-auto rounded-2xl border shadow-sm"
          style={{
            backgroundColor: palette.surfaceCard,
            borderColor: palette.borderColor,
          }}
        >
          <table className="w-full min-w-[980px] border-collapse">
            <thead>
              <tr style={{ backgroundColor: palette.surfaceWarmAlt }}>
                <th
                  className="w-32 border-b border-l p-2.5 text-center text-xs font-extrabold"
                  style={{
                    borderColor: palette.borderColor,
                    color: palette.textHeading,
                  }}
                >
                  بازه زمانی
                </th>
                {orderedDays.map((dayKey) => (
                  <th
                    key={dayKey}
                    className="min-w-[130px] border-b border-l p-2.5 text-center text-sm font-bold last:border-l-0"
                    style={{
                      borderColor: palette.borderColor,
                      color: palette.textHeading,
                    }}
                  >
                    {DAYS_MAP[dayKey]?.fa ?? dayKey}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {TIME_SLOTS.map((slot) => (
                <tr
                  key={slot.id}
                  style={{
                    backgroundColor: slot.isLunch
                      ? `${palette.personalBg}AA`
                      : palette.surfaceCard,
                  }}
                >
                  <td
                    className="border-b border-l p-2.5 align-top"
                    style={{ borderColor: palette.borderColor }}
                  >
                    <div className="flex flex-col gap-0.5">
                      <span
                        className="text-xs font-bold"
                        style={{ color: slot.colorHex }}
                      >
                        {slot.title}
                      </span>
                      <span
                        className="text-[11px] tabular-nums"
                        style={{ color: palette.textMuted }}
                      >
                        {formatNumber(slot.timeRange, usePersian)}
                      </span>
                    </div>
                  </td>

                  {orderedDays.map((dayKey) => {
                    const cellTasks = filteredTasks
                      .filter((t) => t.day === dayKey && getTaskSlot(t) === slot.id)
                      .sort((a, b) => parseTimeMinutes(a.time) - parseTimeMinutes(b.time));
                    const zoneKey = `${dayKey}:${slot.id}`;
                    const isHover = hoverZoneKey === zoneKey;

                    return (
                      <td
                        key={zoneKey}
                        onDragOver={(e) => {
                          e.preventDefault();
                          setHoverZoneKey(zoneKey);
                        }}
                        onDragLeave={() => {
                          if (hoverZoneKey === zoneKey) setHoverZoneKey(null);
                        }}
                        onDrop={(e) => {
                          e.preventDefault();
                          const droppedId =
                            e.dataTransfer.getData('text/plain') || draggingTaskId;
                          setHoverZoneKey(null);
                          setDraggingTaskId(null);
                          if (droppedId) {
                            onDropMoveTask(droppedId, dayKey, slot.id);
                          }
                        }}
                        className="border-b border-l p-1.5 align-top transition-colors last:border-l-0"
                        style={{
                          borderColor: isHover ? palette.accentSecondary : palette.borderColor,
                          backgroundColor: isHover
                            ? `${palette.accentSecondary}24`
                            : slot.isLunch
                              ? `${palette.personalBg}70`
                              : 'transparent',
                        }}
                      >
                        <div className="flex min-h-[60px] flex-col gap-1.5">
                          {cellTasks.map((t) => {
                            const catStyle = resolveCategoryStyle(
                              t.category,
                              categories,
                              palette
                            );
                            return (
                              <div
                                key={t.id}
                                draggable
                                onDragStart={(e) => {
                                  e.dataTransfer.setData('text/plain', t.id);
                                  e.dataTransfer.effectAllowed = 'move';
                                  setDraggingTaskId(t.id);
                                }}
                                onDragEnd={() => {
                                  setDraggingTaskId(null);
                                  setHoverZoneKey(null);
                                }}
                                className="group flex cursor-grab items-start gap-1.5 rounded-xl border px-2 py-1.5 transition-shadow hover:shadow-sm active:cursor-grabbing"
                                style={{
                                  backgroundColor: catStyle.bg,
                                  borderColor: catStyle.border,
                                  opacity: t.completed ? 0.72 : 1,
                                }}
                              >
                                <button
                                  type="button"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    onToggleTask(t.id);
                                  }}
                                  aria-label={t.completed ? 'تکمیل‌شده' : 'تکمیل کار'}
                                  className="mt-0.5 flex h-4 w-4 shrink-0 items-center justify-center rounded-full border"
                                  style={{
                                    backgroundColor: t.completed
                                      ? palette.accentSecondary
                                      : '#FFFFFF',
                                    borderColor: catStyle.accent,
                                  }}
                                >
                                  {t.completed && (
                                    <Check size={10} className="text-white" strokeWidth={3} />
                                  )}
                                </button>

                                <button
                                  type="button"
                                  onClick={() => onEditTask(t)}
                                  className="min-w-0 flex-1 text-right"
                                >
                                  <p
                                    className={`line-clamp-2 text-[11px] font-bold leading-snug ${
                                      t.completed ? 'line-through' : ''
                                    }`}
                                    style={{ color: catStyle.text }}
                                  >
                                    {formatNumber(t.title, usePersian)}
                                  </p>
                                  {t.time && (
                                    <span
                                      className="text-[10px] tabular-nums"
                                      style={{ color: palette.textMuted }}
                                    >
                                      {formatNumber(t.time, usePersian)}
                                    </span>
                                  )}
                                </button>
                              </div>
                            );
                          })}

                          <button
                            type="button"
                            onClick={() => onAddTaskInSlot(dayKey, slot.id)}
                            title="افزودن کار به این بازه"
                            aria-label="افزودن کار به این بازه"
                            className="mt-auto flex w-full items-center justify-center rounded-lg py-1 opacity-55 transition-opacity hover:bg-black/5 hover:opacity-100"
                            style={{ color: palette.textMuted }}
                          >
                            <Plus size={14} />
                          </button>
                        </div>
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showRestoreConfirm && (
        <ConfirmDialog
          title="بازگردانی برنامه جامع ترم ۵ دندانپزشکی"
          message="برنامه فعلی ابتدا به‌طور خودکار در آرشیو ذخیره می‌شود و سپس برنامه ۶۳ وظیفه‌ای ترم ۵ (شامل تایم ناهار ۱۲ تا ۱۳، بیدارباش ۶ صبح و ۳ جلسه ورزش) بارگذاری می‌گردد."
          confirmLabel="بله، بازیابی شود"
          palette={palette}
          onConfirm={() => {
            onRestoreDentalSchedule();
            setShowRestoreConfirm(false);
          }}
          onCancel={() => setShowRestoreConfirm(false)}
        />
      )}
    </div>
  );
}
