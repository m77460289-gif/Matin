package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryLightIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.KonkurViewModel

@Composable
fun TimerScreen(viewModel: KonkurViewModel) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    var showManualLogDialog by remember { mutableStateOf(false) }

    val remaining = timerState.remainingSeconds
    val minutes = remaining / 60
    val seconds = remaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val progress = if (timerState.initialSeconds > 0) {
        timerState.remainingSeconds.toFloat() / timerState.initialSeconds.toFloat()
    } else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
            .testTag("timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تایمر تمرکز ۲ ساعته",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { viewModel.toggleVibrateAlert() },
                modifier = Modifier.testTag("vibrate_toggle_btn")
            ) {
                Icon(
                    imageVector = if (timerState.vibrateAlert) Icons.Default.Vibration else Icons.Default.NotificationsOff,
                    contentDescription = "تنظیم ویبره",
                    tint = if (timerState.vibrateAlert) AccentGold else MaterialTheme.colorScheme.outline
                )
            }
        }

        Text(
            text = "جلسه فعلی: ${timerState.sessionLabel}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 4.dp, bottom = 16.dp)
        )

        // Presets
        Text(
            text = "انتخاب پارت مطالعه بر اساس برنامه ۲ ساعته:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PresetChip(
                label = "۴۵ د (پودمان)",
                minutes = 45,
                isSelected = timerState.initialSeconds == 45 * 60,
                modifier = Modifier.weight(1f)
            ) {
                viewModel.setTimerPreset(45, "مطالعه مفهومی پودمان تخصصی")
            }
            PresetChip(
                label = "۳۵ د (ریاضی)",
                minutes = 35,
                isSelected = timerState.initialSeconds == 35 * 60,
                modifier = Modifier.weight(1f)
            ) {
                viewModel.setTimerPreset(35, "حل تمرین ریاضی و دروس پایه")
            }
            PresetChip(
                label = "۲۵ د (تست)",
                minutes = 25,
                isSelected = timerState.initialSeconds == 25 * 60,
                modifier = Modifier.weight(1f)
            ) {
                viewModel.setTimerPreset(25, "تست‌زنی سرعتی و تحلیل")
            }
            PresetChip(
                label = "۱۰ د (استراحت)",
                minutes = 10,
                isSelected = timerState.initialSeconds == 10 * 60,
                modifier = Modifier.weight(1f)
            ) {
                viewModel.setTimerPreset(10, "استراحت فعال و بازیابی انرژی")
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Circular Timer Display
        val primaryColor = MaterialTheme.colorScheme.primary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
                // Progress Arc
                drawArc(
                    color = if (timerState.isFinished) SuccessGreen else primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (timerState.isFinished) SuccessGreen else MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (timerState.isRunning) PrimaryLightIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            timerState.isFinished -> "پارت کامل شد! 🎉"
                            timerState.isRunning -> "در حال تمرکز..."
                            else -> "آماده شروع"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (timerState.isFinished) SuccessGreen else primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset
            OutlinedButton(
                onClick = { viewModel.resetTimer() },
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("timer_reset_btn"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "بازنشانی", tint = MaterialTheme.colorScheme.outline)
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Main Play/Pause Button
            Button(
                onClick = {
                    if (timerState.isRunning) viewModel.pauseTimer() else viewModel.startOrResumeTimer()
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerState.isRunning) AccentGold else PrimaryIndigo
                ),
                modifier = Modifier
                    .size(72.dp)
                    .testTag("timer_play_pause_btn"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (timerState.isRunning) "توقف" else "شروع",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Manual log study time
            OutlinedButton(
                onClick = { showManualLogDialog = true },
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("manual_log_btn"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "ثبت دستی", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Completion Banner
        AnimatedVisibility(visible = timerState.isFinished) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "آفرین! این پارت مطالعه با موفقیت در گزارش روزانه شما ذخیره شد.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Focus Tips Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "💡 قانون تمرکز ۲ ساعته:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "در مدت زمان شمارش معکوس، گوشی را در حالت پرواز قرار دهید و نوتیفیکیشن‌ها را ببندید. ۲ ساعت مطالعه با تمرکز مطلق ۱۰۰٪ برابر با کل روز مطالعه پراکنده است.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Manual Log Dialog
    if (showManualLogDialog) {
        var manualMinutes by remember { mutableStateOf("45") }
        var manualSubject by remember { mutableStateOf("سی‌شارپ و پایگاه داده") }
        var manualNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showManualLogDialog = false },
            title = { Text(text = "ثبت دستی ساعات مطالعه", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = manualMinutes,
                        onValueChange = { manualMinutes = it },
                        label = { Text("مدت زمان (دقیقه)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manualSubject,
                        onValueChange = { manualSubject = it },
                        label = { Text("عنوان کتاب یا پودمان") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manualNote,
                        onValueChange = { manualNote = it },
                        label = { Text("یادداشت یا نکته اختیاری") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = manualMinutes.toIntOrNull() ?: 45
                        viewModel.logStudyTime(mins, manualSubject, manualNote)
                        showManualLogDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("ثبت در کارنامه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualLogDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun PresetChip(
    label: String,
    minutes: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
