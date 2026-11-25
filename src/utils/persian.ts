import type { DayInfo, TaskItem, TimeSlotInfo, WeekStartOption } from '../types/planner';

const PERSIAN_DIGITS = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

export const DAYS_MAP: Record<string, DayInfo> = {
  sat: { id: 'sat', fa: 'شنبه', shortFa: 'ش', en: 'Saturday', order: 0 },
  sun: { id: 'sun', fa: 'یکشنبه', shortFa: '۱ش', en: 'Sunday', order: 1 },
  mon: { id: 'mon', fa: 'دوشنبه', shortFa: '۲ش', en: 'Monday', order: 2 },
  tue: { id: 'tue', fa: 'سه‌شنبه', shortFa: '۳ش', en: 'Tuesday', order: 3 },
  wed: { id: 'wed', fa: 'چهارشنبه', shortFa: '۴ش', en: 'Wednesday', order: 4 },
  thu: { id: 'thu', fa: 'پنج‌شنبه', shortFa: '۵ش', en: 'Thursday', order: 5 },
  fri: { id: 'fri', fa: 'جمعه', shortFa: 'ج', en: 'Friday', order: 6 },
};

export const DEFAULT_DAYS_ORDER = ['sat', 'sun', 'mon', 'tue', 'wed', 'thu', 'fri'] as const;

export const TIME_SLOTS: TimeSlotInfo[] = [
  {
    id: 'morning_wake',
    title: 'بیدارباش و صبحانه',
    timeRange: '۰۶:۰۰ - ۰۷:۳۰',
    defaultStartTime: '06:00',
    colorHex: '#F59E0B',
    desc: 'بیداری ساعت ۶:۰۰ (شنبه تا چهارشنبه) و صبحانه مقوی',
  },
  {
    id: 'morning_early',
    title: 'صبح اول وقت',
    timeRange: '۰۷:۳۰ - ۰۹:۰۰',
    defaultStartTime: '07:30',
    colorHex: '#EAB308',
    desc: 'کلاس اول صبح دانشگاه',
  },
  {
    id: 'morning_main',
    title: 'صبح دانشگاه',
    timeRange: '۰۹:۰۰ - ۱۲:۰۰',
    defaultStartTime: '09:00',
    colorHex: '#6366F1',
    desc: 'کلاس‌های ۳ ساعته یا مطالعه طلایی',
  },
  {
    id: 'lunch_uni',
    title: 'ناهار و نماز دانشگاه',
    timeRange: '۱۲:۰۰ - ۱۳:۰۰',
    defaultStartTime: '12:00',
    colorHex: '#EA580C',
    desc: 'تایم ثابت ناهار (۱۲ تا ۱۳)',
    isLunch: true,
  },
  {
    id: 'afternoon_uni',
    title: 'بعدازظهر دانشگاه',
    timeRange: '۱۳:۰۰ - ۱۵:۳۰',
    defaultStartTime: '13:00',
    colorHex: '#0284C7',
    desc: 'کلاس‌های بعدازظهر، جراحی و فارما',
  },
  {
    id: 'afternoon_rest',
    title: 'عصرگاه و استراحت',
    timeRange: '۱۵:۳۰ - ۱۸:۰۰',
    defaultStartTime: '15:30',
    colorHex: '#14B8A6',
    desc: 'بازگشت به خانه، عصرانه و ریکاوری',
  },
  {
    id: 'evening_1',
    title: 'شب - بازه اول',
    timeRange: '۱۸:۰۰ - ۱۹:۳۰',
    defaultStartTime: '18:00',
    colorHex: '#10B981',
    desc: 'مطالعه کلیدی یا ۳ جلسه ورزش',
  },
  {
    id: 'evening_2',
    title: 'شب - بازه دوم',
    timeRange: '۱۹:۴۵ - ۲۱:۱۵',
    defaultStartTime: '19:45',
    colorHex: '#EC4899',
    desc: 'مرور و تست / پیش‌خوانی',
  },
  {
    id: 'night_rest',
    title: 'شام و پایان شب',
    timeRange: '۲۱:۳۰ - ۲۳:۰۰',
    defaultStartTime: '21:30',
    colorHex: '#8B5CF6',
    desc: 'شام، استراحت شبانه و خواب منظم',
  },
];

export const MOTIVATIONAL_QUOTES = [
  'هر مرور کوتاه امروز، آرامش شب امتحان فرداست.',
  'پشتکار آرام و پیوسته از فشار لحظه آخر قوی‌تر است.',
  'ورزش و ریکاوری بخشی از موفقیت تحصیلی است، نه اتلاف وقت.',
  'یک دندانپزشک موفق از تعادل جسم، ذهن و زمانش مراقبت می‌کند.',
  'قدم‌های کوچک اما منظم، کل ترم ۵ را به یک تجربه شیرین تبدیل می‌کنند.',
];

export function formatNumber(input: string | number, usePersian: boolean): string {
  const raw = String(input);
  if (!usePersian) return raw;
  return raw.replace(/[0-9]/g, (d) => PERSIAN_DIGITS[Number(d)]);
}

