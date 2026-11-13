package com.example.data.plan

data class PlanPhase(
    val id: String,
    val title: String,
    val startMonth: String,
    val endMonth: String,
    val targetBooks: List<String>,
    val weeklyFocus: String,
    val goal: String
)

data class WeeklyCheckpoint(
    val weekNumber: Int,
    val sundayNote: String = "",
    val thursdayNote: String = "",
    val completedTopics: List<String> = emptyList(),
    val missingResources: List<String> = emptyList()
)

object AnnualPlanData {

    val phases: List<PlanPhase> = listOf(
        PlanPhase(
            id = "phase_10_grade",
            title = "فاز ۱: تثبیت کامل پایه دهم",
            startMonth = "آذر",
            endMonth = "دی",
            targetBooks = listOf("danesh_fani_payeh", "nasb_va_rahandazi", "tolid_mohtava", "kargah_novavari", "riazi_fani"),
            weeklyFocus = "۶ روز هفته پودمان جدید دهم + ریاضی؛ جمعه آزمون و مرور",
            goal = "اتمام کامل ۳ کتاب تخصصی دهم + ۴۰٪ ریاضی ۱"
        ),
        PlanPhase(
            id = "phase_11_grade",
            title = "فاز ۲: یازدهم (C#، وب و پایگاه داده)",
            startMonth = "بهمن",
            endMonth = "اسفند",
            targetBooks = listOf("tosee_barnamehsazi", "tarahi_web", "karbord_fanavari_novin", "riazi_fani"),
            weeklyFocus = "۳ روز سی‌شارپ و SQL، ۲ روز وب، ۱ روز فناوری نوین، جمعه آزمون",
            goal = "تسلط روی #C و SQL + HTML5/CSS3/PHP + فناوری نوین"
        ),
        PlanPhase(
            id = "phase_12_grade",
            title = "فاز ۳: دوازدهم (تخصصی، شبکه، امنیت)",
            startMonth = "فروردین",
            endMonth = "اردیبهشت",
            targetBooks = listOf("danesh_fani_takhasosi", "tajhizat_shabakeh", "tejarat_va_amniat"),
            weeklyFocus = "روزهای زوج دانش فنی تخصصی، روزهای فرد شبکه و امنیت",
            goal = "پوشش کامل ۳ کتاب دوازدهم + تست‌های کنکورهای گذشته"
        ),
        PlanPhase(
            id = "phase_review",
            title = "فاز ۴: جمع‌بندی و مرور جامع",
            startMonth = "خرداد",
            endMonth = "خرداد آخر",
            targetBooks = listOf("all"),
            weeklyFocus = "۲ روز مرور خلاصه‌ها و جعبه لایتنر، ۳ روز آزمون کامل، ۲ روز تحلیل اشتباهات",
            goal = "تثبیت کامل تمام مباحث + آمادگی آزمون شبیه‌ساز"
        ),
        PlanPhase(
            id = "phase_final",
            title = "فاز ۵: دو هفته آخر (تثبیت نهایی)",
            startMonth = "۲ هفته قبل کنکور",
            endMonth = "کنکور",
            targetBooks = listOf("all"),
            weeklyFocus = "مرور نکات طلایی، دفترچه اشتباهات و آزمون‌های شبیه‌ساز",
            goal = "بدون مطلب جدید؛ فقط تثبیت و رفع اشکال"
        )
    )

    val resourceTypes: List<String> = listOf(
        "فیلم آموزشی",
        "جزوه / خلاصه",
        "تست کنکور",
        "تمرین کتاب",
        "نمونه سوال امتحانی"
    )

    val checkpointRules: List<String> = listOf(
        "هر یکشنبه: گزارش هفتگی پیشرفت (چه پودمان‌هایی خوانده شد، چه تست‌هایی زده شد)",
        "هر پنجشنبه: گزارش آزمون هفتگی و وضعیت منابع (فیلم/جزوه/تست) و کمبودها",
        "روی همین ریپو ذخیره می‌شود تا برنامه هفته بعد با آن تنظیم شود"
    )

    val startingStatus: String = "وضعیت شروع: دانش فنی پایه تمام شده + آزمون جامع پنجشنبه صبح زده شد"
}
