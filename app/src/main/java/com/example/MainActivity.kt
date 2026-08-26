package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.StoryViewModel
import com.example.ui.screens.ChatBotScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.StoryLibraryScreen
import com.example.ui.screens.StoryReaderScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val storyViewModel: StoryViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        StoryApp(viewModel = storyViewModel)
      }
    }
  }
}

@Composable
fun StoryApp(viewModel: StoryViewModel) {
  val uiState by viewModel.uiState.collectAsState()

  // Handle system back navigation
  BackHandler(enabled = uiState.currentScreen != AppScreen.HOME) {
    when (uiState.currentScreen) {
      AppScreen.READER -> viewModel.setScreen(AppScreen.HOME)
      AppScreen.LIBRARY -> viewModel.setScreen(AppScreen.HOME)
      AppScreen.CHAT -> {
        if (uiState.currentStory != null) {
          viewModel.setScreen(AppScreen.READER)
        } else {
          viewModel.setScreen(AppScreen.HOME)
        }
      }
      AppScreen.HOME -> {}
    }
  }

  Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(modifier = Modifier.fillMaxSize()) {
      Crossfade(targetState = uiState.currentScreen, label = "screen_transition") { screen ->
        when (screen) {
          AppScreen.HOME -> HomeScreen(viewModel = viewModel, uiState = uiState)
          AppScreen.READER -> StoryReaderScreen(viewModel = viewModel, uiState = uiState)
          AppScreen.LIBRARY -> StoryLibraryScreen(viewModel = viewModel, uiState = uiState)
          AppScreen.CHAT -> ChatBotScreen(viewModel = viewModel, uiState = uiState)
        }
      }

      // Settings Dialog
      if (uiState.showSettingsDialog) {
        SettingsDialog(
          viewModel = viewModel,
          uiState = uiState,
          onDismiss = { viewModel.toggleSettingsDialog(false) }
        )
      }

      // Error Banner
      uiState.errorBanner?.let { errorMsg ->
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 40.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp)
          ) {
            Text(
              text = errorMsg,
              color = Color(0xFFC62828),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.align(Alignment.CenterStart).padding(end = 36.dp)
            )
            IconButton(
              onClick = { viewModel.clearError() },
              modifier = Modifier.align(Alignment.CenterEnd)
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss error",
                tint = Color(0xFFC62828)
              )
            }
          }
        }
      }
    }
  }
}

