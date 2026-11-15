# PROJECT MAP — Konkur Computer

> نقشه کامل پروژه: هر فایل چه کاری می‌کند و چطور به بقیه وصل است.

---

## 1. نمای کلی (خلاصه یک‌خطی)

`Konkur Computer` یک اپلیکیشن اندروید تک‌ماژولی (`:app`) است که:

- داده‌های مطالعه کاربر را در **Room** ذخیره می‌کند
- یک **ViewModel** مشترک (`KonkurViewModel`) وضعیت همه صفحه‌ها را مدیریت می‌کند
- داده‌های ثابت (کتاب‌ها، پودمان‌ها، برنامه هفتگی، سوالات آزمون) در یک **Singleton** کاتلینی (`ComputerCurriculumData`) نگه‌داری می‌شوند
- ۶ صفحه Compose با ناوبری ساده بر اساس ایندکس تب

---

## 2. جریان داده (Data Flow)

```
[User tap] → Compose Screen → viewModel.fn()
   → KonkurViewModel (viewModelScope.launch)
      → StudyRepository
         → StudyDao (Room)
            → SQLite (konkur_computer_db)
   ↑ Flow بازگشتی → StateFlow → Compose (collectAsStateWithLifecycle)
```

- **Static data:** از `ComputerCurriculumData` مستقیم خوانده می‌شود (بدون DB).
- **User data:** در ۴ جدول Room ذخیره می‌شود.

---

## 3. درخت فایل‌ها + نقش هر فایل

### ریشه پروژه (Root)

| فایل | نقش |
|---|---|
| `build.gradle.kts` | پلاگین‌های سراسری (android, kotlin, ksp, secrets, google-services) |
| `settings.gradle.kts` | نام پروژه (`KonkurComputer`) + include `:app` + repositories |
| `gradle.properties` | تنظیمات Gradle/JVM |
| `gradle/libs.versions.toml` | کاتالوگ نسخه‌ها (Version Catalog) |
| `gradle/wrapper/gradle-wrapper.properties` | نسخه Gradle Wrapper |
| `debug.keystore.base64` | کی‌استور دیباگ به صورت Base64 (برای CI) |
| `metadata.json` | متادیتای پروژه (نام فارسی، توضیح، قابلیت‌ها) |
| `server.py` | وب‌سرور لندینگ (`http.server`) که صفحه دانلود APK/ZIP را می‌دهد |
| `.env.example` | نمونه متغیرهای محیطی (Secrets Gradle Plugin) |
| `.gitignore` | فایل‌های Ignore شده |
| `.github/workflows/release.yml` | CI برای ساخت Release |
| `README.md` | معرفی و راهنمای اجرا |
| `PROJECT_MAP.md` | همین فایل (نقشه پروژه) |

### ماژول `app/`

| فایل | نقش |
|---|---|
| `app/build.gradle.kts` | تنظیمات ماژول: `namespace = com.example`, `applicationId = com.aistudio.konkurcomputer.vtfplq`, `minSdk 24`, `targetSdk 36`, signingConfigs، وابستگی‌های Compose + Room + Firebase AI |
| `app/proguard-rules.pro` | قوانین ProGuard |
| `app/src/main/AndroidManifest.xml` | Manifest اپ |

### کد کاتلین (`app/src/main/java/com/example/`)

#### ورودی و Navigation
| فایل | نقش |
|---|---|
| `MainActivity.kt` | `ComponentActivity` + `setContent` + `KonkurApp()` + `ScreenContent()`. شامل ناوبری گوشی (BottomNavigation) و تبلت (`NavigationRail`) و RTL provider |
| `ui/viewmodel/KonkurViewModel.kt` | مغز اپ: `StateFlow` برای تب، پایه، روز، لاگ‌ها، پیشرفت، تایمر، نکات + توابع `startOrResumeTimer`, `pauseTimer`, `resetTimer`, `toggleSlot`, `togglePoodmanStatus`, `logStudyTime`, `addNote`, `deleteNote` |

#### لایه داده
| فایل | نقش |
|---|---|
| `data/model/Models.kt` | ۴ `@Entity` (`StudyLogEntity`, `PoodmanProgressEntity`, `StudyNoteEntity`, `DailySlotStatusEntity`) + ۶ Domain Model (`GradeLevel`, `PoodmanInfo`, `BookInfo`, `DailySlot`, `DaySchedulePlan`, `QuizQuestion`) |
| `data/database/AppDatabase.kt` | Room DB singleton با نام `konkur_computer_db` و نسخه ۱ |
| `data/database/StudyDao.kt` | کوئری‌های CRUD: مطالعه‌ها (insert/delete/sum)، پیشرفت پودمان‌ها (get/save)، وضعیت اسلات‌های روزانه (get/set)، نکات (get/insert/delete) |
| `data/repository/StudyRepository.kt` | پوشش DAO و تبدیل نتایج به `Flow` + توابع میانجی |
| `data/curriculum/ComputerCurriculumData.kt` | داده ثابت: `textbookChangesGeneralNotice`, `books` (۷ کتاب: ۳ دهم + ۲ یازدهم + ۲ دوازدهم هر کدام ۵ پودمان), `weeklyPlan` (برنامه ۷ روزه), `sampleQuestions` (سوالات آزمونک) |

