import { ArrowLeftRight, Check, Clock, GripVertical, Pencil, Trash2 } from 'lucide-react';
import { TASK_PRIORITIES, type CategoryItem, type TaskItem } from '../types/planner';
import { DAYS_MAP, formatNumber } from '../utils/persian';
import { resolveCategoryStyle, type PlannerPalette } from '../utils/theme';

interface TaskCardProps {
  task: TaskItem;
  categories: CategoryItem[];
  palette: PlannerPalette;
  usePersianDigits: boolean;
  compactMode: boolean;
  showDayChip?: boolean;
  showDragHandle?: boolean;
  draggable?: boolean;
  onDragStart?: (task: TaskItem) => void;
  onDragEnd?: () => void;
  onToggleComplete: () => void;
  onEdit: () => void;
  onMove: () => void;
  onDelete: () => void;
}

export function TaskCard({
  task,
  categories,
  palette,
  usePersianDigits,
  compactMode,
  showDayChip = false,
  showDragHandle = false,
  draggable = false,
  onDragStart,
  onDragEnd,
  onToggleComplete,
  onEdit,
  onMove,
  onDelete,
}: TaskCardProps) {
  const catStyle = resolveCategoryStyle(task.category, categories, palette);
  const catName = categories.find((c) => c.id === task.category)?.name ?? 'شخصی';
  const priorityLabel =
    TASK_PRIORITIES.find((p) => p.id === task.priority)?.labelFa ?? 'متوسط';

  return (
    <div
      draggable={draggable}
      onDragStart={(e) => {
        if (!draggable) return;
        e.dataTransfer.setData('text/plain', task.id);
        e.dataTransfer.effectAllowed = 'move';
        onDragStart?.(task);
      }}
      onDragEnd={() => onDragEnd?.()}
      className="rounded-2xl border transition-all duration-200"
      style={{
        backgroundColor: task.completed ? catStyle.bg : palette.surfaceCard,
        borderColor: catStyle.border,
        borderWidth: task.priority === 'high' && !task.completed ? '1.5px' : '1px',
        opacity: task.completed ? 0.8 : 1,
        boxShadow: task.completed ? 'none' : '0 2px 8px rgba(15, 23, 42, 0.04)',
      }}
    >
      <div
        className={`flex items-start gap-2.5 ${compactMode ? 'px-3 py-2' : 'px-3.5 py-3'}`}
      >
        {/* Color accent bar */}
        <div
          className={`mt-1 w-1 shrink-0 rounded-full ${compactMode ? 'h-9' : 'h-11'}`}
          style={{ backgroundColor: catStyle.accent }}
        />

        {/* Accessible 40px completion toggle */}
        <button
          type="button"
          onClick={onToggleComplete}
          aria-label={task.completed ? 'بازگشت به در انتظار' : 'تکمیل کار'}
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full transition-transform active:scale-95"
        >
          <span
            className="flex h-6 w-6 items-center justify-center rounded-full border-2 transition-colors"
            style={{
              backgroundColor: task.completed ? palette.accentSecondary : 'transparent',
              borderColor: task.completed ? palette.accentSecondary : catStyle.accent,
            }}
          >
            {task.completed && <Check size={14} className="text-white" strokeWidth={3} />}
          </span>
        </button>

        {/* Main Content */}
        <button
          type="button"
          onClick={onEdit}
          className="min-w-0 flex-1 text-right focus:outline-none"
        >
          <div className="flex flex-wrap items-center gap-2 text-xs">
            {task.time && (
              <span
                className="inline-flex items-center gap-1 font-semibold tabular-nums"
                style={{ color: palette.textHeading }}
              >
                <Clock size={12} style={{ color: palette.accentPrimary }} />
                {formatNumber(task.time, usePersianDigits)}
              </span>
            )}
            {showDayChip && (
              <span className="font-bold" style={{ color: palette.accentPrimary }}>
                {DAYS_MAP[task.day]?.fa ?? task.day}
              </span>
            )}
            {task.priority === 'high' && !task.completed && (
              <span className="font-bold text-rose-700">• فوری</span>
            )}
          </div>

          <h4
            className={`mt-1 text-sm font-bold leading-snug ${
              task.completed ? 'line-through' : ''
            } ${compactMode ? 'line-clamp-1' : 'line-clamp-2'}`}
            style={{ color: palette.textHeading }}
          >
            {formatNumber(task.title, usePersianDigits)}
          </h4>

          {!compactMode && task.description && (
            <p
              className="mt-1 line-clamp-2 text-xs leading-relaxed"
              style={{ color: palette.textMuted }}
            >
              {formatNumber(task.description, usePersianDigits)}
            </p>
          )}

          {!compactMode && (
            <div
              className="mt-2 flex flex-wrap items-center gap-x-2 gap-y-1 text-[11px] font-medium"
              style={{ color: palette.textMuted }}
            >
              <span style={{ color: catStyle.text }} className="font-semibold">
                {catName}
              </span>
              {task.difficulty && (
                <>
                  <span aria-hidden="true">·</span>
                  <span>سختی: {task.difficulty}</span>
                </>
              )}
              {task.priority !== 'medium' && (
                <>
                  <span aria-hidden="true">·</span>
                  <span>{priorityLabel}</span>
                </>
              )}
            </div>
          )}
        </button>

        {/* Actions */}
        <div className="flex shrink-0 flex-col items-end gap-0.5">
          <div className="flex items-center">
            {showDragHandle && (
              <span
                title="برای جابجایی بکشید و روی روز مقصد رها کنید"
                className="flex h-10 w-7 cursor-grab items-center justify-center active:cursor-grabbing"
                style={{ color: palette.textMuted }}
              >
                <GripVertical size={17} />
              </span>
            )}
            <button
              type="button"
              onClick={onMove}
              title="جابجایی به روز یا ساعت دیگر"
              aria-label="جابجایی به روز یا ساعت دیگر"
              className="flex h-10 w-10 items-center justify-center rounded-xl transition-colors hover:bg-black/5"
              style={{ color: palette.accentSecondary }}
            >
              <ArrowLeftRight size={17} />
            </button>
          </div>
          <div className="flex items-center">
            <button
              type="button"
              onClick={onEdit}
              title="ویرایش کار"
              aria-label="ویرایش کار"
              className="flex h-10 w-10 items-center justify-center rounded-xl transition-colors hover:bg-black/5"
              style={{ color: palette.accentPrimary }}
            >
              <Pencil size={16} />
            </button>
            <button
              type="button"
              onClick={onDelete}
              title="حذف کار"
              aria-label="حذف کار"
              className="flex h-10 w-10 items-center justify-center rounded-xl text-rose-600 transition-colors hover:bg-rose-50"
            >
              <Trash2 size={16} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
