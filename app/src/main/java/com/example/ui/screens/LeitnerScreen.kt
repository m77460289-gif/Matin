package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LeitnerCardEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.KonkurViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val leitnerIntervalsFa = listOf("۱ روز", "۳ روز", "۷ روز", "۱۴ روز", "۳۰ روز")

@Composable
fun LeitnerScreen(viewModel: KonkurViewModel) {
    val dueCards by viewModel.dueLeitnerCards.collectAsStateWithLifecycle()
    val allCards by viewModel.allLeitnerCards.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refreshNow() }

    val masteredCount = allCards.count { it.isMastered }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leitner_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جعبه لایتنر — مرور فاصله‌دار",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "مرورها: ۱ روز → ۳ روز → ۷ روز → ۱۴ روز → ۳۰ روز. فقط ۱۰ دقیقه در روز کافی است.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(label = "مرور امروز", value = dueCards.size.toString(), color = PrimaryIndigo)
                    StatColumn(label = "در جعبه", value = allCards.size.toString(), color = AccentGold)
                    StatColumn(label = "تسلط", value = masteredCount.toString(), color = SuccessGreen)
                }
            }
        }

        if (dueCards.isEmpty()) {
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "مروری برای امروز نداری 🎉",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "هر پودمانی که «مطالعه» تیک بزنی، خودکار وارد جعبه می‌شود.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "برای امروز (${dueCards.size} مرور)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            items(dueCards, key = { "${it.bookId}_${it.poodmanIndex}" }) { card ->
                DueCardRow(card = card, viewModel = viewModel)
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "همه کارت‌های جعبه (${allCards.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (allCards.isEmpty()) {
            item {
                Text(
                    text = "هنوز کارتی در جعبه نیست. از تب «کتاب‌ها و تغییرات» پودمان‌ها را مطالعه تیک بزن.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(allCards, key = { "all_${it.bookId}_${it.poodmanIndex}" }) { card ->
                AllCardRow(card = card, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DueCardRow(card: LeitnerCardEntity, viewModel: KonkurViewModel) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = PrimaryIndigo.copy(alpha = 0.15f)) {
                    Text(
                        text = "جعبه ${card.boxLevel} • ${leitnerIntervalsFa.getOrElse(card.boxLevel - 1) { "تسلط" }}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = card.bookTitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = card.poodmanTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.reviewLeitnerCard(card, remembered = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("یادم بود ✅", fontSize = 13.sp) }
                Button(
                    onClick = { viewModel.reviewLeitnerCard(card, remembered = false) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) { Text("فراموش کردم ❌", fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun AllCardRow(card: LeitnerCardEntity, viewModel: KonkurViewModel) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.poodmanTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.resetLeitnerCard(card) }) {
                    Text("حذف", fontSize = 11.sp)
                }
            }
            Text(
                text = card.bookTitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            val statusText = if (card.isMastered) {
                "تسلط یافت ✅"
            } else {
                val days = TimeUnit.MILLISECONDS.toDays(card.nextReviewAt - System.currentTimeMillis()).coerceAtLeast(0)
                "جعبه ${card.boxLevel} • مرور بعدی: ${formatDateFa(card.nextReviewAt)}" + if (days == 0L) " (امروز)" else ""
            }
            Text(text = statusText, fontSize = 11.sp, color = if (card.isMastered) SuccessGreen else SecondaryTeal)
        }
    }
}

private fun formatDateFa(ts: Long): String {
    if (ts == Long.MAX_VALUE) return "—"
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(ts))
}
