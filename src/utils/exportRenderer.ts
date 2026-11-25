import { jsPDF } from 'jspdf';
import type {
  PdfOrientation,
  PdfTemplateType,
  TaskItem,
  WeekStartOption,
} from '../types/planner';
import {
  DAYS_MAP,
  TIME_SLOTS,
  formatJalaliDate,
  formatNumber,
  getOrderedDays,
  getTaskSlot,
  parseTimeMinutes,
} from './persian';

export interface RenderScheduleOptions {
  weekTitle: string;
  tasks: TaskItem[];
  weekStart: WeekStartOption;
  usePersianDigits: boolean;
  template: PdfTemplateType;
  orientation: PdfOrientation;
  includeStats: boolean;
  includeNotes: boolean;
  scaleFactor?: number;
}

function drawRoundedRect(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  w: number,
  h: number,
  r: number,
  fill?: string,
  stroke?: string,
  lineWidth = 1
) {
  ctx.beginPath();
  ctx.roundRect(x, y, w, h, r);
  if (fill) {
    ctx.fillStyle = fill;
    ctx.fill();
  }
  if (stroke) {
    ctx.strokeStyle = stroke;
    ctx.lineWidth = lineWidth;
    ctx.stroke();
  }
}

function wrapTextLines(
  ctx: CanvasRenderingContext2D,
  text: string,
  maxWidth: number,
  maxLines: number
): string[] {
  const paragraphs = text.split('\n');
  const lines: string[] = [];

  for (const para of paragraphs) {
    const words = para.split(/\s+/).filter(Boolean);
    if (words.length === 0) continue;
    let current = words[0];
    for (let i = 1; i < words.length; i++) {
      const candidate = `${current} ${words[i]}`;
      if (ctx.measureText(candidate).width <= maxWidth) {
        current = candidate;
      } else {
        lines.push(current);
        current = words[i];
        if (lines.length >= maxLines) break;
      }
    }
    if (lines.length < maxLines) {
      lines.push(current);
    }
    if (lines.length >= maxLines) break;
  }

  return lines.slice(0, maxLines);
}

