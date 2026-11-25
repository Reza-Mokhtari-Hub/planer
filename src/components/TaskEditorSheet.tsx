import { useState } from 'react';
import { ListPlus, X } from 'lucide-react';
import { TASK_PRIORITIES, type CategoryItem, type TaskItem } from '../types/planner';
import { DAYS_MAP, DEFAULT_DAYS_ORDER, TIME_SLOTS, formatNumber, getTaskSlot } from '../utils/persian';
import type { PlannerPalette } from '../utils/theme';

interface TaskEditorSheetProps {
  initialTask: TaskItem | null;
  defaultDayId: string;
  defaultSlotId: string | null;
  categories: CategoryItem[];
  palette: PlannerPalette;
  usePersianDigits: boolean;
  onDismiss: () => void;
  onSave: (task: TaskItem, isNew: boolean) => void;
}

export function TaskEditorSheet({
  initialTask,
  defaultDayId,
  defaultSlotId,
  categories,
  palette,
  usePersianDigits,
  onDismiss,
  onSave,
}: TaskEditorSheetProps) {
  const isNew = initialTask === null;
  const resolvedInitialSlot =
    (initialTask ? getTaskSlot(initialTask) : defaultSlotId) || 'morning_main';
  const initialSlotInfo = TIME_SLOTS.find((s) => s.id === resolvedInitialSlot);

  const [title, setTitle] = useState(initialTask?.title ?? '');
  const [description, setDescription] = useState(initialTask?.description ?? '');
  const [selectedDay, setSelectedDay] = useState(initialTask?.day ?? defaultDayId);
  const [selectedSlot, setSelectedSlot] = useState(resolvedInitialSlot);
  const [timeText, setTimeText] = useState(
    (initialTask?.time && initialTask.time.trim()) ||
      initialSlotInfo?.defaultStartTime ||
      '09:00'
  );
  const [selectedCategory, setSelectedCategory] = useState(
    initialTask?.category ?? categories[0]?.id ?? 'study'
  );
  const [selectedPriority, setSelectedPriority] = useState(
    initialTask?.priority ?? 'medium'
  );
  const [difficulty, setDifficulty] = useState(initialTask?.difficulty ?? '');
  const [titleError, setTitleError] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setTitleError(true);
      return;
    }
    const task: TaskItem = {
      id: initialTask?.id ?? '',
      day: selectedDay,
      time: timeText.trim(),
      title: title.trim(),
      description: description.trim(),
      category: selectedCategory,
      priority: selectedPriority,
      completed: initialTask?.completed ?? false,
      difficulty: difficulty.trim(),
      timeSlot: selectedSlot,
      createdAt: initialTask?.createdAt ?? '',
    };
    onSave(task, isNew);
    onDismiss();
  };

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      aria-label={isNew ? 'افزودن کار جدید' : 'ویرایش فعالیت'}
      onClick={onDismiss}
    >
      <div
        className="max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-3xl border p-5 shadow-2xl"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between gap-4 border-b pb-3" style={{ borderColor: palette.borderColor }}>
          <div className="flex items-center gap-2">
            <ListPlus size={22} style={{ color: palette.accentPrimary }} />
            <h2 className="text-lg font-extrabold" style={{ color: palette.textHeading }}>
              {isNew ? 'افزودن کار جدید' : 'ویرایش فعالیت'}
            </h2>
          </div>
          <button
            type="button"
            onClick={onDismiss}
            aria-label="بستن"
            className="flex h-10 w-10 items-center justify-center rounded-xl hover:bg-black/5"
            style={{ color: palette.textMuted }}
          >
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 flex flex-col gap-4">
          <div>
            <label className="mb-1.5 block text-xs font-bold" style={{ color: palette.textHeading }}>
              عنوان فعالیت یا درس *
            </label>
            <input
              type="text"
              value={title}
              onChange={(e) => {
                setTitle(e.target.value);
                setTitleError(false);
              }}
              placeholder="مثلاً: مرور اندودانتیکس یا جلسه ورزش"
              className="w-full rounded-xl border px-3.5 py-2.5 text-sm outline-none transition-colors"
              style={{
                backgroundColor: palette.surfaceCard,
                borderColor: titleError ? '#E11D48' : palette.borderColor,
                color: palette.textMain,
              }}
            />
            {titleError && (
              <p className="mt-1 text-xs font-medium text-rose-600">
                لطفاً عنوان کار را وارد کنید
              </p>
            )}
          </div>

          {/* Day selector */}
          <div>
            <p className="mb-1.5 text-xs font-bold" style={{ color: palette.textHeading }}>
              روز هفته
            </p>
            <div className="flex flex-wrap gap-1.5">
              {DEFAULT_DAYS_ORDER.map((dayKey) => {
                const selected = selectedDay === dayKey;
                return (
                  <button
                    key={dayKey}
                    type="button"
                    onClick={() => setSelectedDay(dayKey)}
                    className="whitespace-nowrap rounded-xl border px-3 py-2 text-xs font-semibold transition-colors"
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

          {/* Time slot selector */}
          <div>
            <p className="mb-1.5 text-xs font-bold" style={{ color: palette.textHeading }}>
              بازه زمانی (ساعت استاندارد)
            </p>
            <div className="flex flex-wrap gap-1.5">
              {TIME_SLOTS.map((slot) => {
                const selected = selectedSlot === slot.id;
                return (
                  <button
                    key={slot.id}
                    type="button"
                    onClick={() => {
                      setSelectedSlot(slot.id);
                      setTimeText(slot.defaultStartTime);
                    }}
                    className="whitespace-nowrap rounded-xl border px-2.5 py-1.5 text-xs font-medium tabular-nums transition-colors"
                    style={{
                      backgroundColor: selected ? palette.accentPrimary : palette.surfaceWarmAlt,
                      borderColor: selected ? palette.accentPrimary : palette.borderColor,
                      color: selected ? '#FFFFFF' : palette.textMain,
                    }}
                  >
                    {formatNumber(`${slot.title} (${slot.defaultStartTime})`, usePersianDigits)}
                  </button>
                );
              })}
            </div>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-xs font-bold" style={{ color: palette.textHeading }}>
                ساعت دقیق
              </label>
              <input
                type="text"
                dir="ltr"
                value={timeText}
                onChange={(e) => setTimeText(e.target.value)}
                placeholder="09:00"
                className="w-full rounded-xl border px-3 py-2 text-center text-sm tabular-nums outline-none"
                style={{
                  borderColor: palette.borderColor,
                  color: palette.textMain,
                }}
              />
            </div>
            <div>
              <label className="mb-1 block text-xs font-bold" style={{ color: palette.textHeading }}>
                سطح سختی / تگ
              </label>
              <input
                type="text"
                value={difficulty}
                onChange={(e) => setDifficulty(e.target.value)}
                placeholder="سنگین، تئوری..."
                className="w-full rounded-xl border px-3 py-2 text-sm outline-none"
                style={{
                  borderColor: palette.borderColor,
                  color: palette.textMain,
                }}
              />
            </div>
          </div>

          {/* Category */}
          <div>
            <p className="mb-1.5 text-xs font-bold" style={{ color: palette.textHeading }}>
              دسته‌بندی
            </p>
            <div className="flex flex-wrap gap-1.5">
              {categories.map((cat) => {
                const selected = selectedCategory === cat.id;
                return (
                  <button
                    key={cat.id}
                    type="button"
                    onClick={() => setSelectedCategory(cat.id)}
                    className="whitespace-nowrap rounded-xl border px-3 py-2 text-xs font-semibold transition-colors"
                    style={{
                      backgroundColor: selected ? palette.accentSecondary : palette.surfaceWarmAlt,
                      borderColor: selected ? palette.accentSecondary : palette.borderColor,
                      color: selected ? '#FFFFFF' : palette.textMain,
                    }}
                  >
                    {cat.name}
                  </button>
                );
              })}
            </div>
          </div>

          {/* Priority */}
          <div>
            <p className="mb-1.5 text-xs font-bold" style={{ color: palette.textHeading }}>
              اولویت
            </p>
            <div className="grid grid-cols-3 gap-2">
              {TASK_PRIORITIES.map((pr) => {
                const selected = selectedPriority === pr.id;
                return (
                  <button
                    key={pr.id}
                    type="button"
                    onClick={() => setSelectedPriority(pr.id)}
                    className="whitespace-nowrap rounded-xl border px-2 py-2 text-xs font-semibold transition-colors"
                    style={{
                      backgroundColor: selected ? palette.accentPrimary : palette.surfaceWarmAlt,
                      borderColor: selected ? palette.accentPrimary : palette.borderColor,
                      color: selected ? '#FFFFFF' : palette.textMain,
                    }}
                  >
                    {pr.labelFa}
                  </button>
                );
              })}
            </div>
          </div>

          <div>
            <label className="mb-1 block text-xs font-bold" style={{ color: palette.textHeading }}>
              توضیحات یا نکات مطالعه
            </label>
            <textarea
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="سرفصل‌ها، مکان کلاس یا نکات تمرینی..."
              className="w-full rounded-xl border px-3.5 py-2.5 text-sm outline-none"
              style={{
                borderColor: palette.borderColor,
                color: palette.textMain,
              }}
            />
          </div>

          <div className="flex items-center gap-3 pt-1">
            <button
              type="button"
              onClick={onDismiss}
              className="flex-1 rounded-xl border px-4 py-2.5 text-sm font-semibold transition-colors hover:bg-black/5"
              style={{
                borderColor: palette.borderColor,
                color: palette.textMain,
              }}
            >
              انصراف
            </button>
            <button
              type="submit"
              className="flex-[1.4] rounded-xl px-4 py-2.5 text-sm font-bold text-white shadow-sm transition-opacity hover:opacity-95"
              style={{ backgroundColor: palette.accentPrimary }}
            >
              {isNew ? 'ثبت در برنامه' : 'ذخیره تغییرات'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
