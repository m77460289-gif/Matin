# نقشه فنی پروژه Konkur Computer

این سند وضعیت فعلی مخزن را توصیف می‌کند؛ در صورت اختلاف، کد شاخه `main` منبع نهایی حقیقت است.

## ۱. معرفی

`Konkur Computer` یک اپ اندروید تک‌ماژولی با Kotlin و Jetpack Compose است. داده‌های شخصی مطالعه در Room ذخیره می‌شوند و وضعیت صفحه‌ها از طریق یک ViewModel مشترک مدیریت می‌شود. اپ برای استفاده محلی و تک‌کاربره طراحی شده است.

مشخصات فعلی:
- Application ID: `com.aistudio.konkurcomputer.vtfplq`
- نسخه برنامه: `1.12.1` (versionCode 13)
- minSdk: 24، targetSdk: 36، compileSdk: 36
- Kotlin: 2.2.10، AGP: 9.1.1، Gradle Wrapper: 9.3.1
- Room: 2.7.0، JDK ساخت CI: 21

## ۲. جریان داده

```
Compose Screen
    ↓ فراخوانی عملیات
KonkurViewModel
    ↓
StudyRepository
    ↓
StudyDao
    ↓
Room / SQLite
    ↑
Flow → StateFlow → collectAsStateWithLifecycle
```

داده‌های آموزشی ثابت مستقیماً از `ComputerCurriculumData` و برنامه سالانه از `AnnualPlanData` خوانده می‌شوند.

## ۳. پیمایش و صفحه‌ها

ناوبری ساده و مبتنی بر index در `MainActivity.kt` است و Navigation Compose استفاده نمی‌شود.

| index | عنوان | فایل |
|---:|---|---|
| 0 | داشبورد | `ui/screens/DashboardScreen.kt` |
| 1 | برنامه ۲ ساعت | `ui/screens/TwoHourScheduleScreen.kt` |
| 2 | کتاب‌ها و تغییرات | `ui/screens/BooksAndChangesScreen.kt` |
| 3 | تایمر تمرکز | `ui/screens/TimerScreen.kt` |
| 4 | آزمونک | `ui/screens/QuizScreen.kt` |
| 5 | آمار و نکات | `ui/screens/AnalyticsAndNotesScreen.kt` |
| 6 | برنامه تا کنکور | `ui/screens/AnnualPlanScreen.kt` |
| 7 | مرور امروز | `ui/screens/LeitnerScreen.kt` |
| 8 | آزمون شبیه‌ساز | `ui/screens/QuizSimulatorScreen.kt` |
| 9 | پنل والد | `ui/screens/ParentReportScreen.kt` |

در گوشی، مقصدهای ۰، ۱، ۳ و ۴ در Bottom Navigation هستند و مقصدهای ۲، ۵، ۶، ۷، ۸ و ۹ از منوی «بیشتر» باز می‌شوند. در تبلت هر ده مقصد در NavigationRail دیده می‌شوند.

## ۴. ساختار کد

| مسیر | مسئولیت |
|---|---|
| `MainActivity.kt` | ورودی برنامه، RTL، پیمایش و مسیریابی |
| `data/model/Models.kt` | هشت Entity دیتابیس و مدل‌های دامنه |
| `data/database/AppDatabase.kt` | تعریف Room، نسخه ۷ |
| `data/database/StudyDao.kt` | عملیات و پرس‌وجوهای دیتابیس |
| `data/repository/StudyRepository.kt` | واسط میان ViewModel و DAO |
| `data/curriculum/ComputerCurriculumData.kt` | ۱۴ کتاب، ۶۸ پودمان، برنامه هفت‌روزه و سوالات آزمونک |
| `data/plan/AnnualPlanData.kt` | پنج فاز برنامه سالانه و قواعد چک‌پوینت |
| `data/backup/BackupManager.kt` | خروجی و بازیابی JSON نسخه‌بندی‌شده |
| `ui/viewmodel/KonkurViewModel.kt` | StateFlowها و منطق مطالعه، تایمر، مرور و گزارش |
| `reminder/ReminderScheduler.kt` | زمان‌بندی AlarmManager |
| `reminder/ReminderReceiver.kt` | ساخت اعلان روزانه |
| `reminder/BootReceiver.kt` | برنامه‌ریزی دوباره یادآور بعد از راه‌اندازی دستگاه |

## ۵. پایگاه داده

Room نسخه ۷ و هشت جدول دارد:

