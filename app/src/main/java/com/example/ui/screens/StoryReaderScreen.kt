package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Chapter
import com.example.data.ReadingFontOption
import com.example.data.ReadingTheme
import com.example.data.Story
import com.example.ui.AppScreen
import com.example.ui.StoryViewModel
import com.example.ui.UiState
import com.example.ui.theme.Background
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.Surface
import com.example.ui.theme.SurfaceVariant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StoryReaderScreen(
  viewModel: StoryViewModel,
  uiState: UiState
) {
  val story = uiState.currentStory ?: return
  val currentChapterIndex = uiState.activeChapterIndex.coerceIn(0, (story.chapters.size - 1).coerceAtLeast(0))
  val currentChapter: Chapter? = story.chapters.getOrNull(currentChapterIndex)
  val playbackState by viewModel.playbackState.collectAsState()

  val theme = uiState.readingTheme
  val isDarkTheme = theme.isDark

  var showAppearancePanel by remember { mutableStateOf(false) }
  var showShareSheet by remember { mutableStateOf(false) }
  var customContinuationInput by remember { mutableStateOf("") }

  val chosenFontFamily = when (uiState.readingFontOption) {
    ReadingFontOption.SERIF -> FontFamily.Serif
    ReadingFontOption.SANS_SERIF -> FontFamily.SansSerif
    ReadingFontOption.MONOSPACE -> FontFamily.Monospace
  }

  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Track user reading progress across scrollable content
  val readingProgress by remember {
    derivedStateOf {
      if (scrollState.maxValue > 0) {
        (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
      } else {
        0f
      }
    }
  }
  val animatedReadingProgress by animateFloatAsState(
    targetValue = readingProgress,
    animationSpec = tween(durationMillis = 120),
    label = "reading_progress_anim"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = story.title,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(if (isDarkTheme) theme.textColor else 0xFF1C1B1F)
              ),
              maxLines = 1
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${story.genre.label} • ${story.chapters.size} ${if (story.chapters.size == 1) "Ch" else "Chs"}",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(if (isDarkTheme) theme.subtitleColor else 0xFF49454F),
                  fontSize = 11.sp
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(theme.cardColor),
                modifier = Modifier.padding(vertical = 1.dp)
              ) {
                Text(
                  text = "💾 Room Offline",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(theme.accentColor),
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
              }
            }
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.setScreen(AppScreen.HOME) },
            modifier = Modifier.testTag("reader_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to home",
              tint = Color(if (isDarkTheme) theme.textColor else 0xFF1C1B1F)
            )
          }
        },
        actions = {
          // Share Story Summary & Link Button
          IconButton(
            onClick = { showShareSheet = true },
            modifier = Modifier.testTag("reader_share_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share story summary or link",
              tint = Color(theme.accentColor)
            )
          }
          // Export Story as PDF Button
          IconButton(
            onClick = { viewModel.exportStoryPdf(context, story) },
            enabled = !uiState.isExportingPdf,
            modifier = Modifier.testTag("reader_export_pdf_button")
          ) {
            if (uiState.isExportingPdf) {
              CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color(theme.accentColor),
                modifier = Modifier.size(18.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "Export story as PDF",
                tint = Color(theme.accentColor)
              )
            }
          }
          // Character Gallery Button
          IconButton(
            onClick = { viewModel.toggleCharacterGallery(true) },
            modifier = Modifier.testTag("reader_character_gallery_button")
          ) {
            Box {
              Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Character Gallery",
                tint = Color(theme.accentColor)
              )
              if (story.characters.isNotEmpty()) {
                Surface(
                  shape = CircleShape,
                  color = Primary,
                  modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.TopEnd)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = "${story.characters.size}",
                      fontSize = 8.sp,
                      color = Color.White,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              }
            }
          }
          // Reading Theme / Appearance Quick Switcher Button
          IconButton(
            onClick = { showAppearancePanel = !showAppearancePanel },
            modifier = Modifier.testTag("reading_theme_toggle_button")
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = "Reading Themes & Comfort",
              tint = if (showAppearancePanel) Color(theme.accentColor) else Color(if (isDarkTheme) theme.textColor else 0xFF49454F)
            )
          }
          IconButton(
            onClick = { viewModel.toggleFavorite() },
            modifier = Modifier.testTag("bookmark_button")
          ) {
            Icon(
              imageVector = if (story.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = if (story.isFavorite) "Bookmarked story" else "Bookmark story",
              tint = if (story.isFavorite) Color(theme.accentColor) else Color(if (isDarkTheme) theme.subtitleColor else 0xFF49454F)
            )
          }
          IconButton(
            onClick = { viewModel.setScreen(AppScreen.CHAT) },
            modifier = Modifier.testTag("chat_story_weaver_button")
          ) {
            Icon(
              imageVector = Icons.Default.ChatBubble,
              contentDescription = "Discuss with Story Weaver",
              tint = Color(theme.accentColor)
            )
          }
          IconButton(
            onClick = { viewModel.toggleSettingsDialog(true) },
            modifier = Modifier.testTag("reader_settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Voice & Settings",
              tint = Color(if (isDarkTheme) theme.subtitleColor else 0xFF49454F)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(theme.backgroundColor))
      )
    },
    containerColor = Color(theme.backgroundColor)
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Visual Reading Progress Bar at the top of the Story Screen
      Surface(
        color = Color(theme.cardColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(theme.dividerColor)),
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("reading_progress_header_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = Color(theme.accentColor),
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (story.chapters.size > 1) "Chapter ${currentChapterIndex + 1} of ${story.chapters.size}" else "Story Progress",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(theme.textColor)
              )
            }
            Text(
              text = "${(animatedReadingProgress * 100).toInt()}% read",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(theme.accentColor),
              modifier = Modifier.testTag("reading_progress_percent_text")
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { animatedReadingProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(5.dp)
              .clip(RoundedCornerShape(3.dp))
              .testTag("visual_reading_progress_bar"),
            color = Color(theme.accentColor),
            trackColor = Color(theme.dividerColor).copy(alpha = if (isDarkTheme) 0.35f else 0.25f)
          )
        }
      }

      // Expandable Appearance Quick Panel
      AnimatedVisibility(
        visible = showAppearancePanel,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Surface(
          shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
          color = Color(theme.cardColor),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)),
          shadowElevation = 8.dp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("appearance_quick_panel")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Reading Comfort & Themes",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(theme.textColor)
                )
              )
              Text(
                text = "Font: ${uiState.readingFontSize.toInt()}sp",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = Color(theme.subtitleColor)
                )
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reading Theme Badges / Chips
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              ReadingTheme.values().forEach { readingThemeOption ->
                val isSelected = uiState.readingTheme == readingThemeOption
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(readingThemeOption.backgroundColor),
                  border = if (isSelected) {
                    androidx.compose.foundation.BorderStroke(2.dp, Color(theme.accentColor))
                  } else {
                    androidx.compose.foundation.BorderStroke(1.dp, Color(readingThemeOption.dividerColor))
                  },
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.setReadingTheme(readingThemeOption) }
                    .testTag("quick_theme_${readingThemeOption.name.lowercase()}")
                ) {
                  Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Text(text = readingThemeOption.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = readingThemeOption.label.substringBefore(" "),
                      fontSize = 10.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      color = Color(readingThemeOption.textColor),
                      maxLines = 1
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Font Size & Typography Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                IconButton(
                  onClick = {
                    val newSize = (uiState.readingFontSize - 1.5f).coerceAtLeast(13f)
                    viewModel.setReadingFontSize(newSize)
                  },
                  modifier = Modifier
                    .size(34.dp)
                    .testTag("decrease_font_size_button")
                ) {
                  Text(
                    text = "A-",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(theme.textColor)
                  )
                }

                IconButton(
                  onClick = {
                    val newSize = (uiState.readingFontSize + 1.5f).coerceAtMost(26f)
                    viewModel.setReadingFontSize(newSize)
                  },
                  modifier = Modifier
                    .size(34.dp)
                    .testTag("increase_font_size_button")
                ) {
                  Text(
                    text = "A+",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(theme.textColor)
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ReadingFontOption.values().forEach { fontOpt ->
                  val isSelected = uiState.readingFontOption == fontOpt
                  FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setReadingFontOption(fontOpt) },
                    label = {
                      Text(
                        text = fontOpt.label,
                        fontSize = 11.sp,
                        fontFamily = when (fontOpt) {
                          ReadingFontOption.SERIF -> FontFamily.Serif
                          ReadingFontOption.SANS_SERIF -> FontFamily.SansSerif
                          ReadingFontOption.MONOSPACE -> FontFamily.Monospace
                        }
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = Color(theme.accentColor),
                      selectedLabelColor = if (isDarkTheme) Color.Black else Color.White,
                      containerColor = Color(theme.backgroundColor),
                      labelColor = Color(theme.textColor)
                    ),
                    modifier = Modifier.testTag("quick_font_${fontOpt.label.lowercase()}")
                  )
                }
              }
            }
          }
        }
      }

      // Main Scrollable Story Area
      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        // Chapter Switcher Tabs (if more than 1 chapter)
        if (story.chapters.size > 1) {
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
          ) {
            story.chapters.forEachIndexed { idx, chap ->
              val isSelected = idx == currentChapterIndex
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(theme.accentColor) else Color(theme.cardColor),
                border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)) else null,
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { viewModel.setActiveChapter(idx) }
                  .testTag("chapter_tab_$idx")
              ) {
                Text(
                  text = "Ch. ${idx + 1}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = if (isSelected) {
                    if (isDarkTheme) Color.Black else Color.White
                  } else {
                    Color(theme.textColor)
                  },
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        // Hero Visual Artwork Card (Vibrant Palette 4:3 with 32dp rounded)
        Card(
          shape = RoundedCornerShape(32.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E1B24) else Color(0xFFE6E1E5)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .shadow(4.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .testTag("story_hero_image_card")
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            val base64Img = currentChapter?.imageBase64
            if (!base64Img.isNullOrBlank()) {
              val bitmap = remember(base64Img) {
                try {
                  val decodedBytes = android.util.Base64.decode(base64Img, android.util.Base64.DEFAULT)
                  BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                } catch (e: Exception) {
                  null
                }
              }
              if (bitmap != null) {
                Image(
                  bitmap = bitmap,
                  contentDescription = currentChapter?.title ?: "Story illustration",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                FallbackArtworkBanner(currentChapter?.title)
              }
            } else {
              FallbackArtworkBanner(currentChapter?.title)
            }

            // Dark gradient overlay for bottom text contrast
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0x99000000)),
                    startY = 150f
                  )
                )
            )

            // Chapter tag at bottom left
            Column(
              modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
              Text(
                text = currentChapter?.title ?: "Chapter 1",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    blurRadius = 4f
                  )
                )
              )
              Text(
                text = "Resolution: ${story.imageSize.label} (${story.imageSize.resolution})",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFFEADDFF),
                  fontSize = 11.sp
                )
              )
            }

            // Image generating badge
            if (uiState.isGeneratingImage) {
              Surface(
                shape = CircleShape,
                color = Color(0xCC000000),
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(14.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Painting...",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cast & Character Quick Bar
        if (story.characters.isNotEmpty()) {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = Color(theme.cardColor)
            ),
            border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)) else null,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .clickable { viewModel.toggleCharacterGallery(true) }
              .testTag("reader_quick_cast_bar")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                tint = Color(theme.accentColor),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Cast (${story.characters.size}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(theme.accentColor)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                story.characters.take(3).forEach { char ->
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.backgroundColor)
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(text = char.emoji, fontSize = 11.sp)
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = char.name.split(" ").first(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(theme.textColor)
                      )
                    }
                  }
                }
                if (story.characters.size > 3) {
                  Text(
                    text = "+${story.characters.size - 3}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(theme.subtitleColor)
                  )
                }
              }
              Text(
                text = "View Gallery ›",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(theme.accentColor)
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // Main Story Text Card with Dynamic Theme (Night mode: dark bg + soft amber text)
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = Color(theme.cardColor)
          ),
          border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.5.dp, Color(theme.dividerColor)) else null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("story_content_card")
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            // Header row with Prompt metadata
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "CURRENT PROMPT",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(theme.accentColor)
                  )
                )
                Text(
                  text = "\"${story.prompt.take(60)}${if (story.prompt.length > 60) "..." else ""}\"",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = Color(theme.subtitleColor)
                  )
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = CircleShape,
                color = if (isDarkTheme) Color(theme.dividerColor) else PrimaryContainer,
                modifier = Modifier.padding(2.dp)
              ) {
                Text(
                  text = story.length.label,
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(if (isDarkTheme) theme.textColor else 0xFF21005D),
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(theme.dividerColor))
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Story Text styled with Theme & Custom Typography
            Text(
              text = currentChapter?.content ?: "Once upon a time in a faraway realm...",
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = uiState.readingFontSize.sp,
                lineHeight = (uiState.readingFontSize * 1.65f).sp,
                color = Color(theme.textColor),
                fontFamily = chosenFontFamily
              ),
              modifier = Modifier.testTag("story_chapter_text")
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Continuation Section ("Will there be a continuation?")
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E1B28) else Color(0xFFEADDFF)
          ),
          border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)) else null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("continuation_section_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(if (isDarkTheme) theme.accentColor else 0xFF21005D),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Will there be a continuation?",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(if (isDarkTheme) theme.textColor else 0xFF21005D)
                )
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Choose a branching path or enter your own idea to weave Chapter ${story.chapters.size + 1}:",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(if (isDarkTheme) theme.subtitleColor else 0xFF381E72)
              )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Suggested Branches
            val continuationOpts = currentChapter?.continuationOptions ?: emptyList()
            continuationOpts.forEachIndexed { index, option ->
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isDarkTheme) Color(0xFF282333) else Color.White
                ),
                border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)) else null,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    viewModel.continueStory(option.branchTitle, option.teaser)
                  }
                  .testTag("continuation_option_$index")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${index + 1}. ${option.branchTitle}",
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(if (isDarkTheme) theme.textColor else 0xFF6750A4)
                      )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = option.teaser,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(if (isDarkTheme) theme.subtitleColor else 0xFF49454F),
                        fontSize = 12.sp
                      )
                    )
                  }
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Choose option",
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom continuation input
            OutlinedTextField(
              value = customContinuationInput,
              onValueChange = { customContinuationInput = it },
              placeholder = {
                Text(
                  "Or write your own idea for next chapter...",
                  fontSize = 13.sp,
                  color = Color(if (isDarkTheme) theme.subtitleColor else 0xFF79747E)
                )
              },
              trailingIcon = {
                if (customContinuationInput.isNotBlank()) {
                  IconButton(
                    onClick = {
                      viewModel.continueStory("Custom Chapter", customContinuationInput)
                      customContinuationInput = ""
                    },
                    modifier = Modifier.testTag("send_custom_continuation_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Send,
                      contentDescription = "Weave custom chapter",
                      tint = Color(theme.accentColor)
                    )
                  }
                }
              },
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkTheme) Color(0xFF282333) else Color.White,
                unfocusedContainerColor = if (isDarkTheme) Color(0xFF282333) else Color.White,
                focusedBorderColor = Color(theme.accentColor),
                unfocusedBorderColor = Color(theme.dividerColor),
                focusedTextColor = Color(theme.textColor),
                unfocusedTextColor = Color(theme.textColor)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_continuation_input")
            )
          }
        }

        // Character Gallery & Cast Dossiers Card
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1E1A29) else Color(0xFFF3EDF7)
          ),
          border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)) else null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("reader_character_gallery_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                shape = CircleShape,
                color = Color(theme.accentColor).copy(alpha = if (isDarkTheme) 0.25f else 0.15f),
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Character Gallery & Cast",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(theme.textColor)
                  )
                )
                Text(
                  text = if (story.characters.isNotEmpty())
                    "${story.characters.size} characters profiled with visual looks & traits"
                  else
                    "Automated character profiling powered by Gemini AI",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(theme.subtitleColor),
                    fontSize = 11.sp
                  )
                )
              }
            }

            if (story.characters.isNotEmpty()) {
              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                story.characters.take(3).forEach { char ->
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkTheme) Color(0xFF282333) else Color.White,
                    modifier = Modifier.weight(1f)
                  ) {
                    Column(
                      modifier = Modifier.padding(8.dp),
                      horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                      Text(text = char.emoji, fontSize = 20.sp)
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = char.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(theme.textColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                      )
                      Text(
                        text = char.role,
                        fontSize = 9.sp,
                        color = Color(theme.subtitleColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = { viewModel.toggleCharacterGallery(true) },
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(theme.accentColor),
                contentColor = if (isDarkTheme) Color.Black else Color.White
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("open_character_gallery_button")
            ) {
              Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (story.characters.isEmpty()) "Scan & View Characters" else "Explore All Character Dossiers",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Share Story & Export PDF Card
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF1F1D2B) else Color(0xFFF2ECFA)
          ),
          border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)) else null,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("export_pdf_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                shape = CircleShape,
                color = Color(theme.accentColor).copy(alpha = if (isDarkTheme) 0.25f else 0.15f),
                modifier = Modifier.size(40.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Share Tale & Export",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(theme.textColor)
                  )
                )
                Text(
                  text = "Share summary & link or export illustrated PDF storybook",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(theme.subtitleColor),
                    fontSize = 11.sp
                  )
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Share Summary & Link Button
              Button(
                onClick = { showShareSheet = true },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(theme.accentColor),
                  contentColor = if (isDarkTheme) Color.Black else Color.White
                ),
                modifier = Modifier
                  .weight(1.2f)
                  .height(44.dp)
                  .testTag("open_share_sheet_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = null,
                  modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Story", fontWeight = FontWeight.Bold)
              }

              // Export PDF Button
              OutlinedButton(
                onClick = { viewModel.exportStoryPdf(context, story) },
                enabled = !uiState.isExportingPdf,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(theme.accentColor)),
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .testTag("export_and_share_pdf_button")
              ) {
                if (uiState.isExportingPdf) {
                  CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = Color(theme.accentColor),
                    modifier = Modifier.size(16.dp)
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(17.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("PDF", fontWeight = FontWeight.Bold, color = Color(theme.accentColor))
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }

      // Bottom Sticky Audio Player & Action Bar
      Surface(
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = if (isDarkTheme) Color(0xFF18161F) else Color.White,
        shadowElevation = 8.dp,
        border = if (isDarkTheme) androidx.compose.foundation.BorderStroke(1.dp, Color(theme.dividerColor)) else null,
        modifier = Modifier
          .fillMaxWidth()
          .shadow(8.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
          // Audio Player Container
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isDarkTheme) Color(0xFF24202F) else Color(0xFFE8DEF8),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Play / Pause Circle Button
              Surface(
                shape = CircleShape,
                color = Color(theme.accentColor),
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .clickable {
                    viewModel.toggleAudioNarration()
                  }
                  .testTag("audio_play_pause_button")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  if (uiState.isGeneratingAudio) {
                    CircularProgressIndicator(
                      color = if (isDarkTheme) Color.Black else Color.White,
                      strokeWidth = 2.dp,
                      modifier = Modifier.size(24.dp)
                    )
                  } else {
                    Icon(
                      imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                      contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                      tint = if (isDarkTheme) Color.Black else Color.White,
                      modifier = Modifier.size(28.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              // Audio progress and Voice info
              Column(modifier = Modifier.weight(1f)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Narrator: ${uiState.selectedVoiceProfile.displayName}",
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = Color(theme.textColor)
                    )
                  )
                  Text(
                    text = formatTime(playbackState.currentPositionSeconds) + " / " + formatTime(playbackState.totalDurationSeconds),
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = Color(theme.subtitleColor)
                    )
                  )
                }

                // Slider / Progress bar
                val progressFraction = if (playbackState.totalDurationSeconds > 0) {
                  (playbackState.currentPositionSeconds / playbackState.totalDurationSeconds).coerceIn(0f, 1f)
                } else 0f

                Slider(
                  value = progressFraction,
                  onValueChange = { viewModel.seekAudio(it) },
                  colors = SliderDefaults.colors(
                    thumbColor = Color(theme.accentColor),
                    activeTrackColor = Color(theme.accentColor),
                    inactiveTrackColor = Color(theme.dividerColor)
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .testTag("audio_progress_slider")
                )
              }

              // Speed badge
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDarkTheme) Color(theme.dividerColor) else PrimaryContainer,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable { viewModel.toggleSettingsDialog(true) }
                  .testTag("audio_speed_badge")
              ) {
                Text(
                  text = "${uiState.playbackSpeed}x",
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = Color(if (isDarkTheme) theme.textColor else 0xFF21005D),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Bottom Action Buttons: "New Story" & "Continue Story"
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.setScreen(AppScreen.HOME) },
              shape = RoundedCornerShape(28.dp),
              modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("reader_new_story_button")
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(theme.accentColor),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "New Story",
                fontWeight = FontWeight.Bold,
                color = Color(theme.accentColor)
              )
            }

            Button(
              onClick = {
                val nextBranch = currentChapter?.continuationOptions?.firstOrNull()?.branchTitle ?: "Next Adventure"
                viewModel.continueStory(nextBranch)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(theme.accentColor),
                contentColor = if (isDarkTheme) Color.Black else Color.White
              ),
              shape = RoundedCornerShape(28.dp),
              modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .testTag("reader_continue_story_button")
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (isDarkTheme) Color.Black else Color.White,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Continue Story",
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.Black else Color.White
              )
            }
          }
        }
      }
    }
  }

  // Share Story Bottom Sheet
  if (showShareSheet) {
    ShareStoryBottomSheet(
      story = story,
      onDismiss = { showShareSheet = false },
      onExportPdf = { viewModel.exportStoryPdf(context, story) }
    )
  }

  // Character Gallery Bottom Sheet
  if (uiState.isCharacterGalleryVisible) {
    CharacterGallerySheet(
      viewModel = viewModel
    )
  }
}

@Composable
private fun FallbackArtworkBanner(title: String?) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_pencil_story_art),
      contentDescription = "Pencil drawing illustration for story: ${title ?: "Story"}",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )

    // Subtle dark vignette gradient overlay
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0x77000000)),
            startY = 80f
          )
        )
    )

    // Pencil Sketch Artistic Tag at the Top Left
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = Color(0xDD1C1B1F),
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(12.dp)
        .testTag("pencil_sketch_artwork_badge")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "✏️", fontSize = 11.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Pencil Sketch Illustration",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFF3EDF7)
        )
      }
    }
  }
}

private fun formatTime(seconds: Float): String {
  val total = seconds.toInt().coerceAtLeast(0)
  val m = total / 60
  val s = total % 60
  return String.format("%d:%02d", m, s)
}
