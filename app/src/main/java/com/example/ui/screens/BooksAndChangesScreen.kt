package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.curriculum.ComputerCurriculumData
import com.example.data.model.BookInfo
import com.example.data.model.GradeLevel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.KonkurViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ResourceChip(label: String, selected: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = { Text(label, fontSize = 11.sp) }
    )
}

@Composable
fun BooksAndChangesScreen(viewModel: KonkurViewModel) {
    val selectedGrade by viewModel.selectedGrade.collectAsStateWithLifecycle()
    val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()

    val filteredBooks = ComputerCurriculumData.books.filter { it.grade == selectedGrade }

    // Map to keep track of expanded books
    val expandedBooks = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("books_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "کتاب‌ها و تحلیل تغییرات درسی",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "سرفصل ۵ پودمان کتب تخصصی کامپیوتر، کد کتاب‌ها و چاپ ملاک کنکور",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Grade Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = GradeLevel.values().indexOf(selectedGrade),
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                GradeLevel.values().forEach { grade ->
                    val isSelected = grade == selectedGrade
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectGrade(grade) },
                        text = {
                            Text(
                                text = grade.titleFa,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("grade_tab_${grade.name}")
                    )
                }
            }
        }

        // Changes Advisory Box for Grade
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(AccentGold, SecondaryTeal)))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "راهنمای چاپ کتاب‌های ${selectedGrade.titleFa}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (selectedGrade) {
                                GradeLevel.GRADE_10 -> "ملاک کنکور، چاپ سالی است که شما پایه دهم بوده‌اید. تفاوت چاپ‌ها عمدتاً در مثال‌های فلوچارت و حذف ویندوزهای قدیمی است."
                                GradeLevel.GRADE_11 -> "زبان برنامه‌نویسی محوری سی‌شارپ (#C) و HTML5/CSS3/PHP است. مباحث ویژوال بیسیک و فرانت‌پِیج قدیمی سال‌هاست منسوخ و حذف شده‌اند."
                                GradeLevel.GRADE_12 -> "جدیدترین چاپ سازمان پژوهش ملاک است. مباحث فناوری‌های نوین (ابری، IoT و هوش مصنوعی) و لینوکس در چاپ‌های جدید ارتقا یافته‌اند."
                                GradeLevel.GENERAL -> "ریاضیات فنی شامل پودمان‌های کاربردی است و ضریب ۶ دارد که در رتبه کل داوطلبان فنی کامپیوتر بسیار سرنوشت‌ساز است."
                            },
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Books List
        items(filteredBooks) { book ->
            val isExpanded = expandedBooks[book.id] ?: true

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Book Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedBooks[book.id] = !isExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = book.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = PrimaryIndigo.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "ضریب ${book.coefficient}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryIndigo,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "کد کتاب: ${book.bookCode} • ${book.description}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "بستن" else "باز کردن",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Book Details & Poodmans (Visible when expanded)
                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            // Textbook Changes Note
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "تحلیل تغییرات چاپ‌های اخیر کتاب:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AccentGold
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = book.bookChangesSummary,
                                        fontSize = 11.sp,
                                        lineHeight = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "نکته کنکوری: ${book.officialEditionsGuideline}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "پودمان‌های کتاب و وضعیت مطالعه:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // 5 Poodmans
                            book.poodmans.forEach { poodman ->
                                val progress = allProgress.find { it.bookId == book.id && it.poodmanIndex == poodman.index }
                                val isStudied = progress?.isStudied ?: false
                                val isReviewed = progress?.isReviewed ?: false
                                val isTested = progress?.isTestPracticed ?: false
                                val hasVideo = progress?.hasVideo ?: false
                                val hasPdf = progress?.hasPdf ?: false
                                val hasTests = progress?.hasTests ?: false

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = poodman.title,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = SecondaryTeal.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = poodman.examImportance,
                                                    fontSize = 10.sp,
                                                    color = SecondaryTeal,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "مباحث کلیدی: " + poodman.topics.joinToString("، "),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = AccentGold,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = poodman.goldenTip,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                        )

                                        // Progress Checkboxes
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    viewModel.togglePoodmanStatus(book.id, poodman.index, studied = !isStudied)
                                                }
                                            ) {
                                                Checkbox(
                                                    checked = isStudied,
                                                    onCheckedChange = { viewModel.togglePoodmanStatus(book.id, poodman.index, studied = it) },
                                                    colors = CheckboxDefaults.colors(checkedColor = SuccessGreen),
                                                    modifier = Modifier.testTag("chk_studied_${book.id}_${poodman.index}")
                                                )
                                                Text(text = "مطالعه کتاب", fontSize = 11.sp)
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    viewModel.togglePoodmanStatus(book.id, poodman.index, reviewed = !isReviewed)
                                                }
                                            ) {
                                                Checkbox(
                                                    checked = isReviewed,
                                                    onCheckedChange = { viewModel.togglePoodmanStatus(book.id, poodman.index, reviewed = it) },
                                                    colors = CheckboxDefaults.colors(checkedColor = SecondaryTeal),
                                                    modifier = Modifier.testTag("chk_reviewed_${book.id}_${poodman.index}")
                                                )
                                                Text(text = "مرور خلاصه", fontSize = 11.sp)
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    viewModel.togglePoodmanStatus(book.id, poodman.index, tested = !isTested)
                                                }
                                            ) {
                                                Checkbox(
                                                    checked = isTested,
                                                    onCheckedChange = { viewModel.togglePoodmanStatus(book.id, poodman.index, tested = it) },
                                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryIndigo),
                                                    modifier = Modifier.testTag("chk_tested_${book.id}_${poodman.index}")
                                                )
                                                Text(text = "تست‌زنی", fontSize = 11.sp)
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 6.dp),
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        )

                                        // Resource availability (منابع)
                                        Text(
                                            text = "منابع موجود برای این پودمان:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SecondaryTeal
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            ResourceChip("فیلم", hasVideo) { viewModel.togglePoodmanStatus(book.id, poodman.index, hasVideo = it) }
                                            ResourceChip("جزوه", hasPdf) { viewModel.togglePoodmanStatus(book.id, poodman.index, hasPdf = it) }
                                            ResourceChip("تست", hasTests) { viewModel.togglePoodmanStatus(book.id, poodman.index, hasTests = it) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
