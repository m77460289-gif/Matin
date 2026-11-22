# راهنمای مشارکت در Konkur Computer

خوش آمدید! این راهنما روش امن توسعه، تست و ارسال تغییرات را توضیح می‌دهد.

## پیش‌نیازها

- Android Studio سازگار با Android Gradle Plugin 9.1.1
- Android SDK با API 36
- JDK 21 برای هماهنگی با GitHub Actions
- Git

دریافت مخزن:

```bash
git clone https://github.com/m77460289-gif/kunkor.git
cd kunkor
```

## گردش کار Git

تغییرات را مستقیماً روی `main` انجام ندهید. برای هر کار شاخه جداگانه بسازید:

```bash
git checkout main
git pull origin main
git checkout -b feat/short-description
```

پس از تکمیل تغییرات:

```bash
git add -A
git commit -m "feat: describe the change"
git push -u origin feat/short-description
```

سپس Pull Request به شاخه `main` باز کنید. ادغام فقط پس از موفقیت تست‌ها و بیلد انجام شود.

پیشوندهای پیشنهادی commit:
- `feat:` قابلیت جدید
- `fix:` رفع اشکال
- `docs:` مستندات
- `test:` تست
- `ci:` گردش کار ساخت
- `chore:` نگه‌داری پروژه

## قواعد کدنویسی

### رابط کاربری

- متن‌های رابط کاربری فارسی و چیدمان RTL باشند.
- برای چیدمان از `start/end` استفاده کنید، نه `left/right`.
- از Compose و Material 3 و رنگ‌های تعریف‌شده در `ui/theme` استفاده کنید.
- برای کنترل‌ها و صفحه‌های مهم `testTag` مناسب بگذارید.
- از تغییر indexهای ناوبری بدون به‌روزرسانی تمام ارجاعات خودداری کنید.

### پایگاه داده

- Room فعلاً نسخه ۷ دارد و `fallbackToDestructiveMigration()` فعال است.
- مخزن schema history و migrationهای نسخه‌های پیشین را نگه نمی‌دارد.
- **صرفاً زیادکردن version یا تکیه به fallback راه‌حل مهاجرت امن نیست** و ممکن است داده محلی کاربران را پاک کند.
- تغییر Entity/ستون/کلید نیازمند طراحی migration واقعی، حفظ داده‌های قبلی و تست مهاجرت است.
- هر Entity جدید باید در `@Database(entities = ...)` ثبت شود.
- هیچ‌گاه هشدار destructive migration را با حذف بی‌برنامه داده‌ها پنهان نکنید.

### ViewModel و داده

- عملیات suspend دیتابیس را خارج از thread رابط کاربری و در Coroutine مناسب اجرا کنید.
- وضعیت رابط را با StateFlow/Flow نگه دارید و در Compose با `collectAsStateWithLifecycle` بخوانید.
- منطق تجاری را تا حد امکان از Composable جدا کنید.
- پشتیبان‌گیری JSON نسخه ۲ همه هشت جدول را شامل می‌شود؛ تغییر مدل پشتیبان باید سازگاری نسخه‌های پیشین را حفظ کند.

## تست و ساخت

پیش از بازکردن PR اجرا کنید:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

در ویندوز از `gradlew.bat` استفاده کنید. GitHub Actions همین تست‌ها و ساخت APK را برای Pull Request به `main` اجرا می‌کند.

چک‌لیست تست دستی:
- [ ] همه ۱۰ مقصد ناوبری باز می‌شوند.
- [ ] ثبت و حذف یادداشت درست کار می‌کند.
- [ ] تایمر شروع، مکث، ادامه و بازنشانی می‌شود.
- [ ] تایمر پس از بازسازی ViewModel زمان باقی‌مانده را بازیابی می‌کند.
- [ ] خروجی و بازیابی JSON هشت جدول را حفظ می‌کند.
- [ ] برای بازیابی اطلاعات، تأیید جایگزینی نمایش داده می‌شود.

## امنیت و فایل‌های خصوصی

هرگز این موارد یا هر نوع رمز و توکن را commit نکنید:
- `release-keystore.jks`
- `release-signing.properties`
- `local.properties`
- فایل‌های محیطی واقعی مانند `.env`
- توکن‌ها و کلیدهای دسترسی

فایل `debug.keystore.base64` مخصوص امضای Debug در CI است و نباید با کلید خصوصی انتشار اشتباه گرفته شود. در صورت افشای کلید انتشار، باید کلید و دسترسی‌های مرتبط فوراً تعویض شوند.

## انتشار

انتشار فقط با تگ Git و توسط نگه‌دارنده پروژه انجام می‌شود:

- تگ دارای پسوند مانند `v1.2.3-beta` یا `v1.2.3-rc`: پیش‌ریلیز با APK دیباگ
- تگ تمیز مانند `v1.2.3`: ریلیز کامل با APK امضاشده؛ Secretهای امضای انتشار باید در GitHub تنظیم شده باشند.
- Push به `main` و Pull Request فقط Artifact آزمایشی Debug می‌سازند و به‌معنای انتشار پایدار نیستند.

برای جزئیات معماری، `AGENTS.md` و `PROJECT_MAP.md` را ببینید.
