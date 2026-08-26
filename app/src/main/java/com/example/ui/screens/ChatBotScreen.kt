package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.AppScreen
import com.example.ui.StoryViewModel
import com.example.ui.UiState
import com.example.ui.theme.Background
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SurfaceVariant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatBotScreen(
  viewModel: StoryViewModel,
  uiState: UiState
) {
  var chatInputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  val suggestedPrompts = listOf(
    "Suggest a wild plot twist for my story!",
    "Help me name a quirky sidekick character.",
    "Give me 3 magical creatures for a forest quest.",
    "How can I make the ending more emotional?"
  )

  LaunchedEffect(uiState.chatMessages.size) {
    if (uiState.chatMessages.isNotEmpty()) {
      listState.animateScrollToItem(uiState.chatMessages.lastIndex)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = PrimaryContainer,
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Psychology,
                  contentDescription = null,
                  tint = Primary,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Story Weaver AI",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1C1B1F)
                )
              )
              Text(
                text = "Model: ${uiState.selectedAiModel}",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = Primary
                )
              )
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = {
              if (uiState.currentStory != null) {
                viewModel.setScreen(AppScreen.READER)
              } else {
                viewModel.setScreen(AppScreen.HOME)
              }
            },
            modifier = Modifier.testTag("chat_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF1C1B1F)
            )
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.toggleSettingsDialog(true) },
            modifier = Modifier.testTag("chat_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = Color(0xFF49454F)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
      )
    },
    containerColor = Background
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Chat Messages List
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          Spacer(modifier = Modifier.height(6.dp))
          // Current Story context pill
          if (uiState.currentStory != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFE8DEF8),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = Primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Discussing: ${uiState.currentStory.title}",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21005D),
                    fontSize = 11.sp
                  )
                )
              }
            }
          }
        }

        items(uiState.chatMessages) { message ->
          ChatMessageBubble(message = message)
        }

        if (uiState.isChatLoading) {
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
            ) {
              CircularProgressIndicator(
                color = Primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Story Weaver is thinking...",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFF79747E),
                  fontSize = 12.sp
                )
              )
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(8.dp))
        }
      }

      // Brainstorming Suggestions Chips
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        suggestedPrompts.forEach { prompt ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEADDFF),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { viewModel.sendChatMessage(prompt) }
          ) {
            Text(
              text = prompt,
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = Color(0xFF21005D),
                fontWeight = FontWeight.Medium
              ),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }

      // Input Field Bar
      Surface(
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = chatInputText,
            onValueChange = { chatInputText = it },
            placeholder = {
              Text("Ask Story Weaver anything...", fontSize = 14.sp, color = Color(0xFF79747E))
            },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFFF3EDF7),
              unfocusedContainerColor = Color(0xFFF3EDF7),
              focusedBorderColor = Primary,
              unfocusedBorderColor = Color(0xFFCAC4D0)
            ),
            maxLines = 4,
            modifier = Modifier
              .weight(1f)
              .testTag("chat_input_field")
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (chatInputText.isNotBlank()) {
                viewModel.sendChatMessage(chatInputText)
                chatInputText = ""
              }
            },
            enabled = chatInputText.isNotBlank() && !uiState.isChatLoading,
            modifier = Modifier
              .size(48.dp)
              .background(
                if (chatInputText.isNotBlank()) Primary else Color(0xFFE0E0E0),
                CircleShape
              )
              .testTag("chat_send_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send message",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(message: ChatMessage) {
  val isUser = message.role == "user"

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
      ),
      color = if (isUser) Primary else Color(0xFFF3EDF7),
      shadowElevation = 1.dp,
      modifier = Modifier.fillMaxWidth(0.82f)
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        if (!isUser) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Primary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Story Weaver",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Primary,
                fontSize = 11.sp
              )
            )
          }
        }

        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = if (isUser) Color.White else Color(0xFF1C1B1F),
            lineHeight = 20.sp
          )
        )
      }
    }
  }
}
