import { useState } from 'react';
import { X } from 'lucide-react';
import type { TaskItem } from '../types/planner';
import { DAYS_MAP, DEFAULT_DAYS_ORDER, TIME_SLOTS, formatNumber, getTaskSlot } from '../utils/persian';
import type { PlannerPalette } from '../utils/theme';

interface MoveTaskDialogProps {
  task: TaskItem;
  palette: PlannerPalette;
  usePersianDigits: boolean;
  onDismiss: () => void;
  onConfirmMove: (dayId: string, slotId: string) => void;
}

export function MoveTaskDialog({
  task,
  palette,
  usePersianDigits,
  onDismiss,
  onConfirmMove,
}: MoveTaskDialogProps) {
  const [selectedDay, setSelectedDay] = useState(task.day);
  const [selectedSlot, setSelectedSlot] = useState(getTaskSlot(task));

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      aria-label="انتقال سریع فعالیت"
      onClick={onDismiss}
    >
      <div
        className="max-h-[88vh] w-full max-w-md overflow-y-auto rounded-3xl border p-5 shadow-2xl"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between border-b pb-3" style={{ borderColor: palette.borderColor }}>
          <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
            انتقال سریع فعالیت
          </h2>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="بستن"
            className="flex h-10 w-10 items-center justify-center rounded-xl hover:bg-black/5"
            style={{ color: palette.textMuted }}
          >
            <X size={18} />
          </button>
        </div>

        <div className="mt-3 flex flex-col gap-3">
          <p className="text-sm font-bold" style={{ color: palette.accentPrimary }}>
            {formatNumber(task.title, usePersianDigits)}
          </p>

          <div>
            <p className="mb-1.5 text-xs font-bold" style={{ color: palette.textHeading }}>
              انتخاب روز مقصد:
            </p>
            <div className="flex flex-wrap gap-1.5">
              {DEFAULT_DAYS_ORDER.map((dayKey) => {
                const selected = selectedDay === dayKey;
                return (
                  <button
                    key={dayKey}
                    type="button"
                    onClick={() => setSelectedDay(dayKey)}
                    className="whitespace-nowrap rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
                    style={{
                      backgroundColor: selected ? palette.accentPrimary : palette.surfaceWarmAlt,
                      borderColor: selected ? palette.accentPrimary : palette.borderColor,
                      color: selected ? '#FFFFFF' : palette.textMain,
                    }}
                  >
                    {DAYS_MAP[dayKey]?.fa ?? dayKey}
                  </button>
                );
              })}
            </div>
          </div>

          <div>
            <p className="mb-1.5 text-xs font-bold" style={{ color: palette.textHeading }}>
              انتخاب بازه زمانی مقصد:
            </p>
            <div className="flex flex-col gap-1.5">
              {TIME_SLOTS.map((slot) => {
                const isSelected = selectedSlot === slot.id;
                return (
                  <button
                    key={slot.id}
                    type="button"
                    onClick={() => setSelectedSlot(slot.id)}
                    className="flex items-center justify-between rounded-xl border px-3 py-2 text-right transition-colors"
                    style={{
                      backgroundColor: isSelected
                        ? `${palette.accentPrimary}20`
                        : palette.surfaceWarmAlt,
                      borderColor: isSelected ? palette.accentPrimary : palette.borderColor,
                    }}
                  >
                    <span
                      className={`text-xs ${isSelected ? 'font-bold' : 'font-medium'}`}
                      style={{ color: palette.textHeading }}
                    >
                      {slot.title}
                    </span>
                    <span
                      className="text-[11px] tabular-nums"
                      style={{
                        color: isSelected ? palette.accentPrimary : palette.textMuted,
                      }}
                    >
                      {formatNumber(slot.timeRange, usePersianDigits)}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          <div className="mt-2 flex items-center gap-2.5">
            <button
              type="button"
              onClick={onDismiss}
              className="flex-1 rounded-xl border px-4 py-2.5 text-sm font-semibold hover:bg-black/5"
              style={{ borderColor: palette.borderColor, color: palette.textMain }}
            >
              انصراف
            </button>
            <button
              type="button"
              onClick={() => {
                onConfirmMove(selectedDay, selectedSlot);
                onDismiss();
              }}
              className="flex-1 rounded-xl px-4 py-2.5 text-sm font-bold text-white"
              style={{ backgroundColor: palette.accentPrimary }}
            >
              انتقال بده
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
