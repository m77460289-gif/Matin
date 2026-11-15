# AGENTS.md — راهنمای هوش مصنوعی پروژه

> **این فایل را قبل از هر کاری بخوان.** زمینه کامل پروژه، ترجیحات کاربر و قواعد کار اینجاست.

---

## 👤 زمینه انسانی

- **صاحب پروژه:** پدری که برای دو فرزندش اپ کنکور می‌سازد
- **رشته بچه‌ها:** فنی‌وحرفه‌ای، شبکه و نرم‌افزار رایانه
- **سال کنکور:** امسال (۱۴۰۴)
- **وضعیت شروع (آبان ۱۴۰۴):**
  - کتاب «دانش فنی پایه» **کامل تمام شده**
  - آزمون جامع تستی هم زده شده (پنجشنبه صبح)
  - بچه‌ها کلاس کنکور می‌روند
- **منابع موجود:** فیلم، جزوه، تست از **تمام دروس**
- **هم‌تیمی:** دوستان پدر هم روی اپ کار می‌کنند

---

## 🎯 هدف اپ

اپ `Konkur Computer` = برنامه‌ریز و مربی مطالعه کنکور فنی‌حرفه‌ای کامپیوتر:

1. **برنامه سالانه** تا ۲ هفته قبل کنکور (۵ فاز)
2. **برنامه هفتگی** ۲ ساعته (پودمان + ریاضی + تست)
3. **تحلیل ۱۲ کتاب** (کد، ضریب، تغییرات، ۵ پودمان هر کتاب)
4. **ردیابی پیشرفت** ۵۰+ پودمان (مطالعه/مرور/تست)
5. **ردیابی منابع** (فیلم/جزوه/تست)
6. **تایمر تمرکز** ۴۵/۳۵/۲۵ با ثبت خودکار
7. **آزمونک** با تحلیل تشریحی
8. **چک‌پوینت هفتگی** (یکشنبه/پنجشنبه) در Room
9. **دفترچه نکات** شخصی

---

## 💬 ترجیحات صاحب پروژه (حفظ کن)

- **همه چیز فارسی** (UI، لحن)
- پاسخ کوتاه، عملی، بدون حاشیه
- **سریع** کار کن، مرحله‌ای فکر نکن، مستقیم برو سراغ کد
- اگر دستور واضح است **بدون پرسش** انجام بده
- **حدس نزن** — اگر ابهامی هست بپرس

### ⚠️ ورکفلوی انتشار (خیلی مهم — اشتباه نکن!)

**دو نوع انتشار داریم:**

| تگ | APK | نوع انتشار | کاربرد |
|---|---|---|---|
| `v1.2.3-beta`, `v1.2.3-test`, `v1.2.3-pre`, `v1.2.3-rc`, `v1.2.3-debug` | **debug** | **PRE-RELEASE** | تست داخلی، تست‌نشده |
| `v1.2.3` (فقط عدد، بدون پسوند) | **release (امضاشده)** | **FULL RELEASE** | تست‌شده، امن، پایدار |
| push به `main` (بدون تگ) | debug | Artifact فقط | بررسی سریع |

**قانون طلایی:**
- APK دیباگ و تست‌نشده **فقط** در پیش‌ریلیز
- APK ریلیز امضاشده و **تست‌شده** فقط در ریلیز کامل
- هرگز برعکسش نکن

### جملات کلیدی کاربر
> «ورژن دیباگ که تست نشده رو روی پری‌ریلیز بفرست؛ نسخه‌های تست‌شده و سالم رو توی ریلیز»

> «هر یکشنبه و پنجشنبه میام بهت میگم بچه‌ها تا کجا رفتند، تو برنامه هفته بعد را تنظیم کن»

> «اگر منبعی کم بود، برو از اینترنت پیدا کن»

---

## 🏗️ معماری پروژه

```
app/src/main/java/com/example/
├── MainActivity.kt             ← ۷ تب، BottomNav (گوشی) / Rail (تبلت)
├── data/
│   ├── model/Models.kt        ← 5 Room Entity + 6 domain Model
│   ├── database/AppDatabase.kt (v3) + StudyDao.kt
│   ├── repository/StudyRepository.kt
│   ├── curriculum/ComputerCurriculumData.kt  ← 12 book + weeklyPlan + quiz
│   └── plan/AnnualPlanData.kt  ← 5 phases + checkin rules
└── ui/
    ├── viewmodel/KonkurViewModel.kt
    ├── screens/
    │   ├── DashboardScreen.kt
    │   ├── TwoHourScheduleScreen.kt
    │   ├── BooksAndChangesScreen.kt
    │   ├── TimerScreen.kt
    │   ├── QuizScreen.kt
    │   ├── AnalyticsAndNotesScreen.kt
    │   └── AnnualPlanScreen.kt
    └── theme/
```

