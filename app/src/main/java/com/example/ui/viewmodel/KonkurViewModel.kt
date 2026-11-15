package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.curriculum.ComputerCurriculumData
import com.example.data.database.AppDatabase
import com.example.data.model.CheckInEntity
import com.example.data.model.DailySlotStatusEntity
import com.example.data.model.GradeLevel
import com.example.data.model.LeitnerCardEntity
import com.example.data.model.WrongAnswerEntity
import com.example.data.model.WeeklyScoreEntity
import com.example.data.model.PoodmanProgressEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.StudyNoteEntity
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TimerUiState(
    val isRunning: Boolean = false,
    val initialSeconds: Int = 45 * 60,
    val remainingSeconds: Int = 45 * 60,
    val sessionLabel: String = "مطالعه پودمان تخصصی",
    val isFinished: Boolean = false,
    val vibrateAlert: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class KonkurViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudyRepository(db.studyDao())
    }

    // Navigation Tab
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    // Selected Grade in Books Explorer
    private val _selectedGrade = MutableStateFlow(GradeLevel.GRADE_10)
    val selectedGrade: StateFlow<GradeLevel> = _selectedGrade.asStateFlow()

    fun selectGrade(grade: GradeLevel) {
        _selectedGrade.value = grade
    }

    // Selected Day in Schedule Plan
    private val _selectedDayIndex = MutableStateFlow(0)
    val selectedDayIndex: StateFlow<Int> = _selectedDayIndex.asStateFlow()

    fun selectDay(index: Int) {
        _selectedDayIndex.value = index
    }

    // Current Date Key (e.g. "2026-11-12")
    private val currentDateKey: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }

    // Database Flows
    val allLogs: StateFlow<List<StudyLogEntity>> = repository.allStudyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMinutes: StateFlow<Int?> = repository.totalMinutesStudied
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allProgress: StateFlow<List<PoodmanProgressEntity>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<StudyNoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDaySlotStatuses: StateFlow<List<DailySlotStatusEntity>> = repository
        .getSlotStatusesForDate(currentDateKey)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCheckIns: StateFlow<List<CheckInEntity>> = repository.allCheckIns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leitner Spaced-Repetition
    private val _nowTick = MutableStateFlow(System.currentTimeMillis())
    val nowTick: StateFlow<Long> = _nowTick.asStateFlow()

    fun refreshNow() { _nowTick.value = System.currentTimeMillis() }

    val dueLeitnerCards: StateFlow<List<LeitnerCardEntity>> = _nowTick
        .flatMapLatest { now -> repository.getDueLeitnerCards(now + 24L * 60 * 60 * 1000) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLeitnerCards: StateFlow<List<LeitnerCardEntity>> = repository.allLeitnerCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Intervals in days for box levels 1..5; box 6+ = mastered. */
    private val leitnerIntervalsDays = intArrayOf(1, 3, 7, 14, 30)
    private val DAY_MS = 24L * 60 * 60 * 1000

    /**
     * Called when user marks a poodman as "studied" for the first time.
     * Enqueues it into the Leitner box at level 1 (review after 1 day).
     */
    private suspend fun enqueueLeitnerIfNeeded(bookId: String, poodmanIndex: Int) {
        val existing = repository.getLeitnerCard(bookId, poodmanIndex)
        if (existing != null) return
        val book = ComputerCurriculumData.books.find { it.id == bookId } ?: return
        val poodman = book.poodmans.getOrNull(poodmanIndex - 1) ?: return
        repository.upsertLeitnerCard(
            LeitnerCardEntity(
                bookId = bookId,
                poodmanIndex = poodmanIndex,
                bookTitle = book.title,
                poodmanTitle = poodman.title,
                boxLevel = 1,
                nextReviewAt = System.currentTimeMillis() + leitnerIntervalsDays[0] * DAY_MS,
                reviewCount = 0,
                isMastered = false
            )
        )
    }

    /**
     * Review result: correct = advance box level; incorrect = reset to box 1.
     */
    fun reviewLeitnerCard(card: LeitnerCardEntity, remembered: Boolean) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (!remembered) {
                repository.upsertLeitnerCard(
                    card.copy(
                        boxLevel = 1,
                        nextReviewAt = now + leitnerIntervalsDays[0] * DAY_MS,
                        lastReviewedAt = now,
                        reviewCount = card.reviewCount + 1,
                        isMastered = false
                    )
                )
            } else {
                val nextBox = card.boxLevel + 1
                if (nextBox > leitnerIntervalsDays.size) {
                    repository.upsertLeitnerCard(
                        card.copy(
                            boxLevel = leitnerIntervalsDays.size + 1,
                            nextReviewAt = Long.MAX_VALUE,
                            lastReviewedAt = now,
                            reviewCount = card.reviewCount + 1,
                            isMastered = true
                        )
                    )
                } else {
                    repository.upsertLeitnerCard(
                        card.copy(
                            boxLevel = nextBox,
                            nextReviewAt = now + leitnerIntervalsDays[nextBox - 1] * DAY_MS,
                            lastReviewedAt = now,
                            reviewCount = card.reviewCount + 1,
                            isMastered = false
                        )
                    )
                }
            }
            refreshNow()
        }
    }

    fun resetLeitnerCard(card: LeitnerCardEntity) {
        viewModelScope.launch {
            repository.deleteLeitnerCard(card.bookId, card.poodmanIndex)
            refreshNow()
        }
    }

    // Wrong Answers Notebook
    val allWrongAnswers: StateFlow<List<WrongAnswerEntity>> = repository.allWrongAnswers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordWrongAnswer(questionId: Int) {
        viewModelScope.launch { repository.recordWrongAnswer(questionId) }
    }

    fun removeWrongAnswer(questionId: Int) {
        viewModelScope.launch { repository.removeWrongAnswer(questionId) }
    }

    fun clearWrongAnswers() {
        viewModelScope.launch { repository.clearWrongAnswers() }
    }

    // Weekly Exam Scores (Progress Trend)
    val allWeeklyScores: StateFlow<List<WeeklyScoreEntity>> = repository.allWeeklyScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addWeeklyScore(weekNumber: Int, scorePercent: Int, examTitle: String) {
        viewModelScope.launch { repository.addWeeklyScore(weekNumber, scorePercent, examTitle) }
    }

    fun deleteWeeklyScore(id: Long) {
        viewModelScope.launch { repository.deleteWeeklyScore(id) }
    }

    // Backup / restore
    suspend fun exportBackupJson(): String {
        val db = AppDatabase.getDatabase(getApplication())
        return com.example.data.backup.BackupManager.exportToJson(db)
    }

    suspend fun importBackupJson(json: String): com.example.data.backup.BackupManager.ImportResult {
        val db = AppDatabase.getDatabase(getApplication())
        return com.example.data.backup.BackupManager.importFromJson(db, json)
    }

    // Streak — consecutive days with at least one study log
    val studyStreak: StateFlow<Int> = allLogs.map { logs ->
        if (logs.isEmpty()) 0 else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val days = logs.map { sdf.format(Date(it.timestamp)) }.toSet()
            var streak = 0
            val cal = java.util.Calendar.getInstance()
            while (true) {
                val key = sdf.format(cal.time)
                if (days.contains(key)) {
                    streak++
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                } else {
                    // Allow today to be missing but keep streak from yesterday
                    if (streak == 0) {
                        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        val yesterdayKey = sdf.format(cal.time)
                        if (days.contains(yesterdayKey)) {
                            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                            continue
                        }
                    }
                    break
                }
            }
            streak
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Timer State
    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    fun setTimerPreset(minutes: Int, label: String) {
        timerJob?.cancel()
        val totalSec = minutes * 60
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            initialSeconds = totalSec,
            remainingSeconds = totalSec,
            sessionLabel = label,
            isFinished = false
        )
    }

    fun startOrResumeTimer() {
        if (_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isRunning = true, isFinished = false)

        timerJob = viewModelScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000L)
                val newRemaining = _timerState.value.remainingSeconds - 1
                if (newRemaining <= 0) {
                    _timerState.value = _timerState.value.copy(
                        isRunning = false,
                        remainingSeconds = 0,
                        isFinished = true
                    )
                    triggerVibration()
                    triggerCompletionNotification()
                    // Auto log completed minutes
                    val minutesCompleted = _timerState.value.initialSeconds / 60
                    if (minutesCompleted > 0) {
                        repository.logStudyTime(
                            minutes = minutesCompleted,
                            subjectTitle = _timerState.value.sessionLabel,
                            note = "ثبت خودکار با اتمام تایمر تمرکز ۲ ساعته"
                        )
                    }
                    break
                } else {
                    _timerState.value = _timerState.value.copy(remainingSeconds = newRemaining)
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(
            isRunning = false,
            remainingSeconds = _timerState.value.initialSeconds,
            isFinished = false
        )
    }

    fun toggleVibrateAlert() {
        _timerState.value = _timerState.value.copy(vibrateAlert = !_timerState.value.vibrateAlert)
    }

    private fun triggerVibration() {
        if (!_timerState.value.vibrateAlert) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(500)
                }
            }
        } catch (_: Exception) {}
    }

    private fun triggerCompletionNotification() {
        try {
            val context = getApplication<Application>()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channelId = "timer_complete"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "اتمام تایمر", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("تایمر به پایان رسید ✅")
                .setContentText("${_timerState.value.sessionLabel} در گزارش ثبت شد")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(1001, notification)
        } catch (_: Exception) {}
    }

    // Toggle Slot Checklist
    fun toggleSlot(slotId: String, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.setSlotStatus(currentDateKey, slotId, !currentCompleted)
        }
    }

    // Toggle Poodman Progress
    fun togglePoodmanStatus(
        bookId: String,
        poodmanIndex: Int,
        studied: Boolean? = null,
        reviewed: Boolean? = null,
        tested: Boolean? = null,
        hasVideo: Boolean? = null,
        hasPdf: Boolean? = null,
        hasTests: Boolean? = null,
        videoUrl: String? = null,
        pdfPath: String? = null,
        testUrl: String? = null
    ) {
        viewModelScope.launch {
            val existing = allProgress.value.find { it.bookId == bookId && it.poodmanIndex == poodmanIndex }
            val updated = PoodmanProgressEntity(
                bookId = bookId,
                poodmanIndex = poodmanIndex,
                isStudied = studied ?: existing?.isStudied ?: false,
                isReviewed = reviewed ?: existing?.isReviewed ?: false,
                isTestPracticed = tested ?: existing?.isTestPracticed ?: false,
                hasVideo = hasVideo ?: existing?.hasVideo ?: false,
                hasPdf = hasPdf ?: existing?.hasPdf ?: false,
                hasTests = hasTests ?: existing?.hasTests ?: false,
                videoUrl = videoUrl ?: existing?.videoUrl ?: "",
                pdfPath = pdfPath ?: existing?.pdfPath ?: "",
                testUrl = testUrl ?: existing?.testUrl ?: "",
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePoodmanProgress(updated)
            // Auto-enqueue into Leitner box when a poodman is marked as studied
            if (updated.isStudied) {
                enqueueLeitnerIfNeeded(bookId, poodmanIndex)
            }
        }
    }

    // Manual Log
    fun logStudyTime(minutes: Int, subject: String, note: String) {
        viewModelScope.launch {
            repository.logStudyTime(minutes, subject, note)
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteStudyLog(id)
        }
    }

    // Notes
    fun addNote(title: String, bookName: String, content: String) {
        viewModelScope.launch {
            repository.addNote(title, bookName, content)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Weekly Check-ins (یکشنبه / پنجشنبه)
    fun addCheckIn(dayType: String, completedText: String, missingText: String) {
        viewModelScope.launch {
            repository.addCheckIn(dayType, completedText, missingText)
        }
    }

    fun deleteCheckIn(id: Long) {
        viewModelScope.launch {
            repository.deleteCheckIn(id)
        }
    }
}
