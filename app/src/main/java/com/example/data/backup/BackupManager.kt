package com.example.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
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
import java.io.IOException

/**
 * Versioned JSON backup / restore utility for all eight Room tables.
 *
 * Version 1 backups did not include daily_slot_status or weekly_checkins.
 * When importing a version 1 file those two newer tables are intentionally
 * left untouched, because their data was never present in the old backup.
 */
object BackupManager {

    const val BACKUP_VERSION = 2

    /**
     * Serialize all database content into one portable JSON document.
     * Stable primary keys are included so a restore preserves record identity.
     */
    suspend fun exportToJson(db: AppDatabase): String {
        val dao = db.studyDao()
        val root = JSONObject()
            .put("version", BACKUP_VERSION)
            .put("exportedAt", System.currentTimeMillis())

        root.put("study_logs", JSONArray().apply {
            dao.getAllStudyLogsOnce().forEach { log ->
                put(JSONObject()
                    .put("id", log.id)
                    .put("timestamp", log.timestamp)
                    .put("durationMinutes", log.durationMinutes)
                    .put("subjectTitle", log.subjectTitle)
                    .put("note", log.note))
            }
        })

        root.put("poodman_progress", JSONArray().apply {
            dao.getAllProgressOnce().forEach { progress ->
                put(JSONObject()
                    .put("bookId", progress.bookId)
                    .put("poodmanIndex", progress.poodmanIndex)
                    .put("isStudied", progress.isStudied)
                    .put("isReviewed", progress.isReviewed)
                    .put("isTestPracticed", progress.isTestPracticed)
                    .put("hasVideo", progress.hasVideo)
                    .put("hasPdf", progress.hasPdf)
                    .put("hasTests", progress.hasTests)
                    .put("videoUrl", progress.videoUrl)
                    .put("pdfPath", progress.pdfPath)
                    .put("testUrl", progress.testUrl)
                    .put("lastUpdated", progress.lastUpdated))
            }
        })

        root.put("study_notes", JSONArray().apply {
            dao.getAllNotesOnce().forEach { note ->
                put(JSONObject()
                    .put("id", note.id)
                    .put("title", note.title)
                    .put("bookName", note.bookName)
                    .put("content", note.content)
                    .put("createdAt", note.createdAt))
            }
        })

        root.put("daily_slot_status", JSONArray().apply {
            dao.getAllDailySlotStatusesOnce().forEach { status ->
                put(JSONObject()
                    .put("dateKey", status.dateKey)
                    .put("slotId", status.slotId)
                    .put("isCompleted", status.isCompleted))
            }
        })

        root.put("weekly_checkins", JSONArray().apply {
            dao.getAllCheckInsOnce().forEach { checkIn ->
                put(JSONObject()
                    .put("id", checkIn.id)
                    .put("dayType", checkIn.dayType)
                    .put("completedText", checkIn.completedText)
                    .put("missingText", checkIn.missingText)
                    .put("createdAt", checkIn.createdAt))
            }
        })

        root.put("leitner_cards", JSONArray().apply {
            dao.getAllLeitnerCardsOnce().forEach { card ->
                put(JSONObject()
                    .put("bookId", card.bookId)
                    .put("poodmanIndex", card.poodmanIndex)
                    .put("bookTitle", card.bookTitle)
                    .put("poodmanTitle", card.poodmanTitle)
                    .put("boxLevel", card.boxLevel)
                    .put("nextReviewAt", card.nextReviewAt)
                    .put("lastReviewedAt", card.lastReviewedAt)
                    .put("reviewCount", card.reviewCount)
                    .put("isMastered", card.isMastered))
            }
        })

        root.put("wrong_answers", JSONArray().apply {
            dao.getAllWrongAnswersOnce().forEach { wrong ->
                put(JSONObject()
                    .put("questionId", wrong.questionId)
                    .put("timesWrong", wrong.timesWrong)
                    .put("lastWrongAt", wrong.lastWrongAt)
                    .put("isMastered", wrong.isMastered))
            }
        })

        root.put("weekly_scores", JSONArray().apply {
            dao.getAllWeeklyScoresOnce().forEach { score ->
                put(JSONObject()
                    .put("id", score.id)
                    .put("weekNumber", score.weekNumber)
                    .put("scorePercent", score.scorePercent)
                    .put("examTitle", score.examTitle)
                    .put("recordedAt", score.recordedAt))
            }
        })

        return root.toString(2)
    }

