import os
import sys
from http.server import HTTPServer, SimpleHTTPRequestHandler
import urllib.parse

PORT = 3000

HTML_CONTENT = """<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>دانلود اپلیکیشن کنکور کامپیوتر فنی حرفه‌ای</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Vazirmatn:wght@300;400;600;700;800;900&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #4338ca;
      --primary-dark: #1e1b4b;
      --secondary: #06b6d4;
      --accent: #f59e0b;
      --bg: #0f172a;
      --card-bg: #1e293b;
      --text: #f1f5f9;
      --text-muted: #94a3b8;
      --success: #10b981;
    }
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: 'Vazirmatn', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }
    body {
      background-color: var(--bg);
      color: var(--text);
      line-height: 1.6;
      padding: 20px 16px 80px 16px;
      direction: rtl;
    }
    .container {
      max-width: 720px;
      margin: 0 auto;
    }
    .hero {
      background: linear-gradient(135deg, #1e1b4b 0%, #312e81 100%);
      border: 1px solid #4338ca;
      border-radius: 20px;
      padding: 28px 20px;
      text-align: center;
      margin-bottom: 24px;
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.4);
    }
    .badge {
      display: inline-block;
      background: rgba(6, 182, 212, 0.2);
      color: var(--secondary);
      border: 1px solid var(--secondary);
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 13px;
      font-weight: 700;
      margin-bottom: 12px;
    }
    h1 {
      font-size: 24px;
      font-weight: 900;
      color: #ffffff;
      margin-bottom: 8px;
    }
    p.subtitle {
      color: #cbd5e1;
      font-size: 14px;
      margin-bottom: 24px;
    }
    .btn-group {
      display: flex;
      flex-direction: column;
      gap: 12px;
      margin-bottom: 16px;
    }
    .btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10px;
      padding: 15px 24px;
      border-radius: 14px;
      text-decoration: none;
      font-weight: 700;
      font-size: 16px;
      transition: all 0.2s ease;
      cursor: pointer;
    }
    .btn-primary {
      background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
      color: #ffffff;
      border: none;
      box-shadow: 0 4px 15px rgba(79, 70, 229, 0.4);
    }
    .btn-primary:hover {
      background: linear-gradient(135deg, #4338ca 0%, #3730a3 100%);
      transform: translateY(-2px);
    }
    .btn-secondary {
      background: #334155;
      color: #f1f5f9;
      border: 1px solid #475569;
    }
    .btn-secondary:hover {
      background: #475569;
    }
    .icon {
      font-size: 20px;
    }
    .card {
      background-color: var(--card-bg);
      border-radius: 16px;
      padding: 20px;
      margin-bottom: 20px;
      border: 1px solid #334155;
    }
    .card h2 {
      font-size: 17px;
      font-weight: 800;
      margin-bottom: 12px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #38bdf8;
    }
    .notice-box {
      background: rgba(245, 158, 11, 0.1);
      border-right: 4px solid var(--accent);
      padding: 14px;
      border-radius: 8px;
      margin-bottom: 16px;
      font-size: 13px;
      color: #fde68a;
      line-height: 1.8;
    }
    .schedule-grid {
      display: grid;
      gap: 10px;
    }
    .schedule-item {
      background: #0f172a;
      border: 1px solid #334155;
      padding: 12px 14px;
      border-radius: 10px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .slot-title {
      font-weight: 700;
      font-size: 14px;
      color: #f8fafc;
    }
    .slot-desc {
      font-size: 12px;
      color: var(--text-muted);
    }
    .slot-time {
      background: rgba(67, 56, 202, 0.3);
      color: #a5b4fc;
      padding: 3px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 700;
      white-space: nowrap;
    }
    .pill {
      background: rgba(16, 185, 129, 0.15);
      color: var(--success);
      padding: 2px 8px;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 700;
    }
    ul {
      padding-right: 18px;
      font-size: 13px;
      color: #cbd5e1;
      margin-top: 8px;
    }
    li {
      margin-bottom: 6px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="hero">
      <span class="badge">رشته شبکه و نرم‌افزار رایانه</span>
      <h1>اپلیکیشن کنکور کامپیوتر فنی‌حرفه‌ای</h1>
      <p class="subtitle">برنامه‌ریزی دقیق ۲ ساعت در روز و راهنمای جامع تغییرات کتب درسی</p>
      
      <div class="btn-group">
        <a href="/konkur-computer.apk" download="konkur-computer.apk" class="btn btn-primary">
          <span class="icon">📲</span>
          <span>دانلود مستقیم فایل نصبی (APK اندروید - ۲۳ مگابایت)</span>
        </a>
        <a href="/konkur-computer-project.zip" download="konkur-computer-project.zip" class="btn btn-secondary">
          <span class="icon">📦</span>
          <span>دانلود سورس‌کد کامل پروژه (فایل ZIP)</span>
        </a>
      </div>
      <p style="font-size: 12px; color: #94a3b8;">
        فایل نصبی تست‌شده و آماده نصب روی تمامی گوشی‌های اندروید (نسخه ۷ به بالا) است.
      </p>
    </div>

    <div class="card">
      <h2>📌 پاسخ به دغدغه تغییرات هر ساله کتاب‌های درسی</h2>
      <div class="notice-box">
        <strong>راهنمای رسمی سازمان سنجش برای کتاب‌های تغییر یافته:</strong><br>
        ۱. <strong>پایه دهم:</strong> چاپ سال ورود شما به دهم ملاک است. مباحث منسوخ مثل ویندوز ۷/۸ حذف شده و ویندوز مدرن و الگوریتم‌های استاندارد جایگزین شده‌اند.<br>
        ۲. <strong>پایه یازدهم:</strong> زبان ویژوال بیسیک و فرانت‌پیج قدیمی کلاً حذف شده است. سوالات دقیقاً از <strong>سی‌شارپ (#C)</strong>، فرم‌های ویندوز، <strong>کوئری‌های SQL</strong> و <strong>طراحی وب با HTML5 و CSS3 و PHP</strong> طرح می‌شوند.<br>
        ۳. <strong>پایه دوازدهم:</strong> جدیدترین چاپ سال فارغ‌التحصیلی ملاک است (لینوکس، فناوری‌های نوظهور IoT و امنیت).
      </div>
    </div>

    <div class="card">
      <h2>⏰ زمان‌بندی طلایی ۲ ساعت در روز (۱۲۰ دقیقه)</h2>
      <p style="font-size: 13px; color: var(--text-muted); margin-bottom: 12px;">
        تقسیم‌بندی علمی جهت حداکثر بازدهی و عدم خستگی مغز:
      </p>
      <div class="schedule-grid">
        <div class="schedule-item">
          <div>
            <div class="slot-title">پارت اول: مطالعه مفهومی پودمان تخصصی</div>
            <div class="slot-desc">خواندن خط‌به‌خط کتاب درسی + کارگاه‌های عملی</div>
          </div>
          <div class="slot-time">۴۵ دقیقه</div>
        </div>
        <div class="schedule-item">
          <div>
            <div class="slot-title">استراحت فعال</div>
            <div class="slot-desc">نوشیدن آب، تمرین کششی، دور شدن از صفحه نمایش</div>
          </div>
          <div class="slot-time" style="background: rgba(245, 158, 11, 0.2); color: #fde68a;">۱۰ دقیقه</div>
        </div>
        <div class="schedule-item">
          <div>
            <div class="slot-title">پارت دوم: تمرین ریاضی فنی یا دروس پایه</div>
            <div class="slot-desc">حل مسائل روی کاغذ (ضریب مهم در رتبه نهایی)</div>
          </div>
          <div class="slot-time">۳۵ دقیقه</div>
        </div>
        <div class="schedule-item">
          <div>
            <div class="slot-title">تنفس کوتاه</div>
            <div class="slot-desc">آماده‌سازی ذهن برای تست‌زنی</div>
          </div>
          <div class="slot-time" style="background: rgba(245, 158, 11, 0.2); color: #fde68a;">۵ دقیقه</div>
        </div>
        <div class="schedule-item">
          <div>
            <div class="slot-title">پارت سوم: تست‌زنی سرعتی و تحلیل تشریحی</div>
            <div class="slot-desc">حل تست‌های کنکور سال‌های گذشته + یادداشت نکات اشتباهات</div>
          </div>
          <div class="slot-time">۲۵ دقیقه</div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2>📚 فهرست ۳۵ پودمان کتب تخصصی در اپلیکیشن</h2>
      <ul>
        <li><strong>دانش فنی پایه (دهم):</strong> الگوریتم و فلوچارت، سیستم‌های عددی (باینری و هگز)، گیت‌های منطقی، مستندسازی و تجهیزات اداری.</li>
        <li><strong>نصب و راه‌اندازی (دهم):</strong> قطعات سخت‌افزار، بایوس و UEFI، تفاوت MBR/GPT، پیکربندی سیستم‌عامل و عیب‌یابی BSOD.</li>
        <li><strong>تولید محتوا (دهم):</strong> گرافیک برداری و شطرنجی، پردازش صوت و تصویر، انیمیشن و مقدمات الگوریتم.</li>
        <li><strong>توسعه برنامه‌سازی (یازدهم):</strong> زبان #C، حلقه‌ها، متدها، آرایه‌ها، نرمال‌سازی دیتابیس، کوئری‌های SQL و ADO.NET.</li>
        <li><strong>طراحی وب (یازدهم):</strong> تگ‌های معنایی HTML5، مدل جعبه‌ای CSS3، وب واکنش‌گرا و برنامه‌نویسی PHP/MySQL سمت سرور.</li>
        <li><strong>دانش فنی تخصصی (دوازدهم):</strong> دستورات ترمینال لینوکس (chmod, grep)، چرخه‌های چابک Agile، معماری کامپیوتر و اینترنت اشیاء.</li>
        <li><strong>تجهیزات شبکه (دوازدهم):</strong> ۷ لایه مدل OSI، کابل‌کشی T568B، پیکربندی سوئیچ و روتر، کلاس‌های IP و ساب‌نتینگ.</li>
        <li><strong>تجارت و امنیت (دوازدهم):</strong> درگاه‌های پرداخت، سئو، فایروال، رمزنگاری متقارن/نامتقارن و پدافند غیرعامل.</li>
      </ul>
    </div>
  </div>
</body>
</html>
"""

class CustomHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        url_path = urllib.parse.urlparse(self.path).path

        if url_path == "/" or url_path == "/index.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write(HTML_CONTENT.encode("utf-8"))
            return

        elif url_path == "/konkur-computer.apk":
            apk_path = os.path.join(os.getcwd(), "konkur-computer.apk")
            if not os.path.exists(apk_path):
                apk_path = os.path.join(os.getcwd(), "app/build/outputs/apk/debug/app-debug.apk")
            
            if os.path.exists(apk_path):
                self.send_response(200)
                self.send_header("Content-Type", "application/vnd.android.package-archive")
                self.send_header("Content-Disposition", 'attachment; filename="konkur-computer.apk"')
                self.send_header("Content-Length", str(os.path.getsize(apk_path)))
                self.end_headers()
                with open(apk_path, "rb") as f:
                    while chunk := f.read(65536):
                        self.wfile.write(chunk)
                return
            else:
                self.send_error(404, "APK not found")
                return

        elif url_path == "/konkur-computer-project.zip":
            zip_path = os.path.join(os.getcwd(), "konkur-computer-project.zip")
            if os.path.exists(zip_path):
                self.send_response(200)
                self.send_header("Content-Type", "application/zip")
                self.send_header("Content-Disposition", 'attachment; filename="konkur-computer-project.zip"')
                self.send_header("Content-Length", str(os.path.getsize(zip_path)))
                self.end_headers()
                with open(zip_path, "rb") as f:
                    while chunk := f.read(65536):
                        self.wfile.write(chunk)
                return
            else:
                self.send_error(404, "ZIP not found")
                return

        elif url_path == "/healthz":
            self.send_response(200)
            self.send_header("Content-Type", "text/plain")
            self.end_headers()
            self.wfile.write(b"OK")
            return

        super().do_GET()

def run():
    server_address = ("0.0.0.0", PORT)
    httpd = HTTPServer(server_address, CustomHandler)
    print(f"Starting server on port {PORT}...")
    httpd.serve_forever()

if __name__ == "__main__":
    run()