**Stack:** Jetpack Compose + Material 3 + Room v3 + Kotlin 2.2 + AGP 9.1 + Gradle 9.3.1 + JDK 17

---

## 🗄️ دیتابیس Room (v3)

| جدول | کلید | کاربرد |
|---|---|---|
| `study_logs` | auto id | جلسات مطالعه |
| `poodman_progress` | (bookId, poodmanIndex) | ۳گانه وضعیت + ۳گانه منابع |
| `study_notes` | auto id | دفترچه نکات |
| `daily_slot_status` | (dateKey, slotId) | تیک برنامه روز |
| `weekly_checkins` | auto id | گزارش یکشنبه/پنجشنبه |
| `leitner_cards` | (bookId, poodmanIndex) | جعبه لایتنر — مرور فاصله‌دار ۱/۳/۷/۱۴/۳۰ روز |
| `wrong_answers` | questionId | دفترچه اشتباهات — سوالات غلط آزمونک |
| `weekly_scores` | auto id | نمره آزمون‌های هفتگی (هفته ۱ تا ۳۰) + روند نمودار خطی |

> 💡 `poodman_progress` در v7 شامل `videoUrl`, `pdfPath`, `testUrl` هم هست — لینک واقعی منابع هر پودمان.

## 🎯 کارت «امروز چه کاری باید بکنم؟»

- بالای `DashboardScreen` (بعد از Hero)
- نمایش: پودمان بعدی + تعداد مرور لایتنر امروز + ۵ تست
- دکمه «شروع X دقیقه‌ای» → مستقیم تایمر را باز می‌کند
- هدف: حذف سردرگمی «از کجا شروع کنم؟»

## 🔔 یادآور و استریک

- `reminder/ReminderScheduler.kt` — زمان‌بندی روزانه ساعت ۱۷:۰۰ با `AlarmManager`
- `reminder/ReminderReceiver.kt` — اعلان «۲ ساعت مطالعه امروز شروع شود؟»
- `reminder/BootReceiver.kt` — باززمان‌بندی بعد از ری‌استارت
- `KonkurViewModel.studyStreak` — استریک از `study_logs` محاسبه می‌شود
- کارت استریک در `DashboardScreen` — «🔥 X روز پیوسته مطالعه کردی»
- سوییچ فعال/غیرفعال در `AnalyticsAndNotesScreen` + `SharedPreferences` (`app_prefs` / `daily_reminder_enabled`)

## 👨‍👩‍👧 پنل والد (خلاصه هفتگی)

- تب `nav_parent` (`ParentReportScreen`)
- خلاصه هفته گذشته: دقیقه مطالعه + تعداد جلسه + استریک + آخرین نمره
- پودمان‌های عقب‌مانده (مطالعه شده اما تست نشده)
- دکمه «📤 ارسال گزارش به تلگرام/واتساپ» با `Intent.ACTION_SEND`

## ⏱️ آزمون شبیه‌ساز (۲۵ سوال / ۳۰ دقیقه)

- تب `nav_simulator` (`QuizSimulatorScreen`)
- تایمر واقعی ۳۰ دقیقه + پایان خودکار
- زمان صرف‌شده هر سوال ثبت می‌شود
- نتیجه: درصد کل، میانگین زمان، ضعیف‌ترین پودمان
- سوالات غلط خودکار در `wrong_answers` ذخیره می‌شوند

**⚠️ نکته:** `fallbackToDestructiveMigration()` فعال است — هر بار schema عوض شد `version` را در `AppDatabase.kt` یکی زیاد کن.

---

## 🚀 ورکفلوی Git و انتشار

### هر بار تغییر دادی:
```bash
git add -A
git commit -m "feat/fix: توضیح انگلیسی"
git push origin main
```

### برای انتشار عمومی (پیش‌ریلیز):
```bash
git tag v1.x.0
git push origin v1.x.0
```
→ Actions خودکار اجرا می‌شود → پیش‌ریلیز با APK می‌سازد

