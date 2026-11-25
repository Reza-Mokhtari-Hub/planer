import type { CategoryItem, CheerfulThemeId } from '../types/planner';

export interface PlannerPalette {
  backgroundMain: string;
  surfaceCard: string;
  surfaceWarmAlt: string;
  borderColor: string;
  textHeading: string;
  textMain: string;
  textMuted: string;
  accentPrimary: string;
  accentSecondary: string;
  glowTopStart: string;
  glowTopEnd: string;
  glowBottom: string;
  uniBg: string;
  uniBorder: string;
  uniText: string;
  studyBg: string;
  studyBorder: string;
  studyText: string;
  sportBg: string;
  sportBorder: string;
  sportText: string;
  restBg: string;
  restBorder: string;
  restText: string;
  personalBg: string;
  personalBorder: string;
  personalText: string;
}

const SHARED_CAT_COLORS = {
  uniBg: '#EEF2FF',
  uniBorder: '#A5B4FC',
  uniText: '#312E81',
  studyBg: '#FFF1F2',
  studyBorder: '#FDA4AF',
  studyText: '#881337',
  sportBg: '#ECFDF5',
  sportBorder: '#6EE7B7',
  sportText: '#064E3B',
  restBg: '#F0F9FF',
  restBorder: '#7DD3FC',
  restText: '#0C4A6E',
  personalBg: '#FFFBEB',
  personalBorder: '#FCD34D',
  personalText: '#78350F',
};

export function paletteForTheme(theme: CheerfulThemeId): PlannerPalette {
  switch (theme) {
    case 'theme-spring-blossom':
      return {
        ...SHARED_CAT_COLORS,
        backgroundMain: '#FFF5F7',
        surfaceCard: '#FFFFFF',
        surfaceWarmAlt: '#FFF1F2',
        borderColor: '#FECDD3',
        textHeading: '#831843',
        textMain: '#1E293B',
        textMuted: '#64748B',
        accentPrimary: '#F43F5E',
        accentSecondary: '#EC4899',
        glowTopStart: 'rgba(254, 205, 211, 0.34)',
        glowTopEnd: 'rgba(253, 230, 138, 0.27)',
        glowBottom: 'rgba(187, 247, 208, 0.27)',
      };
    case 'theme-ocean-mint':
      return {
        ...SHARED_CAT_COLORS,
        backgroundMain: '#F0FDFA',
        surfaceCard: '#FFFFFF',
        surfaceWarmAlt: '#ECFDF5',
        borderColor: '#99F6E4',
        textHeading: '#134E4A',
        textMain: '#0F172A',
        textMuted: '#475569',
        accentPrimary: '#0D9488',
        accentSecondary: '#0284C7',
        glowTopStart: 'rgba(204, 251, 241, 0.34)',
        glowTopEnd: 'rgba(224, 231, 255, 0.27)',
        glowBottom: 'rgba(186, 230, 253, 0.27)',
      };
    case 'theme-sunshine-warm':
      return {
        ...SHARED_CAT_COLORS,
        backgroundMain: '#FFFBEB',
        surfaceCard: '#FFFFFF',
        surfaceWarmAlt: '#FEF3C7',
        borderColor: '#FCD34D',
        textHeading: '#7C2D12',
        textMain: '#1C1917',
        textMuted: '#57534E',
        accentPrimary: '#EA580C',
        accentSecondary: '#D97706',
        glowTopStart: 'rgba(253, 224, 71, 0.34)',
        glowTopEnd: 'rgba(251, 146, 60, 0.27)',
        glowBottom: 'rgba(244, 114, 182, 0.27)',
      };
    case 'theme-energetic-light':
    default:
      return {
        ...SHARED_CAT_COLORS,
        backgroundMain: '#FFFDFA',
        surfaceCard: '#FFFFFF',
        surfaceWarmAlt: '#FFFBEB',
        borderColor: '#FDE68A',
        textHeading: '#0F172A',
        textMain: '#1E293B',
        textMuted: '#64748B',
        accentPrimary: '#F59E0B',
        accentSecondary: '#10B981',
        glowTopStart: 'rgba(254, 215, 170, 0.34)',
        glowTopEnd: 'rgba(216, 180, 254, 0.27)',
        glowBottom: 'rgba(167, 243, 208, 0.27)',
      };
  }
}

export interface CategoryVisualStyle {
  bg: string;
  border: string;
  text: string;
  accent: string;
  iconKey: 'uni' | 'study' | 'sport' | 'rest' | 'personal' | 'tag';
}

function hexToRgba(hex: string, alpha: number): string {
  const clean = hex.replace('#', '');
  if (clean.length !== 6) return `rgba(99, 102, 241, ${alpha})`;
  const r = Number.parseInt(clean.slice(0, 2), 16);
  const g = Number.parseInt(clean.slice(2, 4), 16);
  const b = Number.parseInt(clean.slice(4, 6), 16);
  if ([r, g, b].some((n) => Number.isNaN(n))) return `rgba(99, 102, 241, ${alpha})`;
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}

export function resolveCategoryStyle(
  categoryId: string,
  categories: CategoryItem[],
  palette: PlannerPalette
): CategoryVisualStyle {
  switch (categoryId) {
    case 'uni':
      return {
        bg: palette.uniBg,
        border: palette.uniBorder,
        text: palette.uniText,
        accent: '#4F46E5',
        iconKey: 'uni',
      };
    case 'study':
      return {
        bg: palette.studyBg,
        border: palette.studyBorder,
        text: palette.studyText,
        accent: '#E11D48',
        iconKey: 'study',
      };
    case 'sport':
      return {
        bg: palette.sportBg,
        border: palette.sportBorder,
        text: palette.sportText,
        accent: '#059669',
        iconKey: 'sport',
      };
    case 'rest':
      return {
        bg: palette.restBg,
        border: palette.restBorder,
        text: palette.restText,
        accent: '#0284C7',
        iconKey: 'rest',
      };
    case 'personal':
      return {
        bg: palette.personalBg,
        border: palette.personalBorder,
        text: palette.personalText,
        accent: '#D97706',
        iconKey: 'personal',
      };
    default: {
      const cat = categories.find((c) => c.id === categoryId);
      const base = cat?.color || '#6366F1';
      return {
        bg: hexToRgba(base, 0.12),
        border: hexToRgba(base, 0.45),
        text: palette.textHeading,
        accent: base,
        iconKey: 'tag',
      };
    }
  }
}
