package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.BuildConfig
import com.example.data.ImageSize
import com.example.data.ReadingFontOption
import com.example.data.ReadingTheme
import com.example.data.VoiceProfile
import com.example.ui.StoryViewModel
import com.example.ui.UiState
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SurfaceVariant
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
  viewModel: StoryViewModel,
  uiState: UiState,
  onDismiss: () -> Unit
) {
  val speedPresets = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
  val isKeySet = BuildConfig.GEMINI_API_KEY.isNotBlank() &&
      BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" &&
      !BuildConfig.GEMINI_API_KEY.startsWith("placeholder", ignoreCase = true)

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color(0xFFFDF8FF),
      tonalElevation = 8.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Voice & App Settings",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1C1B1F)
            )
          )
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close settings",
              tint = Color(0xFF49454F)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Reading Comfort & Themes
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Nightlight,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Reading Themes & Comfort",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Primary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Themes selection list
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          ReadingTheme.values().forEach { theme ->
            val isSelected = uiState.readingTheme == theme
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = Color(theme.backgroundColor)
              ),
              border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Primary) else androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)),
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { viewModel.setReadingTheme(theme) }
                .testTag("settings_theme_${theme.name.lowercase()}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                  Text(text = theme.icon, fontSize = 20.sp)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = theme.label,
                      style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(theme.textColor)
                      )
                    )
                    Text(
                      text = theme.description,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(theme.subtitleColor),
                        fontSize = 11.sp
                      )
                    )
                  }
                }
                if (isSelected) {
                  Surface(
                    shape = CircleShape,
                    color = Primary,
                    modifier = Modifier.size(22.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected theme",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Font Size & Typography
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.FormatSize,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Story Font Size (${uiState.readingFontSize.toInt()}sp)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Primary
            )
          )
        }

        Slider(
          value = uiState.readingFontSize,
          onValueChange = { viewModel.setReadingFontSize(it) },
          valueRange = 14f..24f,
          steps = 4,
          colors = SliderDefaults.colors(
            thumbColor = Primary,
            activeTrackColor = Primary,
            inactiveTrackColor = Color(0xFFD0BCFF)
          ),
          modifier = Modifier.testTag("font_size_slider")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ReadingFontOption.values().forEach { fontOpt ->
            val isSelected = uiState.readingFontOption == fontOpt
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setReadingFontOption(fontOpt) },
              label = {
                Text(
                  text = fontOpt.label,
                  fontFamily = when (fontOpt) {
                    ReadingFontOption.SERIF -> FontFamily.Serif
                    ReadingFontOption.SANS_SERIF -> FontFamily.SansSerif
                    ReadingFontOption.MONOSPACE -> FontFamily.Monospace
                  }
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("font_family_${fontOpt.label.lowercase()}")
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 1: Voice Profiles (TTS)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Narrator Voice Profile",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Primary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        VoiceProfile.ALL.forEach { voice ->
          val isSelected = uiState.selectedVoiceProfile.id == voice.id
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) PrimaryContainer else Color(0xFFF3EDF7)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(16.dp))
              .clickable {
                viewModel.setVoiceProfile(voice)
              }
              .testTag("voice_profile_${voice.id.lowercase()}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = voice.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color(0xFF21005D) else Color(0xFF1C1B1F)
                    )
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Surface(
                    shape = CircleShape,
                    color = if (isSelected) Primary else Color(0xFFE8DEF8),
                    modifier = Modifier.padding(2.dp)
                  ) {
                    Text(
                      text = voice.gender,
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) Color.White else Color(0xFF49454F),
                        fontSize = 10.sp
                      ),
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
                Text(
                  text = voice.description,
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isSelected) Color(0xFF381E72) else Color(0xFF49454F)
                  )
                )
              }

              IconButton(
                onClick = {
                  viewModel.setVoiceProfile(voice)
                  viewModel.ttsManager.playText(
                    "Hello! I am ${voice.displayName}, your story narrator.",
                    voice,
                    uiState.playbackSpeed
                  )
                },
                modifier = Modifier.testTag("audition_voice_${voice.id.lowercase()}")
              ) {
                Icon(
                  imageVector = Icons.Default.VolumeUp,
                  contentDescription = "Test voice ${voice.displayName}",
                  tint = if (isSelected) Primary else Color(0xFF79747E)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: Playback Speed
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Playback Speed (${String.format("%.2f", uiState.playbackSpeed)}x)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Primary
            )
          )
        }

        Slider(
          value = uiState.playbackSpeed,
          onValueChange = { viewModel.setPlaybackSpeed(it) },
          valueRange = 0.5f..2.0f,
          steps = 5,
          colors = SliderDefaults.colors(
            thumbColor = Primary,
            activeTrackColor = Primary,
            inactiveTrackColor = Color(0xFFD0BCFF)
          ),
          modifier = Modifier.testTag("speed_slider")
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          speedPresets.forEach { speed ->
            val isSelected = (uiState.playbackSpeed * 100).roundToInt() == (speed * 100).roundToInt()
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setPlaybackSpeed(speed) },
              label = { Text("${speed}x") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Primary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("speed_chip_${speed}x")
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: Voice Pitch
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Voice Pitch (${String.format("%.1f", uiState.ttsPitch)}x)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Primary
            )
          )
        }

        Slider(
          value = uiState.ttsPitch,
          onValueChange = { viewModel.setTtsPitch(it) },
          valueRange = 0.75f..1.5f,
          colors = SliderDefaults.colors(
            thumbColor = Primary,
            activeTrackColor = Primary,
            inactiveTrackColor = Color(0xFFD0BCFF)
          ),
          modifier = Modifier.testTag("pitch_slider")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 4: Image Generation Resolution (1K, 2K, 4K)
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.HighQuality,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Image Quality (gemini-3-pro-image-preview)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Primary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          ImageSize.values().forEach { size ->
            val isSelected = uiState.selectedImageSize == size
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) Primary else Color(0xFFF3EDF7),
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .clickable { viewModel.setImageSize(size) }
                .testTag("image_size_${size.label.lowercase()}")
            ) {
              Column(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = size.label,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) Color.White else Color(0xFF1C1B1F)
                )
                Text(
                  text = size.resolution,
                  fontSize = 10.sp,
                  color = if (isSelected) Color(0xFFEADDFF) else Color(0xFF49454F)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: Gemini AI Model Selector
        Text(
          text = "Gemini Storyteller Model",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            color = Primary
          )
        )
        Spacer(modifier = Modifier.height(6.dp))

        listOf(
          "gemini-3.5-flash" to "General Storyteller (Default)",
          "gemini-3.1-pro-preview" to "Complex Narratives & Deep Lore",
          "gemini-3.1-flash-lite" to "Fast & Lightweight"
        ).forEach { (modelId, desc) ->
          val isSelected = uiState.selectedAiModel == modelId
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) PrimaryContainer else Color(0xFFF3EDF7))
              .clickable { viewModel.setAiModel(modelId) }
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = modelId,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFF21005D) else Color(0xFF1C1B1F)
              )
              Text(
                text = desc,
                fontSize = 11.sp,
                color = if (isSelected) Color(0xFF381E72) else Color(0xFF49454F)
              )
            }
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 6: Local Room Database & Offline Caching
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoStories,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Local Storage & Offline Cache (Room)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.SemiBold,
              color = Primary
            )
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF3EDF7)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            val storyCount by viewModel.storyCount.collectAsState()
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Room SQLite Database",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF1C1B1F)
                )
                Text(
                  text = "$storyCount stories cached for instant offline reading",
                  fontSize = 11.sp,
                  color = Color(0xFF49454F)
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = PrimaryContainer,
                modifier = Modifier.padding(start = 8.dp)
              ) {
                Text(
                  text = "Offline Ready",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = { viewModel.preloadStarterStories() },
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFEADDFF),
                  contentColor = Color(0xFF21005D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(38.dp)
                  .testTag("preload_starter_stories_button")
              ) {
                Text("Preload Starter Tales", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }

              Button(
                onClick = { viewModel.clearAllStoryCache() },
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFFFD8E4),
                  contentColor = Color(0xFF31111D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(38.dp)
                  .testTag("clear_cache_button")
              ) {
                Text("Clear Cache", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // API Key Status Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isKeySet) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Key,
              contentDescription = null,
              tint = if (isKeySet) Color(0xFF2E7D32) else Color(0xFFE65100),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = if (isKeySet) "Gemini API Connected" else "API Key: Offline Sample Mode",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isKeySet) Color(0xFF2E7D32) else Color(0xFFE65100)
              )
              Text(
                text = if (isKeySet) "Using full cloud intelligence & image synthesis" else "Add GEMINI_API_KEY in Secrets for live cloud models",
                fontSize = 10.sp,
                color = Color(0xFF49454F)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = Primary),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("save_settings_button")
        ) {
          Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
