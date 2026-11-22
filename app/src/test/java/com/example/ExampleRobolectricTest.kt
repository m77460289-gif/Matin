package com.example

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.backup.BackupManager
import com.example.data.database.AppDatabase
import com.example.data.repository.StudyRepository
import com.example.data.model.CheckInEntity
import com.example.data.model.DailySlotStatusEntity
import com.example.data.model.LeitnerCardEntity
import com.example.data.model.PoodmanProgressEntity
import com.example.data.model.StudyLogEntity
import com.example.data.model.StudyNoteEntity
import com.example.data.model.WeeklyScoreEntity
import com.example.data.model.WrongAnswerEntity
import com.example.data.curriculum.ComputerCurriculumData
import com.example.ui.viewmodel.KonkurViewModel
import com.example.ui.viewmodel.calculateStudyStreak
import com.example.reminder.ReminderScheduler
import com.example.reminder.isReminderRescheduleAction
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.xmlpull.v1.XmlPullParser
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("کنکور کامپیوتر", appName)
  }

  @Test
  fun `verify computer curriculum books and poodmans data`() {
    val books = ComputerCurriculumData.books
    assertTrue("Books count should be at least 7", books.size >= 7)

    books.forEach { book ->
      assertNotNull("Book title must not be empty", book.title)
      assertEquals("Vocational books should contain 5 poodmans (except combined general)", 
        if (book.id == "riazi_fani") 3 else 5, 
        book.poodmans.size
      )
    }
  }

  @Test
  fun `verify weekly 2-hour plan slots total 120 minutes`() {
    val weeklyPlan = ComputerCurriculumData.weeklyPlan
    assertEquals(7, weeklyPlan.size)

    weeklyPlan.forEach { dayPlan ->
      val totalMinutes = dayPlan.slots.sumOf { it.durationMinutes }
      assertEquals("Daily slots must sum up to exactly 120 minutes (2 hours)", 120, totalMinutes)
    }
  }

  @Test
  fun `backup round trip preserves all eight database tables`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    try {
      val dao = database.studyDao()
      dao.insertStudyLog(StudyLogEntity(id = 11L, durationMinutes = 45, subjectTitle = "شبکه", note = "جلسه اول"))
      dao.savePoodmanProgress(PoodmanProgressEntity(bookId = "network", poodmanIndex = 2, isStudied = true))
      dao.insertNote(StudyNoteEntity(id = 12L, title = "نکته", bookName = "شبکه", content = "محتوای آزمایشی"))
      dao.setSlotStatus(DailySlotStatusEntity(dateKey = "2026-11-21", slotId = "slot-1", isCompleted = true))
      dao.insertCheckIn(CheckInEntity(id = 13L, dayType = "یکشنبه", completedText = "مطالعه", missingText = "مرور"))
      dao.upsertLeitnerCard(
        LeitnerCardEntity(
          bookId = "network",
          poodmanIndex = 2,
          bookTitle = "دانش فنی",
          poodmanTitle = "پودمان دوم",
          nextReviewAt = 1_800_000_000_000L
        )
      )
      dao.upsertWrongAnswer(WrongAnswerEntity(questionId = 101, timesWrong = 2))
      dao.insertWeeklyScore(WeeklyScoreEntity(id = 14L, weekNumber = 3, scorePercent = 85))

      val backup = BackupManager.exportToJson(database)
      val exported = JSONObject(backup)
      assertEquals(BackupManager.BACKUP_VERSION, exported.getInt("version"))
      listOf(
        "study_logs",
        "poodman_progress",
        "study_notes",
        "daily_slot_status",
        "weekly_checkins",
        "leitner_cards",
        "wrong_answers",
        "weekly_scores"
      ).forEach { table -> assertTrue("Missing backup table: $table", exported.has(table)) }

      val emptyBackup = JSONObject()
        .put("version", BackupManager.BACKUP_VERSION)
        .put("study_logs", JSONArray())
        .put("poodman_progress", JSONArray())
        .put("study_notes", JSONArray())
        .put("daily_slot_status", JSONArray())
        .put("weekly_checkins", JSONArray())
        .put("leitner_cards", JSONArray())
        .put("wrong_answers", JSONArray())
        .put("weekly_scores", JSONArray())
      BackupManager.importFromJson(database, emptyBackup.toString())
      val result = BackupManager.importFromJson(database, backup)

      assertEquals(8, result.total)
      assertEquals(1, result.logs)
      assertEquals(1, result.progress)
      assertEquals(1, result.notes)
      assertEquals(1, result.dailySlots)
      assertEquals(1, result.checkIns)
      assertEquals(1, result.leitner)
      assertEquals(1, result.wrongAnswers)
      assertEquals(1, result.scores)

      assertEquals(11L, dao.getAllStudyLogsOnce().single().id)
      assertEquals("network", dao.getAllProgressOnce().single().bookId)
      assertEquals(12L, dao.getAllNotesOnce().single().id)
      assertTrue(dao.getAllDailySlotStatusesOnce().single().isCompleted)
      assertEquals(13L, dao.getAllCheckInsOnce().single().id)
      assertEquals(2, dao.getAllLeitnerCardsOnce().single().poodmanIndex)
      assertEquals(101, dao.getAllWrongAnswersOnce().single().questionId)
      assertEquals(14L, dao.getAllWeeklyScoresOnce().single().id)
    } finally {
      database.close()
    }
  }

  @Test
  fun `invalid backup does not clear existing study data`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    try {
      val dao = database.studyDao()
      dao.insertStudyLog(StudyLogEntity(durationMinutes = 30, subjectTitle = "ریاضی"))

      val invalidBackup = JSONObject()
        .put("version", BackupManager.BACKUP_VERSION)
        .put("study_logs", JSONArray().put("not-an-object"))
        .put("poodman_progress", JSONArray())
        .put("study_notes", JSONArray())
        .put("daily_slot_status", JSONArray())
        .put("weekly_checkins", JSONArray())
        .put("leitner_cards", JSONArray())
        .put("wrong_answers", JSONArray())
        .put("weekly_scores", JSONArray())

      try {
        BackupManager.importFromJson(database, invalidBackup.toString())
        fail("Malformed backup should be rejected")
      } catch (_: Exception) {
        // Expected: all entries are decoded before the transaction starts.
      }

      assertEquals(1, dao.getAllStudyLogsOnce().size)
      assertEquals(30, dao.getAllStudyLogsOnce().single().durationMinutes)
    } finally {
      database.close()
    }
  }

  @Test
  fun `version one backup keeps tables that older backups did not contain`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    try {
      val dao = database.studyDao()
      dao.setSlotStatus(DailySlotStatusEntity(dateKey = "2026-11-21", slotId = "keep-me", isCompleted = true))
      dao.insertCheckIn(CheckInEntity(dayType = "پنجشنبه", completedText = "آزمون", missingText = ""))

      val versionOneBackup = JSONObject()
        .put("version", 1)
        .put("study_logs", JSONArray())
        .put("poodman_progress", JSONArray())
        .put("study_notes", JSONArray())
        .put("leitner_cards", JSONArray())
        .put("wrong_answers", JSONArray())
        .put("weekly_scores", JSONArray())

      val result = BackupManager.importFromJson(database, versionOneBackup.toString())

      assertEquals(0, result.dailySlots)
      assertEquals(0, result.checkIns)
      assertEquals(1, dao.getAllDailySlotStatusesOnce().size)
      assertEquals(1, dao.getAllCheckInsOnce().size)
      assertTrue(dao.getAllDailySlotStatusesOnce().single().isCompleted)
    } finally {
      database.close()
    }
  }

  @Test
  fun `running focus timer resumes after viewmodel recreation`() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    application.getSharedPreferences("timer_state", Context.MODE_PRIVATE)
      .edit().clear().commit()

    val firstViewModel = KonkurViewModel(application)
    firstViewModel.setTimerPreset(2, "جلسه آزمایشی")
    firstViewModel.startOrResumeTimer()

    val recreatedViewModel = KonkurViewModel(application)
    val restored = recreatedViewModel.timerState.value

    assertTrue("A running timer should resume after recreation", restored.isRunning)
    assertEquals("جلسه آزمایشی", restored.sessionLabel)
    assertTrue("Remaining time should be restored from its deadline", restored.remainingSeconds in 1..120)

    firstViewModel.pauseTimer()
    recreatedViewModel.pauseTimer()
    application.getSharedPreferences("timer_state", Context.MODE_PRIVATE)
      .edit().clear().commit()
  }

  @Test
  fun `retrying timer completion with the same id does not duplicate study logs`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    try {
      val repository = StudyRepository(database.studyDao())
      repository.logStudyTime(2, "جلسه آزمایشی", "ثبت تایمر", id = -12345L)
      repository.logStudyTime(2, "جلسه آزمایشی", "ثبت تایمر", id = -12345L)

      val logs = database.studyDao().getAllStudyLogsOnce()
      assertEquals(1, logs.size)
      assertEquals(-12345L, logs.single().id)
    } finally {
      database.close()
    }
  }

  @Test
  fun `daily reminder is rescheduled after system clock and app changes`() {
    val rescheduleActions = listOf(
      Intent.ACTION_BOOT_COMPLETED,
      Intent.ACTION_TIME_CHANGED,
      Intent.ACTION_TIMEZONE_CHANGED,
      Intent.ACTION_MY_PACKAGE_REPLACED,
      "android.intent.action.TIMEZONE_OFFSET_CHANGED",
      "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
    )

    rescheduleActions.forEach { action ->
      assertTrue("Expected reminder reschedule for action: $action", isReminderRescheduleAction(action))
    }

    assertFalse(isReminderRescheduleAction(Intent.ACTION_AIRPLANE_MODE_CHANGED))
    assertFalse(isReminderRescheduleAction(null))
  }

  @Test
  fun `daily reminder preference defaults to enabled and respects opt out`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences(ReminderScheduler.PREFERENCES_NAME, Context.MODE_PRIVATE)
    prefs.edit().clear().commit()

    assertTrue(ReminderScheduler.isEnabled(context))

    prefs.edit().putBoolean(ReminderScheduler.ENABLED_PREFERENCE, false).commit()
    assertFalse(ReminderScheduler.isEnabled(context))

    prefs.edit().clear().commit()
  }

  @Test
  fun `study streak counts yesterday when no session was logged today`() {
    val now = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 12)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun logDaysAgo(daysAgo: Int): StudyLogEntity {
      val timestamp = java.util.Calendar.getInstance().apply {
        timeInMillis = now
        add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
      }.timeInMillis
      return StudyLogEntity(
        timestamp = timestamp,
        durationMinutes = 30,
        subjectTitle = "مطالعه آزمایشی"
      )
    }

    assertEquals(0, calculateStudyStreak(emptyList(), now))
    assertEquals(1, calculateStudyStreak(listOf(logDaysAgo(1)), now))
    assertEquals(2, calculateStudyStreak(listOf(logDaysAgo(0), logDaysAgo(1)), now))
    assertEquals(1, calculateStudyStreak(listOf(logDaysAgo(0), logDaysAgo(2)), now))
  }


  @Test
  fun `verify sample quiz questions format`() {
    val questions = ComputerCurriculumData.sampleQuestions
    assertTrue(questions.isNotEmpty())

    questions.forEach { question ->
      assertEquals(4, question.options.size)
      assertTrue(question.correctIndex in 0..3)
      assertTrue(question.explanation.isNotBlank())
    }
  }
  @Test
  fun `timer runtime preferences are excluded from every Android backup mode`() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    fun containsTimerStateExclusion(resourceId: Int, sectionName: String): Boolean {
      val parser = context.resources.getXml(resourceId)
      try {
        var currentSection: String? = null
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
          when (parser.eventType) {
            XmlPullParser.START_TAG -> {
              if (parser.name == "full-backup-content" ||
                parser.name == "cloud-backup" ||
                parser.name == "device-transfer"
              ) {
                currentSection = parser.name
              }
              if (
                parser.name == "exclude" &&
                currentSection == sectionName &&
                parser.getAttributeValue(null, "domain") == "sharedpref" &&
                parser.getAttributeValue(null, "path") == "timer_state.xml"
              ) {
                return true
              }
            }
            XmlPullParser.END_TAG -> {
              if (parser.name == currentSection) currentSection = null
            }
          }
        }
        return false
      } finally {
        parser.close()
      }
    }

    assertTrue(containsTimerStateExclusion(R.xml.backup_rules, "full-backup-content"))
    assertTrue(containsTimerStateExclusion(R.xml.data_extraction_rules, "cloud-backup"))
    assertTrue(containsTimerStateExclusion(R.xml.data_extraction_rules, "device-transfer"))
  }

}