export async function renderScheduleCanvas(opts: RenderScheduleOptions): Promise<HTMLCanvasElement> {
  if (typeof document !== 'undefined' && 'fonts' in document) {
    try {
      await document.fonts.ready;
    } catch {
      // Proceed with fallback font
    }
  }

  const scale = opts.scaleFactor ?? 1.5;
  const pageWidth = opts.orientation === 'landscape' ? 1188 : 840;
  const pageHeight = opts.orientation === 'landscape' ? 840 : 1188;

  const canvas = document.createElement('canvas');
  canvas.width = Math.round(pageWidth * scale);
  canvas.height = Math.round(pageHeight * scale);

  const ctx = canvas.getContext('2d');
  if (!ctx) return canvas;

  ctx.scale(scale, scale);
  ctx.direction = 'rtl';

  // White background
  ctx.fillStyle = '#FFFFFF';
  ctx.fillRect(0, 0, pageWidth, pageHeight);

  const margin = 24;
  let cursorY = margin;
  const contentWidth = pageWidth - margin * 2;

  // Header
  const headerTitle =
    opts.template === 'matrix'
      ? 'برنامه هفتگی و تقویم تحصیلی ترم ۵ دندانپزشکی'
      : 'چک‌لیست و برنامه روزانه ۷ روز هفته (ترم ۵ دندانپزشکی)';

  ctx.fillStyle = '#0F172A';
  ctx.font = '800 18px Vazirmatn, sans-serif';
  ctx.textAlign = 'right';
  ctx.fillText(headerTitle, pageWidth - margin, cursorY + 18);

  ctx.fillStyle = '#64748B';
  ctx.font = '400 11px Vazirmatn, sans-serif';
  ctx.fillText(
    `${opts.weekTitle}  •  ${formatJalaliDate(new Date(), opts.usePersianDigits)}`,
    pageWidth - margin,
    cursorY + 37
  );

  cursorY += 46;
  ctx.beginPath();
  ctx.moveTo(margin, cursorY);
  ctx.lineTo(pageWidth - margin, cursorY);
  ctx.strokeStyle = '#F59E0B';
  ctx.lineWidth = 2;
  ctx.stroke();
  cursorY += 10;

  // Stats Bar
  if (opts.includeStats) {
    const total = opts.tasks.length;
    const done = opts.tasks.filter((t) => t.completed).length;
    const pct = total === 0 ? 0 : Math.round((done * 100) / total);

    drawRoundedRect(ctx, margin, cursorY, contentWidth, 28, 8, '#FFFBEB', '#FDE68A', 1.2);
    ctx.fillStyle = '#78350F';
    ctx.font = '700 10.5px Vazirmatn, sans-serif';
    ctx.textAlign = 'right';
    const statsLine = formatNumber(
      `کل فعالیت‌ها: ${total}   |   تکمیل‌شده: ${done} (${pct}%)   |   باقیمانده: ${total - done}   |   ناهار ثابت: ۱۲ تا ۱۳   |   ورزش: ج، ۱ش، ۳ش`,
      opts.usePersianDigits
    );
    ctx.fillText(statsLine, pageWidth - margin - 10, cursorY + 18);
    cursorY += 36;
  }

  const notesHeight = opts.includeNotes ? 34 : 0;
  const bodyHeight = Math.max(200, pageHeight - margin - notesHeight - cursorY - 8);

  if (opts.template === 'matrix') {
    drawMatrixBody(ctx, margin, cursorY, contentWidth, bodyHeight, opts);
  } else {
    drawChecklistBody(ctx, margin, cursorY, contentWidth, bodyHeight, opts);
  }

  if (opts.includeNotes) {
    const noteTop = pageHeight - margin - 26;
    drawRoundedRect(ctx, margin, noteTop, contentWidth, 26, 6, '#F8FAFC', '#E2E8F0', 1);
    ctx.fillStyle = '#334155';
    ctx.font = '400 9.5px Vazirmatn, sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText(
      formatNumber(
        'راهنمای برنامه: بیدارباش ۰۶:۰۰ (شنبه تا چهارشنبه) • تایم ثابت ناهار ۱۲ تا ۱۳ • ۳ جلسه ورزش (جمعه، یکشنبه، سه‌شنبه) • شام ۲۱:۳۰',
        opts.usePersianDigits
      ),
      pageWidth - margin - 10,
      noteTop + 17
    );
  }

  return canvas;
}

