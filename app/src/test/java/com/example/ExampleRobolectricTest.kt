package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.curriculum.ComputerCurriculumData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
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
  fun `verify sample quiz questions format`() {
    val questions = ComputerCurriculumData.sampleQuestions
    assertTrue(questions.isNotEmpty())

    questions.forEach { question ->
      assertEquals(4, question.options.size)
      assertTrue(question.correctIndex in 0..3)
      assertTrue(question.explanation.isNotBlank())
    }
  }
}
