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
        hasTests: Boolean? = null
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
                lastUpdated = System.currentTimeMillis()
            )
            repository.savePoodmanProgress(updated)
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
