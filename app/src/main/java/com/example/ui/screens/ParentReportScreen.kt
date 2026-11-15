package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.curriculum.ComputerCurriculumData
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.KonkurViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ParentReportScreen(viewModel: KonkurViewModel) {
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()
    val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()
    val weeklyScores by viewModel.allWeeklyScores.collectAsStateWithLifecycle()
    val streak by viewModel.studyStreak.collectAsStateWithLifecycle()
    val dueLeitner by viewModel.dueLeitnerCards.collectAsStateWithLifecycle()
    val wrongAnswers by viewModel.allWrongAnswers.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Calculate the past 7 days window
    val weekAgoMs = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
    val weekLogs = logs.filter { it.timestamp >= weekAgoMs }
    val weekMinutes = weekLogs.sumOf { it.durationMinutes }
    val weekSessions = weekLogs.size

    val testedCount = allProgress.count { it.isTestPracticed }
    val studiedCount = allProgress.count { it.isStudied }
    val reviewedCount = allProgress.count { it.isReviewed }

    val lastScore = weeklyScores.lastOrNull()
    val lastWeekAvg = if (weeklyScores.isEmpty()) 0 else weeklyScores.takeLast(4).map { it.scorePercent }.average().toInt()

    // Weak poodmans — studied but not tested
    val weakPoodmans = allProgress.filter { it.isStudied && !it.isTestPracticed }

    fun buildReportText(): String {
        val sb = StringBuilder()
        sb.appendLine("📊 گزارش هفتگی کنکور کامپیوتر")
        sb.appendLine("📅 ${SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date())}")
        sb.appendLine()
        sb.appendLine("⏱️ زمان مطالعه هفته گذشته: $weekMinutes دقیقه ($weekSessions جلسه)")
        sb.appendLine("🔥 استریک: $streak روز پیوسته")
        sb.appendLine()
        sb.appendLine("📚 وضعیت پودمان‌ها:")
        sb.appendLine("  • مطالعه: $studiedCount")
        sb.appendLine("  • مرور: $reviewedCount")
        sb.appendLine("  • تست‌زنی: $testedCount")
        sb.appendLine()
        if (lastScore != null) {
            sb.appendLine("📝 آخرین نمره آزمون: ${lastScore.scorePercent}٪ (هفته ${lastScore.weekNumber})")
            sb.appendLine("📈 میانگین ۴ آزمون اخیر: $lastWeekAvg٪")
            sb.appendLine()
        }
        sb.appendLine("⚠️ پودمان‌های عقب‌مانده (مطالعه شده اما تست نشده): ${weakPoodmans.size}")
        weakPoodmans.take(5).forEach { p ->
            val book = ComputerCurriculumData.books.find { it.id == p.bookId }
            val pood = book?.poodmans?.getOrNull(p.poodmanIndex - 1)
            sb.appendLine("  - ${book?.title ?: p.bookId} / ${pood?.title ?: "پودمان ${p.poodmanIndex}"}")
        }
        if (weakPoodmans.size > 5) sb.appendLine("  ... و ${weakPoodmans.size - 5} مورد دیگر")
        sb.appendLine()
        sb.appendLine("🔁 مرورهای امروز لایتنر: ${dueLeitner.size}")
        sb.appendLine("❌ سوالات غلط دفترچه: ${wrongAnswers.size}")
        sb.appendLine()
        sb.appendLine("— ارسال شده از اپ کنکور کامپیوتر")
        return sb.toString()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("parent_report_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پنل والد — خلاصه هفتگی", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "نظارت بدون بازجویی — فقط نگاه کن و پشتیبانی کن ❤️",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⏱️ هفته گذشته", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("$weekMinutes", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                        Text(" دقیقه", fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
                    }
                    Text("$weekSessions جلسه مطالعه ثبت شده", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox("🔥 استریک", "$streak", "روز", AccentGold, Modifier.weight(1f))
                StatBox("📝 آخرین آزمون", if (lastScore != null) "${lastScore.scorePercent}٪" else "—", "", SecondaryTeal, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox("📚 مطالعه", "$studiedCount", "پودمان", SuccessGreen, Modifier.weight(1f))
                StatBox("✅ تست", "$testedCount", "پودمان", PrimaryIndigo, Modifier.weight(1f))
            }
        }

        if (weakPoodmans.isNotEmpty()) {
            item {
                Text("⚠️ پودمان‌های عقب‌مانده", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            items(weakPoodmans.take(8)) { p ->
                val book = ComputerCurriculumData.books.find { it.id == p.bookId }
                val pood = book?.poodmans?.getOrNull(p.poodmanIndex - 1)
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = pood?.title ?: "پودمان ${p.poodmanIndex}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = book?.title ?: p.bookId,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "مطالعه شده اما تست نشده",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    val text = buildReportText()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                        putExtra(Intent.EXTRA_SUBJECT, "گزارش هفتگی کنکور کامپیوتر")
                    }
                    val chooser = Intent.createChooser(shareIntent, "ارسال گزارش به تلگرام یا واتساپ")
                    context.startActivity(chooser)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_parent_report_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("📤 ارسال گزارش به تلگرام/واتساپ")
            }
        }
    }
}

@Composable
private fun StatBox(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
                if (unit.isNotBlank()) {
                    Text(" $unit", fontSize = 11.sp, modifier = Modifier.padding(bottom = 3.dp))
                }
            }
        }
    }
}
