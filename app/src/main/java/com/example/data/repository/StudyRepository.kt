package com.example.data.repository

import com.example.data.database.StudyDao
import com.example.data.model.CheckInEntity
import com.example.data.model.DailySlotStatusEntity
import com.example.data.model.LeitnerCardEntity
import com.example.data.model.WrongAnswerEntity
import com.example.data.model.WeeklyScoreEntity
import com.example.data.model.PoodmanProgressEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.StudyNoteEntity
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val dao: StudyDao) {

    val allStudyLogs: Flow<List<StudyLogEntity>> = dao.getAllStudyLogs()
    val totalMinutesStudied: Flow<Int?> = dao.getTotalMinutesStudied()
    val allProgress: Flow<List<PoodmanProgressEntity>> = dao.getAllProgress()
    val allNotes: Flow<List<StudyNoteEntity>> = dao.getAllNotes()

    suspend fun logStudyTime(
        minutes: Int,
        subjectTitle: String,
        note: String = "",
        id: Long = 0L
    ): Long {
        return dao.insertStudyLog(
            StudyLogEntity(
                id = id,
                durationMinutes = minutes,
                subjectTitle = subjectTitle,
                note = note
            )
        )
    }

    suspend fun deleteStudyLog(id: Long) {
        dao.deleteStudyLog(id)
    }

    suspend fun savePoodmanProgress(progress: PoodmanProgressEntity) {
        dao.savePoodmanProgress(progress)
    }

    fun getSlotStatusesForDate(dateKey: String): Flow<List<DailySlotStatusEntity>> {
        return dao.getSlotStatusesForDate(dateKey)
    }

    suspend fun setSlotStatus(dateKey: String, slotId: String, completed: Boolean) {
        dao.setSlotStatus(DailySlotStatusEntity(dateKey = dateKey, slotId = slotId, isCompleted = completed))
    }

    suspend fun addNote(title: String, bookName: String, content: String): Long {
        return dao.insertNote(
            StudyNoteEntity(
                title = title,
                bookName = bookName,
                content = content
            )
        )
    }

    suspend fun deleteNote(id: Long) {
        dao.deleteNote(id)
    }

    val allLeitnerCards: Flow<List<LeitnerCardEntity>> = dao.getAllLeitnerCards()

    fun getDueLeitnerCards(upToTimestamp: Long): Flow<List<LeitnerCardEntity>> =
        dao.getDueLeitnerCards(upToTimestamp)

    suspend fun getLeitnerCard(bookId: String, poodmanIndex: Int): LeitnerCardEntity? =
        dao.getLeitnerCard(bookId, poodmanIndex)

    suspend fun upsertLeitnerCard(card: LeitnerCardEntity) = dao.upsertLeitnerCard(card)

    suspend fun deleteLeitnerCard(bookId: String, poodmanIndex: Int) =
        dao.deleteLeitnerCard(bookId, poodmanIndex)

    val allWrongAnswers: Flow<List<WrongAnswerEntity>> = dao.getAllWrongAnswers()

    suspend fun recordWrongAnswer(questionId: Int) {
        val existing = dao.getWrongAnswer(questionId)
        if (existing != null) {
            dao.upsertWrongAnswer(existing.copy(timesWrong = existing.timesWrong + 1, lastWrongAt = System.currentTimeMillis(), isMastered = false))
        } else {
            dao.upsertWrongAnswer(WrongAnswerEntity(questionId = questionId))
        }
    }

    suspend fun removeWrongAnswer(questionId: Int) = dao.deleteWrongAnswer(questionId)

    suspend fun clearWrongAnswers() = dao.clearWrongAnswers()

    val allWeeklyScores: Flow<List<WeeklyScoreEntity>> = dao.getAllWeeklyScores()

    suspend fun addWeeklyScore(weekNumber: Int, scorePercent: Int, examTitle: String): Long =
        dao.insertWeeklyScore(WeeklyScoreEntity(weekNumber = weekNumber, scorePercent = scorePercent, examTitle = examTitle))

    suspend fun deleteWeeklyScore(id: Long) = dao.deleteWeeklyScore(id)

    val allCheckIns: Flow<List<CheckInEntity>> = dao.getAllCheckIns()

    suspend fun addCheckIn(dayType: String, completedText: String, missingText: String): Long {
        return dao.insertCheckIn(CheckInEntity(dayType = dayType, completedText = completedText, missingText = missingText))
    }

    suspend fun deleteCheckIn(id: Long) {
        dao.deleteCheckIn(id)
    }
}
