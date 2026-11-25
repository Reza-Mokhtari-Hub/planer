export interface TaskItem {
  id: string;
  day: string;
  time: string;
  title: string;
  description: string;
  category: string;
  priority: 'high' | 'medium' | 'low' | string;
  completed: boolean;
  difficulty: string;
  timeSlot: string;
  createdAt: string;
}

export interface CategoryItem {
  id: string;
  name: string;
  color: string;
  icon: string;
}

export interface ReflectionData {
  wins: string;
  challenges: string;
  improvements: string;
  rating: number;
}

export interface WeekArchiveItem {
  id: string;
  date: string;
  title: string;
  totalTasks: number;
  completedTasks: number;
  rate: number;
  tasksSnapshot: TaskItem[];
  reflection?: ReflectionData | null;
}

export type CheerfulThemeId =
  | 'theme-energetic-light'
  | 'theme-spring-blossom'
  | 'theme-ocean-mint'
  | 'theme-sunshine-warm';

export interface CheerfulThemeInfo {
  id: CheerfulThemeId;
  titleFa: string;
  subtitleFa: string;
  emoji: string;
}

export const CHEERFUL_THEMES: CheerfulThemeInfo[] = [
  { id: 'theme-energetic-light', titleFa: 'شاد و آفتابی', subtitleFa: 'پرانرژی و سرزنده', emoji: '☀️' },
  { id: 'theme-spring-blossom', titleFa: 'بهاری و گل‌بهی', subtitleFa: 'پاستلی شاداب', emoji: '🌸' },
  { id: 'theme-ocean-mint', titleFa: 'اقیانوس و نعنا', subtitleFa: 'خنک و باطراوت', emoji: '🌊' },
  { id: 'theme-sunshine-warm', titleFa: 'پرتقالی فوق‌انرژی', subtitleFa: 'سوپر شارژ و گرم', emoji: '⚡' },
];

export type WeekStartOption = 'saturday' | 'sunday' | 'monday';

export const WEEK_START_OPTIONS: { id: WeekStartOption; labelFa: string }[] = [
  { id: 'saturday', labelFa: 'شنبه (تقویم رسمی ایران)' },
  { id: 'sunday', labelFa: 'یکشنبه (تقویم غربی)' },
  { id: 'monday', labelFa: 'دوشنبه (ISO بین‌المللی)' },
];

export type ViewMode = 'table' | 'cards';

export const VIEW_MODE_OPTIONS: { id: ViewMode; labelFa: string }[] = [
  { id: 'table', labelFa: 'نمای جدولی (ماتریس)' },
  { id: 'cards', labelFa: 'نمای کارتی (تایم‌لاین)' },
];

export type AiModelId = 'gemini-4-hydrogen' | 'gemini-3.1-flash-lite' | 'gemini-4-hydrogen-deep';

export interface AiModelOptionInfo {
  modelId: AiModelId;
  labelFa: string;
  descriptionFa: string;
}

export const AI_MODEL_OPTIONS: AiModelOptionInfo[] = [
  { modelId: 'gemini-4-hydrogen', labelFa: 'Gemini 4 Hydrogen', descriptionFa: 'پیشنهادی، سریع و دقیق' },
  { modelId: 'gemini-3.1-flash-lite', labelFa: 'Gemini 3.1 Flash-Lite', descriptionFa: 'فوق‌سریع و سبک' },
  { modelId: 'gemini-4-hydrogen-deep', labelFa: 'Gemini 4 Hydrogen (تحلیل عمیق)', descriptionFa: 'تحلیل عمیق و استدلال پیچیده' },
];

export type PdfTemplateType = 'matrix' | 'checklist';
export type PdfOrientation = 'landscape' | 'portrait';

export interface DayInfo {
  id: string;
  fa: string;
  shortFa: string;
  en: string;
  order: number;
}

export interface TimeSlotInfo {
  id: string;
  title: string;
  timeRange: string;
  defaultStartTime: string;
  colorHex: string;
  desc: string;
  isLunch?: boolean;
}

export interface PlannerSettings {
  theme: CheerfulThemeId;
  currentWeekTitle: string;
  weekStart: WeekStartOption;
  soundEnabled: boolean;
  persianNumbers: boolean;
  compactView: boolean;
  viewMode: ViewMode;
  aiModel: AiModelId;
}

export interface ChatMessage {
  id: string;
  role: 'user' | 'model' | 'error';
  content: string;
  presetTag?: string;
  timestampMillis: number;
}

export interface TaskFilterState {
  query: string;
  categoryId: string;
  priority: string;
  status: string;
}

export interface PlannerBackupPayload {
  version: string;
  appName: string;
  exportedAt: string;
  currentWeekTitle: string;
  scheduleVersion?: string;
  settings: {
    theme: string;
    weekStart: string;
    soundEnabled: boolean;
    persianNumbers: boolean;
    compactView: boolean;
    viewMode: string;
  };
  categories: CategoryItem[];
  tasks: TaskItem[];
  reflection: ReflectionData;
  archives: WeekArchiveItem[];
}

export const TASK_PRIORITIES: { id: 'high' | 'medium' | 'low'; labelFa: string }[] = [
  { id: 'high', labelFa: 'بالا (فوری)' },
  { id: 'medium', labelFa: 'متوسط' },
  { id: 'low', labelFa: 'پایین' },
];

export type NavTabId = 'dashboard' | 'weekly' | 'stats' | 'archive' | 'gemini' | 'settings';