### لینک‌های مهم
- ریپو: `https://github.com/m77460289-gif/Matin`
- Actions: `https://github.com/m77460289-gif/Matin/actions`
- Releases: `https://github.com/m77460289-gif/Matin/releases`

---

## 🔑 امضای ریلیز (Release Signing)

- **Keystore:** `release-keystore.jks` (root، gitignore)
- **Alias:** `konkur`
- **رمزها:** `release-signing.properties` (gitignore)
- **در CI (Secrets گیت‌هاب):**
  - `RELEASE_KEYSTORE_BASE64`
  - `RELEASE_STORE_PASSWORD`
  - `RELEASE_KEY_PASSWORD`
  - `RELEASE_KEY_ALIAS`

**⚠️ فایل‌های `.jks` و `release-signing.properties` را هرگز push نکن.**

---

## 🔁 جعبه لایتنر (Leitner Spaced-Repetition) — قلب نتیجه کنکور

- **قاعده:** هر پودمان که «مطالعه» تیک بخورد، **خودکار** وارد `leitner_cards` (جعبه ۱) می‌شود.
- **فواصل مرور:** ۱ روز → ۳ روز → ۷ روز → ۱۴ روز → ۳۰ روز → تسلط ✅
- **UI:** تب `مرور امروز` (`LeitnerScreen`) — فقط کارت‌های سررسید امروز یا گذشته، هر بار حدود ۱۰ دقیقه.
- **پاسخ درست** → `boxLevel++` و تاریخ مرور بعدی جلو می‌رود.
- **پاسخ غلط** → برگشت به جعبه ۱ (مرور فردا).
- **تسلط:** بعد از جعبه ۵ → `isMastered = true` و `nextReviewAt = Long.MAX_VALUE`.
- **کد محوری:** `KonkurViewModel.reviewLeitnerCard()`, `enqueueLeitnerIfNeeded()`.

---

## 📅 چک‌پوینت هفتگی (مهم‌ترین جریان کاری)

### هر یکشنبه:
صاحب پروژه گزارش می‌دهد که بچه‌ها هفته قبل چه خواندند.
**تو باید:**
1. در `CHECKIN_LOG.md` ثبت کن
2. برنامه هفته بعد را در `ComputerCurriculumData.weeklyPlan` تنظیم کن
3. اگر منبع کم بود، از اینترنت پیدا کن
4. commit + push

### هر پنجشنبه:
گزارش آزمون + کمبود منابع.
- همان بالا + بررسی آماری ثبت‌شده
- اگر آزمون ضعیف بود، `sampleQuestions` را تقویت کن

---

## 📚 منابع آموزشی (اگر کم بود پیدا کن)

### منابع رایگان معتبر
- **کتاب‌های درسی رسمی:** `chap.sch.ir`
- **کنکورهای گذشته:** `sanjesh.org`
- **یوتیوب:** کانال‌های آموزشی فنی‌حرفه‌ای

### قواعد دانلود
- **کپی‌رایت رعایت شود** — فقط منابع رایگان/عمومی
- لینک را در `CHECKIN_LOG.md` یا `ComputerCurriculumData` ثبت کن
- فایل حجیم در repo نگذار — لینک خارجی

---

## ✅ چک‌لیست قبل از commit

- [ ] کد کامپایل می‌شود؟
- [ ] `*.jks`, `release-signing.properties` commit نشده‌اند؟
- [ ] `.gitignore` چک شد؟
- [ ] commit message انگلیسی و معنادار؟
- [ ] اگر schema عوض شد، `version` در `AppDatabase` یک واحد بیشتر؟

---

## 📖 اسناد مرتبط

| فایل | نقش |
|---|---|
| `README.md` | معرفی کوتاه برای کاربر |
| `PROJECT_MAP.md` | نقشه کامل فایل به فایل |
| `CHECKIN_LOG.md` | گزارش هفتگی (زنده) |
| `CONTRIBUTING.md` | راهنمای تیم |
| `AGENTS.md` (همین فایل) | زمینه کامل برای AI |

---

## 🆘 اگر گیر کردی

1. **خطای بیلد:** Actions → آخرین run → log
2. **دیتابیس migrate نکرد:** `version++` + `fallbackToDestructiveMigration`
3. **UI به هم ریخت:** احتمالاً `}` اضافه/کم — `git diff` استفاده کن
4. **ورکفلو fail:** keystore decode نشده یا Secret تنظیم نیست

---

**آخرین به‌روزرسانی:** ۱۴۰۴/۰۸/۲۳ توسط MAGNUM