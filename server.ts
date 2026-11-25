import express from 'express';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { GoogleGenAI, ThinkingLevel } from '@google/genai';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const SYSTEM_INSTRUCTION = `شما یک مشاور و منتور ارشد فوق‌تخصصی تحصیلی برای دانشجویان پزشکی و دندانپزشکی (به‌ویژه ترم ۵ دندانپزشکی) و متخصص ارگونومی، برنامه‌ریزی عصبی-شناختی و مدیریت زمان هستید.

وظیفه شما:
تحلیل، بهینه‌سازی و پیشنهاد استراتژی‌های عملی و واقع‌گرایانه برای دانشجوی ترم ۵ دندانپزشکی بر اساس اطلاعات واقعی برنامه هفتگی ارائه‌شده.

اصول کلیدی برنامه دانشجوی دندانپزشکی:
۱. بیدارباش و صبحانه: ساعت ۰۶:۰۰ صبح شنبه تا چهارشنبه برای تغذیه مقوی، شادابی و حضور به‌موقع در کلاس ۰۷:۳۰ دانشگاه.
۲. دروس سنگین ترم ۵: فارماکولوژی، ترمیم ۱ و عملی ترمیم، پروتز کامل نظری و عملی، اندودانتیکس، رادیولوژی دهان، ارگونومی و بیماری‌های دهان و سیستمیک.
۳. تایم ثابت ناهار و نماز: همواره ساعت ۱۲:۰۰ تا ۱۳:۰۰ در تمامی روزها.
۴. ۳ جلسه ورزش هفتگی: روزهای جمعه، یک‌شنبه و سه‌شنبه (ساعت ۱۸:۰۰ به بعد) جهت کاهش استرس، تقویت عضلات کمر و گردن دندانپزشک و شادابی ذهنی.
۵. تایم ثابت شام و استراحت: همواره ساعت ۲۱:۳۰ تا ۲۳:۰۰ در تمام روزها و خواب منظم ساعت ۲۳:۰۰.
۶. خستگی بعد از کلینیک و پری‌کلینیک: در نظر گرفتن تایم ریکاوری بعدازظهر و اختصاص بازه شب به دروس نیازمند تمرکز بالا با تکنیک‌های فاصله‌گذاری و مرور فعال (Active Recall & Spaced Repetition).

قوانین مهم:
- پاسخ‌ها باید به زبان فارسی بسیار شیوا، صمیمی، علمی، انگیزه‌بخش، شاد و کاملاً ساختاریافته (با تیترهای Markdown، بولت‌پوینت‌های دقیق و ایموجی‌های مرتبط) ارائه شود.
- شما اجازه ندارید برنامه کاربر را به‌طور خودکار تغییر دهید؛ تمام پیشنهادها را به‌صورت شفاف و گام‌به‌گام به کاربر ارائه دهید تا در صورت تمایل خودش اعمال کند.`;

interface IncomingTask {
  day: string;
  time: string;
  title: string;
  category: string;
  priority: string;
  difficulty: string;
  completed: boolean;
}

interface IncomingReflection {
  wins: string;
  challenges: string;
  improvements: string;
  rating: number;
}

interface IncomingChatMsg {
  role: 'user' | 'model' | 'error';
  content: string;
}

const DAYS_FA: Record<string, string> = {
  sat: 'شنبه',
  sun: 'یکشنبه',
  mon: 'دوشنبه',
  tue: 'سه‌شنبه',
  wed: 'چهارشنبه',
  thu: 'پنج‌شنبه',
  fri: 'جمعه',
};

const DAYS_ORDER = ['sat', 'sun', 'mon', 'tue', 'wed', 'thu', 'fri'];

function parseTimeMinutes(time: string): number {
  const normalized = (time || '').replace(/[۰-۹]/g, (ch) =>
    String('۰۱۲۳۴۵۶۷۸۹'.indexOf(ch))
  );
  const [hStr = '', mStr = ''] = normalized.split(':');
  const h = Number.parseInt(hStr.replace(/\D/g, ''), 10);
  const m = Number.parseInt(mStr.replace(/\D/g, ''), 10);
  return (Number.isFinite(h) ? h : 99) * 60 + (Number.isFinite(m) ? m : 0);
}

function buildScheduleContextJson(
  weekTitle: string,
  tasks: IncomingTask[],
  reflection: IncomingReflection
): string {
  const total = tasks.length;
  const done = tasks.filter((t) => t.completed).length;
  const daysSummary: Record<string, unknown[]> = {};

  for (const dayKey of DAYS_ORDER) {
    const dayName = DAYS_FA[dayKey] ?? dayKey;
    daysSummary[dayName] = tasks
      .filter((t) => t.day === dayKey)
      .sort((a, b) => parseTimeMinutes(a.time) - parseTimeMinutes(b.time))
      .map((t) => ({
        time: t.time,
        title: t.title,
        category: t.category,
        priority: t.priority,
        difficulty: t.difficulty,
        completed: t.completed,
      }));
  }

  return JSON.stringify(
    {
      weekTitle,
      totalTasks: total,
      completedTasks: done,
      completionRatePercent: total === 0 ? 0 : Math.round((done * 100) / total),
      sportTasksCount: tasks.filter((t) => t.category === 'sport').length,
      studyTasksCount: tasks.filter((t) => t.category === 'study').length,
      uniTasksCount: tasks.filter((t) => t.category === 'uni').length,
      restTasksCount: tasks.filter((t) => t.category === 'rest').length,
      reflection,
      daysSummary,
    },
    null,
    2
  );
}

