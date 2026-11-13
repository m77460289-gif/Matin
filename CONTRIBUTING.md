# CONTRIBUTING — راهنمای تیم

سلام رفیق 👋 خوش آمدی به تیم **Konkur Computer**.

## 📋 قبل از شروع (۵ دقیقه)

۱. **Android Studio** نصب کن — `https://developer.android.com/studio`
۲. **JDK 17** نصب باشه (Android Studio همراه خودش JBR دارد)
۳. **Git** نصب باشه
۴. ریپو را clone کن:

```bash
git clone https://github.com/m77460289-gif/Matin.git
cd Matin
```

۵. **`AGENTS.md` را بخوان** — همه چیز آنجاست (معماری، دیتابیس، ورکفلو، ترجیحات)

## 🌿 ورکفلوی Git

```bash
git checkout -b feat/اسم-کوتاه
git add -A
git commit -m "feat: توضیح انگلیسی"
git push origin feat/اسم-کوتاه
```

> **قواعد کامیت:**
> - `feat:` برای قابلیت جدید
> - `fix:` برای رفع باگ
> - `docs:` برای مستندات
> - `ci:` برای تغییرات ورکفلو
> - `chore:` برای کارهای جانبی
> - **انگلیسی بنویس**

## 🏗️ قواعد کدنویسی

### UI (Compose)
- **همه متن‌ها فارسی**
- **RTL** فعال — از `start/end` به جای `left/right`
- Material 3 + رنگ‌های پروژه (`PrimaryIndigo`, `SecondaryTeal`, `AccentGold`, `SuccessGreen`)
- برای هر صفحه `testTag` بگذار (`nav_...`, `screen_...`, `btn_...`)

### دیتابیس
- بعد از هر تغییر Entity:
  1. `version` در `AppDatabase` را **۱ عدد** زیاد کن
  2. `fallbackToDestructiveMigration()` قبلاً فعال است
- هر `@Entity` جدید باید در لیست `entities` باشد

### ViewModel
- **همه** تغییرات دیتابیس در `viewModelScope.launch`
- از `StateFlow` و `collectAsStateWithLifecycle` استفاده کن
- برای هر عملیات، یک متد مستقل در `KonkurViewModel`

## 🧪 تست

### تست دستی (قبل از PR)
- [ ] اپ اجرا می‌شود، صفحه سفید نمی‌ماند
- [ ] ۷ تب همه باز می‌شوند
- [ ] افزودن نکته → بستن اپ → باز کردن → نکته هست
- [ ] تایمر کار می‌کند (۱ دقیقه تست کن)

### تست خودکار (CI)
- هر `push` روی `main` → Actions خودکار بیلد و تست می‌کند
- موفق: تیک سبز ✅ | ناموفق: لاگ را ببین و رفع کن

## 🚀 انتشار نسخه جدید

فقط **صاحب پروژه** (پدر) تگ می‌زند:

```bash
git tag v1.x.0
git push origin v1.x.0
```

→ Actions خودکار پیش‌ریلیز می‌سازد

## 🔐 اسرار (خطرناک)

**هرگز push نکن:**
- ❌ `release-keystore.jks`
- ❌ `release-signing.properties`
- ❌ `local.properties`
- ❌ `*.env`
- ❌ توکن یا رمز هر جایی

اگر به اشتباه push کردی، **سریع به پدر خبر بده** تا کلید را عوض کنیم.

## 🎨 سبک طراحی

- **رنگ اصلی:** `#4338CA` (PrimaryIndigo)
- **رنگ ثانویه:** `#06B6D4` (SecondaryTeal)
- **رنگ تاکیدی:** `#F59E0B` (AccentGold)
- **رنگ موفقیت:** `#10B981` (SuccessGreen)
- **گوشه‌ها:** `RoundedCornerShape(12-18.dp)`
- **فاصله:** بین کارت‌ها ۱۲-۱۴dp، داخل کارت ۱۴dp

## 📂 ساختار پوشه (خلاصه)

- `data/curriculum/` — محتوای درسی
- `data/plan/` — برنامه سالانه
- `data/database/` — Room
- `data/repository/` — لایه میانی
- `ui/screens/` — هر صفحه یک فایل
- `ui/viewmodel/` — مغز اپ

> جزئیات کامل در **`AGENTS.md`** و **`PROJECT_MAP.md`**

## 🆘 مشکل داری؟

- **بیلد خطا:** `https://github.com/m77460289-gif/Matin/actions` → آخرین run → log
- **روش کار:** `AGENTS.md` را دوباره بخوان
- **ابهام:** از پدر بپرس، یا PR باز کن و کامنت بگذار

---

**خوش آمدی به تیم 🚀**