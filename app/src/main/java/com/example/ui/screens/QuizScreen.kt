package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.curriculum.ComputerCurriculumData
import com.example.data.model.QuizQuestion
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.KonkurViewModel

@Composable
fun QuizScreen(viewModel: KonkurViewModel) {
    val allQuestions = ComputerCurriculumData.sampleQuestions
    val wrongAnswers by viewModel.allWrongAnswers.collectAsStateWithLifecycle()

    var retakeMode by remember { mutableStateOf(false) }

    val wrongIds = wrongAnswers.map { it.questionId }.toSet()
    val questions: List<QuizQuestion> = if (retakeMode) {
        allQuestions.filter { it.id in wrongIds }
    } else {
        allQuestions
    }

    var currentIndex by remember(retakeMode, questions.size) { mutableStateOf(0) }
    val userAnswers = remember(retakeMode) { mutableStateMapOf<Int, Int>() }

    if (questions.isEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("quiz_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("دفترچه اشتباهات خالی است 🎉", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("هیچ سوال غلطی ثبت نشده. ادامه بده!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Button(
                    onClick = { retakeMode = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("بازگشت به آزمونک اصلی") }
            }
        }
        return
    }

    val safeIndex = currentIndex.coerceIn(0, questions.size - 1)
    val currentQuestion = questions[safeIndex]
    val selectedOptionIndex = userAnswers[currentQuestion.id]
    val isAnswered = selectedOptionIndex != null

    val totalScore = userAnswers.count { (qId, ans) ->
        val q = allQuestions.find { it.id == qId }
        q?.correctIndex == ans
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quiz_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (retakeMode) "آزمون مجدد از غلط‌ها" else "آزمونک پارت ۲۵ دقیقه‌ای تست",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = SecondaryTeal.copy(alpha = 0.15f)) {
                        Text(
                            text = "امتیاز: $totalScore از ${questions.size}",
                            color = SecondaryTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = if (retakeMode) "فقط سوالاتی که قبلاً غلط زده‌اید — تا اشتباه تکرار نشود." else "تست‌های احتمالی و پرتکرار کنکور فنی‌حرفه‌ای با تحلیل کامل",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            OutlinedButton(
                onClick = { viewModel.selectTab(8) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_simulator_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Timer, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("آزمون شبیه‌ساز ۲۵ سوال / ۳۰ دقیقه")
            }
        }

        if (!retakeMode && wrongAnswers.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("دفترچه اشتباهات: ${wrongAnswers.size} سوال غلط", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                            Text("یک اشتباه را دو بار تکرار نکن — همین حالا مرور کن.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { retakeMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("retake_wrongs_btn")
                        ) { Text("آزمون مجدد", fontSize = 12.sp) }
                    }
                }
            }
        }

        if (retakeMode) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { retakeMode = false }) { Text("بازگشت به آزمون اصلی", fontSize = 12.sp) }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = PrimaryIndigo.copy(alpha = 0.15f)) {
                            Text(
                                text = "تست ${safeIndex + 1} از ${questions.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "${currentQuestion.bookTitle} • پودمان ${currentQuestion.poodmanIndex}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = currentQuestion.questionText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    currentQuestion.options.forEachIndexed { optIndex, optText ->
                        val isSelected = selectedOptionIndex == optIndex
                        val isCorrectOption = optIndex == currentQuestion.correctIndex

                        val containerColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            isCorrectOption -> SuccessGreen.copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }
                        val borderColor = when {
                            !isAnswered -> Color.Transparent
                            isCorrectOption -> SuccessGreen
                            isSelected -> MaterialTheme.colorScheme.error
                            else -> Color.Transparent
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = containerColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = !isAnswered) {
                                    userAnswers[currentQuestion.id] = optIndex
                                    val correct = optIndex == currentQuestion.correctIndex
                                    if (correct) {
                                        if (retakeMode) viewModel.removeWrongAnswer(currentQuestion.id)
                                    } else {
                                        viewModel.recordWrongAnswer(currentQuestion.id)
                                    }
                                }
                                .testTag("option_${currentQuestion.id}_$optIndex")
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = when {
                                                isAnswered && isCorrectOption -> SuccessGreen
                                                isAnswered && isSelected -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAnswered && isCorrectOption) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else if (isAnswered && isSelected) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text("${optIndex + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = optText,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected || (isAnswered && isCorrectOption)) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isAnswered) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("📖 تحلیل تشریحی پاسخ:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(currentQuestion.explanation, fontSize = 12.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نکته طلایی: ${currentQuestion.goldenRule}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = { if (safeIndex > 0) currentIndex = safeIndex - 1 },
                    enabled = safeIndex > 0,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("prev_quiz_btn")
                ) {
                    Icon(Icons.Default.NavigateNext, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("سوال قبلی")
                }
                Button(
                    onClick = {
                        if (safeIndex < questions.size - 1) {
                            currentIndex = safeIndex + 1
                        } else {
                            userAnswers.clear()
                            currentIndex = 0
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.testTag("next_quiz_btn")
                ) {
                    Text(if (safeIndex < questions.size - 1) "سوال بعدی" else "شروع مجدد")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (safeIndex < questions.size - 1) Icons.Default.NavigateBefore else Icons.Default.Refresh,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
