(() => {
"use strict";
const KEY="konkur-web-v1";
const Store=window.KonkurData;
if(!Store)throw new Error("ماژول اعتبارسنجی اطلاعات بارگذاری نشد.");
let storageWarning="";
let state=load(), page="dashboard", tick=null, quiz=null, flash=null;
const $=s=>document.querySelector(s);
const esc=Store.escapeHtml;
function load(){
 try{
  const raw=window.localStorage.getItem(KEY);
  return raw?Store.normalizeState(JSON.parse(raw)):Store.createDefaultState();
 }catch(error){
  console.error("Unable to load saved study data:",error);
  storageWarning="خواندن اطلاعات ذخیره‌شده ممکن نشد؛ برای جلوگیری از آسیب بیشتر، برنامه با وضعیت خالی باز شد. فایل پشتیبان را بررسی کن.";
  return Store.createDefaultState();
 }
}
function save(){
 try{
  window.localStorage.setItem(KEY,JSON.stringify(state));
  return true;
 }catch(error){
  console.error("Unable to save study data:",error);
  toast("ذخیره اطلاعات انجام نشد. فضای مرورگر یا اجازه ذخیره‌سازی را بررسی کن و از اطلاعاتت خروجی بگیر.");
  return false;
 }
}
function toast(s){const n=document.createElement("div");n.className="toast";n.textContent=s;document.body.append(n);setTimeout(()=>n.remove(),2600)}
const books=[
 {name:"دانش فنی پایه",grade:"پایه دهم",mods:["مفاهیم پایه فناوری اطلاعات","الگوریتم و فلوچارت","سیستم‌های عددی","سخت‌افزار و نرم‌افزار","شبکه و اینترنت"]},
 {name:"نصب و راه‌اندازی سیستم",grade:"پایه دهم",mods:["شناخت قطعات رایانه","نصب سیستم‌عامل","پیکربندی و عیب‌یابی","نگهداری رایانه"]},
 {name:"تولید محتوای الکترونیک",grade:"پایه دهم",mods:["مبانی تولید محتوا","تصویر و گرافیک","صوت و ویدئو","ارائه و انتشار محتوا"]},
 {name:"توسعه برنامه‌سازی",grade:"پایه یازدهم",mods:["مبانی برنامه‌نویسی","متغیر و انواع داده","شرط و حلقه","آرایه و تابع","پروژه برنامه‌سازی"]},
 {name:"پیاده‌سازی سیستم‌های اطلاعاتی",grade:"پایه یازدهم",mods:["مبانی پایگاه داده","طراحی جدول‌ها","زبان SQL","ارتباط با پایگاه داده"]},
 {name:"طراحی و پیاده‌سازی وب",grade:"پایه یازدهم",mods:["ساختار HTML","استایل‌دهی CSS","طراحی واکنش‌گرا","مبانی برنامه‌نویسی سمت سرور"]},
 {name:"دانش فنی تخصصی",grade:"پایه دوازدهم",mods:["معماری رایانه","سیستم‌عامل و لینوکس","فناوری‌های نوین","امنیت اطلاعات"]},
 {name:"نصب و نگهداری شبکه",grade:"پایه دوازدهم",mods:["مفاهیم شبکه","مدل OSI و TCP/IP","آدرس‌دهی IP","تجهیزات شبکه","عیب‌یابی شبکه"]},
 {name:"تجارت الکترونیک و امنیت",grade:"پایه دوازدهم",mods:["مبانی تجارت الکترونیک","خدمات و پرداخت آنلاین","امنیت و رمزنگاری","حفاظت از داده‌ها"]}
];
const questions=[
 {q:"کدام گزینه برای نمایش الگوریتم به‌صورت نموداری استفاده می‌شود؟",a:["فلوچارت","پایگاه داده","کامپایلر","پروتکل"],c:0,why:"فلوچارت مراحل و مسیر اجرای الگوریتم را با نمادهای استاندارد نمایش می‌دهد."},
 {q:"وظیفه اصلی RAM چیست؟",a:["ذخیره دائمی اطلاعات","نگهداری موقت داده‌های در حال پردازش","تامین برق سیستم","اتصال به شبکه"],c:1,why:"RAM حافظه موقت و سریع برای داده‌ها و برنامه‌های در حال اجراست."},
 {q:"کدام تگ HTML برای ایجاد پیوند استفاده می‌شود؟",a:["<p>","<div>","<a>","<img>"],c:2,why:"تگ a همراه ویژگی href یک پیوند ایجاد می‌کند."},
 {q:"در شبکه، DNS چه کاری انجام می‌دهد؟",a:["رمزنگاری فایل","تبدیل نام دامنه به IP","تخصیص برق","فشرده‌سازی تصویر"],c:1,why:"DNS نام دامنه را به نشانی IP متناظر تبدیل می‌کند."},
 {q:"کدام دستور SQL برای دریافت داده‌هاست؟",a:["DELETE","UPDATE","SELECT","INSERT"],c:2,why:"SELECT برای پرس‌وجو و خواندن داده از جدول‌هاست."},
 {q:"کدام ساختار برای تکرار دستورات به‌کار می‌رود؟",a:["if","for","switch","return"],c:1,why:"حلقه for یک ساختار تکرار است."},
 {q:"کدام مورد یک نشانی خصوصی IPv4 است؟",a:["8.8.8.8","192.168.1.10","1.1.1.1","200.10.1.2"],c:1,why:"بازه 192.168.0.0 تا 192.168.255.255 برای شبکه‌های خصوصی است."},
 {q:"هدف اصلی CSS چیست؟",a:["ذخیره داده","مدیریت سرور","تعیین ظاهر و چیدمان صفحه","اجرای کوئری"],c:2,why:"CSS ظاهر، رنگ، فاصله و چیدمان عناصر وب را کنترل می‌کند."},
 {q:"کدام واحد کوچک‌ترین واحد داده در رایانه است؟",a:["بایت","بیت","کیلوبایت","فایل"],c:1,why:"بیت کوچک‌ترین واحد داده و دارای مقدار صفر یا یک است."},
 {q:"فایروال برای چه منظوری استفاده می‌شود؟",a:["کنترل ترافیک شبکه","افزایش RAM","ساخت صفحه وب","خنک‌کردن پردازنده"],c:0,why:"فایروال ارتباطات ورودی و خروجی را طبق قواعد امنیتی کنترل می‌کند."}
];
function todayFa(){return new Intl.DateTimeFormat("fa-IR",{weekday:"long",year:"numeric",month:"long",day:"numeric"}).format(new Date())}
function mins(n){return Math.floor(n/60)+" ساعت و "+(n%60)+" دقیقه"}
function totalMinutes(){return state.logs.reduce((a,x)=>a+(+x.minutes||0),0)}
function dayLogs(){const d=new Date().toDateString();return state.logs.filter(x=>new Date(x.date).toDateString()===d)}
function doneModules(){return Object.values(state.modules).filter(Boolean).length}
function allModules(){return books.reduce((n,b)=>n+b.mods.length,0)}
function pct(){return Math.round(doneModules()/allModules()*100)}
function addLog(minutes,subject,detail){state.logs.push({id:Date.now()+Math.random(),date:new Date().toISOString(),minutes:+minutes,subject:subject||"مطالعه آزاد",detail:detail||""});save()}
function go(p){page=p;document.querySelectorAll(".nav-item").forEach(x=>x.classList.toggle("active",x.dataset.page===p));$("#pageTitle").textContent=({dashboard:"داشبورد",schedule:"برنامه مطالعه",books:"کتاب‌ها و پودمان‌ها",timer:"تایمر تمرکز",quiz:"آزمونک",simulator:"آزمون شبیه‌ساز",review:"مرور لایتنر",notes:"یادداشت‌ها و اشتباهات",progress:"گزارش پیشرفت",annual:"برنامه تا کنکور",parent:"گزارش همراه"})[p]||"کنکور کامپیوتر";$("#sidebar").classList.remove("open");render()}
function head(title,sub,button){return '<div class="section-head"><div><h1>'+title+'</h1><p>'+sub+'</p></div>'+(button||"")+'</div>'}
function panel(title,body,extra){return '<section class="panel"><div class="panel-head"><h2>'+title+'</h2>'+(extra||"")+'</div>'+body+'</section>'}
function stat(title,value,sub,icon){return '<div class="stat"><div class="stat-top"><span>'+title+'</span><span class="stat-icon">'+icon+'</span></div><strong>'+value+'</strong><small>'+sub+'</small></div>'}
function renderDashboard(){
 const today=dayLogs().reduce((a,x)=>a+x.minutes,0),goal=state.dailyGoal||120,percent=Math.min(100,Math.round(today/goal*100));
 const recent=state.logs.slice(-5).reverse();
 const tasks=state.tasks.filter(x=>x.date===new Date().toDateString());
 return '<div class="welcome"><div><span class="eyebrow">خوش آمدی به فضای مطالعه‌ات</span><h1>هر قدم کوچک، تو را جلوتر می‌برد ✨</h1><p>امروز را با یک هدف روشن شروع کن؛ استمرار از ساعت‌های طولانی مهم‌تر است.</p></div><div class="date-chip">'+todayFa()+'</div></div>'+
 '<div class="grid stats">'+stat("مطالعه امروز",mins(today),"هدف روزانه: "+mins(goal),"◷")+stat("کل زمان مطالعه",mins(totalMinutes()),state.logs.length+" جلسه ثبت‌شده","⌁")+stat("پودمان‌های تکمیل‌شده",doneModules()+" از "+allModules(),pct()+"٪ پیشرفت محتوایی","▤")+stat("یادداشت‌ها و کارت‌ها",state.notes.length+" / "+state.cards.length,"یادداشت / مرور لایتنر","▧")+'</div>'+
 '<div class="grid two-col">'+panel("هدف مطالعه امروز",'<div class="progress-line"><span>پیشرفت هدف</span><b>'+percent+'٪</b></div><div class="progress-track"><div class="progress-fill" style="width:'+percent+'%"></div></div><div class="progress-line"><span>'+mins(today)+' مطالعه شده</span><span>'+mins(Math.max(0,goal-today))+' باقی‌مانده</span></div><div class="form-row"><button class="btn" data-go="timer">شروع تمرکز</button><button class="btn secondary" data-go="schedule">ثبت مطالعه</button></div>')+
 panel("کارهای امروز",'<div id="todayTasks">'+(tasks.length?tasks.map(t=>'<div class="task"><input type="checkbox" data-task="'+t.id+'" '+(t.done?"checked":"")+'><span>'+esc(t.text)+'</span><span class="spacer"></span></div>').join(""):'<div class="empty">هنوز کاری برای امروز ثبت نکرده‌ای.</div>')+'</div><div class="form-row"><input id="taskText" placeholder="مثلاً مطالعه پودمان دوم"><button class="btn small" data-action="add-task">افزودن</button></div>')+'</div>'+
 '<div class="grid two-col">'+panel("آخرین فعالیت‌ها",recent.length?recent.map(x=>'<div class="task"><span class="stat-icon">◷</span><span>'+esc(x.subject)+'<small>'+new Date(x.date).toLocaleDateString("fa-IR")+' · '+mins(x.minutes)+'</small></span></div>').join(""):'<div class="empty">با ثبت اولین جلسه مطالعه، فعالیت‌ها اینجا نمایش داده می‌شوند.</div>')+
 panel("دسترسی سریع",'<div class="grid" style="grid-template-columns:repeat(2,1fr)"><button class="choice" data-go="quiz">✎ آزمونک سریع</button><button class="choice" data-go="review">↻ مرور کارت‌ها</button><button class="choice" data-go="notes">▧ ثبت نکته</button><button class="choice" data-go="progress">◫ گزارش پیشرفت</button></div>')+'</div>';
}
function renderSchedule(){
 const logs=state.logs.slice().reverse().slice(0,15);
 return head("برنامه مطالعه","جلسه‌های مطالعه را ثبت کن و هدف روزانه‌ات را دنبال کن.")+
 '<div class="grid two-col">'+panel("ثبت جلسه مطالعه",'<form id="logForm"><div class="form-row"><div class="field"><label>مدت مطالعه (دقیقه)</label><input name="minutes" type="number" min="1" max="1440" value="45" required></div><div class="field"><label>موضوع یا درس</label><input name="subject" placeholder="مثلاً دانش فنی پایه" required></div></div><div class="field"><label>توضیحات (اختیاری)</label><input name="detail" placeholder="چه چیزی مطالعه کردی؟"></div><div class="form-row"><button class="btn">ثبت جلسه</button></div></form>')+
 panel("تنظیم هدف روزانه",'<div class="field"><label>هدف مطالعه به دقیقه</label><input id="goalInput" type="number" min="15" max="720" value="'+state.dailyGoal+'"></div><div class="form-row"><button class="btn secondary" data-action="save-goal">ذخیره هدف</button></div><div class="callout">پیشنهاد: هدف قابل‌دستیابی انتخاب کن و هر هفته آن را بازبینی کن.</div>')+'</div>'+
 panel("سوابق مطالعه",logs.length?'<table class="table"><thead><tr><th>تاریخ</th><th>موضوع</th><th>مدت</th><th>توضیح</th></tr></thead><tbody>'+logs.map(x=>'<tr><td>'+new Date(x.date).toLocaleDateString("fa-IR")+'</td><td>'+esc(x.subject)+'</td><td>'+x.minutes+' دقیقه</td><td>'+esc(x.detail)+'</td></tr>').join("")+'</tbody></table>':'<div class="empty">هنوز جلسه‌ای ثبت نشده است.</div>');
}
function renderBooks(){return head("کتاب‌ها و پودمان‌ها","وضعیت یادگیری هر پودمان را علامت بزن. پیشرفت به‌صورت خودکار محاسبه می‌شود.")+'<div class="grid book-grid">'+books.map((b,i)=>'<article class="book"><span class="tag">'+b.grade+'</span><h3>'+b.name+'</h3><p>'+b.mods.filter((m,j)=>state.modules[i+"-"+j]).length+' از '+b.mods.length+' پودمان تکمیل شده</p><div class="progress-track"><div class="progress-fill" style="width:'+Math.round(b.mods.filter((m,j)=>state.modules[i+"-"+j]).length/b.mods.length*100)+'%"></div></div>'+b.mods.map((m,j)=>'<label class="module"><input type="checkbox" data-module="'+i+'-'+j+'" '+(state.modules[i+"-"+j]?"checked":"")+'><span>'+m+'</span><small>'+(state.modules[i+"-"+j]?"تمام شد":"در انتظار")+'</small></label>').join("")+'</article>').join("")+'</div><div class="callout">عنوان‌ها برای شروع برنامه‌ریزی دسته‌بندی شده‌اند؛ آن‌ها را با فهرست چاپ کتاب‌های خودت تطبیق بده.</div>'}
function renderTimer(){const t=state.timer;const left=t.running?Math.max(0,Math.ceil((t.end-Date.now())/1000)):t.seconds;const pct=Math.max(0,Math.min(100,(left/Math.max(1,t.total||1500))*100));return head("تایمر تمرکز","جلسه‌های تمرکز را زمان‌بندی کن؛ پس از پایان، زمان مطالعه در سوابق ثبت می‌شود.")+'<div class="panel timer-wrap"><div class="field" style="max-width:280px;margin:auto"><label>عنوان جلسه</label><input id="timerLabel" value="'+esc(t.label||"تمرکز")+'" '+(t.running?"disabled":"")+'></div><div class="timer-ring" style="--pct:'+pct+'%"><div><div class="timer-time" id="timerValue">'+String(Math.floor(left/60)).padStart(2,"0")+':'+String(left%60).padStart(2,"0")+'</div><div class="timer-caption">'+(t.running?"در حال تمرکز":"آماده شروع")+'</div></div></div><div class="form-row" style="justify-content:center"><button class="btn secondary" data-timer="25">۲۵ دقیقه</button><button class="btn secondary" data-timer="45">۴۵ دقیقه</button><button class="btn secondary" data-timer="60">۶۰ دقیقه</button></div><div class="timer-controls"><button class="btn" data-action="timer-toggle">'+(t.running?"توقف موقت":"شروع تمرکز")+'</button><button class="btn ghost" data-action="timer-reset">بازنشانی</button></div><p class="muted" style="font-size:11px;margin-top:20px">تایمر حتی با تازه‌سازی صفحه، زمان باقی‌مانده را از روی ساعت سیستم محاسبه می‌کند.</p></div>'}
function startTimer(){const t=state.timer;t.running=true;t.end=Date.now()+t.seconds*1000;t.label=$("#timerLabel")?.value||t.label;save();runTicker();render()}
function pauseTimer(){const t=state.timer;t.seconds=Math.max(0,Math.ceil((t.end-Date.now())/1000));t.running=false;t.end=0;save();render()}
function runTicker(){clearInterval(tick);if(!state.timer.running)return;tick=setInterval(()=>{let t=state.timer,left=Math.max(0,Math.ceil((t.end-Date.now())/1000));const el=$("#timerValue");if(el)el.textContent=String(Math.floor(left/60)).padStart(2,"0")+":"+String(left%60).padStart(2,"0");if(left<=0){clearInterval(tick);t.running=false;const elapsed=Math.round((t.total||1500)/60);addLog(elapsed,t.label||"تمرکز","ثبت‌شده توسط تایمر");t.seconds=t.total||1500;t.end=0;save();toast("آفرین! جلسه تمرکز ثبت شد.");render()}},300)}
function beginQuiz(sim){quiz={sim,index:0,answers:[],list:sim?questions.slice(0,10):questions.slice(0,5).sort(()=>Math.random()-.5)};renderQuiz()}
function renderQuiz(){if(!quiz)return;const q=quiz.list[quiz.index],answered=quiz.answers[quiz.index]!==undefined;$("#content").innerHTML=head(quiz.sim?"آزمون شبیه‌ساز":"آزمونک","پاسخ‌ها پس از ثبت هر سؤال بررسی می‌شوند.")+'<section class="panel" style="max-width:760px;margin:auto"><div class="quiz-meta"><span>سؤال '+(quiz.index+1)+' از '+quiz.list.length+'</span><span>پاسخ داده‌شده: '+quiz.answers.filter(x=>x!==undefined).length+'</span></div><div class="progress-track" style="margin:12px 0 22px"><div class="progress-fill" style="width:'+((quiz.index+1)/quiz.list.length*100)+'%"></div></div><div class="question">'+esc(q.q)+'</div><div class="choice-grid">'+q.a.map((a,i)=>'<button class="choice '+(answered?(i===q.c?"correct":quiz.answers[quiz.index]===i?"wrong":""):"")+'" data-answer="'+i+'" '+(answered?"disabled":"")+'>'+String.fromCharCode(1776+i)+' . '+esc(a)+'</button>').join("")+'</div>'+(answered?'<div class="callout">'+(quiz.answers[quiz.index]===q.c?"درست پاسخ دادی!":"این گزینه درست نبود.")+' '+esc(q.why)+'</div>':"")+'<div class="form-row"><button class="btn secondary" data-action="quiz-exit">خروج</button><button class="btn" data-action="quiz-next" '+(!answered?"disabled":"")+'>'+(quiz.index===quiz.list.length-1?"مشاهده نتیجه":"سؤال بعدی")+'</button></div></section>'}
function finishQuiz(){const correct=quiz.answers.filter((a,i)=>a===quiz.list[i].c).length;state.quizHistory.push({date:new Date().toISOString(),correct,total:quiz.list.length,sim:quiz.sim});save();$("#content").innerHTML=head("نتیجه آزمون","عملکردت را بررسی کن و نکته‌های اشتباه را ثبت کن.")+'<div class="panel timer-wrap"><div class="stat-icon" style="margin:auto;width:60px;height:60px;font-size:26px">✓</div><h1 style="font-size:40px">'+Math.round(correct/quiz.list.length*100)+'٪</h1><p>پاسخ صحیح: '+correct+' از '+quiz.list.length+'</p><div class="form-row" style="justify-content:center"><button class="btn" data-go="quiz">آزمون دوباره</button><button class="btn secondary" data-go="notes">ثبت نکته</button></div></div>';quiz=null}
function renderQuizHome(sim){return head(sim?"آزمون شبیه‌ساز":"آزمونک",sim?"یک آزمون ۱۰ سؤالی برای سنجش آموخته‌ها انجام بده.":"با آزمون‌های کوتاه، یادگیری خودت را محک بزن.")+'<div class="grid two-col">'+panel(sim?"آزمون جامع تمرینی":"آزمون سریع ۵ سؤالی",'<p class="muted" style="font-size:12px">سؤالات نمونه از مفاهیم پایه کامپیوتر، برنامه‌نویسی، شبکه و وب هستند. نتیجه در گزارش پیشرفت ذخیره می‌شود.</p><div class="form-row"><button class="btn" data-action="'+(sim?"start-sim":"start-quiz")+'">شروع آزمون</button></div>')+panel("نتایج قبلی",state.quizHistory.length?state.quizHistory.slice(-6).reverse().map(x=>'<div class="task"><span>'+(x.sim?"شبیه‌ساز":"آزمونک")+'</span><span class="spacer"></span><b>'+x.correct+'/'+x.total+'</b><small>'+new Date(x.date).toLocaleDateString("fa-IR")+'</small></div>').join(""):'<div class="empty">هنوز آزمونی انجام نداده‌ای.</div>')+'</div>'}
function renderNotes(){return head("یادداشت‌ها و اشتباهات","نکته‌های مهم و سؤال‌هایی را که اشتباه پاسخ داده‌ای ثبت کن.")+'<div class="grid two-col">'+panel("یادداشت جدید",'<form id="noteForm"><div class="field"><label>عنوان</label><input name="title" required placeholder="عنوان نکته"></div><div class="field" style="margin-top:10px"><label>متن یادداشت</label><textarea name="body" required placeholder="نکته یا توضیح را بنویس..."></textarea></div><div class="form-row"><button class="btn">ذخیره یادداشت</button></div></form>')+panel("ثبت پاسخ اشتباه",'<form id="wrongForm"><div class="field"><label>سؤال یا موضوع</label><input name="question" required></div><div class="field"><label>پاسخ درست / نکته</label><textarea name="answer" required></textarea></div><div class="form-row"><button class="btn secondary">ثبت در دفترچه اشتباهات</button></div></form>')+'</div>'+panel("یادداشت‌های من",state.notes.length?state.notes.slice().reverse().map(n=>'<article class="note"><b>'+esc(n.title)+'</b><p>'+esc(n.body)+'</p><small>'+new Date(n.date).toLocaleDateString("fa-IR")+'</small><button class="btn small danger" data-delete-note="'+n.id+'">حذف</button></article>').join(""):'<div class="empty">هنوز یادداشتی ثبت نشده است.</div>')+panel("دفترچه اشتباهات",state.wrong.length?state.wrong.slice().reverse().map(n=>'<article class="note"><b>'+esc(n.question)+'</b><p>'+esc(n.answer)+'</p><small>'+new Date(n.date).toLocaleDateString("fa-IR")+'</small><button class="btn small danger" data-delete-wrong="'+n.id+'">حذف</button></article>').join(""):'<div class="empty">اشتباهات آزمون را اینجا ثبت کن تا دوباره مرورشان کنی.</div>')}
function dueCards(){return state.cards.filter(c=>!c.next||c.next<=Date.now())}
function renderReview(){const due=dueCards();if(flash){const c=state.cards.find(x=>x.id===flash);return head("مرور لایتنر","ابتدا پاسخ را در ذهن مرور کن، سپس کارت را برگردان.")+'<div class="panel flashcard" data-action="flip-card"><div><span class="tag">کارت مرور</span><h2>'+esc(c.front)+'</h2><p>'+(c.flipped?"پاسخ: "+esc(c.back):"برای دیدن پاسخ لمس کن")+'</p></div></div>'+(c.flipped?'<div class="actions"><button class="btn danger" data-rate="0">نیاز به تمرین</button><button class="btn secondary" data-rate="1">سخت بود</button><button class="btn" data-rate="2">بلدم</button></div>':"")}
 return head("مرور فاصله‌دار لایتنر","کارت‌های پرسش و پاسخ بساز و در فاصله‌های زمانی مرور کن.")+'<div class="grid two-col">'+panel("ساخت کارت جدید",'<form id="cardForm"><div class="field"><label>روی کارت (پرسش)</label><input name="front" required></div><div class="field" style="margin-top:10px"><label>پشت کارت (پاسخ)</label><textarea name="back" required></textarea></div><div class="form-row"><button class="btn">افزودن کارت</button></div></form>')+panel("آمار مرور",'<strong style="font-size:30px">'+due.length+'</strong><p class="muted" style="font-size:11px">کارت آماده مرور از '+state.cards.length+' کارت</p><button class="btn" data-action="review-start" '+(!due.length?"disabled":"")+'>شروع مرور</button>')+'</div>'+panel("کارت‌های ثبت‌شده",state.cards.length?state.cards.map(c=>'<div class="task"><span>'+esc(c.front)+'</span><span class="spacer"></span><span class="tag">جعبه '+(c.box||1)+'</span><button class="btn small danger" data-delete-card="'+c.id+'">حذف</button></div>').join(""):'<div class="empty">هنوز کارتی نساخته‌ای.</div>')}
function renderProgress(){const total=totalMinutes(),hours=Math.floor(total/60),by={};state.logs.forEach(x=>by[x.subject]=(by[x.subject]||0)+x.minutes);const subjects=Object.entries(by).sort((a,b)=>b[1]-a[1]).slice(0,6);return head("گزارش پیشرفت","نمای کلی زمان مطالعه، آزمون‌ها و تکمیل پودمان‌ها.")+'<div class="grid stats">'+stat("کل مطالعه",hours+" ساعت",state.logs.length+" جلسه","◷")+stat("میانگین هر جلسه",state.logs.length?Math.round(total/state.logs.length)+" دقیقه":"۰","بر اساس سوابق ثبت‌شده","⌁")+stat("میانگین آزمون‌ها",state.quizHistory.length?Math.round(state.quizHistory.reduce((a,x)=>a+x.correct/x.total*100,0)/state.quizHistory.length)+"٪":"—",state.quizHistory.length+" آزمون","✎")+stat("پیشرفت پودمان‌ها",pct()+"٪",doneModules()+" پودمان تکمیل‌شده","▤")+'</div><div class="grid two-col">'+panel("زمان مطالعه به تفکیک درس",subjects.length?subjects.map(([s,v])=>'<div class="bar-row"><span>'+esc(s)+'</span><div class="bar"><i style="width:'+Math.min(100,v/Math.max(1,total)*100)+'%"></i></div><b>'+v+'د</b></div>').join(""):'<div class="empty">برای نمایش نمودار، جلسه مطالعه ثبت کن.</div>')+panel("روند آزمون‌ها",state.quizHistory.length?state.quizHistory.slice(-8).map(x=>'<div class="task"><span>'+new Date(x.date).toLocaleDateString("fa-IR")+'</span><span class="spacer"></span><b>'+Math.round(x.correct/x.total*100)+'٪</b></div>').join(""):'<div class="empty">نتایج آزمون‌ها اینجا نمایش داده می‌شوند.</div>')+'</div>'}
function renderAnnual(){const target=state.targetDate;const diff=target?Math.ceil((new Date(target+"T23:59:59")-new Date())/86400000):null;return head("برنامه تا کنکور","تاریخ هدف را تعیین کن و مسیر باقی‌مانده را ببین.")+'<div class="grid two-col">'+panel("تاریخ کنکور یا هدف",'<div class="field"><label>تاریخ هدف</label><input id="targetDate" type="date" value="'+esc(target)+'"></div><div class="form-row"><button class="btn" data-action="save-target">ذخیره تاریخ</button></div><div class="callout">'+(diff===null?"هنوز تاریخ هدفی ثبت نکرده‌ای.":diff>=0?"تا تاریخ هدف "+diff+" روز باقی مانده است.":"تاریخ هدف گذشته است؛ تاریخ جدیدی تنظیم کن.")+'</div>')+panel("تقسیم مسیر پیشنهادی",'<div class="task"><span class="tag">مرحله ۱</span><span>یادگیری مفهومی کتاب‌ها و پودمان‌ها</span></div><div class="task"><span class="tag">مرحله ۲</span><span>تمرین و تست مبحثی پس از هر پودمان</span></div><div class="task"><span class="tag">مرحله ۳</span><span>مرور فاصله‌دار و رفع اشکال</span></div><div class="task"><span class="tag">مرحله ۴</span><span>آزمون جامع و تحلیل نتیجه</span></div>')+'</div>'}
function renderParent(){return head("گزارش همراه","نمای خلاصه‌ای از فعالیت‌های ثبت‌شده برای پیگیری روند مطالعه.")+'<div class="grid stats">'+stat("زمان مطالعه امروز",mins(dayLogs().reduce((a,x)=>a+x.minutes,0)),"بر اساس ثبت امروز","◷")+stat("کل زمان مطالعه",mins(totalMinutes()),state.logs.length+" جلسه","⌁")+stat("پودمان تکمیل‌شده",doneModules(),pct()+"٪ از کل","▤")+stat("آزمون انجام‌شده",state.quizHistory.length,"آزمونک و شبیه‌ساز","✎")+'</div>'+panel("آخرین فعالیت‌ها",state.logs.length?'<table class="table"><thead><tr><th>تاریخ</th><th>درس</th><th>زمان</th></tr></thead><tbody>'+state.logs.slice(-12).reverse().map(x=>'<tr><td>'+new Date(x.date).toLocaleDateString("fa-IR")+'</td><td>'+esc(x.subject)+'</td><td>'+x.minutes+' دقیقه</td></tr>').join("")+'</tbody></table>':'<div class="empty">هنوز فعالیتی ثبت نشده است.</div>')+'<div class="callout">این گزارش فقط داده‌هایی را نشان می‌دهد که در همین مرورگر ثبت شده‌اند؛ برای ارسال گزارش به شخص دیگر، از پشتیبان‌گیری خروجی بگیر.</div>'}
function render(){if(page==="dashboard")$("#content").innerHTML=renderDashboard();else if(page==="schedule")$("#content").innerHTML=renderSchedule();else if(page==="books")$("#content").innerHTML=renderBooks();else if(page==="timer")$("#content").innerHTML=renderTimer();else if(page==="quiz")$("#content").innerHTML=renderQuizHome(false);else if(page==="simulator")$("#content").innerHTML=renderQuizHome(true);else if(page==="notes")$("#content").innerHTML=renderNotes();else if(page==="review")$("#content").innerHTML=renderReview();else if(page==="progress")$("#content").innerHTML=renderProgress();else if(page==="annual")$("#content").innerHTML=renderAnnual();else if(page==="parent")$("#content").innerHTML=renderParent();if(page==="timer"&&state.timer.running)runTicker()}
function exportData(){
 const blob=new Blob([JSON.stringify({...state,schemaVersion:Store.SCHEMA_VERSION,exportedAt:new Date().toISOString()},null,2)],{type:"application/json"});
 const url=URL.createObjectURL(blob),a=document.createElement("a");
 a.href=url;a.download="konkur-computer-backup.json";a.click();
 setTimeout(()=>URL.revokeObjectURL(url),1000);
 toast("فایل پشتیبان آماده شد.");
}
function importData(file){
 if(!file)return;
 if(file.size>10*1024*1024){toast("حجم فایل پشتیبان نباید بیشتر از ۱۰ مگابایت باشد.");return}
 const r=new FileReader();
 r.onerror=()=>toast("خواندن فایل پشتیبان ناموفق بود.");
 r.onload=()=>{
  let imported;
  try{imported=Store.validateBackup(JSON.parse(String(r.result||"")))}
  catch(error){toast(error.message||"فایل پشتیبان معتبر نیست.");return}
  const previous=state;
  state=imported;
  if(!save()){state=previous;return}
  clearInterval(tick);quiz=null;flash=null;
  render();toast("اطلاعات با موفقیت اعتبارسنجی و بازیابی شد.");
 };
 r.readAsText(file);
}
document.addEventListener("click",e=>{
 const nav=e.target.closest("[data-page]");if(nav){go(nav.dataset.page);return}
 const g=e.target.closest("[data-go]");if(g){go(g.dataset.go);return}
 const act=e.target.closest("[data-action]")?.dataset.action;
 if(act==="add-task"){const v=$("#taskText").value.trim();if(v){state.tasks.push({id:Date.now(),text:v,date:new Date().toDateString(),done:false});save();render()}}
 if(act==="save-goal"){state.dailyGoal=Math.max(15,Math.min(720,+$("#goalInput").value||120));save();render();toast("هدف روزانه ذخیره شد.")}
 if(act==="timer-toggle"){state.timer.running?pauseTimer():startTimer()}
 if(act==="timer-reset"){clearInterval(tick);state.timer={seconds:1500,total:1500,running:false,end:0,label:"تمرکز"};save();render()}
 if(act==="start-quiz")beginQuiz(false);if(act==="start-sim")beginQuiz(true);
 if(act==="quiz-exit"){quiz=null;go("quiz")}if(act==="quiz-next"){if(quiz.index<quiz.list.length-1){quiz.index++;renderQuiz()}else finishQuiz()}
 if(act==="flip-card"){const c=state.cards.find(x=>x.id===flash);if(c){c.flipped=true;save();render()}}
 if(act==="review-start"){flash=dueCards()[0]?.id;if(flash){state.cards.find(x=>x.id===flash).flipped=false;render()}}
 if(act==="save-target"){state.targetDate=$("#targetDate").value;save();render();toast("تاریخ هدف ذخیره شد.")}
 const answer=e.target.closest("[data-answer]");if(answer&&quiz){quiz.answers[quiz.index]=+answer.dataset.answer;renderQuiz()}
 const tm=e.target.closest("[data-timer]");if(tm&&!state.timer.running){const m=+tm.dataset.timer;state.timer={seconds:m*60,total:m*60,running:false,end:0,label:"تمرکز"};save();render()}
 const rate=e.target.closest("[data-rate]");if(rate&&flash){const c=state.cards.find(x=>x.id===flash),n=+rate.dataset.rate;c.box=n===0?1:n===1?Math.max(1,c.box||1):Math.min(5,(c.box||1)+1);c.next=Date.now()+[0,1,3,7,14,30][c.box]*86400000;c.flipped=false;save();flash=null;render();toast("مرور کارت ثبت شد.")}
 const dn=e.target.closest("[data-delete-note]");if(dn){state.notes=state.notes.filter(x=>x.id!=dn.dataset.deleteNote);save();render()}
 const dw=e.target.closest("[data-delete-wrong]");if(dw){state.wrong=state.wrong.filter(x=>x.id!=dw.dataset.deleteWrong);save();render()}
 const dc=e.target.closest("[data-delete-card]");if(dc){state.cards=state.cards.filter(x=>x.id!=dc.dataset.deleteCard);save();render()}
});
document.addEventListener("change",e=>{
 if(e.target.matches("[data-module]")){state.modules[e.target.dataset.module]=e.target.checked;save();render()}
 if(e.target.matches("[data-task]")){const t=state.tasks.find(x=>x.id==e.target.dataset.task);if(t)t.done=e.target.checked;save()}
});
document.addEventListener("submit",e=>{
 e.preventDefault();const f=new FormData(e.target);
 if(e.target.id==="logForm"){addLog(f.get("minutes"),f.get("subject"),f.get("detail"));render();toast("جلسه مطالعه ثبت شد.")}
 if(e.target.id==="noteForm"){state.notes.push({id:Date.now(),title:f.get("title"),body:f.get("body"),date:new Date().toISOString()});save();render();toast("یادداشت ذخیره شد.")}
 if(e.target.id==="wrongForm"){state.wrong.push({id:Date.now(),question:f.get("question"),answer:f.get("answer"),date:new Date().toISOString()});save();render();toast("به دفترچه اشتباهات اضافه شد.")}
 if(e.target.id==="cardForm"){state.cards.push({id:Date.now(),front:f.get("front"),back:f.get("back"),box:1,next:0,flipped:false});save();render();toast("کارت جدید ساخته شد.")}
});
$("#menuBtn").addEventListener("click",()=>$("#sidebar").classList.toggle("open"));
$("#backupBtn").addEventListener("click",exportData);
$("#restoreFile").addEventListener("change",e=>{if(e.target.files[0])importData(e.target.files[0]);e.target.value=""});
$("#today").textContent=todayFa();
document.querySelectorAll("[data-go]").forEach(x=>x.addEventListener("click",()=>go(x.dataset.go)));
render();
if(storageWarning)toast(storageWarning);
if(state.timer.running){if(state.timer.end<=Date.now()){state.timer.running=false;addLog(Math.round((state.timer.total||1500)/60),state.timer.label,"بازیابی پایان تایمر");state.timer.seconds=state.timer.total||1500;save();toast("تایمر در زمان بسته بودن صفحه تمام شد و ثبت شد.")}else runTicker()}
})();