package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CheckInEntity
import com.example.data.model.DailySlotStatusEntity
import com.example.data.model.LeitnerCardEntity
import com.example.data.model.WrongAnswerEntity
import com.example.data.model.WeeklyScoreEntity
import com.example.data.model.PoodmanProgressEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.StudyNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // Study Logs
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    fun getAllStudyLogs(): Flow<List<StudyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyLog(log: StudyLogEntity): Long

    @Query("DELETE FROM study_logs WHERE id = :id")
    suspend fun deleteStudyLog(id: Long)

    @Query("SELECT SUM(durationMinutes) FROM study_logs")
    fun getTotalMinutesStudied(): Flow<Int?>

    // Poodman Progress
    @Query("SELECT * FROM poodman_progress")
    fun getAllProgress(): Flow<List<PoodmanProgressEntity>>

    @Query("SELECT * FROM poodman_progress WHERE bookId = :bookId")
    fun getProgressForBook(bookId: String): Flow<List<PoodmanProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePoodmanProgress(progress: PoodmanProgressEntity)

    // Daily Checklist Slot Status
    @Query("SELECT * FROM daily_slot_status WHERE dateKey = :dateKey")
    fun getSlotStatusesForDate(dateKey: String): Flow<List<DailySlotStatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSlotStatus(status: DailySlotStatusEntity)

    // Study Notes
    @Query("SELECT * FROM study_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    // Weekly Check-ins
    @Query("SELECT * FROM weekly_checkins ORDER BY createdAt DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("DELETE FROM weekly_checkins WHERE id = :id")
    suspend fun deleteCheckIn(id: Long)

    // Leitner Spaced-Repetition
    @Query("SELECT * FROM leitner_cards ORDER BY nextReviewAt ASC")
    fun getAllLeitnerCards(): Flow<List<LeitnerCardEntity>>

    @Query("SELECT * FROM leitner_cards WHERE isMastered = 0 AND nextReviewAt <= :upToTimestamp ORDER BY nextReviewAt ASC")
    fun getDueLeitnerCards(upToTimestamp: Long): Flow<List<LeitnerCardEntity>>

    @Query("SELECT * FROM leitner_cards WHERE bookId = :bookId AND poodmanIndex = :poodmanIndex LIMIT 1")
    suspend fun getLeitnerCard(bookId: String, poodmanIndex: Int): LeitnerCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLeitnerCard(card: LeitnerCardEntity)

    @Query("DELETE FROM leitner_cards WHERE bookId = :bookId AND poodmanIndex = :poodmanIndex")
    suspend fun deleteLeitnerCard(bookId: String, poodmanIndex: Int)

    // Wrong Answers Notebook
    @Query("SELECT * FROM wrong_answers ORDER BY lastWrongAt DESC")
    fun getAllWrongAnswers(): Flow<List<WrongAnswerEntity>>

    @Query("SELECT * FROM wrong_answers WHERE questionId = :id LIMIT 1")
    suspend fun getWrongAnswer(id: Int): WrongAnswerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWrongAnswer(entity: WrongAnswerEntity)

    @Query("DELETE FROM wrong_answers WHERE questionId = :id")
    suspend fun deleteWrongAnswer(id: Int)

    @Query("DELETE FROM wrong_answers")
    suspend fun clearWrongAnswers()

    // Weekly Exam Scores
    @Query("SELECT * FROM weekly_scores ORDER BY weekNumber ASC")
    fun getAllWeeklyScores(): Flow<List<WeeklyScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyScore(score: WeeklyScoreEntity): Long

    @Query("DELETE FROM weekly_scores WHERE id = :id")
    suspend fun deleteWeeklyScore(id: Long)

    // Backup/restore — one-shot getters (suspend)
    @Query("SELECT * FROM study_logs ORDER BY timestamp DESC")
    suspend fun getAllStudyLogsOnce(): List<StudyLogEntity>

    @Query("SELECT * FROM poodman_progress")
    suspend fun getAllProgressOnce(): List<PoodmanProgressEntity>

    @Query("SELECT * FROM study_notes ORDER BY createdAt DESC")
    suspend fun getAllNotesOnce(): List<StudyNoteEntity>

    @Query("SELECT * FROM leitner_cards")
    suspend fun getAllLeitnerCardsOnce(): List<LeitnerCardEntity>

    @Query("SELECT * FROM wrong_answers")
    suspend fun getAllWrongAnswersOnce(): List<WrongAnswerEntity>

    @Query("SELECT * FROM weekly_scores")
    suspend fun getAllWeeklyScoresOnce(): List<WeeklyScoreEntity>

    @Query("SELECT * FROM daily_slot_status ORDER BY dateKey ASC, slotId ASC")
    suspend fun getAllDailySlotStatusesOnce(): List<DailySlotStatusEntity>

    @Query("SELECT * FROM weekly_checkins ORDER BY createdAt DESC")
    suspend fun getAllCheckInsOnce(): List<CheckInEntity>

    // Backup/restore — clear helpers
    @Query("DELETE FROM study_logs")
    suspend fun clearAllStudyLogs()

    @Query("DELETE FROM poodman_progress")
    suspend fun clearAllProgress()

    @Query("DELETE FROM study_notes")
    suspend fun clearAllNotes()

    @Query("DELETE FROM leitner_cards")
    suspend fun clearAllLeitnerCards()

    @Query("DELETE FROM weekly_scores")
    suspend fun clearAllWeeklyScores()

    @Query("DELETE FROM daily_slot_status")
    suspend fun clearAllDailySlotStatuses()

    @Query("DELETE FROM weekly_checkins")
    suspend fun clearAllCheckIns()
}
