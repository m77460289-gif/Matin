package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.database.AppDatabase
import com.example.data.model.CheckInEntity
import com.example.data.model.DailySlotStatusEntity
import com.example.data.model.LeitnerCardEntity
import com.example.data.model.PoodmanProgressEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.StudyNoteEntity
import com.example.data.model.WeeklyScoreEntity
import com.example.data.model.WrongAnswerEntity
import org.json.JSONArray
import org.json.JSONObject

/**
 * JSON backup / restore utility for all Room tables.
 * One file (URI) contains everything: study_logs, poodman_progress, notes,
 * daily_slot_status, weekly_checkins, leitner_cards, wrong_answers, weekly_scores.
 */
object BackupManager {

    const val BACKUP_VERSION = 1

    /**
     * Serialize all database content into a single JSON object string.
     */
    suspend fun exportToJson(db: AppDatabase): String {
        val dao = db.studyDao()
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        // study_logs
        val logs = dao.getAllStudyLogsOnce()
        val logsArr = JSONArray()
        logs.forEach { l ->
            logsArr.put(JSONObject().apply {
                put("timestamp", l.timestamp)
                put("durationMinutes", l.durationMinutes)
                put("subjectTitle", l.subjectTitle)
                put("note", l.note)
            })
        }
        root.put("study_logs", logsArr)

        // poodman_progress
        val progress = dao.getAllProgressOnce()
        val progArr = JSONArray()
        progress.forEach { p ->
            progArr.put(JSONObject().apply {
                put("bookId", p.bookId)
                put("poodmanIndex", p.poodmanIndex)
                put("isStudied", p.isStudied)
                put("isReviewed", p.isReviewed)
                put("isTestPracticed", p.isTestPracticed)
                put("hasVideo", p.hasVideo)
                put("hasPdf", p.hasPdf)
                put("hasTests", p.hasTests)
                put("videoUrl", p.videoUrl)
                put("pdfPath", p.pdfPath)
                put("testUrl", p.testUrl)
                put("lastUpdated", p.lastUpdated)
            })
        }
        root.put("poodman_progress", progArr)

        // notes
        val notes = dao.getAllNotesOnce()
        val notesArr = JSONArray()
        notes.forEach { n ->
            notesArr.put(JSONObject().apply {
                put("title", n.title)
                put("bookName", n.bookName)
                put("content", n.content)
                put("createdAt", n.createdAt)
            })
        }
        root.put("study_notes", notesArr)

        // leitner_cards
        val leitner = dao.getAllLeitnerCardsOnce()
        val leitArr = JSONArray()
        leitner.forEach { c ->
            leitArr.put(JSONObject().apply {
                put("bookId", c.bookId)
                put("poodmanIndex", c.poodmanIndex)
                put("bookTitle", c.bookTitle)
                put("poodmanTitle", c.poodmanTitle)
                put("boxLevel", c.boxLevel)
                put("nextReviewAt", c.nextReviewAt)
                put("lastReviewedAt", c.lastReviewedAt)
                put("reviewCount", c.reviewCount)
                put("isMastered", c.isMastered)
            })
        }
        root.put("leitner_cards", leitArr)

        // wrong_answers
        val wrong = dao.getAllWrongAnswersOnce()
        val wrongArr = JSONArray()
        wrong.forEach { w ->
            wrongArr.put(JSONObject().apply {
                put("questionId", w.questionId)
                put("timesWrong", w.timesWrong)
                put("lastWrongAt", w.lastWrongAt)
                put("isMastered", w.isMastered)
            })
        }
        root.put("wrong_answers", wrongArr)

        // weekly_scores
        val scores = dao.getAllWeeklyScoresOnce()
        val scoreArr = JSONArray()
        scores.forEach { s ->
            scoreArr.put(JSONObject().apply {
                put("weekNumber", s.weekNumber)
                put("scorePercent", s.scorePercent)
                put("examTitle", s.examTitle)
                put("recordedAt", s.recordedAt)
            })
        }
        root.put("weekly_scores", scoreArr)

        return root.toString(2)
    }

