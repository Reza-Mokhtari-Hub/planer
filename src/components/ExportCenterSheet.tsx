import { useEffect, useRef, useState } from 'react';
import { Code, Copy, Download, FileText, Image as ImageIcon, Printer, Upload, X } from 'lucide-react';
import type { PdfOrientation, PdfTemplateType, PlannerSettings, TaskItem } from '../types/planner';
import {
  downloadSchedulePdf,
  downloadSchedulePng,
  printSchedule,
  renderSchedulePreviewDataUrl,
} from '../utils/exportRenderer';
import type { PlannerPalette } from '../utils/theme';

interface ExportCenterSheetProps {
  tasks: TaskItem[];
  settings: PlannerSettings;
  palette: PlannerPalette;
  buildJsonBackupString: () => string;
  onImportJsonString: (raw: string) => boolean;
  onExportSuccess: (message: string) => void;
  onExportError: (message: string) => void;
  onDismiss: () => void;
}

export function ExportCenterSheet({
  tasks,
  settings,
  palette,
  buildJsonBackupString,
  onImportJsonString,
  onExportSuccess,
  onExportError,
  onDismiss,
}: ExportCenterSheetProps) {
  const [template, setTemplate] = useState<PdfTemplateType>('matrix');
  const [orientation, setOrientation] = useState<PdfOrientation>('landscape');
  const [includeStats, setIncludeStats] = useState(true);
  const [includeNotes, setIncludeNotes] = useState(true);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  const [isRendering, setIsRendering] = useState(true);
  const [rawJsonModal, setRawJsonModal] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    let cancelled = false;
    setIsRendering(true);
    renderSchedulePreviewDataUrl({
      weekTitle: settings.currentWeekTitle,
      tasks,
      weekStart: settings.weekStart,
      usePersianDigits: settings.persianNumbers,
      template,
      orientation,
      includeStats,
      includeNotes,
    })
      .then((url) => {
        if (!cancelled) {
          setPreviewUrl(url);
          setIsRendering(false);
        }
      })
      .catch(() => {
        if (!cancelled) setIsRendering(false);
      });
    return () => {
      cancelled = true;
    };
  }, [template, orientation, includeStats, includeNotes, tasks, settings]);

  const exportOpts = {
    weekTitle: settings.currentWeekTitle,
    tasks,
    weekStart: settings.weekStart,
    usePersianDigits: settings.persianNumbers,
    template,
    orientation,
    includeStats,
    includeNotes,
  };

  const handleSavePdf = async () => {
    try {
      await downloadSchedulePdf(exportOpts);
      onExportSuccess('فایل PDF استاندارد با موفقیت ذخیره شد');
    } catch (e) {
      onExportError(`خطا در ساخت PDF: ${e instanceof Error ? e.message : ''}`);
    }
  };

  const handlePrintPdf = async () => {
    try {
      await printSchedule(exportOpts);
    } catch (e) {
      onExportError(`خطا در آماده‌سازی چاپ: ${e instanceof Error ? e.message : ''}`);
    }
  };

  const handleSavePng = async () => {
    try {
      await downloadSchedulePng(exportOpts);
      onExportSuccess('تصویر باکیفیت PNG با موفقیت ذخیره شد');
    } catch (e) {
      onExportError(`خطا در ساخت PNG: ${e instanceof Error ? e.message : ''}`);
    }
  };

  const handleDownloadJson = () => {
    try {
      const raw = buildJsonBackupString();
      const blob = new Blob([raw], { type: 'application/json;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'weekly-planner-backup.json';
      a.click();
      URL.revokeObjectURL(url);
      onExportSuccess('فایل پشتیبان JSON با موفقیت ذخیره شد');
    } catch (e) {
      onExportError(`خطا در ذخیره JSON: ${e instanceof Error ? e.message : ''}`);
    }
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const text = await file.text();
      const ok = onImportJsonString(text);
      if (ok) onDismiss();
    } catch (err) {
      onExportError(`خطا در خواندن فایل: ${err instanceof Error ? err.message : ''}`);
    } finally {
      e.target.value = '';
    }
  };

  return (
    <div
      className="fixed inset-0 z-40 flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      aria-label="مرکز خروجی PDF، تصویر و پشتیبان JSON"
      onClick={onDismiss}
    >
      <div
        className="max-h-[92vh] w-full max-w-2xl overflow-y-auto rounded-3xl border p-5 shadow-2xl"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div
          className="flex items-center justify-between border-b pb-3"
          style={{ borderColor: palette.borderColor }}
        >
          <div className="flex items-center gap-2">
            <FileText size={20} style={{ color: palette.accentPrimary }} />
            <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
              مرکز خروجی PDF، تصویر و پشتیبان JSON
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

        <div className="mt-4 flex flex-col gap-4">
          {/* Template Selector */}
          <div>
            <p className="mb-2 text-xs font-bold" style={{ color: palette.textHeading }}>
              انتخاب قالب خروجی
            </p>
            <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
              {[
                {
                  id: 'matrix' as PdfTemplateType,
                  title: 'جدول ماتریسی کامل هفتگی',
                  desc: 'تمامی ۷ روز و ۹ بازه زمانی با تفکیک رنگی و ساعات ثابت',
                },
                {
                  id: 'checklist' as PdfTemplateType,
                  title: 'چک‌لیست ستونی ۷ روزه',
                  desc: '۷ کارت مجزا با کادر تیک‌زدن فیزیکی و اولویت‌ها',
                },
              ].map((t) => {
                const selected = template === t.id;
                return (
                  <button
                    key={t.id}
                    type="button"
                    onClick={() => setTemplate(t.id)}
                    className="rounded-2xl border p-3 text-right transition-colors"
                    style={{
                      backgroundColor: selected
                        ? `${palette.accentPrimary}18`
                        : palette.surfaceWarmAlt,
                      borderColor: selected ? palette.accentPrimary : palette.borderColor,
                      borderWidth: selected ? '1.5px' : '1px',
                    }}
                  >
                    <p className="text-xs font-bold" style={{ color: palette.textHeading }}>
                      {t.title}
                    </p>
                    <p className="mt-1 text-[11px] leading-relaxed" style={{ color: palette.textMuted }}>
                      {t.desc}
                    </p>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Orientation */}
          <div className="grid grid-cols-2 gap-2.5">
            {[
              { id: 'landscape' as PdfOrientation, label: 'افقی (Landscape)' },
              { id: 'portrait' as PdfOrientation, label: 'عمودی (Portrait)' },
            ].map((o) => {
              const selected = orientation === o.id;
              return (
                <button
                  key={o.id}
                  type="button"
                  onClick={() => setOrientation(o.id)}
                  className="rounded-xl border px-3 py-2 text-xs font-bold transition-colors"
                  style={{
                    backgroundColor: selected ? palette.accentPrimary : palette.surfaceWarmAlt,
                    borderColor: selected ? palette.accentPrimary : palette.borderColor,
                    color: selected ? '#FFFFFF' : palette.textMain,
                  }}
                >
                  {o.label}
                </button>
              );
            })}
          </div>

          {/* Toggles */}
          <div className="flex flex-col gap-2">
            <label className="flex cursor-pointer items-center justify-between text-sm" style={{ color: palette.textMain }}>
              <span>درج نوار آمار پیشرفت هفتگی</span>
              <input
                type="checkbox"
                checked={includeStats}
                onChange={(e) => setIncludeStats(e.target.checked)}
                className="h-4 w-4 accent-amber-500"
              />
            </label>
            <label className="flex cursor-pointer items-center justify-between text-sm" style={{ color: palette.textMain }}>
              <span>درج راهنمای تایم ناهار، ورزش و خواب</span>
              <input
                type="checkbox"
                checked={includeNotes}
                onChange={(e) => setIncludeNotes(e.target.checked)}
                className="h-4 w-4 accent-amber-500"
              />
            </label>
          </div>

          {/* Live Preview */}
          <div>
            <p className="mb-1.5 text-xs font-bold" style={{ color: palette.textHeading }}>
              پیش‌نمایش زنده خروجی
            </p>
            <div
              className="flex items-center justify-center overflow-hidden rounded-2xl border bg-white p-2"
              style={{
                borderColor: palette.borderColor,
                aspectRatio: orientation === 'landscape' ? '1.414 / 1' : '0.78 / 1',
                maxHeight: '340px',
              }}
            >
              {isRendering || !previewUrl ? (
                <div className="h-8 w-8 animate-spin rounded-full border-4 border-amber-200 border-t-amber-500" />
              ) : (
                <img
                  src={previewUrl}
                  alt="پیش‌نمایش خروجی برنامه"
                  className="h-full w-full object-contain"
                />
              )}
            </div>
          </div>

          {/* Export Buttons */}
          <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-3">
            <button
              type="button"
              onClick={handleSavePdf}
              className="flex items-center justify-center gap-1.5 rounded-xl px-3 py-2.5 text-xs font-bold text-white"
              style={{ backgroundColor: palette.accentPrimary }}
            >
              <Download size={16} />
              ذخیره PDF
            </button>
            <button
              type="button"
              onClick={handlePrintPdf}
              className="flex items-center justify-center gap-1.5 rounded-xl border px-3 py-2.5 text-xs font-bold hover:bg-black/5"
              style={{ borderColor: palette.borderColor, color: palette.textHeading }}
            >
              <Printer size={16} />
              چاپ مستقیم PDF
            </button>
            <button
              type="button"
              onClick={handleSavePng}
              className="flex items-center justify-center gap-1.5 rounded-xl border px-3 py-2.5 text-xs font-bold hover:bg-black/5"
              style={{ borderColor: palette.borderColor, color: palette.textHeading }}
            >
              <ImageIcon size={16} />
              ذخیره تصویر PNG
            </button>
          </div>

          <hr className="border-0 border-t" style={{ borderColor: palette.borderColor }} />

          <div>
            <p className="mb-2 text-xs font-bold" style={{ color: palette.textHeading }}>
              پشتیبان‌گیری و انتقال داده (JSON)
            </p>
            <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
              <button
                type="button"
                onClick={handleDownloadJson}
                className="flex items-center justify-center gap-1.5 rounded-xl border px-3 py-2.5 text-xs font-bold hover:bg-black/5"
                style={{ borderColor: palette.borderColor, color: palette.textHeading }}
              >
                <Download size={16} />
                ذخیره JSON
              </button>
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                className="flex items-center justify-center gap-1.5 rounded-xl border px-3 py-2.5 text-xs font-bold hover:bg-black/5"
                style={{ borderColor: palette.borderColor, color: palette.textHeading }}
              >
                <Upload size={16} />
                بازگردانی فایل JSON
              </button>
              <button
                type="button"
                onClick={() => setRawJsonModal(buildJsonBackupString())}
                className="flex items-center justify-center gap-1.5 rounded-xl border px-3 py-2.5 text-xs font-bold hover:bg-black/5"
                style={{ borderColor: palette.borderColor, color: palette.textHeading }}
              >
                <Code size={16} />
                مشاهده JSON خام
              </button>
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept=".json,application/json"
              className="hidden"
              onChange={handleFileChange}
            />
          </div>
        </div>
      </div>

      {rawJsonModal && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/60 p-4"
          onClick={() => setRawJsonModal(null)}
        >
          <div
            className="w-full max-w-xl rounded-2xl border p-4 shadow-2xl"
            style={{
              backgroundColor: palette.surfaceCard,
              borderColor: palette.borderColor,
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="mb-2 text-sm font-extrabold" style={{ color: palette.textHeading }}>
              ساختار JSON پشتیبان
            </h3>
            <pre
              dir="ltr"
              className="h-80 overflow-auto rounded-xl bg-slate-900 p-3 text-left font-mono text-xs text-slate-200"
            >
              {rawJsonModal}
            </pre>
            <div className="mt-3 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => setRawJsonModal(null)}
                className="rounded-xl border px-4 py-2 text-xs font-semibold"
                style={{ borderColor: palette.borderColor, color: palette.textMain }}
              >
                بستن
              </button>
              <button
                type="button"
                onClick={() => {
                  navigator.clipboard?.writeText(rawJsonModal).catch(() => {});
                  onExportSuccess('متن JSON در حافظه کپی شد');
                  setRawJsonModal(null);
                }}
                className="flex items-center gap-1.5 rounded-xl px-4 py-2 text-xs font-bold text-white"
                style={{ backgroundColor: palette.accentPrimary }}
              >
                <Copy size={14} />
                کپی در حافظه
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
