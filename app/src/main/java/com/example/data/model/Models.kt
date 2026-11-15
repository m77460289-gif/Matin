package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Log of a completed study session in minutes
 */
@Entity(tableName = "study_logs")
data class StudyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val subjectTitle: String,
    val note: String = ""
)

/**
 * Progress status for each poodman (1 to 5) of each vocational book
 */
@Entity(tableName = "poodman_progress", primaryKeys = ["bookId", "poodmanIndex"])
data class PoodmanProgressEntity(
    val bookId: String,
    val poodmanIndex: Int,
    val isStudied: Boolean = false,
    val isReviewed: Boolean = false,
    val isTestPracticed: Boolean = false,
    val hasVideo: Boolean = false,
    val hasPdf: Boolean = false,
    val hasTests: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * User study notes, formula cheat sheets, or key tips
 */
@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val bookName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Daily schedule checkpoint completion status for a specific date (YYYY-MM-DD)
 */
@Entity(tableName = "daily_slot_status", primaryKeys = ["dateKey", "slotId"])
data class DailySlotStatusEntity(
    val dateKey: String,
    val slotId: String,
    val isCompleted: Boolean = false
)

/**
 * Leitner spaced-repetition card — auto-enqueued when a poodman is marked as studied.
 * Box levels 1..5 correspond to review intervals: 1, 3, 7, 14, 30 days.
 * When boxLevel reaches 6, the card is considered mastered.
 */
@Entity(tableName = "leitner_cards", primaryKeys = ["bookId", "poodmanIndex"])
data class LeitnerCardEntity(
    val bookId: String,
    val poodmanIndex: Int,
    val bookTitle: String,
    val poodmanTitle: String,
    val boxLevel: Int = 1,
    val nextReviewAt: Long,
    val lastReviewedAt: Long = 0L,
    val reviewCount: Int = 0,
    val isMastered: Boolean = false
)

/**
 * Weekly check-in report (Sunday / Thursday) from the students' progress
 */
@Entity(tableName = "weekly_checkins")
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val dayType: String, // یکشنبه یا پنجشنبه
    val completedText: String,
    val missingText: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Wrong-answer notebook entry. Auto-saved whenever the user answers a quiz question incorrectly.
 * The `questionId` refers to `QuizQuestion.id` in `ComputerCurriculumData.sampleQuestions`.
 */
@Entity(tableName = "wrong_answers")
data class WrongAnswerEntity(
    @PrimaryKey val questionId: Int,
    val timesWrong: Int = 1,
    val lastWrongAt: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false
)

/**
 * Weekly exam score entry. One record per week (weekNumber 1..30), tracking percent progress.
 */
@Entity(tableName = "weekly_scores")
data class WeeklyScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val weekNumber: Int,
    val scorePercent: Int,
    val examTitle: String = "آزمون هفتگی",
    val recordedAt: Long = System.currentTimeMillis()
)

// Domain Data Models
enum class GradeLevel(val titleFa: String) {
    GRADE_10("پایه دهم"),
    GRADE_11("پایه یازدهم"),
    GRADE_12("پایه دوازدهم"),
    GENERAL("دروس پایه و عمومی")
}

data class PoodmanInfo(
    val index: Int,
    val title: String,
    val topics: List<String>,
    val examImportance: String, // مثلاً "بسیار مهم (۳ الی ۴ تست)"
    val goldenTip: String
)

data class BookInfo(
    val id: String,
    val title: String,
    val bookCode: String,
    val grade: GradeLevel,
    val coefficient: Int, // ضریب کنکور
    val description: String,
    val bookChangesSummary: String, // تحلیل تغییرات کتب جدید
    val officialEditionsGuideline: String, // کتاب کدام سال تحصیلی ملاک است
    val poodmans: List<PoodmanInfo>
)

data class DailySlot(
    val id: String,
    val timeLabel: String,
    val durationMinutes: Int,
    val title: String,
    val subTitle: String,
    val category: String, // تخصصی، پایه، مرور و تست
    val iconName: String
)

data class DaySchedulePlan(
    val dayOfWeek: String, // شنبه، یکشنبه، ...
    val focusSubject: String,
    val targetGrade: String,
    val slots: List<DailySlot>,
    val dailyAdvice: String
)

data class QuizQuestion(
    val id: Int,
    val subjectTitle: String,
    val bookTitle: String,
    val poodmanIndex: Int,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val goldenRule: String
)