    /**
     * Parse a backup JSON string and replace all tables' contents.
     * Returns counts (logs, progress, notes, ...) for confirmation message.
     */
    suspend fun importFromJson(db: AppDatabase, json: String): ImportResult {
        val dao = db.studyDao()
        val root = JSONObject(json)

        val logs = root.optJSONArray("study_logs") ?: JSONArray()
        val progress = root.optJSONArray("poodman_progress") ?: JSONArray()
        val notes = root.optJSONArray("study_notes") ?: JSONArray()
        val leitner = root.optJSONArray("leitner_cards") ?: JSONArray()
        val wrongs = root.optJSONArray("wrong_answers") ?: JSONArray()
        val scores = root.optJSONArray("weekly_scores") ?: JSONArray()

        // Clear existing data first
        dao.clearAllStudyLogs()
        dao.clearAllProgress()
        dao.clearAllNotes()
        dao.clearAllLeitnerCards()
        dao.clearWrongAnswers()
        dao.clearAllWeeklyScores()

        var logCount = 0; var progCount = 0; var noteCount = 0
        var leitCount = 0; var wrongCount = 0; var scoreCount = 0

        for (i in 0 until logs.length()) {
            val o = logs.getJSONObject(i)
            dao.insertStudyLog(StudyLogEntity(
                timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                durationMinutes = o.optInt("durationMinutes", 0),
                subjectTitle = o.optString("subjectTitle", ""),
                note = o.optString("note", "")
            ))
            logCount++
        }
        for (i in 0 until progress.length()) {
            val o = progress.getJSONObject(i)
            dao.savePoodmanProgress(PoodmanProgressEntity(
                bookId = o.optString("bookId"),
                poodmanIndex = o.optInt("poodmanIndex"),
                isStudied = o.optBoolean("isStudied"),
                isReviewed = o.optBoolean("isReviewed"),
                isTestPracticed = o.optBoolean("isTestPracticed"),
                hasVideo = o.optBoolean("hasVideo"),
                hasPdf = o.optBoolean("hasPdf"),
                hasTests = o.optBoolean("hasTests"),
                videoUrl = o.optString("videoUrl", ""),
                pdfPath = o.optString("pdfPath", ""),
                testUrl = o.optString("testUrl", ""),
                lastUpdated = o.optLong("lastUpdated", System.currentTimeMillis())
            ))
            progCount++
        }
        for (i in 0 until notes.length()) {
            val o = notes.getJSONObject(i)
            dao.insertNote(StudyNoteEntity(
                title = o.optString("title"),
                bookName = o.optString("bookName"),
                content = o.optString("content"),
                createdAt = o.optLong("createdAt", System.currentTimeMillis())
            ))
            noteCount++
        }
        for (i in 0 until leitner.length()) {
            val o = leitner.getJSONObject(i)
            dao.upsertLeitnerCard(LeitnerCardEntity(
                bookId = o.optString("bookId"),
                poodmanIndex = o.optInt("poodmanIndex"),
                bookTitle = o.optString("bookTitle"),
                poodmanTitle = o.optString("poodmanTitle"),
                boxLevel = o.optInt("boxLevel", 1),
                nextReviewAt = o.optLong("nextReviewAt"),
                lastReviewedAt = o.optLong("lastReviewedAt", 0),
                reviewCount = o.optInt("reviewCount", 0),
                isMastered = o.optBoolean("isMastered")
            ))
            leitCount++
        }
        for (i in 0 until wrongs.length()) {
            val o = wrongs.getJSONObject(i)
            dao.upsertWrongAnswer(WrongAnswerEntity(
                questionId = o.optInt("questionId"),
                timesWrong = o.optInt("timesWrong", 1),
                lastWrongAt = o.optLong("lastWrongAt", System.currentTimeMillis()),
                isMastered = o.optBoolean("isMastered")
            ))
            wrongCount++
        }
        for (i in 0 until scores.length()) {
            val o = scores.getJSONObject(i)
            dao.insertWeeklyScore(WeeklyScoreEntity(
                weekNumber = o.optInt("weekNumber"),
                scorePercent = o.optInt("scorePercent"),
                examTitle = o.optString("examTitle", "آزمون هفتگی"),
                recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
            ))
            scoreCount++
        }

        return ImportResult(logCount, progCount, noteCount, leitCount, wrongCount, scoreCount)
    }

    data class ImportResult(
        val logs: Int,
        val progress: Int,
        val notes: Int,
        val leitner: Int,
        val wrongAnswers: Int,
        val scores: Int
    ) {
        val total = logs + progress + notes + leitner + wrongAnswers + scores
    }

    /** Write JSON string to a user-picked URI. */
    fun writeJsonToUri(context: Context, uri: Uri, json: String) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(json.toByteArray(Charsets.UTF_8))
        }
    }

    /** Read JSON string from a user-picked URI. */
    fun readJsonFromUri(context: Context, uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: ""
    }
}
