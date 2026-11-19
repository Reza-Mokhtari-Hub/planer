package com.example.util

import com.example.model.DayInfo
import com.example.model.TaskItem
import com.example.model.TimeSlotInfo
import com.example.model.WeekStartOption
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PersianUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    val DAYS_MAP: Map<String, DayInfo> = mapOf(
        "sat" to DayInfo("sat", "شنبه", "ش", "Saturday", 0),
        "sun" to DayInfo("sun", "یکشنبه", "۱ش", "Sunday", 1),
        "mon" to DayInfo("mon", "دوشنبه", "۲ش", "Monday", 2),
        "tue" to DayInfo("tue", "سه‌شنبه", "۳ش", "Tuesday", 3),
        "wed" to DayInfo("wed", "چهارشنبه", "۴ش", "Wednesday", 4),
        "thu" to DayInfo("thu", "پنج‌شنبه", "۵ش", "Thursday", 5),
        "fri" to DayInfo("fri", "جمعه", "ج", "Friday", 6)
    )

    val DEFAULT_DAYS_ORDER = listOf("sat", "sun", "mon", "tue", "wed", "thu", "fri")

    val TIME_SLOTS: List<TimeSlotInfo> = listOf(
        TimeSlotInfo(
            id = "morning_wake",
            title = "بیدارباش و صبحانه",
            timeRange = "۰۶:۰۰ - ۰۷:۳۰",
            defaultStartTime = "06:00",
            colorHex = 0xFFF59E0B,
            desc = "بیداری ساعت ۶:۰۰ (شنبه تا چهارشنبه) و صبحانه مقوی"
        ),
        TimeSlotInfo(
            id = "morning_early",
            title = "صبح اول وقت",
            timeRange = "۰۷:۳۰ - ۰۹:۰۰",
            defaultStartTime = "07:30",
            colorHex = 0xFFEAB308,
            desc = "کلاس اول صبح دانشگاه"
        ),
        TimeSlotInfo(
            id = "morning_main",
            title = "صبح دانشگاه",
            timeRange = "۰۹:۰۰ - ۱۲:۰۰",
            defaultStartTime = "09:00",
            colorHex = 0xFF6366F1,
            desc = "کلاس‌های ۳ ساعته یا مطالعه طلایی"
        ),
        TimeSlotInfo(
            id = "lunch_uni",
            title = "ناهار و نماز دانشگاه",
            timeRange = "۱۲:۰۰ - ۱۳:۰۰",
            defaultStartTime = "12:00",
            colorHex = 0xFFEA580C,
            desc = "تایم ثابت ناهار (۱۲ تا ۱۳)",
            isLunch = true
        ),
        TimeSlotInfo(
            id = "afternoon_uni",
            title = "بعدازظهر دانشگاه",
            timeRange = "۱۳:۰۰ - ۱۵:۳۰",
            defaultStartTime = "13:00",
            colorHex = 0xFF0284C7,
            desc = "کلاس‌های بعدازظهر، جراحی و فارما"
        ),
        TimeSlotInfo(
            id = "afternoon_rest",
            title = "عصرگاه و استراحت",
            timeRange = "۱۵:۳۰ - ۱۸:۰۰",
            defaultStartTime = "15:30",
            colorHex = 0xFF14B8A6,
            desc = "بازگشت به خانه، عصرانه و ریکاوری"
        ),
        TimeSlotInfo(
            id = "evening_1",
            title = "شب - بازه اول",
            timeRange = "۱۸:۰۰ - ۱۹:۳۰",
            defaultStartTime = "18:00",
            colorHex = 0xFF10B981,
            desc = "مطالعه کلیدی یا ۳ جلسه ورزش"
        ),
        TimeSlotInfo(
            id = "evening_2",
            title = "شب - بازه دوم",
            timeRange = "۱۹:۴۵ - ۲۱:۱۵",
            defaultStartTime = "19:45",
            colorHex = 0xFFEC4899,
            desc = "مرور و تست / پیش‌خوانی"
        ),
        TimeSlotInfo(
            id = "night_rest",
            title = "شام و پایان شب",
            timeRange = "۲۱:۳۰ - ۲۳:۰۰",
            defaultStartTime = "21:30",
            colorHex = 0xFF8B5CF6,
            desc = "شام، استراحت شبانه و خواب منظم"
        )
    )

    val MOTIVATIONAL_QUOTES = listOf(
        "هر مرور کوتاه امروز، آرامش شب امتحان فرداست.",
        "پشتکار آرام و پیوسته از فشار لحظه آخر قوی‌تر است.",
        "ورزش و ریکاوری بخشی از موفقیت تحصیلی است، نه اتلاف وقت.",
        "یک دندانپزشک موفق از تعادل جسم، ذهن و زمانش مراقبت می‌کند.",
        "قدم‌های کوچک اما منظم، کل ترم ۵ را به یک تجربه شیرین تبدیل می‌کنند."
    )

    fun formatNumber(input: Any, usePersian: Boolean): String {
        val raw = input.toString()
        if (!usePersian) return raw
        val sb = StringBuilder(raw.length)
        for (ch in raw) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun getOrderedDays(weekStart: WeekStartOption): List<String> = when (weekStart) {
        WeekStartOption.SATURDAY -> listOf("sat", "sun", "mon", "tue", "wed", "thu", "fri")
        WeekStartOption.SUNDAY -> listOf("sun", "mon", "tue", "wed", "thu", "fri", "sat")
        WeekStartOption.MONDAY -> listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
    }

    fun getCurrentDayId(): String {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> "sat"
            Calendar.SUNDAY -> "sun"
            Calendar.MONDAY -> "mon"
            Calendar.TUESDAY -> "tue"
            Calendar.WEDNESDAY -> "wed"
            Calendar.THURSDAY -> "thu"
            Calendar.FRIDAY -> "fri"
            else -> "sat"
        }
    }

    fun getTaskSlot(task: TaskItem): String {
        if (task.timeSlot.isNotBlank()) {
            return if (task.timeSlot == "noon") "lunch_uni" else task.timeSlot
        }
        return resolveSlotFromTime(task.time)
    }

    fun resolveSlotFromTime(time: String): String {
        val parts = time.trim().split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val minutes = h * 60 + m
        return when {
            minutes < 7 * 60 + 30 -> "morning_wake"
            minutes < 9 * 60 -> "morning_early"
            minutes < 12 * 60 -> "morning_main"
            minutes < 13 * 60 -> "lunch_uni"
            minutes < 15 * 60 + 30 -> "afternoon_uni"
            minutes < 18 * 60 -> "afternoon_rest"
            minutes < 19 * 60 + 40 -> "evening_1"
            minutes < 21 * 60 + 30 -> "evening_2"
            else -> "night_rest"
        }
    }

    fun parseTimeMinutes(time: String): Int {
        val normalized = time
            .replace('۰', '0').replace('۱', '1').replace('۲', '2')
            .replace('۳', '3').replace('۴', '4').replace('۵', '5')
            .replace('۶', '6').replace('۷', '7').replace('۸', '8')
            .replace('۹', '9')
        val parts = normalized.split(":")
        val h = parts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull() ?: 99
        val m = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    /**
     * Converts Gregorian date to Persian (Jalali) formatted string without external dependencies.
     */
    fun formatJalaliDate(date: Date = Date(), usePersianDigits: Boolean = true): String {
        val cal = Calendar.getInstance().apply { time = date }
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)

        val monthNames = arrayOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
        val dayName = DAYS_MAP[getDayIdForCalendar(cal)]?.fa ?: ""
        val result = "$dayName $jd ${monthNames[jm - 1]} $jy"
        return formatNumber(result, usePersianDigits)
    }

    fun formatShortJalaliIsoDate(isoDate: String, usePersianDigits: Boolean = true): String {
        val date = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate.take(10)) ?: Date()
        } catch (_: Exception) {
            Date()
        }
        val cal = Calendar.getInstance().apply { time = date }
        val (jy, jm, jd) = gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
        return formatNumber(String.format(Locale.US, "%04d/%02d/%02d", jy, jm, jd), usePersianDigits)
    }

    private fun getDayIdForCalendar(cal: Calendar): String = when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SATURDAY -> "sat"
        Calendar.SUNDAY -> "sun"
        Calendar.MONDAY -> "mon"
        Calendar.TUESDAY -> "tue"
        Calendar.WEDNESDAY -> "wed"
        Calendar.THURSDAY -> "thu"
        Calendar.FRIDAY -> "fri"
        else -> "sat"
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + if (days < 186) (days % 31) else ((days - 186) % 30)
        return Triple(jy, jm, jd)
    }

    fun nowIsoTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
}
