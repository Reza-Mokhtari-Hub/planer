import { useState } from 'react';
import { CheckCircle2, FileText, Plus, RotateCcw, Trash2 } from 'lucide-react';
import { ConfirmDialog } from '../components/ConfirmDialog';
import {
  AI_MODEL_OPTIONS,
  CHEERFUL_THEMES,
  VIEW_MODE_OPTIONS,
  WEEK_START_OPTIONS,
  type AiModelId,
  type CategoryItem,
  type CheerfulThemeId,
  type PlannerSettings,
  type ViewMode,
  type WeekStartOption,
} from '../types/planner';
import { paletteForTheme, type PlannerPalette } from '../utils/theme';

interface SettingsScreenProps {
  settings: PlannerSettings;
  categories: CategoryItem[];
  palette: PlannerPalette;
  onSelectTheme: (theme: CheerfulThemeId) => void;
  onUpdateWeekTitle: (title: string) => void;
  onSelectWeekStart: (option: WeekStartOption) => void;
  onSelectViewMode: (mode: ViewMode) => void;
  onSetSoundEnabled: (enabled: boolean) => void;
  onSetPersianNumbers: (enabled: boolean) => void;
  onSetCompactView: (compact: boolean) => void;
  onSelectAiModel: (modelId: AiModelId) => void;
  onAddCategory: (name: string, colorHex: string) => void;
  onDeleteCategory: (catId: string) => void;
  onOpenExportCenter: () => void;
  onRestoreDentalSchedule: () => void;
}

const COLOR_OPTIONS = [
  '#6366F1',
  '#E11D48',
  '#10B981',
  '#0EA5E9',
  '#F59E0B',
  '#8B5CF6',
  '#EC4899',
];

