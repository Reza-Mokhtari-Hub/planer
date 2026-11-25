import type { PlannerPalette } from '../utils/theme';

interface ConfirmDialogProps {
  title: string;
  message: string;
  confirmLabel: string;
  destructive?: boolean;
  palette: PlannerPalette;
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  title,
  message,
  confirmLabel,
  destructive = false,
  palette,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/45 p-4 backdrop-blur-[2px]"
      role="dialog"
      aria-modal="true"
      onClick={onCancel}
    >
      <div
        className="w-full max-w-sm rounded-3xl border p-5 shadow-2xl"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h3 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
          {title}
        </h3>
        <p className="mt-2 text-sm leading-relaxed" style={{ color: palette.textMain }}>
          {message}
        </p>
        <div className="mt-4 flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="rounded-xl border px-4 py-2 text-xs font-semibold hover:bg-black/5"
            style={{ borderColor: palette.borderColor, color: palette.textMain }}
          >
            انصراف
          </button>
          <button
            type="button"
            onClick={onConfirm}
            className="rounded-xl px-4 py-2 text-xs font-bold text-white"
            style={{
              backgroundColor: destructive ? '#E11D48' : palette.accentPrimary,
            }}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
