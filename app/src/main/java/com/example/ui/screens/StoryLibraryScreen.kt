package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Story
import com.example.ui.AppScreen
import com.example.ui.StoryViewModel
import com.example.ui.UiState
import com.example.ui.theme.Background
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.SurfaceVariant

enum class LibraryFilter {
  ALL,
  BOOKMARKED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryLibraryScreen(
  viewModel: StoryViewModel,
  uiState: UiState
) {
  val savedStories by viewModel.savedStories.collectAsState()
  var currentFilter by remember { mutableStateOf(LibraryFilter.ALL) }
  var searchQuery by remember { mutableStateOf("") }
  var storyToShare by remember { mutableStateOf<Story?>(null) }

  val bookmarkedCount = savedStories.count { it.isFavorite }
  val filteredStories = savedStories.filter { story ->
    val matchesFilter = when (currentFilter) {
      LibraryFilter.ALL -> true
      LibraryFilter.BOOKMARKED -> story.isFavorite
    }
    val matchesSearch = searchQuery.isBlank() ||
      story.title.contains(searchQuery, ignoreCase = true) ||
      story.prompt.contains(searchQuery, ignoreCase = true) ||
      story.genre.label.contains(searchQuery, ignoreCase = true)
    matchesFilter && matchesSearch
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CollectionsBookmark,
              contentDescription = null,
              tint = Primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Story Vault",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
              )
            )
          }
        },
        navigationIcon = {
          IconButton(
            onClick = { viewModel.setScreen(AppScreen.HOME) },
            modifier = Modifier.testTag("library_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF1C1B1F)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.setScreen(AppScreen.HOME) },
        containerColor = Primary,
        contentColor = Color.White,
        modifier = Modifier.testTag("library_fab_new_story")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Create new story")
      }
    },
    containerColor = Background
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (savedStories.isEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Surface(
            shape = CircleShape,
            color = PrimaryContainer,
            modifier = Modifier.size(80.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.AutoStories,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(40.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "No Stories Written Yet",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1C1B1F)
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Craft your first tale by entering a hint on the home screen!",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color(0xFF49454F)
            )
          )
        }
      } else {
        Column(modifier = Modifier.fillMaxSize()) {
          // Filter Chips and Search Bar
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            // Filter Tabs: All Stories vs Bookmarked
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = currentFilter == LibraryFilter.ALL,
                  onClick = { currentFilter = LibraryFilter.ALL },
                  label = {
                    Text(
                      text = "All Tales (${savedStories.size})",
                      fontWeight = FontWeight.SemiBold
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.CollectionsBookmark,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White,
                    containerColor = Color(0xFFF3EDF7),
                    labelColor = Color(0xFF1C1B1F)
                  ),
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.testTag("filter_all_stories")
                )

                FilterChip(
                  selected = currentFilter == LibraryFilter.BOOKMARKED,
                  onClick = { currentFilter = LibraryFilter.BOOKMARKED },
                  label = {
                    Text(
                      text = "Bookmarked ($bookmarkedCount)",
                      fontWeight = FontWeight.SemiBold
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = Icons.Default.Bookmark,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp)
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF6750A4),
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color(0xFFFFD8E4),
                    containerColor = Color(0xFFF3EDF7),
                    labelColor = Color(0xFF1C1B1F)
                  ),
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.testTag("filter_bookmarked_stories")
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier.padding(start = 4.dp)
              ) {
                Text(
                  text = "💾 Offline",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF2E7D32),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar for quick access
            OutlinedTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              placeholder = {
                Text(
                  text = "Search by title, genre or topic...",
                  fontSize = 13.sp,
                  color = Color(0xFF79747E)
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = null,
                  tint = Color(0xFF79747E),
                  modifier = Modifier.size(18.dp)
                )
              },
              trailingIcon = {
                if (searchQuery.isNotBlank()) {
                  IconButton(onClick = { searchQuery = "" }) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Clear search",
                      tint = Color(0xFF79747E),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              },
              singleLine = true,
              shape = RoundedCornerShape(16.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF3EDF7),
                unfocusedContainerColor = Color(0xFFF3EDF7),
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color(0xFFE6E0E9)
              ),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("story_search_input")
            )
          }

          // Stories List or Empty State for Filter
          val context = LocalContext.current
          if (filteredStories.isEmpty()) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Surface(
                shape = CircleShape,
                color = PrimaryContainer,
                modifier = Modifier.size(64.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = if (currentFilter == LibraryFilter.BOOKMARKED) Icons.Default.BookmarkBorder else Icons.Default.Search,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(32.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = if (currentFilter == LibraryFilter.BOOKMARKED) "No Bookmarked Stories" else "No matching stories found",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF1C1B1F)
                )
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (currentFilter == LibraryFilter.BOOKMARKED) {
                  "Tap the bookmark icon on any story card to save your favorite tales here for quick access."
                } else {
                  "Try changing your search terms or view all stories."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                  color = Color(0xFF49454F)
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(filteredStories, key = { it.id }) { story ->
                StoryVaultCard(
                  story = story,
                  onSelect = { viewModel.selectStory(story) },
                  onShare = { storyToShare = story },
                  onToggleBookmark = { viewModel.toggleStoryBookmark(story) },
                  onExportPdf = { viewModel.exportStoryPdf(context, story) },
                  onDelete = { viewModel.deleteStory(story.id) }
                )
              }
              item {
                Spacer(modifier = Modifier.height(80.dp))
              }
            }
          }
        }
      }
    }
  }

  // Share Story Bottom Sheet
  storyToShare?.let { story ->
    val context = LocalContext.current
    ShareStoryBottomSheet(
      story = story,
      onDismiss = { storyToShare = null },
      onExportPdf = { viewModel.exportStoryPdf(context, story) }
    )
  }
}

