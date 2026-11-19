import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI } from "@google/genai";
import dotenv from "dotenv";

dotenv.config();

const app = express();
const PORT = 3000;

app.use(express.json({ limit: "10mb" }));

// Helper to get Gemini Client with fallback to environment or custom user key
function getGeminiClient(customApiKey?: string) {
  const apiKey = (customApiKey && customApiKey.trim()) || process.env.GEMINI_API_KEY;
  if (!apiKey) {
    return null;
  }
  return new GoogleGenAI({
    apiKey: apiKey.trim(),
    httpOptions: {
      headers: {
        'User-Agent': 'aistudio-build',
      },
    },
  });
}

// Health check endpoint
app.get("/api/health", (req, res) => {
  res.json({ status: "ok", time: new Date().toISOString() });
});

// Gemini Analysis & Schedule Intelligence Endpoint
app.post("/api/gemini/analyze", async (req, res) => {
  try {
    const { prompt, analysisType, scheduleData, customApiKey, modelName } = req.body;
    
    // Check key
    const clientHeaderKey = req.headers["x-gemini-api-key"] as string | undefined;
    const apiKeyToUse = customApiKey || clientHeaderKey || process.env.GEMINI_API_KEY;

    const ai = getGeminiClient(apiKeyToUse);
    if (!ai) {
      return res.status(400).json({
        error: "API_KEY_MISSING",
        message: "کلید Gemini API یافت نشد. لطفاً در بخش تنظیمات یا تنظیمات جمینای، کلید API خود را وارد کنید یا در متغیر محیطی سرور تنظیم نمایید."
      });
    }

    const selectedModel = modelName || "gemini-2.5-flash";

    const systemInstruction = `شما یک مشاور و منتور ارشد فوق‌تخصصی تحصیلی برای دانشجویان پزشکی و دندانپزشکی (به‌ویژه ترم ۵ دندانپزشکی) و متخصص ارگونومی، برنامه‌ریزی عصبی-شناختی و مدیریت زمان هستید.

وظیفه شما:
تحلیل، بهینه‌سازی و پیشنهاد استراتژی‌های عملی و واقع‌گرایانه برای دانشجوی ترم ۵ دندانپزشکی بر اساس اطلاعات برنامه هفتگی ارائه‌شده.

اصول کلیدی برنامه دانشجوی دندانپزشکی:
۱. بیدارباش و صبحانه: ساعت ۰۶:۰۰ صبح شنبه تا چهارشنبه برای تغذیه مقوی، شادابی و حضور به‌موقع در کلاس ۰۷:۳۰ دانشگاه.
۲. دروس سنگین ترم ۵: فارماکولوژی، ترمیم ۱ و عملی ترمیم، پروتز کامل نظری و عملی، اندودانتیکس، رادیولوژی دهان، ارگونومی و اخلاق پزشکی.
۳. تایم ثابت ناهار و نماز: همواره ساعت ۱۲:۰۰ تا ۱۳:۰۰ در تمامی روزها.
۴. ۳ جلسه ورزش هفتگی: روزهای جمعه، یک‌شنبه و سه‌شنبه (ساعت ۱۸:۰۰ به بعد) جهت کاهش استرس، تقویت عضلات کمر و گردن دندانپزشک و شادابی ذهنی.
۵. تایم ثابت شام و استراحت: همواره ساعت ۲۱:۳۰ تا ۲۳:۰۰ در تمام روزها و خواب منظم ساعت ۲۳:۰۰.
۶. خستگی بعد از کلینیک و پری‌کلینیک: در نظر گرفتن تایم ریکاوری بعد از ظهر و اختصاص بازه شب به دروس نیازمند تمرکز بالا با تکنیک‌های فاصله‌گذاری و مرور فعال (Active Recall & Spaced Repetition).

پاسخ‌ها باید:
- به زبان فارسی بسیار شیوا، صمیمی، انگیزه‌بخش، شاد و کاملاً ساختاریافته (با تیترها، بولت‌پوینت‌های دقیق، فرمول‌های عملی و ایموجی‌های مرتبط) ارائه شود.
- خروجی شامل تحلیل نقاط قوت برنامه، گلوگاه‌های احتمالی (خستگی مفرط)، پیشنهادات گام‌به‌گام و ترفندهای ویژه قبولی با نمره عالی باشد.`;

    const scheduleSummary = scheduleData ? JSON.stringify(scheduleData, null, 2) : "اطلاعات برنامه ارسال نشده است.";

    const fullUserPrompt = `[نوع تحلیل درخواستی: ${analysisType || 'جامع'}]

اطلاعات فعلی برنامه و کارهای هفتگی کاربر:
\`\`\`json
${scheduleSummary}
\`\`\`

درخواست یا سوال کاربر:
${prompt || 'لطفاً تحلیل جامع و عمیق از تعادل درسی، بازدهی، ۳ جلسه ورزش، تایم‌های استراحت و آمادگی امتحانات ارائه بده.'}`;

    const response = await ai.models.generateContent({
      model: selectedModel,
      contents: fullUserPrompt,
      config: {
        systemInstruction: systemInstruction,
        temperature: 0.7,
      },
    });

    const resultText = response.text || "پاسخی از مدل دریافت نشد.";

    return res.json({
      success: true,
      text: resultText,
      modelUsed: selectedModel,
      timestamp: new Date().toISOString()
    });
  } catch (error: any) {
    console.error("Gemini API Error:", error);
    return res.status(500).json({
      error: "GEMINI_EXECUTION_ERROR",
      message: error?.message || "خطا در برقراری ارتباط با جمینای. لطفاً صحت کلید API و اتصال اینترنت را بررسی فرمایید."
    });
  }
});

