import { useState } from 'react';
import {
  Archive,
  History,
  Repeat,
  RotateCcw,
  Trash2,
} from 'lucide-react';
import { ConfirmDialog } from '../components/ConfirmDialog';
import type { PlannerSettings, WeekArchiveItem } from '../types/planner';
import { formatNumber, formatShortJalaliIsoDate } from '../utils/persian';
import type { PlannerPalette } from '../utils/theme';

interface ArchiveScreenProps {
  archives: WeekArchiveItem[];
  settings: PlannerSettings;
  palette: PlannerPalette;
  onArchiveCurrentWeek: () => void;
  onRestoreArchive: (archiveId: string) => void;
  onRepeatFromArchive: (
    archiveId: string,
    archiveCurrentFirst: boolean,
    resetCompleted: boolean,
    resetReflection: boolean
  ) => void;
  onRepeatCurrentWeek: (
    archiveCurrentFirst: boolean,
    resetCompleted: boolean,
    resetReflection: boolean
  ) => void;
  onDeleteArchive: (archiveId: string) => void;
  onUncheckAllTasks: () => void;
  onArchiveAndClearCompleted: () => void;
  onClearAllTasks: () => void;
}

export function ArchiveScreen({
  archives,
  settings,
  palette,
  onArchiveCurrentWeek,
  onRestoreArchive,
  onRepeatFromArchive,
  onRepeatCurrentWeek,
  onDeleteArchive,
  onUncheckAllTasks,
  onArchiveAndClearCompleted,
  onClearAllTasks,
}: ArchiveScreenProps) {
  const usePersian = settings.persianNumbers;
  const [repeatTargetArchive, setRepeatTargetArchive] = useState<WeekArchiveItem | null>(null);
  const [showRepeatCurrentDialog, setShowRepeatCurrentDialog] = useState(false);
  const [confirmRestoreArchive, setConfirmRestoreArchive] = useState<WeekArchiveItem | null>(null);
  const [confirmDeleteArchive, setConfirmDeleteArchive] = useState<WeekArchiveItem | null>(null);
  const [confirmClearAllDialog, setConfirmClearAllDialog] = useState(false);
  const [confirmArchiveClearCompletedDialog, setConfirmArchiveClearCompletedDialog] =
    useState(false);

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-4 px-4 py-4 pb-24 md:pb-8">
      <h1 className="text-xl font-extrabold" style={{ color: palette.textHeading }}>
        آرشیو هفته‌ها و مدیریت چرخه هفته
      </h1>

      {/* Quick Archive & Repeat */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
          ثبت و تکرار برنامه هفتگی
        </h2>
        <p className="mt-1 text-xs leading-relaxed" style={{ color: palette.textMuted }}>
          هفته جاری را با حفظ جزئیات و یادداشت‌ها در تاریخچه ذخیره کنید یا با گزینه‌های هوشمند برای هفته جدید تکرار نمایید.
        </p>

        <div className="mt-3 grid grid-cols-1 gap-2.5 sm:grid-cols-2">
          <button
            type="button"
            onClick={onArchiveCurrentWeek}
            className="flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-xs font-bold text-white"
            style={{ backgroundColor: palette.accentPrimary }}
          >
            <Archive size={16} />
            آرشیو هفته جاری
          </button>
          <button
            type="button"
            onClick={() => setShowRepeatCurrentDialog(true)}
            className="flex items-center justify-center gap-2 rounded-xl border px-4 py-2.5 text-xs font-bold hover:bg-black/5"
            style={{ borderColor: palette.borderColor, color: palette.textHeading }}
          >
            <Repeat size={16} />
            تکرار هفته جدید
          </button>
        </div>
      </section>

      {/* Reset Week */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          بازنشانی وضعیت هفته جاری
        </h2>
        <div className="flex flex-col gap-2">
          <button
            type="button"
            onClick={onUncheckAllTasks}
            className="flex w-full items-center justify-center gap-2 rounded-xl border px-4 py-2.5 text-xs font-bold hover:bg-black/5"
            style={{ borderColor: palette.borderColor, color: palette.textHeading }}
          >
            <RotateCcw size={16} />
            برداشتن تمام تیک‌ها (شروع تازه بدون حذف کارها)
          </button>
          <button
            type="button"
            onClick={() => setConfirmArchiveClearCompletedDialog(true)}
            className="flex w-full items-center justify-center gap-2 rounded-xl border px-4 py-2.5 text-xs font-bold hover:bg-black/5"
            style={{ borderColor: palette.borderColor, color: palette.textHeading }}
          >
            <Archive size={16} />
            آرشیو هفته + حذف کارهای انجام‌شده
          </button>
          <button
            type="button"
            onClick={() => setConfirmClearAllDialog(true)}
            className="flex w-full items-center justify-center gap-2 rounded-xl border border-rose-200 px-4 py-2.5 text-xs font-bold text-rose-600 hover:bg-rose-50"
          >
            <Trash2 size={16} />
            پاک‌سازی کامل تمام کارهای هفته
          </button>
        </div>
      </section>

      {/* Archive History */}
      <div className="flex items-center gap-2 pt-1">
        <History size={18} style={{ color: palette.accentPrimary }} />
        <h2 className="text-base font-bold" style={{ color: palette.textHeading }}>
          {formatNumber(`تاریخچه هفته‌های آرشیوشده (${archives.length})`, usePersian)}
        </h2>
      </div>

      {archives.length === 0 ? (
        <div
          className="rounded-2xl border p-5 text-sm"
          style={{
            backgroundColor: palette.surfaceCard,
            borderColor: palette.borderColor,
            color: palette.textMuted,
          }}
        >
          هنوز هیچ هفته‌ای آرشیو نشده است. پس از پایان هفته می‌توانید گزارش آن را اینجا ذخیره کنید.
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {archives.map((arch) => {
            const wins = arch.reflection?.wins?.trim() ?? '';
            return (
              <div
                key={arch.id}
                className="rounded-2xl border p-4 shadow-sm"
                style={{
                  backgroundColor: palette.surfaceCard,
                  borderColor: palette.borderColor,
                }}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <h3 className="text-sm font-bold" style={{ color: palette.textHeading }}>
                      {arch.title}
                    </h3>
                    <p className="mt-0.5 text-xs tabular-nums" style={{ color: palette.textMuted }}>
                      {formatShortJalaliIsoDate(arch.date, usePersian)}
                    </p>
                  </div>
                  <span
                    className="shrink-0 text-xs font-extrabold tabular-nums"
                    style={{ color: palette.accentSecondary }}
                  >
                    {formatNumber(
                      `${arch.completedTasks}/${arch.totalTasks} (${arch.rate}%)`,
                      usePersian
                    )}
                  </span>
                </div>

                {wins && (
                  <p className="mt-2 text-xs leading-relaxed" style={{ color: palette.textMain }}>
                    نکات مثبت: {wins}
                  </p>
                )}

                <div className="mt-3 flex flex-wrap items-center gap-2">
                  <button
                    type="button"
                    onClick={() => setRepeatTargetArchive(arch)}
                    className="inline-flex flex-1 items-center justify-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-bold hover:bg-black/5"
                    style={{ borderColor: palette.borderColor, color: palette.textHeading }}
                  >
                    <Repeat size={14} />
                    تکرار
                  </button>
                  <button
                    type="button"
                    onClick={() => setConfirmRestoreArchive(arch)}
                    className="inline-flex flex-1 items-center justify-center gap-1.5 rounded-xl border px-3 py-2 text-xs font-bold hover:bg-black/5"
                    style={{ borderColor: palette.borderColor, color: palette.textHeading }}
                  >
                    <RotateCcw size={14} />
                    بازیابی
                  </button>
                  <button
                    type="button"
                    onClick={() => setConfirmDeleteArchive(arch)}
                    className="rounded-xl px-3 py-2 text-xs font-bold text-rose-600 hover:bg-rose-50"
                  >
                    حذف
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {showRepeatCurrentDialog && (
        <RepeatWeekOptionsDialog
          title="تکرار برنامه جاری برای هفته جدید"
          palette={palette}
          onDismiss={() => setShowRepeatCurrentDialog(false)}
          onConfirm={(archiveFirst, resetChecks, resetRef) => {
            onRepeatCurrentWeek(archiveFirst, resetChecks, resetRef);
            setShowRepeatCurrentDialog(false);
          }}
        />
      )}

      {repeatTargetArchive && (
        <RepeatWeekOptionsDialog
          title={`تکرار هفته از آرشیو «${repeatTargetArchive.title}»`}
          palette={palette}
          onDismiss={() => setRepeatTargetArchive(null)}
          onConfirm={(archiveFirst, resetChecks, resetRef) => {
            onRepeatFromArchive(repeatTargetArchive.id, archiveFirst, resetChecks, resetRef);
            setRepeatTargetArchive(null);
          }}
        />
      )}

      {confirmRestoreArchive && (
        <ConfirmDialog
          title="بازیابی برنامه آرشیوشده"
          message={`آیا مطمئن هستید؟ برنامه فعلی جایگزین برنامه «${confirmRestoreArchive.title}» خواهد شد.`}
          confirmLabel="بله، بازیابی شود"
          palette={palette}
          onConfirm={() => {
            onRestoreArchive(confirmRestoreArchive.id);
            setConfirmRestoreArchive(null);
          }}
          onCancel={() => setConfirmRestoreArchive(null)}
        />
      )}

      {confirmDeleteArchive && (
        <ConfirmDialog
          title="حذف از آرشیو"
          message={`آیا رکورد «${confirmDeleteArchive.title}» برای همیشه حذف شود؟`}
          confirmLabel="حذف"
          destructive
          palette={palette}
          onConfirm={() => {
            onDeleteArchive(confirmDeleteArchive.id);
            setConfirmDeleteArchive(null);
          }}
          onCancel={() => setConfirmDeleteArchive(null)}
        />
      )}

      {confirmArchiveClearCompletedDialog && (
        <ConfirmDialog
          title="آرشیو و پاک‌سازی انجام‌شده‌ها"
          message="ابتدا از هفته جاری نسخه آرشیو ساخته می‌شود و سپس کارهای تیک‌خورده حذف می‌شوند."
          confirmLabel="تایید"
          palette={palette}
          onConfirm={() => {
            onArchiveAndClearCompleted();
            setConfirmArchiveClearCompletedDialog(false);
          }}
          onCancel={() => setConfirmArchiveClearCompletedDialog(false)}
        />
      )}

      {confirmClearAllDialog && (
        <ConfirmDialog
          title="حذف تمام کارهای هفته"
          message="هشدار: تمام کارهای هفته جاری حذف خواهند شد. آیا ادامه می‌دهید؟"
          confirmLabel="بله، حذف کن"
          destructive
          palette={palette}
          onConfirm={() => {
            onClearAllTasks();
            setConfirmClearAllDialog(false);
          }}
          onCancel={() => setConfirmClearAllDialog(false)}
        />
      )}
    </div>
  );
}

interface RepeatWeekOptionsDialogProps {
  title: string;
  palette: PlannerPalette;
  onDismiss: () => void;
  onConfirm: (
    archiveCurrentFirst: boolean,
    resetCompleted: boolean,
    resetReflection: boolean
  ) => void;
}

function RepeatWeekOptionsDialog({
  title,
  palette,
  onDismiss,
  onConfirm,
}: RepeatWeekOptionsDialogProps) {
  const [archiveFirst, setArchiveFirst] = useState(true);
  const [resetCompleted, setResetCompleted] = useState(true);
  const [resetReflection, setResetReflection] = useState(true);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      onClick={onDismiss}
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
          {title}
        </h3>
        <div className="mt-4 flex flex-col gap-3 text-sm" style={{ color: palette.textMain }}>
          <label className="flex cursor-pointer items-center gap-2.5">
            <input
              type="checkbox"
              checked={archiveFirst}
              onChange={(e) => setArchiveFirst(e.target.checked)}
              className="h-4 w-4 accent-amber-500"
            />
            <span className="text-xs">ابتدا هفته جاری در آرشیو ذخیره شود</span>
          </label>
          <label className="flex cursor-pointer items-center gap-2.5">
            <input
              type="checkbox"
              checked={resetCompleted}
              onChange={(e) => setResetCompleted(e.target.checked)}
              className="h-4 w-4 accent-amber-500"
            />
            <span className="text-xs">تمام تیک‌ها برای شروع هفته جدید برداشته شوند</span>
          </label>
          <label className="flex cursor-pointer items-center gap-2.5">
            <input
              type="checkbox"
              checked={resetReflection}
              onChange={(e) => setResetReflection(e.target.checked)}
              className="h-4 w-4 accent-amber-500"
            />
            <span className="text-xs">فرم بازتاب هفتگی برای هفته جدید خالی شود</span>
          </label>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button
            type="button"
            onClick={onDismiss}
            className="rounded-xl border px-4 py-2 text-xs font-semibold"
            style={{ borderColor: palette.borderColor, color: palette.textMain }}
          >
            انصراف
          </button>
          <button
            type="button"
            onClick={() => onConfirm(archiveFirst, resetCompleted, resetReflection)}
            className="rounded-xl px-4 py-2 text-xs font-bold text-white"
            style={{ backgroundColor: palette.accentPrimary }}
          >
            اجرای تکرار هفته
          </button>
        </div>
      </div>
    </div>
  );
}
