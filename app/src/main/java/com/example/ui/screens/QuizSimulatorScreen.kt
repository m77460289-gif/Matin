package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.curriculum.ComputerCurriculumData
import com.example.data.model.QuizQuestion
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.KonkurViewModel
import kotlinx.coroutines.delay

private const val SIM_QUESTION_COUNT = 25
private const val SIM_TOTAL_SECONDS = 30 * 60

@Composable
fun QuizSimulatorScreen(viewModel: KonkurViewModel) {
    val baseQuestions = ComputerCurriculumData.sampleQuestions
    var started by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableIntStateOf(SIM_TOTAL_SECONDS) }
    var currentIndex by remember { mutableIntStateOf(0) }

    val sessionQuestions = remember {
        if (baseQuestions.isEmpty()) emptyList()
        else List(SIM_QUESTION_COUNT) { i -> baseQuestions[i % baseQuestions.size] }
    }
    val answers = remember { mutableStateMapOf<Int, Int>() }
    val perQuestionSeconds = remember { mutableStateListOf<Int>().apply { repeat(SIM_QUESTION_COUNT) { add(0) } } }

    LaunchedEffect(started, finished) {
        if (!started || finished) return@LaunchedEffect
        while (!finished && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (currentIndex in perQuestionSeconds.indices) {
                perQuestionSeconds[currentIndex] = perQuestionSeconds[currentIndex] + 1
            }
            if (remainingSeconds <= 0) {
                finished = true
                break
            }
        }
    }

    if (baseQuestions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("سوالی برای آزمون شبیه‌ساز موجود نیست.", fontSize = 14.sp)
        }
        return
    }

    if (!started) {
        SimulatorIntro(onStart = { started = true })
        return
    }

    if (finished) {
        SimulatorResults(
            questions = sessionQuestions,
            answers = answers,
            perQuestionSeconds = perQuestionSeconds,
            totalUsedSeconds = SIM_TOTAL_SECONDS - remainingSeconds,
            onRetry = {
                answers.clear()
                for (i in perQuestionSeconds.indices) perQuestionSeconds[i] = 0
                currentIndex = 0
                remainingSeconds = SIM_TOTAL_SECONDS
                finished = false
                started = false
            },
            onRecordWrong = { qid -> viewModel.recordWrongAnswer(qid) }
        )
        return
    }

    val currentQuestion = sessionQuestions[currentIndex]
    val selected = answers[currentIndex]
    val isAnswered = selected != null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("simulator_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.12f))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = if (remainingSeconds < 300) MaterialTheme.colorScheme.error else PrimaryIndigo)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(formatSeconds(remainingSeconds), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (remainingSeconds < 300) MaterialTheme.colorScheme.error else PrimaryIndigo)
                    }
                    Text("سوال ${currentIndex + 1} از ${sessionQuestions.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / sessionQuestions.size },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${currentQuestion.bookTitle} • پودمان ${currentQuestion.poodmanIndex}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = currentQuestion.questionText, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    currentQuestion.options.forEachIndexed { optIndex, optText ->
                        val isSelected = selected == optIndex
                        val correct = optIndex == currentQuestion.correctIndex
                        val containerColor = when {
                            !isAnswered -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            correct -> SuccessGreen.copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        }
                        val borderColor = when {
                            !isAnswered -> Color.Transparent
                            correct -> SuccessGreen
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
                                    answers[currentIndex] = optIndex
                                    if (optIndex != currentQuestion.correctIndex) {
                                        viewModel.recordWrongAnswer(currentQuestion.id)
                                    }
                                }
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .background(
                                            color = when {
                                                isAnswered && correct -> SuccessGreen
                                                isAnswered && isSelected -> MaterialTheme.colorScheme.error
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isAnswered && correct) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    else if (isAnswered && isSelected) Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    else Text("${optIndex + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(optText, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(10.dp)
                ) { Text("قبلی") }

                Button(
                    onClick = {
                        if (currentIndex < sessionQuestions.size - 1) currentIndex++
                        else finished = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) { Text(if (currentIndex < sessionQuestions.size - 1) "بعدی" else "پایان و مشاهده نتیجه") }
            }
        }
    }
}