function drawMatrixBody(
  ctx: CanvasRenderingContext2D,
  left: number,
  top: number,
  width: number,
  height: number,
  opts: RenderScheduleOptions
) {
  const orderedDays = getOrderedDays(opts.weekStart);
  const slots = TIME_SLOTS;
  const slotColWidth = 96;
  const dayColWidth = (width - slotColWidth) / orderedDays.length;
  const headerHeight = 28;
  const rowHeight = (height - headerHeight) / slots.length;

  // RTL columns: rightmost is slotCol
  const slotColLeft = left + dayColWidth * orderedDays.length;

  // Header cell for Time Slot
  ctx.fillStyle = '#FFFBEB';
  ctx.fillRect(slotColLeft, top, slotColWidth, headerHeight);
  ctx.strokeStyle = '#E2E8F0';
  ctx.lineWidth = 1;
  ctx.strokeRect(slotColLeft, top, slotColWidth, headerHeight);

  ctx.fillStyle = '#78350F';
  ctx.font = '700 10.5px Vazirmatn, sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText('بازه زمانی', slotColLeft + slotColWidth / 2, top + 18);

  orderedDays.forEach((dayKey, idx) => {
    const colRight = slotColLeft - idx * dayColWidth;
    const colLeft = colRight - dayColWidth;
    ctx.fillStyle = '#FFFBEB';
    ctx.fillRect(colLeft, top, dayColWidth, headerHeight);
    ctx.strokeStyle = '#E2E8F0';
    ctx.strokeRect(colLeft, top, dayColWidth, headerHeight);

    ctx.fillStyle = '#78350F';
    ctx.font = '700 10.5px Vazirmatn, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(DAYS_MAP[dayKey]?.fa ?? dayKey, colLeft + dayColWidth / 2, top + 18);
  });

  slots.forEach((slot, rIdx) => {
    const rowTop = top + headerHeight + rIdx * rowHeight;

    ctx.fillStyle = slot.isLunch ? '#FEF3C7' : '#FFF7ED';
    ctx.fillRect(slotColLeft, rowTop, slotColWidth, rowHeight);
    ctx.strokeStyle = '#E2E8F0';
    ctx.strokeRect(slotColLeft, rowTop, slotColWidth, rowHeight);

    ctx.fillStyle = '#0F172A';
    ctx.font = '700 9px Vazirmatn, sans-serif';
    ctx.textAlign = 'center';
    ctx.fillText(slot.title, slotColLeft + slotColWidth / 2, rowTop + rowHeight * 0.44);

    ctx.fillStyle = '#B45309';
    ctx.font = '400 8px Vazirmatn, sans-serif';
    ctx.fillText(
      formatNumber(slot.timeRange, opts.usePersianDigits),
      slotColLeft + slotColWidth / 2,
      rowTop + rowHeight * 0.72
    );

    orderedDays.forEach((dayKey, dIdx) => {
      const colRight = slotColLeft - dIdx * dayColWidth;
      const colLeft = colRight - dayColWidth;

      if (slot.isLunch) {
        ctx.fillStyle = '#FEF3C7';
        ctx.fillRect(colLeft, rowTop, dayColWidth, rowHeight);
      }
      ctx.strokeStyle = '#E2E8F0';
      ctx.strokeRect(colLeft, rowTop, dayColWidth, rowHeight);

      const cellTasks = opts.tasks.filter(
        (t) => t.day === dayKey && getTaskSlot(t) === slot.id
      );

      if (cellTasks.length > 0) {
        ctx.save();
        ctx.beginPath();
        ctx.rect(colLeft + 2, rowTop + 2, dayColWidth - 4, rowHeight - 4);
        ctx.clip();

        ctx.fillStyle = '#1E293B';
        ctx.font = '400 8px Vazirmatn, sans-serif';
        ctx.textAlign = 'right';

        const summary = cellTasks
          .map((t) => {
            const mark = t.completed ? '✓ ' : '• ';
            const timePrefix = t.time ? `${formatNumber(t.time, opts.usePersianDigits)} ` : '';
            return `${mark}${timePrefix}${t.title}`;
          })
          .join('\n');

        const maxLines = Math.max(1, Math.floor((rowHeight - 6) / 11));
        const lines = wrapTextLines(ctx, summary, dayColWidth - 10, maxLines);
        lines.forEach((line, lIdx) => {
          ctx.fillText(line, colRight - 5, rowTop + 12 + lIdx * 11);
        });
        ctx.restore();
      }
    });
  });
}

function drawChecklistBody(
  ctx: CanvasRenderingContext2D,
  left: number,
  top: number,
  width: number,
  height: number,
  opts: RenderScheduleOptions
) {
  const orderedDays = getOrderedDays(opts.weekStart);
  const cols = opts.orientation === 'landscape' ? 4 : 2;
  const rows = Math.ceil(orderedDays.length / cols);
  const gap = 10;
  const cardWidth = (width - gap * (cols - 1)) / cols;
  const cardHeight = (height - gap * (rows - 1)) / rows;

  orderedDays.forEach((dayKey, idx) => {
    const r = Math.floor(idx / cols);
    const cFromRight = idx % cols;
    const cardRight = left + width - cFromRight * (cardWidth + gap);
    const cardLeft = cardRight - cardWidth;
    const cardTop = top + r * (cardHeight + gap);

    drawRoundedRect(ctx, cardLeft, cardTop, cardWidth, cardHeight, 10, '#FFFFFF', '#CBD5E1', 1.2);

    const dayInfo = DAYS_MAP[dayKey];
    const dayTasks = opts.tasks
      .filter((t) => t.day === dayKey)
      .sort((a, b) => parseTimeMinutes(a.time) - parseTimeMinutes(b.time));

    ctx.fillStyle = '#78350F';
    ctx.font = '700 12px Vazirmatn, sans-serif';
    ctx.textAlign = 'right';
    ctx.fillText(
      formatNumber(`${dayInfo?.fa ?? dayKey} (${dayTasks.length})`, opts.usePersianDigits),
      cardRight - 10,
      cardTop + 18
    );

    ctx.beginPath();
    ctx.moveTo(cardLeft + 8, cardTop + 24);
    ctx.lineTo(cardRight - 8, cardTop + 24);
    ctx.strokeStyle = '#F59E0B';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    ctx.save();
    ctx.beginPath();
    ctx.rect(cardLeft + 6, cardTop + 28, cardWidth - 12, cardHeight - 34);
    ctx.clip();

    ctx.fillStyle = '#1E293B';
    ctx.font = '400 8.8px Vazirmatn, sans-serif';
    ctx.textAlign = 'right';

    const checklistText =
      dayTasks.length === 0
        ? 'برنامه‌ای ثبت نشده است.'
        : dayTasks
            .map((t) => {
              const box = t.completed ? '☑' : '☐';
              const timePart = t.time ? `[${formatNumber(t.time, opts.usePersianDigits)}] ` : '';
              return `${box} ${timePart}${t.title}`;
            })
            .join('\n');

    const maxLines = Math.max(2, Math.floor((cardHeight - 36) / 13));
    const lines = wrapTextLines(ctx, checklistText, cardWidth - 16, maxLines);
    lines.forEach((line, lIdx) => {
      ctx.fillText(line, cardRight - 10, cardTop + 39 + lIdx * 13);
    });

    ctx.restore();
  });
}

