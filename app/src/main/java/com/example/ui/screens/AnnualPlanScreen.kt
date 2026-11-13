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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.plan.AnnualPlanData
import com.example.data.plan.PlanPhase
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen

@Composable
fun AnnualPlanScreen(viewModel: com.example.ui.viewmodel.KonkurViewModel) {
    var showCheckInDialog by remember { mutableStateOf(false) }
    val checkIns by viewModel.allCheckIns.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("annual_plan_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "برنامه راهبردی تا کنکور",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "۵ فاز از آذر تا هفته آخر کنکور + چک‌پوینت هر یکشنبه و پنجشنبه",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.12f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = SecondaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "وضعیت شروع", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = AnnualPlanData.startingStatus,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(AnnualPlanData.phases) { phase ->
            PhaseCard(phase)
        }

        item {
            Text(
                text = "قواعد چک‌پوینت هفتگی",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        items(AnnualPlanData.checkpointRules) { rule ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = rule, fontSize = 12.sp)
            }
        }

        if (checkIns.isNotEmpty()) {
            item {
                Text(text = "چک‌پوینت‌های ثبت‌شده", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            items(checkIns) { ci ->
                OutlinedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = ci.dayType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryIndigo)
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.deleteCheckIn(ci.id) }) { Text("حذف", fontSize = 11.sp) }
                        }
                        Text(text = "پیشرفت: ${ci.completedText}", fontSize = 12.sp)
                        if (ci.missingText.isNotBlank()) {
                            Text(text = "کمبود: ${ci.missingText}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { showCheckInDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("checkin_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ثبت چک‌پوینت هفتگی (یکشنبه / پنجشنبه)")
            }
        }
    }

    if (showCheckInDialog) {
        var dayType by remember { mutableStateOf("یکشنبه") }
        var completed by remember { mutableStateOf("") }
        var missing by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCheckInDialog = false },
            title = { Text("چک‌پوینت هفتگی", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("یکشنبه", "پنجشنبه").forEach { type ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (dayType == type) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                TextButton(onClick = { dayType = type }) {
                                    Text(type, color = if (dayType == type) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = completed,
                        onValueChange = { completed = it },
                        label = { Text("تا کجا رفتند / چه کار کردند") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = missing,
                        onValueChange = { missing = it },
                        label = { Text("کمبود منابع (فیلم/جزوه/تست)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (completed.isNotBlank()) {
                        viewModel.addCheckIn(dayType, completed, missing)
                    }
                    showCheckInDialog = false
                }) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { showCheckInDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun PhaseCard(phase: PlanPhase) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = phase.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentGold.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${phase.startMonth} → ${phase.endMonth}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = phase.goal, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "تمرکز هفتگی: ${phase.weeklyFocus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