@Composable
private fun SimulatorIntro(onStart: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("simulator_intro"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("آزمون شبیه‌ساز کنکور", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Text("شرایط واقعی کنکور: ۲۵ سوال در ۳۰ دقیقه", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• تایمر واقعی ۳۰ دقیقه‌ای (پایان خودکار)", fontSize = 12.sp)
                    Text("• ۲۵ سوال چندپودمانی از بانک سوالات", fontSize = 12.sp)
                    Text("• ثبت زمان صرف‌شده برای هر سوال", fontSize = 12.sp)
                    Text("• تحلیل نهایی: درصد کل + ضعیف‌ترین پودمان", fontSize = 12.sp)
                    Text("• سوالات غلط خودکار در دفترچه اشتباهات ذخیره می‌شوند", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        item {
            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("start_simulator_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("شروع آزمون شبیه‌ساز")
            }
        }
    }
}

@Composable
private fun SimulatorResults(
    questions: List<QuizQuestion>,
    answers: Map<Int, Int>,
    perQuestionSeconds: List<Int>,
    totalUsedSeconds: Int,
    onRetry: () -> Unit,
    onRecordWrong: (Int) -> Unit
) {
    val correctCount = questions.indices.count { i ->
        val q = questions[i]
        answers[i] == q.correctIndex
    }
    val answered = answers.size
    val percent = if (questions.isEmpty()) 0 else (correctCount * 100) / questions.size

    // Weakest poodman analysis
    val wrongByPoodman = mutableMapOf<Int, Int>()
    val wrongByBook = mutableMapOf<String, Int>()
    questions.forEachIndexed { i, q ->
        if (answers[i] != null && answers[i] != q.correctIndex) {
            wrongByPoodman[q.poodmanIndex] = (wrongByPoodman[q.poodmanIndex] ?: 0) + 1
            wrongByBook[q.bookTitle] = (wrongByBook[q.bookTitle] ?: 0) + 1
        }
    }
    val weakestPoodman = wrongByPoodman.maxByOrNull { it.value }?.key
    val weakestBook = wrongByBook.maxByOrNull { it.value }?.key

    val avgTime = if (questions.isEmpty()) 0 else totalUsedSeconds / questions.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("simulator_results"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.12f))) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("نتیجه آزمون شبیه‌ساز", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$percent٪", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = if (percent >= 50) SuccessGreen else MaterialTheme.colorScheme.error)
                    Text("$correctCount از ${questions.size} پاسخ صحیح", fontSize = 12.sp)
                    Text("زمان: ${formatSeconds(totalUsedSeconds)} • میانگین ${formatSeconds(avgTime)}/سوال", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("پاسخ داده: $answered از ${questions.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (weakestBook != null) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🔍 تحلیل ضعیف‌ترین نقاط", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• کتاب با بیشترین خطا: $weakestBook", fontSize = 12.sp)
                        Text("• ضعیف‌ترین پودمان: پودمان ${weakestPoodman ?: "-"}", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("پیشنهاد: این پودمان را در برنامه هفته آینده مرور و تست بزن.", fontSize = 11.sp, color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item { Text("📋 زمان هر سوال", fontWeight = FontWeight.Bold, fontSize = 14.sp) }

        itemsIndexed(questions) { i, q ->
            val ans = answers[i]
            val isCorrect = ans == q.correctIndex
            OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("سوال ${i + 1}: ${if (ans == null) "نزده" else if (isCorrect) "صحیح ✅" else "غلط ❌"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (ans == null) MaterialTheme.colorScheme.onSurfaceVariant else if (isCorrect) SuccessGreen else MaterialTheme.colorScheme.error)
                        Text(q.bookTitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(formatSeconds(perQuestionSeconds.getOrElse(i) { 0 }), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) { Text("شروع آزمون جدید") }
        }
    }
}

private fun formatSeconds(s: Int): String {
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}