export function SettingsScreen({
  settings,
  categories,
  palette,
  onSelectTheme,
  onUpdateWeekTitle,
  onSelectWeekStart,
  onSelectViewMode,
  onSetSoundEnabled,
  onSetPersianNumbers,
  onSetCompactView,
  onSelectAiModel,
  onAddCategory,
  onDeleteCategory,
  onOpenExportCenter,
  onRestoreDentalSchedule,
}: SettingsScreenProps) {
  const [weekTitleDraft, setWeekTitleDraft] = useState(settings.currentWeekTitle);
  const [newCategoryName, setNewCategoryName] = useState('');
  const [selectedColor, setSelectedColor] = useState(COLOR_OPTIONS[0]);
  const [showRestoreConfirm, setShowRestoreConfirm] = useState(false);

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-4 px-4 py-4 pb-24 md:pb-8">
      <h1 className="text-xl font-extrabold" style={{ color: palette.textHeading }}>
        تنظیمات، پوسته‌ها و شخصی‌سازی
      </h1>

      {/* 4 Cheerful Themes */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          انتخاب پوسته رنگی پرانرژی
        </h2>
        <div className="grid grid-cols-1 gap-2.5 sm:grid-cols-2">
          {CHEERFUL_THEMES.map((theme) => {
            const preview = paletteForTheme(theme.id);
            const selected = settings.theme === theme.id;
            return (
              <button
                key={theme.id}
                type="button"
                onClick={() => onSelectTheme(theme.id)}
                className="flex items-center gap-3 rounded-2xl border p-3 text-right transition-all"
                style={{
                  backgroundColor: preview.surfaceWarmAlt,
                  borderColor: selected ? preview.accentPrimary : preview.borderColor,
                  borderWidth: selected ? '2px' : '1px',
                }}
              >
                <span
                  className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm"
                  style={{ backgroundColor: preview.accentPrimary }}
                >
                  {theme.emoji}
                </span>
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-bold" style={{ color: preview.textHeading }}>
                    {theme.titleFa}
                  </p>
                  <p className="text-[11px]" style={{ color: preview.textMuted }}>
                    {theme.subtitleFa}
                  </p>
                </div>
                {selected && (
                  <CheckCircle2 size={18} style={{ color: preview.accentPrimary }} />
                )}
              </button>
            );
          })}
        </div>
      </section>

      {/* General Settings */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          تنظیمات عمومی برنامه
        </h2>

        <div className="flex items-center gap-2">
          <input
            type="text"
            value={weekTitleDraft}
            onChange={(e) => setWeekTitleDraft(e.target.value)}
            placeholder="عنوان برنامه هفته"
            className="flex-1 rounded-xl border px-3.5 py-2 text-sm outline-none"
            style={{ borderColor: palette.borderColor, color: palette.textMain }}
          />
          <button
            type="button"
            onClick={() => {
              if (weekTitleDraft.trim()) onUpdateWeekTitle(weekTitleDraft.trim());
            }}
            className="rounded-xl px-4 py-2 text-xs font-bold text-white"
            style={{ backgroundColor: palette.accentPrimary }}
          >
            ثبت
          </button>
        </div>

        <div className="mt-4 flex flex-col gap-3">
          {[
            {
              label: 'افکت‌های صوتی شاد هنگام تیک زدن',
              checked: settings.soundEnabled,
              onChange: onSetSoundEnabled,
            },
            {
              label: 'نمایش اعداد فارسی (۰ تا ۹)',
              checked: settings.persianNumbers,
              onChange: onSetPersianNumbers,
            },
            {
              label: 'نمای فشرده کارت‌های برنامه',
              checked: settings.compactView,
              onChange: onSetCompactView,
            },
          ].map((item) => (
            <label
              key={item.label}
              className="flex cursor-pointer items-center justify-between gap-3 text-sm"
              style={{ color: palette.textMain }}
            >
              <span>{item.label}</span>
              <input
                type="checkbox"
                checked={item.checked}
                onChange={(e) => item.onChange(e.target.checked)}
                className="h-4 w-4 accent-amber-500"
              />
            </label>
          ))}
        </div>

        <div className="mt-4">
          <p className="mb-2 text-xs font-bold" style={{ color: palette.textHeading }}>
            نمای پیش‌فرض برنامه هفتگی
          </p>
          <div className="flex flex-wrap gap-2">
            {VIEW_MODE_OPTIONS.map((vm) => {
              const selected = settings.viewMode === vm.id;
              return (
                <button
                  key={vm.id}
                  type="button"
                  onClick={() => onSelectViewMode(vm.id)}
                  className="whitespace-nowrap rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
                  style={{
                    backgroundColor: selected ? palette.accentPrimary : palette.surfaceWarmAlt,
                    borderColor: selected ? palette.accentPrimary : palette.borderColor,
                    color: selected ? '#FFFFFF' : palette.textMain,
                  }}
                >
                  {vm.labelFa}
                </button>
              );
            })}
          </div>
        </div>

        <div className="mt-4">
          <p className="mb-2 text-xs font-bold" style={{ color: palette.textHeading }}>
            روز شروع هفته
          </p>
          <div className="flex flex-col gap-1.5">
            {WEEK_START_OPTIONS.map((opt) => (
              <label
                key={opt.id}
                className="flex cursor-pointer items-center gap-2.5 text-sm"
                style={{ color: palette.textMain }}
              >
                <input
                  type="radio"
                  name="weekStart"
                  checked={settings.weekStart === opt.id}
                  onChange={() => onSelectWeekStart(opt.id)}
                  className="h-4 w-4 accent-amber-500"
                />
                <span>{opt.labelFa}</span>
              </label>
            ))}
          </div>
        </div>
      </section>

      {/* Categories Manager */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          مدیریت دسته‌بندی‌ها
        </h2>
        <div className="flex flex-col gap-2">
          {categories.map((cat) => (
            <div
              key={cat.id}
              className="flex items-center justify-between rounded-xl px-3 py-2"
              style={{ backgroundColor: palette.surfaceWarmAlt }}
            >
              <div className="flex items-center gap-2.5">
                <span
                  className="h-3.5 w-3.5 rounded-full"
                  style={{ backgroundColor: cat.color }}
                />
                <span className="text-sm font-medium" style={{ color: palette.textHeading }}>
                  {cat.name}
                </span>
              </div>
              {categories.length > 1 && (
                <button
                  type="button"
                  onClick={() => onDeleteCategory(cat.id)}
                  aria-label="حذف دسته‌بندی"
                  className="flex h-8 w-8 items-center justify-center rounded-lg text-rose-600 hover:bg-rose-50"
                >
                  <Trash2 size={16} />
                </button>
              )}
            </div>
          ))}
        </div>

        <div className="mt-3 flex items-center gap-2">
          <input
            type="text"
            value={newCategoryName}
            onChange={(e) => setNewCategoryName(e.target.value)}
            placeholder="نام دسته‌بندی جدید"
            className="flex-1 rounded-xl border px-3.5 py-2 text-sm outline-none"
            style={{ borderColor: palette.borderColor, color: palette.textMain }}
          />
          <button
            type="button"
            onClick={() => {
              if (newCategoryName.trim()) {
                onAddCategory(newCategoryName.trim(), selectedColor);
                setNewCategoryName('');
              }
            }}
            aria-label="افزودن دسته"
            className="flex h-10 w-11 items-center justify-center rounded-xl text-white"
            style={{ backgroundColor: palette.accentPrimary }}
          >
            <Plus size={18} />
          </button>
        </div>

        <div className="mt-2.5 flex items-center gap-2">
          {COLOR_OPTIONS.map((hex) => {
            const selected = selectedColor === hex;
            return (
              <button
                key={hex}
                type="button"
                onClick={() => setSelectedColor(hex)}
                aria-label={`رنگ ${hex}`}
                className="h-7 w-7 rounded-full transition-transform hover:scale-105"
                style={{
                  backgroundColor: hex,
                  outline: selected ? `2.5px solid ${palette.textHeading}` : 'none',
                  outlineOffset: '2px',
                }}
              />
            );
          })}
        </div>
      </section>

      {/* Gemini Model Selector */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
          تنظیمات هوش مصنوعی Gemini
        </h2>
        <p className="mt-1 text-xs leading-relaxed" style={{ color: palette.textMuted }}>
          تمامی درخواست‌ها از طریق سرور امن برنامه ارسال می‌شوند.
        </p>
        <div className="mt-3 flex flex-col gap-2">
          {AI_MODEL_OPTIONS.map((opt) => (
            <label
              key={opt.modelId}
              className="flex cursor-pointer items-center gap-2.5 rounded-xl border p-2.5"
              style={{
                borderColor:
                  settings.aiModel === opt.modelId
                    ? palette.accentPrimary
                    : palette.borderColor,
                backgroundColor:
                  settings.aiModel === opt.modelId
                    ? palette.surfaceWarmAlt
                    : 'transparent',
              }}
            >
              <input
                type="radio"
                name="aiModel"
                checked={settings.aiModel === opt.modelId}
                onChange={() => onSelectAiModel(opt.modelId)}
                className="h-4 w-4 accent-amber-500"
              />
              <div>
                <p className="text-sm font-bold" style={{ color: palette.textHeading }}>
                  {opt.labelFa}
                </p>
                <p className="text-[11px]" style={{ color: palette.textMuted }}>
                  {opt.descriptionFa}
                </p>
              </div>
            </label>
          ))}
        </div>
      </section>

      {/* Export & Restore */}
      <section
        className="rounded-3xl border p-4 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <h2 className="mb-3 text-base font-extrabold" style={{ color: palette.textHeading }}>
          خروجی، پشتیبان‌گیری و بازیابی الگو
        </h2>
        <div className="flex flex-col gap-2.5">
          <button
            type="button"
            onClick={onOpenExportCenter}
            className="flex w-full items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-xs font-bold text-white"
            style={{ backgroundColor: palette.accentSecondary }}
          >
            <FileText size={16} />
            باز کردن مرکز خروجی PDF، تصویر و JSON
          </button>
          <button
            type="button"
            onClick={() => setShowRestoreConfirm(true)}
            className="flex w-full items-center justify-center gap-2 rounded-xl border px-4 py-2.5 text-xs font-bold hover:bg-black/5"
            style={{ borderColor: palette.borderColor, color: palette.textHeading }}
          >
            <RotateCcw size={16} />
            بازگردانی برنامه استاندارد ترم ۵ دندانپزشکی
          </button>
        </div>
      </section>

      {showRestoreConfirm && (
        <ConfirmDialog
          title="بازگردانی برنامه ترم ۵ دندانپزشکی"
          message="برنامه فعلی در آرشیو ذخیره و برنامه ۶۳ وظیفه‌ای استاندارد ترم ۵ جایگزین خواهد شد."
          confirmLabel="تایید بازیابی"
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
