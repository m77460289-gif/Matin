package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CheckInEntity
import com.example.data.model.DailySlotStatusEntity
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
}