async function startServer() {
  const app = express();
  app.use(express.json({ limit: '2mb' }));

  app.post('/api/ai/stream', async (req, res) => {
    res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
    res.setHeader('Cache-Control', 'no-cache, no-transform');
    res.setHeader('Connection', 'keep-alive');

    const sendEvent = (payload: Record<string, unknown>) => {
      res.write(`data: ${JSON.stringify(payload)}\n\n`);
    };

    try {
      const apiKey = process.env.GEMINI_API_KEY;
      if (!apiKey) {
        sendEvent({
          type: 'error',
          message:
            'کلید Gemini API در سرور تنظیم نشده است. لطفاً کلید را در بخش Settings > Secrets بررسی کنید.',
        });
        res.end();
        return;
      }

      const {
        modelId = 'gemini-4-hydrogen',
        userPrompt = '',
        weekTitle = '',
        tasks = [],
        reflection = { wins: '', challenges: '', improvements: '', rating: 5 },
        chatHistory = [],
      } = req.body as {
        modelId?: string;
        userPrompt?: string;
        weekTitle?: string;
        tasks?: IncomingTask[];
        reflection?: IncomingReflection;
        chatHistory?: IncomingChatMsg[];
      };

      const trimmedPrompt = userPrompt.trim();
      if (!trimmedPrompt) {
        sendEvent({ type: 'error', message: 'متن پرسش نمی‌تواند خالی باشد.' });
        res.end();
        return;
      }

      const ai = new GoogleGenAI({
        apiKey,
        httpOptions: {
          headers: {
            'User-Agent': 'aistudio-build',
          },
        },
      });

      let resolvedModel = 'gemini-4-hydrogen';
      let thinkingConfig: { thinkingLevel: ThinkingLevel } | undefined = {
        thinkingLevel: ThinkingLevel.MEDIUM,
      };

      if (modelId === 'gemini-3.1-flash-lite') {
        resolvedModel = 'gemini-3.1-flash-lite';
        thinkingConfig = undefined;
      } else if (modelId === 'gemini-4-hydrogen-deep') {
        resolvedModel = 'gemini-4-hydrogen';
        thinkingConfig = { thinkingLevel: ThinkingLevel.HIGH };
      }

      const historyContents = (chatHistory || [])
        .filter((m) => m.role === 'user' || m.role === 'model')
        .slice(-10)
        .map((m) => ({
          role: m.role,
          parts: [{ text: m.content }],
        }));

      const scheduleJson = buildScheduleContextJson(weekTitle, tasks, reflection);
      const fullPrompt = `اطلاعات واقعی برنامه هفتگی جاری کاربر:\n\`\`\`json\n${scheduleJson}\n\`\`\`\n\nپرسش یا درخواست کاربر:\n${trimmedPrompt}`;

      const chat = ai.chats.create({
        model: resolvedModel,
        history: historyContents,
        config: {
          systemInstruction: SYSTEM_INSTRUCTION,
          ...(thinkingConfig ? { thinkingConfig } : {}),
        },
      });

      const streamResponse = await chat.sendMessageStream({
        message: fullPrompt,
      });

      let accumulated = '';
      for await (const chunk of streamResponse) {
        const piece = chunk.text ?? '';
        if (piece) {
          accumulated += piece;
          sendEvent({ type: 'chunk', text: accumulated });
        }
      }

      if (accumulated.trim().length > 0) {
        sendEvent({ type: 'done', finalText: accumulated });
      } else {
        sendEvent({ type: 'error', message: 'پاسخی از مدل هوش مصنوعی دریافت نشد.' });
      }
      res.end();
    } catch (err) {
      const rawMessage = err instanceof Error ? err.message : String(err);
      let friendly = `خطا در ارتباط امن با Gemini: ${rawMessage}`;
      if (rawMessage.includes('PERMISSION_DENIED') || rawMessage.includes('API_KEY_INVALID')) {
        friendly =
          'خطای دسترسی یا کلید نامعتبر Gemini API. لطفاً کلید را در بخش Settings > Secrets بررسی کنید.';
      } else if (rawMessage.includes('RESOURCE_EXHAUSTED') || rawMessage.includes('429')) {
        friendly = 'سهمیه موقت سرویس Gemini پر شده است. لطفاً چند لحظه بعد دوباره تلاش کنید.';
      }
      sendEvent({ type: 'error', message: friendly });
      res.end();
    }
  });

  if (process.env.NODE_ENV !== 'production') {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distDir = path.join(__dirname, 'dist');
    app.use(express.static(distDir));
    app.use((req, res, next) => {
      if (req.method !== 'GET' || req.path.startsWith('/api/')) {
        next();
        return;
      }
      res.sendFile(path.join(distDir, 'index.html'));
    });
  }

  const port = 3000;
  app.listen(port, '0.0.0.0', () => {
    console.log(`Server listening on http://0.0.0.0:${port}`);
  });
}

startServer();
