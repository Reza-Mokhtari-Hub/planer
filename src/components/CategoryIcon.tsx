import {
  BedDouble,
  BookOpen,
  Dumbbell,
  GraduationCap,
  Tag,
  User,
} from 'lucide-react';
import type { CategoryVisualStyle } from '../utils/theme';

interface CategoryIconProps {
  iconKey: CategoryVisualStyle['iconKey'];
  color?: string;
  size?: number;
  className?: string;
}

export function CategoryIcon({ iconKey, color, size = 16, className }: CategoryIconProps) {
  const props = { size, style: color ? { color } : undefined, className };
  switch (iconKey) {
    case 'uni':
      return <GraduationCap {...props} />;
    case 'study':
      return <BookOpen {...props} />;
    case 'sport':
      return <Dumbbell {...props} />;
    case 'rest':
      return <BedDouble {...props} />;
    case 'personal':
      return <User {...props} />;
    default:
      return <Tag {...props} />;
  }
}