    /**
     * Validate and decode the entire backup before changing the database.
     * The replacement itself runs in one Room transaction, so a failed write
     * rolls back instead of leaving a partially restored database.
     */
    suspend fun importFromJson(db: AppDatabase, json: String): ImportResult {
        val root = try {
            JSONObject(json)
        } catch (error: Exception) {
            throw IllegalArgumentException("فایل پشتیبان معتبر نیست.", error)
        }

        val version = root.optInt("version", -1)
        require(version in 1..BACKUP_VERSION) {
            "نسخه فایل پشتیبان پشتیبانی نمی‌شود: $version"
        }

        fun requiredArray(key: String): JSONArray =
            root.optJSONArray(key)
                ?: throw IllegalArgumentException("بخش $key در فایل پشتیبان وجود ندارد یا معتبر نیست.")

        val logs = requiredArray("study_logs").let { array ->
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                StudyLogEntity(
                    id = item.optLong("id", 0L),
                    timestamp = item.optLong("timestamp", System.currentTimeMillis()),
                    durationMinutes = item.optInt("durationMinutes", 0),
                    subjectTitle = item.optString("subjectTitle", ""),
                    note = item.optString("note", "")
                )
            }
        }

        val progress = requiredArray("poodman_progress").let { array ->
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                PoodmanProgressEntity(
                    bookId = item.optString("bookId", ""),
                    poodmanIndex = item.optInt("poodmanIndex", 0),
                    isStudied = item.optBoolean("isStudied"),
                    isReviewed = item.optBoolean("isReviewed"),
                    isTestPracticed = item.optBoolean("isTestPracticed"),
                    hasVideo = item.optBoolean("hasVideo"),
                    hasPdf = item.optBoolean("hasPdf"),
                    hasTests = item.optBoolean("hasTests"),
                    videoUrl = item.optString("videoUrl", ""),
                    pdfPath = item.optString("pdfPath", ""),
                    testUrl = item.optString("testUrl", ""),
                    lastUpdated = item.optLong("lastUpdated", System.currentTimeMillis())
                )
            }
        }

        val notes = requiredArray("study_notes").let { array ->
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                StudyNoteEntity(
                    id = item.optLong("id", 0L),
                    title = item.optString("title", ""),
                    bookName = item.optString("bookName", ""),
                    content = item.optString("content", ""),
                    createdAt = item.optLong("createdAt", System.currentTimeMillis())
                )
            }
        }

        val dailySlots = if (version >= 2) {
            requiredArray("daily_slot_status").let { array ->
                (0 until array.length()).map { index ->
                    val item = array.getJSONObject(index)
                    DailySlotStatusEntity(
                        dateKey = item.optString("dateKey", ""),
                        slotId = item.optString("slotId", ""),
                        isCompleted = item.optBoolean("isCompleted")
                    )
                }
            }
        } else {
            null
        }

        val checkIns = if (version >= 2) {
            requiredArray("weekly_checkins").let { array ->
                (0 until array.length()).map { index ->
                    val item = array.getJSONObject(index)
                    CheckInEntity(
                        id = item.optLong("id", 0L),
                        dayType = item.optString("dayType", ""),
                        completedText = item.optString("completedText", ""),
                        missingText = item.optString("missingText", ""),
                        createdAt = item.optLong("createdAt", System.currentTimeMillis())
                    )
                }
            }
        } else {
            null
        }

        val leitner = requiredArray("leitner_cards").let { array ->
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                LeitnerCardEntity(
                    bookId = item.optString("bookId", ""),
                    poodmanIndex = item.optInt("poodmanIndex", 0),
                    bookTitle = item.optString("bookTitle", ""),
                    poodmanTitle = item.optString("poodmanTitle", ""),
                    boxLevel = item.optInt("boxLevel", 1),
                    nextReviewAt = item.optLong("nextReviewAt", 0L),
                    lastReviewedAt = item.optLong("lastReviewedAt", 0L),
                    reviewCount = item.optInt("reviewCount", 0),
                    isMastered = item.optBoolean("isMastered")
                )
            }
        }

        val wrongAnswers = requiredArray("wrong_answers").let { array ->
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                WrongAnswerEntity(
                    questionId = item.optInt("questionId", 0),
                    timesWrong = item.optInt("timesWrong", 1),
                    lastWrongAt = item.optLong("lastWrongAt", System.currentTimeMillis()),
                    isMastered = item.optBoolean("isMastered")
                )
            }
        }

        val scores = requiredArray("weekly_scores").let { array ->
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                WeeklyScoreEntity(
                    id = item.optLong("id", 0L),
                    weekNumber = item.optInt("weekNumber", 0),
                    scorePercent = item.optInt("scorePercent", 0),
                    examTitle = item.optString("examTitle", "آزمون هفتگی"),
                    recordedAt = item.optLong("recordedAt", System.currentTimeMillis())
                )
            }
        }

        val dao = db.studyDao()
        db.withTransaction {
            dao.clearAllStudyLogs()
            dao.clearAllProgress()
            dao.clearAllNotes()
            dao.clearAllLeitnerCards()
            dao.clearWrongAnswers()
            dao.clearAllWeeklyScores()

            // Version 1 backups never contained these tables. Preserve their
            // current contents when restoring an older file.
            if (dailySlots != null && checkIns != null) {
                dao.clearAllDailySlotStatuses()
                dao.clearAllCheckIns()
            }

            logs.forEach { dao.insertStudyLog(it) }
            progress.forEach { dao.savePoodmanProgress(it) }
            notes.forEach { dao.insertNote(it) }
            dailySlots?.forEach { dao.setSlotStatus(it) }
            checkIns?.forEach { dao.insertCheckIn(it) }
            leitner.forEach { dao.upsertLeitnerCard(it) }
            wrongAnswers.forEach { dao.upsertWrongAnswer(it) }
            scores.forEach { dao.insertWeeklyScore(it) }
        }

        return ImportResult(
            logs = logs.size,
            progress = progress.size,
            notes = notes.size,
            leitner = leitner.size,
            wrongAnswers = wrongAnswers.size,
            scores = scores.size,
            dailySlots = dailySlots?.size ?: 0,
            checkIns = checkIns?.size ?: 0
        )
    }

    data class ImportResult(
        val logs: Int,
        val progress: Int,
        val notes: Int,
        val leitner: Int,
        val wrongAnswers: Int,
        val scores: Int,
        val dailySlots: Int = 0,
        val checkIns: Int = 0
    ) {
        val total: Int
            get() = logs + progress + notes + leitner + wrongAnswers + scores + dailySlots + checkIns
    }

    /** Write JSON string to a user-picked URI. */
    fun writeJsonToUri(context: Context, uri: Uri, json: String) {
        val output = context.contentResolver.openOutputStream(uri)
            ?: throw IOException("امکان ایجاد فایل پشتیبان وجود ندارد.")
        output.use { it.write(json.toByteArray(Charsets.UTF_8)) }
    }

    /** Read JSON string from a user-picked URI. */
    fun readJsonFromUri(context: Context, uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("امکان خواندن فایل پشتیبان وجود ندارد.")
        return input.use { it.readBytes().toString(Charsets.UTF_8) }
    }
}