// Gemini Interactive Chat & Quick Advice Endpoint
app.post("/api/gemini/chat", async (req, res) => {
  try {
    const { messages, scheduleData, customApiKey, modelName } = req.body;
    
    const clientHeaderKey = req.headers["x-gemini-api-key"] as string | undefined;
    const apiKeyToUse = customApiKey || clientHeaderKey || process.env.GEMINI_API_KEY;

    const ai = getGeminiClient(apiKeyToUse);
    if (!ai) {
      return res.status(400).json({
        error: "API_KEY_MISSING",
        message: "کلید API یافت نشد. لطفاً در تنظیمات کلید جمینای را ثبت کنید."
      });
    }

    const selectedModel = modelName || "gemini-2.5-flash";

    const systemInstruction = `شما مشاور هوشمند و دستیار اختصاصی جمینای در برنامه هفتگی دندانپزشکی هستید. با لحن صمیمانه، انرژی‌بخش، انگیزشی و کاملاً علمی به تمام سوالات درسی، رژیم مطالعه، برنامه‌ریزی امتحانات و ورزش پاسخ دهید. همیشه به فارسی روان بنویسید.`;

    // Format last user query or conversation
    const lastUserMessage = messages && messages.length > 0 
      ? messages[messages.length - 1].content 
      : "سلام، چطور می‌توانی به برنامه‌ریزی من کمک کنی؟";

    const contextPayload = `اطلاعات برنامه هفتگی جاری:
\`\`\`json
${scheduleData ? JSON.stringify(scheduleData) : "{}"}
\`\`\`

پیام کاربر:
${lastUserMessage}`;

    const response = await ai.models.generateContent({
      model: selectedModel,
      contents: contextPayload,
      config: {
        systemInstruction: systemInstruction,
        temperature: 0.75,
      },
    });

    return res.json({
      success: true,
      reply: response.text || "",
      modelUsed: selectedModel
    });
  } catch (error: any) {
    console.error("Gemini Chat Error:", error);
    return res.status(500).json({
      error: "GEMINI_CHAT_ERROR",
      message: error?.message || "خطا در گفتگوی هوش مصنوعی"
    });
  }
});

async function startServer() {
  // Vite middleware for development
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://0.0.0.0:${PORT}`);
  });
}

startServer();