export function getOrderedDays(weekStart: WeekStartOption): string[] {
  switch (weekStart) {
    case 'sunday':
      return ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
    case 'monday':
      return ['mon', 'tue', 'wed', 'thu', 'fri', 'sat', 'sun'];
    case 'saturday':
    default:
      return ['sat', 'sun', 'mon', 'tue', 'wed', 'thu', 'fri'];
  }
}

export function getDayIdFromDate(date: Date): string {
  switch (date.getDay()) {
    case 6:
      return 'sat';
    case 0:
      return 'sun';
    case 1:
      return 'mon';
    case 2:
      return 'tue';
    case 3:
      return 'wed';
    case 4:
      return 'thu';
    case 5:
      return 'fri';
    default:
      return 'sat';
  }
}

export function getCurrentDayId(): string {
  return getDayIdFromDate(new Date());
}

export function resolveSlotFromTime(time: string): string {
  const normalized = time
    .replace(/[۰-۹]/g, (ch) => String(PERSIAN_DIGITS.indexOf(ch)))
    .trim();
  const parts = normalized.split(':');
  const h = Number.parseInt(parts[0] ?? '', 10);
  const m = Number.parseInt(parts[1] ?? '', 10);
  const hour = Number.isFinite(h) ? h : 12;
  const min = Number.isFinite(m) ? m : 0;
  const minutes = hour * 60 + min;

  if (minutes < 7 * 60 + 30) return 'morning_wake';
  if (minutes < 9 * 60) return 'morning_early';
  if (minutes < 12 * 60) return 'morning_main';
  if (minutes < 13 * 60) return 'lunch_uni';
  if (minutes < 15 * 60 + 30) return 'afternoon_uni';
  if (minutes < 18 * 60) return 'afternoon_rest';
  if (minutes < 19 * 60 + 40) return 'evening_1';
  if (minutes < 21 * 60 + 30) return 'evening_2';
  return 'night_rest';
}

export function getTaskSlot(task: TaskItem): string {
  if (task.timeSlot && task.timeSlot.trim().length > 0) {
    return task.timeSlot === 'noon' ? 'lunch_uni' : task.timeSlot;
  }
  return resolveSlotFromTime(task.time);
}

export function parseTimeMinutes(time: string): number {
  const normalized = time.replace(/[۰-۹]/g, (ch) => String(PERSIAN_DIGITS.indexOf(ch)));
  const parts = normalized.split(':');
  const hStr = (parts[0] ?? '').replace(/\D/g, '');
  const mStr = (parts[1] ?? '').replace(/\D/g, '');
  const h = hStr ? Number.parseInt(hStr, 10) : 99;
  const m = mStr ? Number.parseInt(mStr, 10) : 0;
  return h * 60 + m;
}

function gregorianToJalali(gy: number, gm: number, gd: number): [number, number, number] {
  const gdm = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
  const gy2 = gm > 2 ? gy + 1 : gy;
  let days =
    355666 +
    365 * gy +
    Math.floor((gy2 + 3) / 4) -
    Math.floor((gy2 + 99) / 100) +
    Math.floor((gy2 + 399) / 400) +
    gd +
    gdm[gm - 1];
  let jy = -1595 + 33 * Math.floor(days / 12053);
  days %= 12053;
  jy += 4 * Math.floor(days / 1461);
  days %= 1461;
  if (days > 365) {
    jy += Math.floor((days - 1) / 365);
    days = (days - 1) % 365;
  }
  const jm = days < 186 ? 1 + Math.floor(days / 31) : 7 + Math.floor((days - 186) / 30);
  const jd = 1 + (days < 186 ? days % 31 : (days - 186) % 30);
  return [jy, jm, jd];
}

export function formatJalaliDate(date: Date = new Date(), usePersianDigits = true): string {
  const gy = date.getFullYear();
  const gm = date.getMonth() + 1;
  const gd = date.getDate();
  const [jy, jm, jd] = gregorianToJalali(gy, gm, gd);
  const monthNames = [
    'فروردین',
    'اردیبهشت',
    'خرداد',
    'تیر',
    'مرداد',
    'شهریور',
    'مهر',
    'آبان',
    'آذر',
    'دی',
    'بهمن',
    'اسفند',
  ];
  const dayName = DAYS_MAP[getDayIdFromDate(date)]?.fa ?? '';
  const result = `${dayName} ${jd} ${monthNames[jm - 1]} ${jy}`;
  return formatNumber(result, usePersianDigits);
}

export function formatShortJalaliIsoDate(isoDate: string, usePersianDigits = true): string {
  const parsed = new Date(isoDate);
  const date = Number.isNaN(parsed.getTime()) ? new Date() : parsed;
  const [jy, jm, jd] = gregorianToJalali(date.getFullYear(), date.getMonth() + 1, date.getDate());
  const pad = (n: number) => String(n).padStart(2, '0');
  return formatNumber(`${String(jy).padStart(4, '0')}/${pad(jm)}/${pad(jd)}`, usePersianDigits);
}

export function getDayOfYear(date: Date = new Date()): number {
  const start = new Date(date.getFullYear(), 0, 0);
  const diff = date.getTime() - start.getTime();
  return Math.floor(diff / (1000 * 60 * 60 * 24));
}

export function nowIsoTimestamp(): string {
  return new Date().toISOString();
}
