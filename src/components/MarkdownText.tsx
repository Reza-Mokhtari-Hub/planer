import type { ReactNode } from 'react';
import type { PlannerPalette } from '../utils/theme';

interface MarkdownTextProps {
  markdown: string;
  palette: PlannerPalette;
  baseColor?: string;
}

function renderInline(text: string, accentPrimary: string): ReactNode[] {
  const nodes: ReactNode[] = [];
  let idx = 0;
  let key = 0;

  while (idx < text.length) {
    if (text.startsWith('**', idx)) {
      const end = text.indexOf('**', idx + 2);
      if (end !== -1) {
        nodes.push(
          <strong key={key++} className="font-bold">
            {text.slice(idx + 2, end)}
          </strong>
        );
        idx = end + 2;
        continue;
      }
    }
    if (text[idx] === '`') {
      const end = text.indexOf('`', idx + 1);
      if (end !== -1) {
        nodes.push(
          <code
            key={key++}
            className="mx-0.5 rounded px-1.5 py-0.5 font-mono text-xs font-medium"
            style={{ backgroundColor: `${accentPrimary}22` }}
          >
            {text.slice(idx + 1, end)}
          </code>
        );
        idx = end + 1;
        continue;
      }
    }
    const nextSpecial = (() => {
      const boldIdx = text.indexOf('**', idx);
      const codeIdx = text.indexOf('`', idx);
      if (boldIdx === -1) return codeIdx;
      if (codeIdx === -1) return boldIdx;
      return Math.min(boldIdx, codeIdx);
    })();

    if (nextSpecial === -1) {
      nodes.push(text.slice(idx));
      break;
    } else if (nextSpecial === idx) {
      nodes.push(text[idx]);
      idx++;
    } else {
      nodes.push(text.slice(idx, nextSpecial));
      idx = nextSpecial;
    }
  }
  return nodes;
}

export function MarkdownText({ markdown, palette, baseColor }: MarkdownTextProps) {
  const lines = markdown.split(/\r?\n/);
  const elements: ReactNode[] = [];
  const textColor = baseColor ?? palette.textMain;

  let i = 0;
  let blockKey = 0;

  while (i < lines.length) {
    const trimmed = lines[i].trim();

    if (!trimmed) {
      elements.push(<div key={blockKey++} className="h-1.5" />);
    } else if (trimmed.startsWith('```')) {
      const codeLines: string[] = [];
      i++;
      while (i < lines.length && !lines[i].trim().startsWith('```')) {
        codeLines.push(lines[i]);
        i++;
      }
      elements.push(
        <pre
          key={blockKey++}
          dir="ltr"
          className="overflow-x-auto rounded-xl bg-slate-900 p-3 text-left font-mono text-xs leading-relaxed text-slate-200"
        >
          <code>{codeLines.join('\n')}</code>
        </pre>
      );
    } else if (trimmed.startsWith('### ')) {
      elements.push(
        <h4
          key={blockKey++}
          className="pt-1 text-sm font-bold"
          style={{ color: palette.textHeading }}
        >
          {renderInline(trimmed.slice(4), palette.accentPrimary)}
        </h4>
      );
    } else if (trimmed.startsWith('## ')) {
      elements.push(
        <h3
          key={blockKey++}
          className="pt-1.5 text-base font-extrabold"
          style={{ color: palette.textHeading }}
        >
          {renderInline(trimmed.slice(3), palette.accentPrimary)}
        </h3>
      );
    } else if (trimmed.startsWith('# ')) {
      elements.push(
        <h2
          key={blockKey++}
          className="pt-1.5 text-lg font-extrabold"
          style={{ color: palette.textHeading }}
        >
          {renderInline(trimmed.slice(2), palette.accentPrimary)}
        </h2>
      );
    } else if (trimmed.startsWith('> ')) {
      elements.push(
        <blockquote
          key={blockKey++}
          className="rounded-xl border px-3 py-2 text-sm"
          style={{
            backgroundColor: palette.surfaceWarmAlt,
            borderColor: palette.borderColor,
            color: palette.textHeading,
          }}
        >
          {renderInline(trimmed.slice(2), palette.accentPrimary)}
        </blockquote>
      );
    } else if (
      trimmed.startsWith('- ') ||
      trimmed.startsWith('* ') ||
      trimmed.startsWith('• ')
    ) {
      elements.push(
        <div key={blockKey++} className="flex items-start gap-2 pr-1 text-sm leading-relaxed">
          <span className="font-bold" style={{ color: palette.accentPrimary }}>
            •
          </span>
          <div className="flex-1" style={{ color: textColor }}>
            {renderInline(trimmed.slice(2), palette.accentPrimary)}
          </div>
        </div>
      );
    } else if (trimmed === '---' || trimmed === '***') {
      elements.push(
        <hr key={blockKey++} className="my-1 border-0 border-t" style={{ borderColor: palette.borderColor }} />
      );
    } else {
      elements.push(
        <p key={blockKey++} className="text-sm leading-relaxed" style={{ color: textColor }}>
          {renderInline(trimmed, palette.accentPrimary)}
        </p>
      );
    }
    i++;
  }

  return <div className="flex flex-col gap-1.5">{elements}</div>;
}
