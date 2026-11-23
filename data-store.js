(function (root, factory) {
  "use strict";
  const api = factory();
  if (typeof module !== "undefined" && module.exports) module.exports = api;
  if (root) root.KonkurData = api;
})(typeof globalThis !== "undefined" ? globalThis : this, function () {
  "use strict";

  const SCHEMA_VERSION = 2;
  const MAX_ITEMS = 10000;
  const MAX_MODULES = 1000;
  const DEFAULT_TIMER = Object.freeze({
    seconds: 1500, total: 1500, running: false, end: 0, label: "تمرکز"
  });

  function isRecord(value) {
    if (!value || typeof value !== "object" || Array.isArray(value)) return false;
    const proto = Object.getPrototypeOf(value);
    return proto === Object.prototype || proto === null;
  }

  function isText(value, max, allowEmpty) {
    return typeof value === "string" && value.length <= max &&
      (allowEmpty || value.trim().length > 0);
  }

  function validId(value) {
    return (typeof value === "number" && Number.isFinite(value) && value > 0 && value <= Number.MAX_SAFE_INTEGER) ||
      (typeof value === "string" && /^[A-Za-z0-9_-]{1,80}$/.test(value));
  }

  function validDate(value) {
    return typeof value === "string" && value.length <= 80 &&
      value.trim().length > 0 && Number.isFinite(Date.parse(value));
  }

  function fail(message) {
    throw new Error(message);
  }

  function validateList(source, key, validateItem, strict) {
    if (source === undefined && !strict) return [];
    if (!Array.isArray(source)) {
      if (strict) fail("ساختار بخش " + key + " معتبر نیست.");
      return [];
    }
    if (source.length > MAX_ITEMS) {
      if (strict) fail("تعداد موارد بخش " + key + " بیش از حد مجاز است.");
      source = source.slice(0, MAX_ITEMS);
    }
    const result = [];
    for (let i = 0; i < source.length; i++) {
      const clean = validateItem(source[i]);
      if (!clean) {
        if (strict) fail("اطلاعات مورد شماره " + (i + 1) + " در بخش " + key + " معتبر نیست.");
        continue;
      }
      result.push(clean);
    }
    return result;
  }

  function validLog(x) {
    if (!isRecord(x) || !validId(x.id) || !validDate(x.date) ||
        !Number.isFinite(x.minutes) || x.minutes < 1 || x.minutes > 1440 ||
        !isText(x.subject, 160, true) || !isText(x.detail, 1000, true)) return null;
    return { id: x.id, date: x.date, minutes: x.minutes, subject: x.subject, detail: x.detail };
  }

  function validNote(x) {
    if (!isRecord(x) || !validId(x.id) || !isText(x.title, 160, false) ||
        !isText(x.body, 10000, false) || !validDate(x.date)) return null;
    return { id: x.id, title: x.title, body: x.body, date: x.date };
  }

  function validWrong(x) {
    if (!isRecord(x) || !validId(x.id) || !isText(x.question, 1000, false) ||
        !isText(x.answer, 10000, false) || !validDate(x.date)) return null;
    return { id: x.id, question: x.question, answer: x.answer, date: x.date };
  }

  function validCard(x) {
    if (!isRecord(x) || !validId(x.id) || !isText(x.front, 1000, false) ||
        !isText(x.back, 10000, false) || !Number.isInteger(x.box) ||
        x.box < 1 || x.box > 5 || !Number.isFinite(x.next) || x.next < 0 ||
        typeof x.flipped !== "boolean") return null;
    return { id: x.id, front: x.front, back: x.back, box: x.box, next: x.next, flipped: x.flipped };
  }

  function validTask(x) {
    if (!isRecord(x) || !validId(x.id) || !isText(x.text, 500, false) ||
        !validDate(x.date) || typeof x.done !== "boolean") return null;
    return { id: x.id, text: x.text, date: x.date, done: x.done };
  }

  function validQuiz(x) {
    if (!isRecord(x) || !validDate(x.date) || !Number.isInteger(x.correct) ||
        !Number.isInteger(x.total) || x.total < 1 || x.total > 500 ||
        x.correct < 0 || x.correct > x.total || typeof x.sim !== "boolean") return null;
    return { date: x.date, correct: x.correct, total: x.total, sim: x.sim };
  }

  function createDefaultState() {
    return {
      schemaVersion: SCHEMA_VERSION,
      logs: [], modules: {}, notes: [], cards: [], wrong: [], tasks: [], quizHistory: [],
      timer: { ...DEFAULT_TIMER }, targetDate: "", dailyGoal: 120
    };
  }

  function normalizeState(input, strict) {
    const source = isRecord(input) ? input : {};
    if (strict && !isRecord(input)) fail("ساختار فایل پشتیبان معتبر نیست.");

    const defaults = createDefaultState();
    const modules = Object.create(null);
    if (source.modules !== undefined && !isRecord(source.modules)) {
      if (strict) fail("ساختار پودمان‌ها معتبر نیست.");
    } else if (isRecord(source.modules)) {
      const entries = Object.entries(source.modules);
      if (entries.length > MAX_MODULES && strict) fail("تعداد پودمان‌ها بیش از حد مجاز است.");
      for (const [key, value] of entries.slice(0, MAX_MODULES)) {
        if (/^\d{1,3}-\d{1,3}$/.test(key) && typeof value === "boolean") modules[key] = value;
        else if (strict) fail("وضعیت یکی از پودمان‌ها معتبر نیست.");
      }
    }

    let timer = defaults.timer;
    if (source.timer !== undefined) {
      const t = source.timer;
      const timerValid = isRecord(t) &&
        Number.isInteger(t.seconds) && t.seconds >= 0 && t.seconds <= 86400 &&
        (t.total === undefined || (Number.isInteger(t.total) && t.total >= 1 && t.total <= 86400)) &&
        typeof t.running === "boolean" && Number.isFinite(t.end) && t.end >= 0 &&
        (t.label === undefined || isText(t.label, 120, true));
      if (!timerValid && strict) fail("تنظیمات تایمر معتبر نیست.");
      if (timerValid) {
        timer = {
          seconds: t.seconds,
          total: t.total === undefined ? 1500 : t.total,
          running: t.running && t.end > 0,
          end: t.running && t.end > 0 ? t.end : 0,
          label: t.label === undefined ? "تمرکز" : t.label
        };
      }
    }

    let targetDate = "";
    if (source.targetDate !== undefined) {
      const validTarget = typeof source.targetDate === "string" &&
        (source.targetDate === "" ||
          (/^\d{4}-\d{2}-\d{2}$/.test(source.targetDate) &&
           Number.isFinite(Date.parse(source.targetDate + "T00:00:00Z")) &&
           new Date(source.targetDate + "T00:00:00Z").toISOString().slice(0, 10) === source.targetDate));
      if (!validTarget && strict) fail("تاریخ هدف معتبر نیست.");
      if (validTarget) targetDate = source.targetDate;
    }

    let dailyGoal = defaults.dailyGoal;
    if (source.dailyGoal !== undefined) {
      if (Number.isInteger(source.dailyGoal) && source.dailyGoal >= 15 && source.dailyGoal <= 720) {
        dailyGoal = source.dailyGoal;
      } else if (strict) fail("هدف روزانه باید عددی بین ۱۵ تا ۷۲۰ دقیقه باشد.");
    }

    return {
      schemaVersion: SCHEMA_VERSION,
      logs: validateList(source.logs, "سوابق مطالعه", validLog, !!strict),
      modules,
      notes: validateList(source.notes, "یادداشت‌ها", validNote, !!strict),
      cards: validateList(source.cards, "کارت‌های مرور", validCard, !!strict),
      wrong: validateList(source.wrong, "دفترچه اشتباهات", validWrong, !!strict),
      tasks: validateList(source.tasks, "کارهای روزانه", validTask, !!strict),
      quizHistory: validateList(source.quizHistory, "سوابق آزمون", validQuiz, !!strict),
      timer, targetDate, dailyGoal
    };
  }

  function validateBackup(input) {
    if (!isRecord(input) || !Array.isArray(input.logs) ||
        !isRecord(input.modules) || !Array.isArray(input.notes)) {
      fail("این فایل پشتیبان کنکور کامپیوتر نیست یا ساختار آن ناقص است.");
    }
    return normalizeState(input, true);
  }

  function escapeHtml(value) {
    return String(value == null ? "" : value).replace(/[&<>"']/g, function (character) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[character];
    });
  }

  return Object.freeze({ SCHEMA_VERSION, createDefaultState, normalizeState, validateBackup, escapeHtml });
});