#### صفحه‌ها (UI)
| فایل | نقش |
|---|---|
| `ui/screens/DashboardScreen.kt` | خلاصه وضعیت: درصد پیشرفت، دکمه‌های میان‌بر، پیشنهاد روز |
| `ui/screens/TwoHourScheduleScreen.kt` | برنامه ۲ ساعته هفتگی با ۵ اسلات در هر روز + چک‌لیست با `toggleSlot` + دکمه رفتن به تایمر |
| `ui/screens/BooksAndChangesScreen.kt` | تب پایه‌ها (`ScrollableTabRow`) → کارت هر کتاب → تحلیل تغییرات + ۵ پودمان با ۳ چک‌باکس (مطالعه / مرور / تست) |
| `ui/screens/TimerScreen.kt` | نمایش شمارش معکوس، Play/Pause/Reset، PresetChip ها، دیالوگ ثبت دستی، بنر اتمام و ذخیره خودکار در `study_logs` |
| `ui/screens/QuizScreen.kt` | نمایش سوال، گزینه‌ها با بازخورد درست/غلط، تحلیل تشریحی + نکته طلایی، ناوبری سوال بعد/قبل + امتیاز |
| `ui/screens/AnalyticsAndNotesScreen.kt` | نمودار پیشرفت کلی (LinearProgress)، شمارش مطالعه/مرور/تست، افزودن و حذف نکات، تاریخچه لاگ‌ها |

#### برنامه سالانه (جدید)
| فایل | نقش |
|---|---|
| `data/plan/AnnualPlanData.kt` | ۵ فاز برنامه تا کنکور (آذر→کنکور) + قواعد چک‌پوینت یکشنبه/پنجشنبه + وضعیت شروع |
| `ui/screens/AnnualPlanScreen.kt` | نمایش فازها + دیالوگ ثبت چک‌پوینت هفتگی + لیست چک‌پوینت‌های ذخیره‌شده |
| `CheckInEntity` (در `Models.kt`) | جدول `weekly_checkins` برای ذخیره چک‌پوینت‌ها در Room |

#### جعبه لایتنر (مرور فاصله‌دار)
| فایل | نقش |
|---|---|
| `LeitnerCardEntity` (در `Models.kt`) | جدول `leitner_cards` — هر پودمان با `boxLevel` و `nextReviewAt` |
| `ui/screens/LeitnerScreen.kt` | تب «مرور امروز»: کارت‌های سررسید، دکمه «یادم بود / فراموش کردم»، لیست کل کارت‌ها |
| `KonkurViewModel.reviewLeitnerCard()` | پاسخ درست → جعبه بعدی؛ پاسخ غلط → برگشت به جعبه ۱ |

#### دفترچه اشتباهات (Error Notebook)
| فایل | نقش |
|---|---|
| `WrongAnswerEntity` (در `Models.kt`) | جدول `wrong_answers` — سوال غلط با `timesWrong` و `lastWrongAt` |
| `QuizScreen` (بازنویسی) | ذخیره خودکار سوال غلط + دکمه «آزمون مجدد از غلط‌ها» |
| `KonkurViewModel.recordWrongAnswer()` | افزایش شمارنده غلط + reset `isMastered` |

#### آزمون شبیه‌ساز زمان‌دار
| فایل | نقش |
|---|---|
| `ui/screens/QuizSimulatorScreen.kt` | ۲۵ سوال / ۳۰ دقیقه با تایمر واقعی، زمان هر سوال، درصد و تحلیل ضعیف‌ترین پودمان |
| `MainActivity` (index 8) | تب «آزمون شبیه‌ساز» + دکمه میان‌بر در `QuizScreen` |

#### ثبت نمره آزمون‌های هفتگی (روند پیشرفت)
| فایل | نقش |
|---|---|
| `WeeklyScoreEntity` (در `Models.kt`) | جدول `weekly_scores` — هفته، درصد نمره، عنوان آزمون |
| `AnalyticsAndNotesScreen` → `WeeklyScoreTrendSection` | نمودار خطی (Canvas) + آمار آخرین/بهترین/تغییر |
| `KonkurViewModel.addWeeklyScore()` | ثبت نمره هفتگی برای تحلیل روند |

#### تم
| فایل | نقش |
|---|---|
| `ui/theme/Color.kt` | رنگ‌های برند: `PrimaryIndigo`, `PrimaryLightIndigo`, `SecondaryTeal`, `AccentGold`, `SuccessGreen` |
| `ui/theme/Theme.kt` | `MyApplicationTheme` (Material3 colorScheme) |
| `ui/theme/Type.kt` | Typography |

