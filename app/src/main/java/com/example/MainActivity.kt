package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.util.exportCompletePdf
import com.example.viewmodel.PregnancyViewModel

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
  private val viewModel: PregnancyViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val sharedPrefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
        var isEnglish by remember { mutableStateOf(sharedPrefs.getBoolean("pref_is_english", false)) }
        var showSplash by remember { mutableStateOf(true) }
        var selectedTab by remember { mutableStateOf(0) }
        var showSettingsScreen by remember { mutableStateOf(false) }

        fun toggleLanguage() {
          val next = !isEnglish
          isEnglish = next
          sharedPrefs.edit().putBoolean("pref_is_english", next).apply()
        }

        if (showSplash) {
          SplashScreen(onSplashFinished = { showSplash = false })
        } else if (showSettingsScreen) {
          SettingsScreen(
            viewModel = viewModel,
            isEnglish = isEnglish,
            onBack = { showSettingsScreen = false }
          )
        } else {
          Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
              TopAppBar(
                title = {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Image(
                      painter = painterResource(id = R.drawable.app_custom_logo),
                      contentDescription = "Logo",
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                      contentScale = ContentScale.Crop
                    )
                    Column {
                      Text(
                        text = if (isEnglish) "Mom & Baby" else "Maman & Bébé",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      Text(
                        text = if (isEnglish) "One day, one step, one heartbeat at a time" else "Un jour, un pas, un battement de cœur à la fois",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                },
                actions = {
                  // PDF export button on top
                  IconButton(
                    onClick = {
                      exportCompletePdf(context, viewModel, isEnglish)
                    },
                    modifier = Modifier.testTag("btn_top_pdf_export")
                  ) {
                    Icon(
                      imageVector = Icons.Default.PictureAsPdf,
                      contentDescription = if (isEnglish) "Export PDF" else "Exporter PDF",
                      tint = Color(0xFF3B2314)
                    )
                  }

                  // Dual Language Switcher button visible and active across the ENTIRE app!
                  Surface(
                    onClick = { toggleLanguage() },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    tonalElevation = 0.dp,
                    modifier = Modifier.padding(end = 12.dp)
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Text(
                        text = if (isEnglish) "🇬🇧 EN" else "🇫🇷 FR",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                      )
                    }
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                  containerColor = Color(0xFFE2D3B8),
                  titleContentColor = Color(0xFF3B2314),
                  actionIconContentColor = Color(0xFF3B2314)
                )
              )
            },
            bottomBar = {
              NavigationBar(
                containerColor = Color(0xFFE2D3B8)
              ) {
                NavigationBarItem(
                  selected = selectedTab == 0,
                  onClick = { selectedTab = 0 },
                  icon = { Icon(Icons.Default.Home, contentDescription = if (isEnglish) "Home" else "Accueil") },
                  label = { Text(if (isEnglish) "Home" else "Accueil") },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF3B2314),
                    selectedTextColor = Color(0xFF3B2314),
                    unselectedIconColor = Color(0xFF6E4D32),
                    unselectedTextColor = Color(0xFF6E4D32),
                    indicatorColor = Color(0xFFC8B492)
                  )
                )
                NavigationBarItem(
                  selected = selectedTab == 1,
                  onClick = { selectedTab = 1 },
                  icon = { Icon(Icons.Default.Timeline, contentDescription = if (isEnglish) "Timeline" else "Chronologie") },
                  label = { Text(if (isEnglish) "Timeline" else "Chrono") },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF3B2314),
                    selectedTextColor = Color(0xFF3B2314),
                    unselectedIconColor = Color(0xFF6E4D32),
                    unselectedTextColor = Color(0xFF6E4D32),
                    indicatorColor = Color(0xFFC8B492)
                  )
                )
                NavigationBarItem(
                  selected = selectedTab == 2,
                  onClick = { selectedTab = 2 },
                  icon = { Icon(Icons.Default.MonitorWeight, contentDescription = if (isEnglish) "Tracking" else "Suivi") },
                  label = { Text(if (isEnglish) "Tracking" else "Suivi") },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF3B2314),
                    selectedTextColor = Color(0xFF3B2314),
                    unselectedIconColor = Color(0xFF6E4D32),
                    unselectedTextColor = Color(0xFF6E4D32),
                    indicatorColor = Color(0xFFC8B492)
                  )
                )
                NavigationBarItem(
                  selected = selectedTab == 3,
                  onClick = { selectedTab = 3 },
                  icon = { Icon(Icons.Default.MenuBook, contentDescription = if (isEnglish) "Guide" else "Guide") },
                  label = { Text(if (isEnglish) "Guide" else "Guide") },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF3B2314),
                    selectedTextColor = Color(0xFF3B2314),
                    unselectedIconColor = Color(0xFF6E4D32),
                    unselectedTextColor = Color(0xFF6E4D32),
                    indicatorColor = Color(0xFFC8B492)
                  )
                )
                NavigationBarItem(
                  selected = selectedTab == 4,
                  onClick = { selectedTab = 4 },
                  icon = { Icon(Icons.Default.Checklist, contentDescription = if (isEnglish) "Checklist" else "Trousseau") },
                  label = { Text(if (isEnglish) "Checklist" else "Trousseau") },
                  modifier = Modifier.testTag("btn_nav_checklist"),
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF3B2314),
                    selectedTextColor = Color(0xFF3B2314),
                    unselectedIconColor = Color(0xFF6E4D32),
                    unselectedTextColor = Color(0xFF6E4D32),
                    indicatorColor = Color(0xFFC8B492)
                  )
                )
              }
            }
          ) { innerPadding ->
            Surface(
              modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
              color = MaterialTheme.colorScheme.background
            ) {
              when (selectedTab) {
                0 -> HomeScreen(
                  viewModel = viewModel,
                  isEnglish = isEnglish,
                  onToggleLanguage = { toggleLanguage() },
                  onNavigateToTab = { tab -> selectedTab = tab },
                  onNavigateToSettings = { showSettingsScreen = true }
                )
                1 -> TimelineScreen(viewModel = viewModel, isEnglish = isEnglish)
                2 -> ToolsScreen(
                  viewModel = viewModel,
                  isEnglish = isEnglish,
                  onNavigateToSettings = { showSettingsScreen = true }
                )
                3 -> GuideGlossaryScreen(viewModel = viewModel, isEnglish = isEnglish)
                4 -> ProjectJournalScreen(viewModel = viewModel, isEnglish = isEnglish)
              }
            }
          }
        }
      }
    }
  }
}
