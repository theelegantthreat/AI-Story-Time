package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.ImageSize
import com.example.data.ImageStylePreset
import com.example.ui.StoryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizeChapterImageDialog(
  viewModel: StoryViewModel,
  onDismiss: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val story = uiState.currentStory ?: return
  val chapterIndex = uiState.customizingChapterIndex
  val chapter = story.chapters.getOrNull(chapterIndex) ?: return
  val theme = uiState.readingTheme
  val isDarkTheme = theme.isDark

  val quickPromptTags = listOf(
    "+ Golden Hour",
    "+ Close-up Portrait",
    "+ Wide Landscape",
    "+ Dramatic Lighting",
    "+ Moonlight Glow",
    "+ Mystical Magic Sparks",
    "+ Misty Mountains"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color(theme.cardColor),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)),
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth(0.95f)
        .padding(vertical = 24.dp)
        .clip(RoundedCornerShape(28.dp))
        .testTag("customize_image_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Surface(
              shape = CircleShape,
              color = Color(theme.accentColor).copy(alpha = 0.15f),
              modifier = Modifier.size(42.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = Color(theme.accentColor),
                  modifier = Modifier.size(22.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Customize Chapter Artwork",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(theme.textColor)
              )
              Text(
                text = "Chapter ${chapterIndex + 1}: ${chapter.title}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(theme.subtitleColor),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.testTag("close_customize_image_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(theme.subtitleColor)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Image Preview Area
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF141414) else Color(0xFFE6E1E5)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .clip(RoundedCornerShape(20.dp))
            .testTag("custom_image_preview_card")
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            val previewBase64 = uiState.customImagePreviewBase64
            if (!previewBase64.isNullOrBlank()) {
              val bitmap = remember(previewBase64) {
                try {
                  val decodedBytes = android.util.Base64.decode(previewBase64, android.util.Base64.DEFAULT)
                  BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                } catch (e: Exception) {
                  null
                }
              }
              if (bitmap != null) {
                Image(
                  bitmap = bitmap,
                  contentDescription = "Custom artwork preview",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                DefaultPencilBanner()
              }
            } else {
              DefaultPencilBanner()
            }

            // Dark subtle gradient overlay
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0x88000000)),
                    startY = 100f
                  )
                )
            )

            // Current Style Badge
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xDD1C1B1F),
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = uiState.selectedImageStyle.icon, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = uiState.selectedImageStyle.label,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }

            // Loading overlay
            if (uiState.isCustomImageGenerating) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                  )
                  Spacer(modifier = Modifier.height(10.dp))
                  Text(
                    text = "Generating ${uiState.selectedImageStyle.label} Artwork...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "Using Gemini / Imagen with ${uiState.selectedCustomImageSize.label} resolution",
                    color = Color(0xFFD0BCFF),
                    fontSize = 11.sp
                  )
                }
              }
            }
          }
        }

        // Error message if any
        if (uiState.customImageError != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = uiState.customImageError ?: "",
              color = MaterialTheme.colorScheme.onErrorContainer,
              fontSize = 12.sp,
              modifier = Modifier.padding(10.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Style Presets Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Brush,
            contentDescription = null,
            tint = Color(theme.accentColor),
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Choose Art Style Preset",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(theme.textColor)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Art Styles Selection Grid
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          ImageStylePreset.values().forEach { style ->
            val isSelected = uiState.selectedImageStyle == style
            FilterChip(
              selected = isSelected,
              onClick = { viewModel.setSelectedImageStyle(style) },
              leadingIcon = {
                Text(text = style.icon, fontSize = 13.sp)
              },
              label = {
                Text(
                  text = style.label,
                  fontSize = 12.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(theme.accentColor),
                selectedLabelColor = if (isDarkTheme) Color.Black else Color.White,
                selectedLeadingIconColor = if (isDarkTheme) Color.Black else Color.White,
                containerColor = Color(theme.backgroundColor),
                labelColor = Color(theme.textColor)
              ),
              modifier = Modifier.testTag("style_chip_${style.name.lowercase()}")
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Prompt Extraction Button
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(theme.accentColor).copy(alpha = 0.10f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.35f)),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !uiState.isAnalyzingChapterForPrompt && !uiState.isCustomImageGenerating) {
              viewModel.autoGeneratePromptFromChapterStory(chapterIndex)
            }
            .testTag("auto_extract_prompt_button")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              if (uiState.isAnalyzingChapterForPrompt) {
                CircularProgressIndicator(
                  color = Color(theme.accentColor),
                  strokeWidth = 2.dp,
                  modifier = Modifier.size(18.dp)
                )
              } else {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = Color(theme.accentColor),
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Generate Prompt from Chapter Story",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color(theme.accentColor)
                )
                Text(
                  text = "AI analyzes this chapter's text to build a scenic prompt",
                  fontSize = 11.sp,
                  color = Color(theme.subtitleColor)
                )
              }
            }
            Text(
              text = "Auto ✨",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = Color(theme.accentColor)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Custom Prompt Input Field
        Text(
          text = "Custom Image Prompt / Scene Description",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color(theme.textColor)
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = uiState.customImagePromptInput,
          onValueChange = { viewModel.setCustomImagePrompt(it) },
          placeholder = {
            Text(
              text = "Describe what you want to see in this chapter's illustration...",
              fontSize = 12.sp,
              color = Color(theme.subtitleColor).copy(alpha = 0.7f)
            )
          },
          minLines = 3,
          maxLines = 5,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(theme.accentColor),
            unfocusedBorderColor = Color(theme.dividerColor),
            focusedContainerColor = Color(theme.backgroundColor),
            unfocusedContainerColor = Color(theme.backgroundColor),
            focusedTextColor = Color(theme.textColor),
            unfocusedTextColor = Color(theme.textColor)
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_image_prompt_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Tag Append Chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          quickPromptTags.forEach { tag ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(theme.backgroundColor),
              border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(theme.dividerColor)),
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  val current = uiState.customImagePromptInput
                  val updated = if (current.isBlank()) tag.removePrefix("+ ") else "$current, ${tag.removePrefix("+ ")}"
                  viewModel.setCustomImagePrompt(updated)
                }
            ) {
              Text(
                text = tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(theme.subtitleColor),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resolution Choice Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.HighQuality,
              contentDescription = null,
              tint = Color(theme.accentColor),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Resolution:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(theme.textColor)
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ImageSize.values().forEach { sizeOpt ->
              val isSelected = uiState.selectedCustomImageSize == sizeOpt
              FilterChip(
                selected = isSelected,
                onClick = { viewModel.setSelectedCustomImageSize(sizeOpt) },
                label = {
                  Text(
                    text = sizeOpt.label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = Color(theme.accentColor),
                  selectedLabelColor = if (isDarkTheme) Color.Black else Color.White,
                  containerColor = Color(theme.backgroundColor),
                  labelColor = Color(theme.textColor)
                ),
                modifier = Modifier.testTag("custom_size_${sizeOpt.name.lowercase()}")
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Action Buttons
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Generate Button
          Button(
            onClick = {
              viewModel.generateCustomChapterImage(
                chapterIndex = chapterIndex,
                customPrompt = uiState.customImagePromptInput,
                style = uiState.selectedImageStyle,
                size = uiState.selectedCustomImageSize
              )
            },
            enabled = !uiState.isCustomImageGenerating && uiState.customImagePromptInput.isNotBlank(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(theme.accentColor),
              contentColor = if (isDarkTheme) Color.Black else Color.White
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("generate_custom_image_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              if (uiState.isCustomImageGenerating) {
                CircularProgressIndicator(
                  color = if (isDarkTheme) Color.Black else Color.White,
                  strokeWidth = 2.dp,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating Artwork...", fontWeight = FontWeight.Bold)
              } else {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Generate & Save to Chapter ${chapterIndex + 1}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
            }
          }

          // Secondary Action Row: Reset to Default & Close
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Reset to Default Art
            OutlinedButton(
              onClick = {
                viewModel.removeChapterImage(chapterIndex)
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(theme.subtitleColor)
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("reset_custom_image_button")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.RestartAlt,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Reset Art", fontSize = 12.sp, fontWeight = FontWeight.Medium)
              }
            }

            // Done / Dismiss Button
            Button(
              onClick = onDismiss,
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(theme.backgroundColor),
                contentColor = Color(theme.textColor)
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("done_customize_image_button")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Done", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DefaultPencilBanner() {
  Box(modifier = Modifier.fillMaxSize()) {
    Image(
      painter = painterResource(id = R.drawable.img_pencil_story_art),
      contentDescription = "Default pencil sketch artwork",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
  }
}
