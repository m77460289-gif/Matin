package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StudyLogEntity
import com.example.data.model.StudyNoteEntity
import com.example.data.model.WeeklyScoreEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.KonkurViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsAndNotesScreen(viewModel: KonkurViewModel) {
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()
    val totalMinutes by viewModel.totalMinutes.collectAsStateWithLifecycle()
    val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddScoreDialog by remember { mutableStateOf(false) }
    var backupMsg by remember { mutableStateOf<String?>(null) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE) }
    var reminderOn by remember { mutableStateOf(prefs.getBoolean("daily_reminder_enabled", true)) }
    val weeklyScores by viewModel.allWeeklyScores.collectAsStateWithLifecycle()

    val studiedCount = allProgress.count { it.isStudied }
    val reviewedCount = allProgress.count { it.isReviewed }
    val testedCount = allProgress.count { it.isTestPracticed }

    val totalPoodmans = com.example.data.curriculum.ComputerCurriculumData.books.sumOf { it.poodmans.size }
    val overallPercent = (studiedCount.toFloat() / totalPoodmans.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "آمار مطالعه و دفترچه نکات",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "ثبت عملکرد جلسات ۲ ساعته و فرمول‌ها و نکات کلیدی دروس کامپیوتر",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary Statistics Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "وضعیت تسلط بر پودمان‌های کنکور کامپیوتر",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { overallPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (overallPercent >= 1f) SuccessGreen else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "مطالعه کتاب", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$studiedCount از $totalPoodmans", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SuccessGreen)
                        }
                        Column {
                            Text(text = "مرور خلاصه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$reviewedCount از $totalPoodmans", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryTeal)
                        }
                        Column {
                            Text(text = "تست‌زنی", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$testedCount از $totalPoodmans", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryIndigo)
                        }
                        Column {
                            Text(text = "ساعات کل", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${(totalMinutes ?: 0) / 60} ساعت",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AccentGold
                            )
                        }
                    }
                }
            }
        }

        // Backup & Restore
        item {
            BackupSection(
                viewModel = viewModel,
                onMessage = { backupMsg = it }
            )
        }

        // Daily Reminder Setting
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🔔 یادآور روزانه ساعت ۱۷:۰۰", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            if (reminderOn) "«۲ ساعت مطالعه امروز شروع شود؟» — فعال" else "غیرفعال",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = reminderOn,
                        onCheckedChange = { enabled ->
                            reminderOn = enabled
                            prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()
                            if (enabled) {
                                com.example.reminder.ReminderScheduler.scheduleDaily(ctx)
                            } else {
                                com.example.reminder.ReminderScheduler.cancel(ctx)
                            }
                        }
                    )
                }
            }
        }

        // Weekly Exam Score Trend
        item {
            WeeklyScoreTrendSection(
                scores = weeklyScores,
                onAddClick = { showAddScoreDialog = true },
                onDelete = { viewModel.deleteWeeklyScore(it) }
            )
        }

        // Notes Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "دفترچه نکات و خلاصه‌های شخصی",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { showAddNoteDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_note_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("نکته جدید", fontSize = 12.sp)
                }
            }
        }

        // Notes List
        if (notes.isEmpty()) {
            item {
                OutlinedCard(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "هنوز نکته‌ای ثبت نکرده‌اید.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "فرمول‌های ریاضی، کدهای سی‌شارپ، آدرس‌های IP و نکات مهم پودمان‌ها را اینجا یادداشت کنید تا در روز جمعه مرور کنید.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(notes) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = note.bookName,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.content,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Study History Header
        item {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تاریخچه جلسات مطالعه ثبت‌شده",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Logs List
        if (logs.isEmpty()) {
            item {
                Text(
                    text = "هنوز جلسه مطالعه‌ای ثبت نشده است. با اتمام تایمر تمرکز، جلسات به صورت خودکار ثبت خواهند شد.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(logs) { log ->
                val dateStr = remember(log.timestamp) {
                    val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
                    sdf.format(Date(log.timestamp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = log.subjectTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${log.durationMinutes} دقیقه",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (log.note.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = log.note,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = dateStr,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(onClick = { viewModel.deleteLog(log.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف لاگ",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    backupMsg?.let { msg ->
        AlertDialog(
            onDismissRequest = { backupMsg = null },
            title = { Text("📦 پشتیبان‌گیری", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text(msg, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { backupMsg = null }) { Text("باشه") }
            }
        )
    }

    // Add Weekly Score Dialog
    if (showAddScoreDialog) {
        var weekStr by remember { mutableStateOf((weeklyScores.maxOfOrNull { it.weekNumber }?.plus(1) ?: 1).toString()) }
        var scoreStr by remember { mutableStateOf("") }
        var examTitle by remember { mutableStateOf("آزمون هفتگی") }

        AlertDialog(
            onDismissRequest = { showAddScoreDialog = false },
            title = { Text(text = "ثبت نمره آزمون هفتگی", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weekStr,
                        onValueChange = { weekStr = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("شماره هفته (۱ تا ۳۰)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scoreStr,
                        onValueChange = { scoreStr = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("درصد نمره (۰ تا ۱۰۰)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = examTitle,
                        onValueChange = { examTitle = it },
                        label = { Text("عنوان آزمون (اختیاری)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = weekStr.toIntOrNull()
                        val s = scoreStr.toIntOrNull()
                        if (w != null && s != null && w in 1..30 && s in 0..100) {
                            viewModel.addWeeklyScore(w, s, examTitle.ifBlank { "آزمون هفتگی" })
                            showAddScoreDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) { Text("ثبت") }
            },
            dismissButton = {
                TextButton(onClick = { showAddScoreDialog = false }) { Text("انصراف") }
            }
        )
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteBook by remember { mutableStateOf("توسعه برنامه‌سازی (#C)") }
        var noteContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text(text = "ثبت نکته طلایی جدید", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("عنوان نکته یا فرمول") },
                        placeholder = { Text("مثال: فرمول ساب‌نتینگ یا اولویت عملگرها") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteBook,
                        onValueChange = { noteBook = it },
                        label = { Text("کتاب مربوطه") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("متن نکته / کد / توضیح") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                            viewModel.addNote(noteTitle, noteBook, noteContent)
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("ذخیره در دفترچه")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun BackupSection(
    viewModel: KonkurViewModel,
    onMessage: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = viewModel.exportBackupJson()
                com.example.data.backup.BackupManager.writeJsonToUri(context, uri, json)
                onMessage("✅ فایل پشتیبان با موفقیت ذخیره شد.\n\nمحل ذخیره را در گالری فایل‌ها یا Google Drive ببینید.")
            } catch (e: Exception) {
                onMessage("❌ خطا در ذخیره فایل: ${e.message}")
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = com.example.data.backup.BackupManager.readJsonFromUri(context, uri)
                val result = viewModel.importBackupJson(json)
                onMessage("✅ بازیابی موفق:\n\n• ${result.logs} جلسه مطالعه\n• ${result.progress} پیشرفت پودمان\n• ${result.notes} نکته\n• ${result.leitner} کارت لایتنر\n• ${result.wrongAnswers} سوال غلط\n• ${result.scores} نمره آزمون\n\nمجموع: ${result.total} رکورد")
            } catch (e: Exception) {
                onMessage("❌ خطا در بازیابی: ${e.message}\n\nمطمئن شوید فایل JSON معتبر است.")
            }
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryTeal.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Backup,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("پشتیبان‌گیری و بازیابی", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "اگر گوشی عوض شد، اطلاعات مطالعه از دست می‌رود. هر هفته یک نسخه پشتیبان بگیرید.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val fname = "konkur-backup-${sdf.format(Date())}.json"
                        exportLauncher.launch(fname)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_backup_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("📤 خروجی JSON", fontSize = 12.sp)
                }
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_backup_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("📥 ورودی JSON", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "⚠️ بازیابی، داده‌های فعلی را جایگزین می‌کند. قبل از بازیابی، ابتدا خروجی بگیرید.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun WeeklyScoreTrendSection(
    scores: List<WeeklyScoreEntity>,
    onAddClick: () -> Unit,
    onDelete: (Long) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("روند نمره آزمون‌های هفتگی", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_score_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ثبت نمره", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "هر پنجشنبه نمره آزمون را ثبت کن — نمودار پیشرفت هفته ۱ تا ۳۰",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (scores.isEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "هنوز نمره‌ای ثبت نشده. با دکمه «ثبت نمره» شروع کن.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                ScoreLineChart(scores = scores)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("آخرین نمره", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${scores.last().scorePercent}٪", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryIndigo)
                    }
                    Column {
                        Text("بهترین", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${scores.maxOf { it.scorePercent }}٪", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SuccessGreen)
                    }
                    Column {
                        Text("تعداد آزمون", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${scores.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentGold)
                    }
                    if (scores.size >= 2) {
                        val diff = scores.last().scorePercent - scores.first().scorePercent
                        Column {
                            Text("تغییر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                (if (diff >= 0) "+$diff" else "$diff") + "٪",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (diff >= 0) SuccessGreen else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("برای حذف، روی هر نمره بزنید:", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(4.dp))
                scores.takeLast(6).reversed().forEach { s ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("هفته ${s.weekNumber} • ${s.examTitle}", fontSize = 11.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${s.scorePercent}٪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                            IconButton(onClick = { onDelete(s.id) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "حذف",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreLineChart(scores: List<WeeklyScoreEntity>) {
    val lineColor = PrimaryIndigo
    val pointColor = AccentGold
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(4.dp)
    ) {
        val w = size.width
        val h = size.height
        val padLeft = 30f
        val padBottom = 20f
        val plotW = w - padLeft
        val plotH = h - padBottom

        for (i in 0..4) {
            val y = plotH - (plotH * i / 4f)
            drawLine(
                color = gridColor,
                start = Offset(padLeft, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }

        if (scores.isEmpty()) return@Canvas

        val count = scores.size
        val stepX = if (count > 1) plotW / (count - 1).toFloat() else plotW / 2f

        val points = scores.mapIndexed { i, s ->
            val x = if (count > 1) padLeft + i * stepX else padLeft + plotW / 2f
            val y = plotH - (plotH * (s.scorePercent.coerceIn(0, 100) / 100f))
            Offset(x, y)
        }

        val path = Path().apply {
            points.forEachIndexed { i, p ->
                if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
            }
        }
        drawPath(path, color = lineColor, style = Stroke(width = 4f))

        points.forEach { p ->
            drawCircle(color = pointColor, radius = 7f, center = p)
            drawCircle(color = lineColor, radius = 3f, center = p)
        }

        val paint = android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = 22f
            isAntiAlias = true
        }
        for (i in 0..4) {
            val y = plotH - (plotH * i / 4f)
            drawContext.canvas.nativeCanvas.drawText(
                "${i * 25}",
                2f,
                y + 8f,
                paint
            )
        }
    }
}
