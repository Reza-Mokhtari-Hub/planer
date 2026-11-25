import { useEffect, useRef, useState } from 'react';
import {
  AlertTriangle,
  Brain,
  Copy,
  Dumbbell,
  GraduationCap,
  Send,
  SlidersHorizontal,
  Sparkles,
  Trash2,
} from 'lucide-react';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { MarkdownText } from '../components/MarkdownText';
import {
  AI_MODEL_OPTIONS,
  type AiModelId,
  type ChatMessage,
  type PlannerSettings,
} from '../types/planner';
import type { PlannerPalette } from '../utils/theme';

interface GeminiMentorScreenProps {
  chatMessages: ChatMessage[];
  streamingAiText: string | null;
  isAiGenerating: boolean;
  settings: PlannerSettings;
  palette: PlannerPalette;
  onSendPreset: (presetKey: string) => void;
  onSendPrompt: (prompt: string) => void;
  onSelectModel: (modelId: AiModelId) => void;
  onClearChat: () => void;
  onCopySuccess: () => void;
}

export function GeminiMentorScreen({
  chatMessages,
  streamingAiText,
  isAiGenerating,
  settings,
  palette,
  onSendPreset,
  onSendPrompt,
  onSelectModel,
  onClearChat,
  onCopySuccess,
}: GeminiMentorScreenProps) {
  const [promptText, setPromptText] = useState('');
  const [showClearConfirm, setShowClearConfirm] = useState(false);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages.length, streamingAiText]);

  const handleSend = () => {
    const trimmed = promptText.trim();
    if (!trimmed || isAiGenerating) return;
    setPromptText('');
    onSendPrompt(trimmed);
  };

  const presets = [
    {
      key: 'comprehensive',
      title: 'توازن درسی و سلامت',
      subtitle: 'ناهار، دروس سنگین و ورزش',
      icon: Brain,
      accent: palette.accentPrimary,
    },
    {
      key: 'exams',
      title: 'استراتژی امتحانات',
      subtitle: 'مرور فعال و فارماکولوژی',
      icon: GraduationCap,
      accent: '#4F46E5',
    },
    {
      key: 'recovery',
      title: 'ارگونومی و ۳ ورزش',
      subtitle: 'جمعه، یکشنبه، سه‌شنبه + خواب',
      icon: Dumbbell,
      accent: palette.accentSecondary,
    },
    {
      key: 'optimize',
      title: 'بهینه‌سازی چیدمان',
      subtitle: 'شناسایی گلوگاه‌های خستگی',
      icon: SlidersHorizontal,
      accent: '#EC4899',
    },
  ];

  return (
    <div className="mx-auto flex h-[calc(100vh-4.5rem)] w-full max-w-4xl flex-col md:h-screen">
      {/* Top Header */}
      <header
        className="shrink-0 rounded-b-3xl border-b px-4 py-3 shadow-sm"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center gap-2.5">
            <Sparkles size={22} style={{ color: palette.accentPrimary }} />
            <div>
              <h1 className="text-base font-extrabold" style={{ color: palette.textHeading }}>
                منتور هوشمند Gemini (ترم ۵ دندانپزشکی)
              </h1>
              <p className="text-[11px]" style={{ color: palette.accentSecondary }}>
                تحلیل و پیشنهاد بر اساس برنامه هفتگی واقعی شما
              </p>
            </div>
          </div>

          {chatMessages.length > 0 && (
            <button
              type="button"
              onClick={() => setShowClearConfirm(true)}
              title="پاک کردن تاریخچه گفتگو"
              aria-label="پاک کردن تاریخچه گفتگو"
              className="flex h-10 w-10 items-center justify-center rounded-xl text-rose-600 hover:bg-rose-50"
            >
              <Trash2 size={18} />
            </button>
          )}
        </div>

        {/* Model Selector Chips */}
        <div className="mt-2.5 flex items-center gap-1.5 overflow-x-auto pb-0.5">
          {AI_MODEL_OPTIONS.map((opt) => {
            const active = settings.aiModel === opt.modelId;
            return (
              <button
                key={opt.modelId}
                type="button"
                onClick={() => onSelectModel(opt.modelId)}
                className="whitespace-nowrap shrink-0 rounded-xl border px-3 py-1.5 text-xs font-semibold transition-colors"
                style={{
                  backgroundColor: active ? palette.accentPrimary : palette.surfaceWarmAlt,
                  borderColor: active ? palette.accentPrimary : palette.borderColor,
                  color: active ? '#FFFFFF' : palette.textMain,
                }}
              >
                {opt.labelFa}
              </button>
            );
          })}
        </div>
      </header>

      {/* Scrollable Chat + Presets */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        <div className="flex flex-col gap-3">
          <div>
            <p className="mb-2 text-xs font-semibold" style={{ color: palette.textMuted }}>
              تحلیل‌های تخصصی یک‌کلیکی بر اساس برنامه فعلی شما:
            </p>
            <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
              {presets.map((p) => {
                const IconComp = p.icon;
                return (
                  <button
                    key={p.key}
                    type="button"
                    disabled={isAiGenerating}
                    onClick={() => onSendPreset(p.key)}
                    className="flex flex-col items-start gap-1 rounded-2xl border p-3.5 text-right transition-all hover:shadow-sm disabled:opacity-50"
                    style={{
                      backgroundColor: palette.surfaceCard,
                      borderColor: palette.borderColor,
                    }}
                  >
                    <IconComp size={20} style={{ color: p.accent }} />
                    <span className="text-sm font-bold" style={{ color: palette.textHeading }}>
                      {p.title}
                    </span>
                    <span className="text-[11px]" style={{ color: palette.textMuted }}>
                      {p.subtitle}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {chatMessages.map((msg) => {
            if (msg.role === 'user') {
              return (
                <div key={msg.id} className="flex justify-start">
                  <div
                    className="max-w-lg rounded-2xl rounded-tr-sm px-4 py-2.5 text-sm text-white shadow-sm"
                    style={{ backgroundColor: palette.accentPrimary }}
                  >
                    {msg.content}
                  </div>
                </div>
              );
            }
            if (msg.role === 'error') {
              return (
                <div
                  key={msg.id}
                  className="flex items-start gap-2.5 rounded-2xl border border-rose-300 bg-rose-50 p-3.5 text-xs text-rose-900"
                >
                  <AlertTriangle size={18} className="mt-0.5 shrink-0 text-rose-600" />
                  <p className="leading-relaxed">
                    {msg.content}
                    {'\n'}(در صورت قطع اینترنت، تمام قابلیت‌های مدیریت برنامه هفتگی بدون محدودیت فعال هستند.)
                  </p>
                </div>
              );
            }
            return (
              <div
                key={msg.id}
                className="rounded-2xl rounded-tl-sm border p-4 shadow-sm"
                style={{
                  backgroundColor: palette.surfaceCard,
                  borderColor: palette.borderColor,
                }}
              >
                <div className="mb-2 flex items-center justify-between">
                  <span
                    className="text-xs font-extrabold"
                    style={{ color: palette.accentSecondary }}
                  >
                    پیشنهاد منتور Gemini
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      navigator.clipboard?.writeText(msg.content).catch(() => {});
                      onCopySuccess();
                    }}
                    title="کپی پاسخ"
                    aria-label="کپی پاسخ"
                    className="flex h-8 w-8 items-center justify-center rounded-lg hover:bg-black/5"
                    style={{ color: palette.textMuted }}
                  >
                    <Copy size={15} />
                  </button>
                </div>
                <MarkdownText markdown={msg.content} palette={palette} />
              </div>
            );
          })}

          {isAiGenerating && (
            <div
              className="rounded-2xl border p-4"
              style={{
                backgroundColor: palette.surfaceWarmAlt,
                borderColor: palette.accentPrimary,
              }}
            >
              <div className="flex items-center gap-2">
                <div
                  className="h-4 w-4 animate-spin rounded-full border-2 border-t-transparent"
                  style={{ borderColor: `${palette.accentPrimary} transparent ${palette.accentPrimary} ${palette.accentPrimary}` }}
                />
                <span className="text-xs font-bold" style={{ color: palette.accentPrimary }}>
                  در حال تحلیل برنامه و نگارش پاسخ...
                </span>
              </div>
              {streamingAiText && streamingAiText.trim().length > 0 && (
                <div className="mt-3">
                  <MarkdownText markdown={streamingAiText} palette={palette} />
                </div>
              )}
            </div>
          )}

          <div ref={bottomRef} />
        </div>
      </div>

      {/* Prompt Composer */}
      <div
        className="shrink-0 border-t px-3 py-2.5 pb-20 md:pb-3"
        style={{
          backgroundColor: palette.surfaceCard,
          borderColor: palette.borderColor,
        }}
      >
        <div className="flex items-end gap-2">
          <textarea
            rows={1}
            value={promptText}
            onChange={(e) => setPromptText(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSend();
              }
            }}
            placeholder="سوال خود را درباره برنامه درسی بپرسید..."
            className="max-h-28 min-h-[44px] flex-1 resize-none rounded-2xl border px-3.5 py-2.5 text-sm outline-none"
            style={{ borderColor: palette.borderColor, color: palette.textMain }}
          />
          <button
            type="button"
            disabled={!promptText.trim() || isAiGenerating}
            onClick={handleSend}
            aria-label="ارسال پیام"
            className="flex h-11 w-12 shrink-0 items-center justify-center rounded-2xl text-white transition-opacity disabled:opacity-45"
            style={{ backgroundColor: palette.accentPrimary }}
          >
            <Send size={18} />
          </button>
        </div>
      </div>

      {showClearConfirm && (
        <ConfirmDialog
          title="پاک کردن تاریخچه گفتگو"
          message="تمام پیام‌های قبلی با منتور Gemini از حافظه پاک شوند؟"
          confirmLabel="پاک کن"
          destructive
          palette={palette}
          onConfirm={() => {
            onClearChat();
            setShowClearConfirm(false);
          }}
          onCancel={() => setShowClearConfirm(false)}
        />
      )}
    </div>
  );
}
