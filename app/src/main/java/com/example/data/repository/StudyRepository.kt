package com.example.data.repository

import com.example.data.database.StudyDao
import com.example.data.model.DailySlotStatusEntity
import com.example.data.model.PoodmanProgressEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.StudyNoteEntity
import kotlinx.coroutines.flow.Flow

class StudyRepository(private val dao: StudyDao) {

    val allStudyLogs: Flow<List<StudyLogEntity>> = dao.getAllStudyLogs()
    val totalMinutesStudied: Flow<Int?> = dao.getTotalMinutesStudied()
    val allProgress: Flow<List<PoodmanProgressEntity>> = dao.getAllProgress()
    val allNotes: Flow<List<StudyNoteEntity>> = dao.getAllNotes()

    suspend fun logStudyTime(minutes: Int, subjectTitle: String, note: String = ""): Long {
        return dao.insertStudyLog(
            StudyLogEntity(
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
}
