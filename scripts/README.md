# scripts/ — ابزارهای نگهداری

ابزارهای یک‌بارمصرف یا اداری. این‌ها بخشی از اپ نیستند.

## `update_secrets.py`

**کاربرد:** آپلود مجدد keystore و رمزهای ریلیز در GitHub Secrets.

**پیش‌نیاز:**
- Python 3.10+
- `pynacl` — `python -m pip install pynacl`
- فایل `release-keystore.jks` در ریشه پروژه
- توکن گیت‌هاب با دسترسی `repo`

**اجرا:**
```bash
# Linux/macOS
export GH_TOKEN=ghp_...
python scripts/update_secrets.py

# Windows PowerShell
$env:GH_TOKEN="ghp_..."
python scripts/update_secrets.py
```

**⚠️ هرگز توکن را داخل فایل یا کامیت نگذار.**