"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const Store = require("../data-store.js");

test("default state has a complete, versioned schema", () => {
  const state = Store.createDefaultState();
  assert.equal(state.schemaVersion, Store.SCHEMA_VERSION);
  for (const key of ["logs", "notes", "cards", "wrong", "tasks", "quizHistory"]) assert.deepEqual(state[key], []);
  assert.equal(state.timer.total, 1500);
  assert.equal(state.dailyGoal, 120);
});

test("legacy browser data is migrated without losing valid records", () => {
  const legacy = {
    logs: [{ id: 123.456, date: "2026-11-23T10:00:00.000Z", minutes: 45, subject: "شبکه", detail: "" }],
    modules: { "0-1": true },
    notes: [{ id: 456, title: "نکته", body: "متن", date: "2026-11-23T10:00:00.000Z" }],
    timer: { seconds: 900, running: false, end: 0, label: "تمرکز" },
    dailyGoal: 150
  };
  const migrated = Store.normalizeState(legacy);
  assert.equal(migrated.schemaVersion, 2);
  assert.equal(migrated.logs[0].minutes, 45);
  assert.equal(migrated.modules["0-1"], true);
  assert.equal(migrated.notes[0].title, "نکته");
  assert.equal(migrated.timer.total, 1500);
  assert.equal(migrated.dailyGoal, 150);
});

test("malformed local data is reduced to safe defaults without throwing", () => {
  const normalized = Store.normalizeState({
    logs: [{ id: "bad id", date: "not-a-date", minutes: 99999, subject: "<script>", detail: "" }],
    modules: { "0-1": true, "__proto__": true, "1-2": "yes" },
    notes: "not an array",
    timer: { seconds: -1, running: "yes", end: "now", label: {} },
    dailyGoal: 999
  });
  assert.deepEqual(normalized.logs, []);
  assert.deepEqual(Object.keys(normalized.modules), ["0-1"]);
  assert.deepEqual(normalized.notes, []);
  assert.equal(normalized.timer.running, false);
  assert.equal(normalized.dailyGoal, 120);
});

test("valid current-format backup is accepted", () => {
  const backup = {
    schemaVersion: 2,
    logs: [{ id: 123, date: "2026-11-23T10:00:00.000Z", minutes: 30, subject: "وب", detail: "" }],
    modules: { "2-3": false },
    notes: [],
    cards: [{ id: 321, front: "سؤال", back: "پاسخ", box: 1, next: 0, flipped: false }],
    wrong: [],
    tasks: [{ id: 654, text: "مرور", date: "Mon Nov 23 2026", done: false }],
    quizHistory: [{ date: "2026-11-23T10:00:00.000Z", correct: 4, total: 5, sim: false }],
    timer: { seconds: 600, total: 1500, running: false, end: 0, label: "تمرکز" },
    targetDate: "2027-06-01",
    dailyGoal: 120
  };
  const restored = Store.validateBackup(backup);
  assert.equal(restored.logs.length, 1);
  assert.equal(restored.cards.length, 1);
  assert.equal(restored.tasks.length, 1);
  assert.equal(restored.quizHistory[0].correct, 4);
  assert.equal(restored.targetDate, "2027-06-01");
});

test("backup import rejects invalid records instead of silently importing partial data", () => {
  const backup = {
    logs: [], modules: {}, notes: [],
    cards: [{ id: 2, front: "question", back: "answer", box: 99, next: 0, flipped: false }]
  };
  assert.throws(() => Store.validateBackup(backup), /کارت‌های مرور/);
});

test("backup import rejects missing required sections and oversized collections", () => {
  assert.throws(() => Store.validateBackup({ logs: [], modules: {} }), /ساختار/);
  const backup = { logs: new Array(10001).fill(null), modules: {}, notes: [] };
  assert.throws(() => Store.validateBackup(backup), /بیش از حد/);
});

test("HTML-sensitive text is escaped before being inserted into the interface", () => {
  assert.equal(
    Store.escapeHtml("<img src=x onerror=\"alert(1)\">&'"),
    "&lt;img src=x onerror=&quot;alert(1)&quot;&gt;&amp;&#39;"
  );
  assert.equal(Store.escapeHtml(null), "");
});