### منابع (`app/src/main/res/`)

| فایل | نقش |
|---|---|
| `values/strings.xml` | نام اپ: «کنکور کامپیوتر» |
| `values/colors.xml` | رنگ‌های پایه |
| `values/themes.xml` | تم اندروید |
| `drawable/*` | آیکون‌ها و تصویر Hero |
| `mipmap-*/*` | آیکون لانچر در تراکم‌های مختلف |
| `xml/backup_rules.xml`, `xml/data_extraction_rules.xml` | قوانین Backup |

### مستندات ریشه (جدید)
| فایل | نقش |
|---|---|
| `CHECKIN_LOG.md` | گزارش هفتگی یکشنبه/پنجشنبه (چه خواندند، چه تست زدند، کمبود منابع) — منبع تنظیم برنامه هفته بعد |
| `README.md` | معرفی پروژه |
| `PROJECT_MAP.md` | همین فایل (نقشه کامل) |

### تست‌ها

| فایل | نقش |
|---|---|
| `test/java/.../ExampleUnitTest.kt` | تست واحد نمونه |
| `test/java/.../ExampleRobolectricTest.kt` | تست Robolectric نمونه |
| `androidTest/java/.../ExampleInstrumentedTest.kt` | تست ابزاری نمونه |

---

## 4. جریان تایمر (مهم‌ترین منطق)

1. کاربر از `DashboardScreen` یا `TwoHourScheduleScreen` روی یک اسلات کلیک می‌کند
2. `onNavigateToTimer(minutes, label)` صدا زده می‌شود → `viewModel.setTimerPreset(minutes, label)` و `selectTab(3)`
3. در `TimerScreen` دکمه Play → `viewModel.startOrResumeTimer()`
4. درون ViewModel یک `viewModelScope.launch { while(...) delay(1000) }` هر ثانیه `remainingSeconds` را کم می‌کند
5. هنگام رسیدن به صفر:
   - `triggerVibration()` (روی API 31+ از `VibratorManager`)
   - `repository.logStudyTime(minutes = initialSeconds/60, subject = sessionLabel)`
6. لاگ در `study_logs` ذخیره شده و در `AnalyticsAndNotesScreen` نمایش داده می‌شود

---

## 5. جریان پیشرفت پودمان

- کاربر در `BooksAndChangesScreen` چک‌باکس‌ها را می‌زند
- `togglePoodmanStatus(bookId, poodmanIndex, studied/reviewed/tested)`
- ViewModel با `existing` مقایسه و رکورد جدید `PoodmanProgressEntity` را `savePoodmanProgress` می‌کند
- `AnalyticsAndNotesScreen` روی `allProgress` شمارش انجام می‌دهد

---

## 6. نکات مهم برای توسعه‌دهنده بعدی

- **کتاب‌های اپ:** الان ۱۲ کتاب (۳ دهم تخصصی + کارگاه نوآوری + ۲ یازدهم تخصصی + فناوری نوین + ۳ دوازدهم تخصصی + ریاضی + ۳ عمومی)
- **برنامه سالانه:** `AnnualPlanData.phases` ۵ فاز دارد؛ برای تغییر آن فقط این فایل را ویرایش کن
- **چک‌پوینت:** هر یکشنبه/پنجشنبه کاربر گزارش می‌دهد؛ در `CHECKIN_LOG.md` ثبت کن و برنامه هفته بعد را در همان فایل یا در `weeklyPlan` تنظیم کن
- ناوبری **بدون `Navigation-Compose`** است؛ فقط یک `Int` در `_currentTab`. برای اضافه کردن صفحه جدید:
  1. یک `NavTabItem` به `navItems` در `MainActivity.kt` اضافه کن
  2. در `ScreenContent()` یک `when` جدید بنویس
- برای اضافه کردن کتاب جدید: فقط به `ComputerCurriculumData.books` یک `BookInfo` اضافه کن (۵ پودمان الزامی است)
- برای اضافه کردن سوال آزمونک: به `ComputerCurriculumData.sampleQuestions` اضافه کن
- **توجه:** `totalPoodmans` در `AnalyticsAndNotesScreen` به صورت Hard-coded روی `35` است (۷ کتاب × ۵ پودمان) — اگر کتاب اضافه شد این عدد باید به‌روز شود
- `server.py` فقط برای توزیع APK/ZIP است؛ اپلیکیشن به آن وابسته نیست
- بخش Firebase AI در `app/build.gradle.kts` تعریف شده اما در کد استفاده نمی‌شود (برای آینده)

---

## 7. خلاصه نقشه در یک نگاه

```
MainActivity
   └── KonkurViewModel ── StudyRepository ── StudyDao ── Room DB
              │
              └── StateFlow<Tab>
                     └── ScreenContent → 6 Screens

ComputerCurriculumData (static: books, poodmans, weeklyPlan, questions)
        ↑
   همه صفحه‌ها از آن می‌خوانند
```