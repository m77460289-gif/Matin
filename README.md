# Konkur Computer - کنکور کامپیوتر

اپلیکیشن اندروید برنامه‌ریزی مطالعه کنکور فنی‌وحرفه‌ای، رشته شبکه و نرم‌افزار رایانه.

## درباره پروژه

- برنامه‌ریزی دقیق ۲ ساعت مطالعه روزانه
- تحلیل تغییرات کتب درسی دهم / یازدهم / دوازدهم
- پیگیری وضعیت ۳۵ پودمان (مطالعه / مرور / تست)
- تایمر تمرکز (پومودورو ۴۵/۳۵/۲۵ دقیقه)
- آمار پیشرفت + دفترچه نکات شخصی
- آزمونک تستی با تحلیل تشریحی و نکته طلایی

## اجرا

```bash
./gradlew assembleDebug
# خروجی: app/build/outputs/apk/debug/app-debug.apk

python server.py        # وب‌لندینگ روی http://localhost:3000
```

## معماری

- UI: Jetpack Compose + Material 3 (RTL فارسی)
- State: KonkurViewModel (AndroidViewModel + StateFlow)
- Data: Room (۴ جدول) + Repository Pattern
- Static Data: ComputerCurriculumData
- Adaptive UI: گوشی (Bottom Nav) + تبلت (NavigationRail)

## ماژول‌های کلیدی

- MainActivity.kt - ورودی و Navigation
- KonkurViewModel.kt - State، تایمر، ثبت مطالعه
- data/model/Models.kt - Entity و Domain Models
- data/database/AppDatabase.kt و StudyDao.kt - Room
- data/repository/StudyRepository.kt
- data/curriculum/ComputerCurriculumData.kt - داده‌های ثابت
- ui/screens/* - شش صفحه اپلیکیشن
- server.py - وب‌لندینگ + Serve فایل APK و ZIP

## جداول دیتابیس

| جدول | نقش |
|---|---|
| study_logs | جلسات مطالعه ثبت‌شده |
| poodman_progress | وضعیت مطالعه هر پودمان |
| study_notes | دفترچه نکات شخصی |
| daily_slot_status | چک‌لیست برنامه روزانه |

## حداقل سیستم‌عامل

- minSdk 24 (اندروید ۷)
- targetSdk 36