@Composable
private fun StoryVaultCard(
  story: Story,
  onSelect: () -> Unit,
  onShare: () -> Unit,
  onToggleBookmark: () -> Unit,
  onExportPdf: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (story.isFavorite) Color(0xFFF6F2FA) else Color(0xFFF3EDF7)
    ),
    border = if (story.isFavorite) {
      androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD0BCFF))
    } else null,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .clickable { onSelect() }
      .testTag("story_card_${story.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = PrimaryContainer
          ) {
            Text(
              text = "${story.genre.icon} ${story.genre.label}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF21005D),
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          if (story.isFavorite) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFFFD8E4)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Bookmark,
                  contentDescription = null,
                  tint = Color(0xFFB3261E),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Favorite",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF601410),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                  )
                )
              }
            }
          }

          if (story.characters.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFEDE7F6)
            ) {
              Text(
                text = "🎭 ${story.characters.size} cast",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6750A4),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Share Summary & Link Button
          IconButton(
            onClick = onShare,
            modifier = Modifier
              .size(36.dp)
              .testTag("share_story_button_${story.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share story",
              tint = Primary,
              modifier = Modifier.size(18.dp)
            )
          }

          // Export Story as PDF Document
          IconButton(
            onClick = onExportPdf,
            modifier = Modifier
              .size(36.dp)
              .testTag("export_pdf_button_${story.id}")
          ) {
            Icon(
              imageVector = Icons.Default.PictureAsPdf,
              contentDescription = "Export story as PDF",
              tint = Primary,
              modifier = Modifier.size(19.dp)
            )
          }

          // Bookmark / Favorite Tag Button
          IconButton(
            onClick = onToggleBookmark,
            modifier = Modifier
              .size(36.dp)
              .testTag("bookmark_story_button_${story.id}")
          ) {
            Icon(
              imageVector = if (story.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = if (story.isFavorite) "Remove bookmark" else "Bookmark story",
              tint = if (story.isFavorite) Color(0xFF6750A4) else Color(0xFF79747E),
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(36.dp)
              .testTag("delete_story_button_${story.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete story",
              tint = Color(0xFF79747E),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = story.title,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1C1B1F)
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      val previewText = story.chapters.firstOrNull()?.content ?: story.prompt
      Text(
        text = previewText,
        style = MaterialTheme.typography.bodySmall.copy(
          color = Color(0xFF49454F),
          lineHeight = 16.sp
        ),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${story.chapters.size} Chapter${if (story.chapters.size == 1) "" else "s"} • Narrator: ${story.voiceProfile.displayName}",
          style = MaterialTheme.typography.labelSmall.copy(
            color = Primary,
            fontWeight = FontWeight.Medium
          )
        )
        Text(
          text = story.imageSize.label,
          style = MaterialTheme.typography.labelSmall.copy(
            color = Color(0xFF79747E)
          )
        )
      }
    }
  }
}