export async function renderSchedulePreviewDataUrl(opts: RenderScheduleOptions): Promise<string> {
  const canvas = await renderScheduleCanvas({ ...opts, scaleFactor: 1.2 });
  return canvas.toDataURL('image/png');
}

export async function downloadSchedulePng(opts: RenderScheduleOptions): Promise<void> {
  const canvas = await renderScheduleCanvas({ ...opts, scaleFactor: 2.0 });
  const dataUrl = canvas.toDataURL('image/png');
  const link = document.createElement('a');
  link.href = dataUrl;
  link.download = 'weekly-schedule-dental-term5.png';
  link.click();
}

export async function downloadSchedulePdf(opts: RenderScheduleOptions): Promise<void> {
  const canvas = await renderScheduleCanvas({ ...opts, scaleFactor: 2.0 });
  const imgData = canvas.toDataURL('image/png');
  const isLandscape = opts.orientation === 'landscape';
  const pdf = new jsPDF({
    orientation: isLandscape ? 'landscape' : 'portrait',
    unit: 'pt',
    format: 'a4',
  });
  const pageWidth = pdf.internal.pageSize.getWidth();
  const pageHeight = pdf.internal.pageSize.getHeight();
  pdf.addImage(imgData, 'PNG', 0, 0, pageWidth, pageHeight, undefined, 'FAST');
  pdf.save('weekly-schedule-dental-term5.pdf');
}

export async function printSchedule(opts: RenderScheduleOptions): Promise<void> {
  const canvas = await renderScheduleCanvas({ ...opts, scaleFactor: 2.0 });
  const dataUrl = canvas.toDataURL('image/png');
  const iframe = document.createElement('iframe');
  iframe.style.position = 'fixed';
  iframe.style.right = '0';
  iframe.style.bottom = '0';
  iframe.style.width = '0';
  iframe.style.height = '0';
  iframe.style.border = '0';
  document.body.appendChild(iframe);

  const doc = iframe.contentWindow?.document;
  if (!doc) {
    document.body.removeChild(iframe);
    return;
  }

  const pageSizeCss = opts.orientation === 'landscape' ? 'A4 landscape' : 'A4 portrait';
  doc.open();
  doc.write(`
    <!doctype html>
    <html>
      <head>
        <title>چاپ برنامه هفتگی</title>
        <style>
          @page { size: ${pageSizeCss}; margin: 0; }
          html, body { margin: 0; padding: 0; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; background: #fff; }
          img { max-width: 100%; max-height: 100%; object-fit: contain; }
        </style>
      </head>
      <body>
        <img src="${dataUrl}" alt="برنامه هفتگی" />
      </body>
    </html>
  `);
  doc.close();

  const img = doc.querySelector('img');
  const triggerPrint = () => {
    iframe.contentWindow?.focus();
    iframe.contentWindow?.print();
    setTimeout(() => {
      if (iframe.parentNode) {
        iframe.parentNode.removeChild(iframe);
      }
    }, 1500);
  };

  if (img && !img.complete) {
    img.onload = triggerPrint;
  } else {
    setTimeout(triggerPrint, 100);
  }
}