| Entity | جدول | کاربرد |
|---|---|---|
| `StudyLogEntity` | `study_logs` | مدت و عنوان جلسات مطالعه |
| `PoodmanProgressEntity` | `poodman_progress` | وضعیت مطالعه/مرور/تست و منابع هر پودمان |
| `StudyNoteEntity` | `study_notes` | یادداشت شخصی |
| `DailySlotStatusEntity` | `daily_slot_status` | تکمیل اسلات برنامه هر روز |
| `CheckInEntity` | `weekly_checkins` | گزارش‌های پیگیری |
| `LeitnerCardEntity` | `leitner_cards` | وضعیت کارت مرور فاصله‌دار |
| `WrongAnswerEntity` | `wrong_answers` | شمارش و وضعیت سوالات غلط |
| `WeeklyScoreEntity` | `weekly_scores` | درصد آزمون و روند نمره |

### نکته مهاجرت

`AppDatabase.kt` از `fallbackToDestructiveMigration()` استفاده می‌کند و `exportSchema = false` است. مخزن schema history و migrationهای واقعی نسخه‌های قبلی را نگه نمی‌دارد. بنابراین صرفاً افزایش version ممکن است داده محلی را حذف کند؛ تغییر schema تا زمان طراحی و آزمایش migration امن نباید انجام شود.

## ۶. پشتیبان‌گیری

`BackupManager` فرمت JSON نسخه ۲ را برای همه هشت جدول ایجاد می‌کند. خروجی از snapshot تراکنشی ساخته می‌شود؛ شناسه رکوردهای خودکار حفظ می‌شود و بازیابی پس از اعتبارسنجی کل داده‌ها در تراکنش انجام می‌گیرد.

فایل نسخه ۱ نیز پذیرفته می‌شود. چون آن نسخه `daily_slot_status` و `weekly_checkins` را نداشت، این دو جدول هنگام بازیابی نسخه ۱ دست‌نخورده باقی می‌مانند. رابط کاربری پیش از جایگزینی داده‌ها تأیید می‌گیرد.

مسیر UI پشتیبان‌گیری: `AnalyticsAndNotesScreen.kt` و بخش `BackupSection`. انتخاب فایل با `CreateDocument` و `OpenDocument` انجام می‌شود. JSON مسیرها و لینک‌های منابع را ذخیره می‌کند، نه خود فایل‌های PDF یا ویدئو را.

## ۷. تایمر تمرکز

`TimerScreen` وضعیت را از `KonkurViewModel.timerState` می‌گیرد. منطق تایمر در ViewModel است:

- زمان پایان مطلق و جزئیات جلسه در SharedPreferences با نام `timer_state` ذخیره می‌شوند.
- با ساخت دوباره ViewModel یا بازگشت به برنامه، زمان باقی‌مانده بر اساس زمان پایان محاسبه می‌شود.
- توقف، ادامه و بازنشانی وضعیت ذخیره‌شده را به‌روزرسانی می‌کنند.
- اگر زمان در زمان بسته‌بودن برنامه گذشته باشد، پایان جلسه هنگام بازگشت ViewModel بازیابی می‌شود.
- لاگ پایان جلسه شناسه ثابت دارد تا retry همان رکورد را جایگزین کند و لاگ تکراری نسازد.

تایمر سرویس foreground ندارد؛ بنابراین اعلان دقیق در لحظه پایان، زمانی که فرایند برنامه بسته است، تضمین نشده است.

## ۸. یادآور روزانه

یادآور پیش‌فرض ساعت ۱۷:۰۰ محلی است. `ReminderScheduler` از AlarmManager استفاده می‌کند، `ReminderReceiver` اعلان را نشان می‌دهد و زمان‌بندی روز بعد را انجام می‌دهد. `BootReceiver` پس از دریافت `BOOT_COMPLETED` یادآور را دوباره تنظیم می‌کند. وضعیت سوییچ در SharedPreferences با نام `app_prefs` و کلید `daily_reminder_enabled` نگه‌داری می‌شود.

## ۹. آزمون و ساخت

تست‌ها در:
- `app/src/test/java/com/example/ExampleUnitTest.kt`
- `app/src/test/java/com/example/ExampleRobolectricTest.kt`
- `app/src/androidTest/java/com/example/ExampleInstrumentedTest.kt`

فرمان‌های اصلی:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

GitHub Actions در `.github/workflows/release.yml` روی Pull Request به `main` تست و APK دیباگ می‌سازد. Push به `main` نیز تست و APK دیباگ را به‌عنوان Artifact ذخیره می‌کند. تگ‌های آزمایشی پیش‌ریلیز و تگ نسخه تمیز ریلیز کامل را ایجاد می‌کنند؛ ریلیز کامل به Secretهای امضا نیاز دارد.

## ۱۰. محدودیت‌های شناخته‌شده

- Room fallback فعلاً destructive است و migration واقعی در مخزن وجود ندارد.
- تایمر در زمان بسته بودن برنامه سرویس foreground ندارد.
- پشتیبان JSON محتوای فایل‌های خارجی را کپی نمی‌کند.
- داده‌های کتاب و آزمون ثابت در کد هستند و تغییر آن‌ها نیازمند بازبینی محتوای آموزشی است.
