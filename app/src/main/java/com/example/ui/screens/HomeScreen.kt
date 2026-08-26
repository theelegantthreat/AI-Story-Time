package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoryGenre
import com.example.data.StoryLength
import com.example.ui.AppScreen
import com.example.ui.StoryViewModel
import com.example.ui.UiState
import com.example.ui.theme.Background
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SurfaceVariant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  viewModel: StoryViewModel,
  uiState: UiState
) {
  val inspirationSparks = listOf(
    "A curious baby dragon who wanted to bake cupcakes",
    "A pocket-sized robot discovering an enchanted glass forest",
    "A secret magical door hidden in an ancient grandfather clock",
    "A starlight compass that leads to forgotten sky islands",
    "A clever fox who solved mysteries in the Whispering Woods"
  )
  val storyCount by viewModel.storyCount.collectAsState()

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
                  imageVector = Icons.Default.AutoStories,
                  contentDescription = null,
                  tint = Primary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "AI Story Time",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
              )
            )
          }
        },
        actions = {
          IconButton(
            onClick = { viewModel.setScreen(AppScreen.CHAT) },
            modifier = Modifier.testTag("chat_bot_nav_button")
          ) {
            Icon(
              imageVector = Icons.Default.ChatBubble,
              contentDescription = "Story Weaver Chatbot",
              tint = Primary
            )
          }
          IconButton(
            onClick = { viewModel.setScreen(AppScreen.LIBRARY) },
            modifier = Modifier.testTag("library_nav_button")
          ) {
            Icon(
              imageVector = Icons.Default.CollectionsBookmark,
              contentDescription = "Story Library",
              tint = Primary
            )
          }
          IconButton(
            onClick = { viewModel.toggleSettingsDialog(true) },
            modifier = Modifier.testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = Color(0xFF49454F)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Background
        )
      )
    },
    containerColor = Background
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 12.dp)
      ) {
        // Hero Header
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF6750A4), Color(0xFF9C27B0), Color(0xFF5C6BC0))
              )
            )
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFFD8E4),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "CRAFT A NEW TALE",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(0xFFFFD8E4),
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.2.sp
                )
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "What story shall we bring to life?",
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Personalized illustrated stories narrated in custom AI voices with interactive branching continuations.",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFF3EDF7),
                lineHeight = 18.sp
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Room Offline Storage & Cache Status Banner
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("room_cache_banner")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Surface(
                shape = CircleShape,
                color = Color(0xFFEADDFF),
                modifier = Modifier.size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Room Offline Cache",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color(0xFF1C1B1F)
                )
                Text(
                  text = "$storyCount ${if (storyCount == 1) "story" else "stories"} stored locally on device",
                  fontSize = 11.sp,
                  color = Color(0xFF49454F)
                )
              }
            }

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFE8F5E9),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { viewModel.setScreen(AppScreen.LIBRARY) }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.OfflinePin,
                  contentDescription = null,
                  tint = Color(0xFF2E7D32),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Vault",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2E7D32)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: Prompt Input
        Text(
          text = "Story Prompt or Hint",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F)
          )
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = uiState.promptInput,
          onValueChange = { viewModel.updatePromptInput(it) },
          placeholder = {
            Text(
              "e.g. A brave squirrel building an airship to reach the moon...",
              color = Color(0xFF79747E),
              fontSize = 14.sp
            )
          },
          trailingIcon = {
            if (uiState.promptInput.isNotBlank()) {
              IconButton(onClick = { viewModel.updatePromptInput("") }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear text",
                  tint = Color(0xFF79747E)
                )
              }
            }
          },
          shape = RoundedCornerShape(20.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF3EDF7),
            unfocusedContainerColor = Color(0xFFF3EDF7),
            focusedBorderColor = Primary,
            unfocusedBorderColor = Color(0xFFCAC4D0)
          ),
          minLines = 3,
          maxLines = 5,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("prompt_input_field")
        )

        // Instant Cache Match Card if Prompt already exists in Room
        val cacheMatch = uiState.cacheMatchStory
        if (cacheMatch != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("cache_match_card")
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
                  Icon(
                    imageVector = Icons.Default.DownloadDone,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Saved in Room Database",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Primary
                  )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = cacheMatch.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF1C1B1F)
                )
                Text(
                  text = "${cacheMatch.genre.label} • ${cacheMatch.chapters.size} Chapters • No API call needed",
                  fontSize = 11.sp,
                  color = Color(0xFF49454F)
                )
              }

              Button(
                onClick = { viewModel.selectStory(cacheMatch) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .padding(start = 8.dp)
                  .testTag("open_cached_story_button")
              ) {
                Text("Read Offline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Inspiration Sparks Chips
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = Color(0xFFFFB703),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Story Sparks:",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF49454F)
            )
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          inspirationSparks.forEach { spark ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFEADDFF),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.updatePromptInput(spark) }
            ) {
              Text(
                text = "✨ $spark",
                style = MaterialTheme.typography.bodySmall.copy(
                  fontSize = 11.sp,
                  color = Color(0xFF21005D),
                  fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: Story Length
        Text(
          text = "Story Length",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F)
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StoryLength.values().forEach { len ->
            val isSelected = uiState.selectedLength == len
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Primary else Color(0xFFF3EDF7)
              ),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .clickable { viewModel.setLength(len) }
                .testTag("length_option_${len.name.lowercase()}")
            ) {
              Column(
                modifier = Modifier
                  .padding(vertical = 12.dp, horizontal = 4.dp)
                  .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = len.label,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (isSelected) Color.White else Color(0xFF1C1B1F)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${len.approximateWords}w",
                  fontSize = 11.sp,
                  color = if (isSelected) Color(0xFFEADDFF) else Color(0xFF49454F)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: Story Genre
        Text(
          text = "Genre & Atmosphere",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1C1B1F)
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          StoryGenre.values().forEach { genre ->
            val isSelected = uiState.selectedGenre == genre
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setGenre(genre) },
              label = { Text("${genre.icon} ${genre.label}") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary,
                selectedLabelColor = Color.White,
                containerColor = Color(0xFFF3EDF7),
                labelColor = Color(0xFF1C1B1F)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("genre_chip_${genre.name.lowercase()}")
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Settings Pill Bar (Narrator & Image Size)
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8DEF8)),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { viewModel.toggleSettingsDialog(true) }
            .testTag("voice_quick_settings_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.RecordVoiceOver,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Narrator: ${uiState.selectedVoiceProfile.displayName}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF21005D)
                )
                Text(
                  text = "Speed: ${uiState.playbackSpeed}x • Quality: ${uiState.selectedImageSize.label}",
                  fontSize = 11.sp,
                  color = Color(0xFF49454F)
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Customize",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Primary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prefer Offline Cache Toggle Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.OfflinePin,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Prefer Local Offline Cache",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color(0xFF1C1B1F)
                )
                Text(
                  text = "Load matching stories from Room DB without API calls",
                  fontSize = 11.sp,
                  color = Color(0xFF49454F)
                )
              }
            }
            Switch(
              checked = uiState.preferOfflineCache,
              onCheckedChange = { viewModel.togglePreferOfflineCache(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = Color(0xFF79747E),
                uncheckedTrackColor = Color(0xFFEADDFF)
              ),
              modifier = Modifier.testTag("prefer_cache_switch")
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Create Story Button
        Button(
          onClick = { viewModel.startNewStory() },
          enabled = !uiState.isGeneratingStory,
          colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            disabledContainerColor = Color(0xFFCAC4D0)
          ),
          shape = RoundedCornerShape(28.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag("create_story_button")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = Color.White
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Create Story",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(30.dp))
      }

      // Generation Overlay
      AnimatedVisibility(
        visible = uiState.isGeneratingStory,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC1C1B1F))
            .clickable(enabled = false) {},
          contentAlignment = Alignment.Center
        ) {
          Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF8FF)),
            modifier = Modifier
              .fillMaxWidth(0.85f)
              .padding(24.dp)
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(
                color = Primary,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Weaving Story...",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1C1B1F)
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = uiState.generationStatusText.ifBlank { "Consulting the storyteller..." },
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFF49454F)
                )
              )
            }
          }
        }
      }
    }
  }
}
