package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsAndNotesScreen
import com.example.ui.screens.BooksAndChangesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LeitnerScreen
import com.example.ui.screens.QuizSimulatorScreen
import com.example.ui.screens.ParentReportScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.TwoHourScheduleScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.KonkurViewModel

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            val perms = arrayOf("android.permission.POST_NOTIFICATIONS")
            if (checkSelfPermission(perms[0]) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(perms, 1001)
            }
        }
        if (com.example.reminder.ReminderScheduler.isEnabled(this)) {
            com.example.reminder.ReminderScheduler.scheduleDaily(this)
        }
        setContent {
            MyApplicationTheme {
                KonkurApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonkurApp(viewModel: KonkurViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val navItems = listOf(
        NavTabItem("داشبورد", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
        NavTabItem("برنامه ۲ ساعت", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_schedule"),
        NavTabItem("کتاب‌ها و تغییرات", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_books"),
        NavTabItem("تایمر تمرکز", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_timer"),
        NavTabItem("آزمونک", Icons.Filled.Quiz, Icons.Outlined.Quiz, "nav_quiz"),
        NavTabItem("آمار و نکات", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_analytics"),
        NavTabItem("برنامه تا کنکور", Icons.Filled.DateRange, Icons.Outlined.DateRange, "nav_annual_plan"),
        NavTabItem("مرور امروز", Icons.Filled.Refresh, Icons.Outlined.Refresh, "nav_leitner"),
        NavTabItem("آزمون شبیه‌ساز", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_simulator"),
        NavTabItem("پنل والد", Icons.Filled.SupervisorAccount, Icons.Outlined.SupervisorAccount, "nav_parent")
    )

    // RTL Provider for Persian
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (isTablet) {
            // Adaptive Tablet Layout with Side Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    navItems.forEachIndexed { index, item ->
                        NavigationRailItem(
                            selected = currentTab == index,
                            onClick = { viewModel.selectTab(index) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == index) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title, fontSize = 11.sp) },
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    ScreenContent(
                        currentTab = currentTab,
                        viewModel = viewModel
                    )
                }
            }
        } else {
            // Mobile Layout with TopAppBar & Bottom Navigation Bar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "کنکور کامپیوتر | برنامه‌ریز ۲ ساعته",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    // Keep every NavigationBarItem as a direct child of NavigationBar.
                    // The More popup is a sibling overlay so it cannot participate in
                    // NavigationBar's item measurement or expand the bottom bar.
                    Box {
                        val secondaryTabIndices = listOf(2, 5, 6, 7, 8, 9)
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            // Keep the four most-used destinations in the bottom bar.
                            // The remaining destinations stay available from the More menu.
                            val primaryTabIndices = listOf(0, 1, 3, 4)
                            primaryTabIndices.forEach { index ->
                                val item = navItems[index]
                                val isSelected = currentTab == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTab(index) },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.title,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryIndigo,
                                        selectedTextColor = PrimaryIndigo,
                                        indicatorColor = PrimaryIndigo.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }

                            val isMoreSelected = currentTab in secondaryTabIndices
                            NavigationBarItem(
                                selected = isMoreSelected,
                                onClick = { moreMenuExpanded = true },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.MoreHoriz,
                                        contentDescription = "بخش‌های بیشتر",
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "بیشتر",
                                        fontSize = 10.sp,
                                        fontWeight = if (isMoreSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryIndigo,
                                    selectedTextColor = PrimaryIndigo,
                                    indicatorColor = PrimaryIndigo.copy(alpha = 0.12f)
                                ),
                                modifier = Modifier.testTag("nav_more")
                            )
                        }

                        DropdownMenu(
                            expanded = moreMenuExpanded,
                            onDismissRequest = { moreMenuExpanded = false }
                        ) {
                            secondaryTabIndices.forEach { index ->
                                val item = navItems[index]
                                DropdownMenuItem(
                                    text = { Text(item.title) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (currentTab == index) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        moreMenuExpanded = false
                                        viewModel.selectTab(index)
                                    },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    ScreenContent(
                        currentTab = currentTab,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    currentTab: Int,
    viewModel: KonkurViewModel
) {
    when (currentTab) {
        0 -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToTimer = { minutes, label ->
                viewModel.setTimerPreset(minutes, label)
                viewModel.selectTab(3)
            },
            onNavigateToBooks = { viewModel.selectTab(2) },
            onNavigateToSchedule = { viewModel.selectTab(1) }
        )
        1 -> TwoHourScheduleScreen(
            viewModel = viewModel,
            onNavigateToTimer = { minutes, label ->
                viewModel.setTimerPreset(minutes, label)
                viewModel.selectTab(3)
            }
        )
        2 -> BooksAndChangesScreen(viewModel = viewModel)
        3 -> TimerScreen(viewModel = viewModel)
        4 -> QuizScreen(viewModel = viewModel)
        5 -> AnalyticsAndNotesScreen(viewModel = viewModel)
        6 -> com.example.ui.screens.AnnualPlanScreen(viewModel = viewModel)
        7 -> LeitnerScreen(viewModel = viewModel)
        8 -> QuizSimulatorScreen(viewModel = viewModel)
        9 -> ParentReportScreen(viewModel = viewModel)
        else -> DashboardScreen(
            viewModel = viewModel,
            onNavigateToTimer = { minutes, label ->
                viewModel.setTimerPreset(minutes, label)
                viewModel.selectTab(3)
            },
            onNavigateToBooks = { viewModel.selectTab(2) },
            onNavigateToSchedule = { viewModel.selectTab(1) }
        )
    }
}
