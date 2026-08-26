package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StoryCharacter
import com.example.ui.StoryViewModel
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.Secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterGallerySheet(
  viewModel: StoryViewModel,
  onDismiss: () -> Unit = {}
) {
  val uiState by viewModel.uiState.collectAsState()
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = {
      onDismiss()
      viewModel.toggleCharacterGallery(false)
    },
    sheetState = sheetState,
    containerColor = Color(0xFFFCF8FF),
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    modifier = Modifier.testTag("character_gallery_sheet")
  ) {
    CharacterGalleryContent(
      viewModel = viewModel,
      onClose = {
        onDismiss()
        viewModel.toggleCharacterGallery(false)
      }
    )
  }
}

@Composable
fun CharacterGalleryContent(
  viewModel: StoryViewModel,
  onClose: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val story = uiState.currentStory
  val characters = story?.characters ?: emptyList()
  val isExtracting = uiState.isExtractingCharacters
  val context = LocalContext.current

  var searchQuery by remember { mutableStateOf("") }
  var selectedRoleFilter by remember { mutableStateOf("All") }

  val filteredCharacters = characters.filter { char ->
    val matchesSearch = searchQuery.isBlank() ||
      char.name.contains(searchQuery, ignoreCase = true) ||
      char.appearance.contains(searchQuery, ignoreCase = true) ||
      char.personality.contains(searchQuery, ignoreCase = true) ||
      char.summary.contains(searchQuery, ignoreCase = true)

    val matchesRole = selectedRoleFilter == "All" ||
      char.role.contains(selectedRoleFilter, ignoreCase = true)

    matchesSearch && matchesRole
  }

  val availableRoles = remember(characters) {
    listOf("All") + characters.map { it.role.split("/").first().trim() }.distinct()
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .fillMaxHeight(0.92f)
      .padding(horizontal = 20.dp)
  ) {
    // Top Bar / Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
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
          modifier = Modifier.size(40.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = Primary,
              modifier = Modifier.size(22.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Character Gallery",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1B1F)
              )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = PrimaryContainer,
              modifier = Modifier.padding(vertical = 2.dp)
            ) {
              Text(
                text = "${characters.size} cast",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = story?.title ?: "Current Story",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color(0xFF49454F),
              fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // AI Refresh Button
        IconButton(
          onClick = { viewModel.extractCharactersForCurrentStory(forceAiScan = true) },
          enabled = !isExtracting,
          modifier = Modifier.testTag("rescan_characters_button")
        ) {
          if (isExtracting) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = Primary
            )
          } else {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "Scan with AI",
              tint = Primary
            )
          }
        }

        if (onClose != null) {
          IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_character_gallery_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF49454F)
            )
          }
        }
      }
    }

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search by name, look, traits...", fontSize = 13.sp) },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = Color(0xFF79747E),
          modifier = Modifier.size(18.dp)
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { searchQuery = "" }) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Clear",
              modifier = Modifier.size(16.dp)
            )
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFF3EDF7),
        focusedBorderColor = Primary,
        unfocusedBorderColor = Color.Transparent
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("character_search_input")
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Role Filter Chips
    if (availableRoles.size > 1) {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(availableRoles) { role ->
          FilterChip(
            selected = selectedRoleFilter == role,
            onClick = { selectedRoleFilter = role },
            label = {
              Text(
                text = role,
                fontSize = 12.sp,
                fontWeight = if (selectedRoleFilter == role) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Primary,
              selectedLabelColor = Color.White,
              containerColor = Color(0xFFF3EDF7),
              labelColor = Color(0xFF49454F)
            ),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    // AI Extraction status banner
    AnimatedVisibility(visible = isExtracting) {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp)
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = Primary
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Analyzing story chapters and profiling characters with Gemini AI...",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF21005D)
          )
        }
      }
    }

    // Characters List
    if (filteredCharacters.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(24.dp)
        ) {
          Text(text = "🎭", fontSize = 48.sp)
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = if (characters.isEmpty()) "No Characters Profiled Yet" else "No matching characters found",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1C1B1F)
            ),
            textAlign = TextAlign.Center
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = if (characters.isEmpty())
              "Tap 'Scan with AI' to automatically summarize all characters, appearances, and traits mentioned in your story!"
            else
              "Try clearing your search term or selecting another role filter.",
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color(0xFF49454F),
              textAlign = TextAlign.Center
            )
          )
          if (characters.isEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = { viewModel.extractCharactersForCurrentStory(forceAiScan = true) },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
              Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Scan Characters with AI")
            }
          }
        }
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(filteredCharacters, key = { it.id }) { character ->
          CharacterDossierCard(
            character = character,
            onSpeak = { viewModel.speakCharacterBio(character) },
            onCopy = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText(
                "Character Bio: ${character.name}",
                "${character.name} (${character.role})\n\nAppearance: ${character.appearance}\n\nTraits: ${character.personality}\n\nSummary: ${character.summary}"
              )
              clipboard.setPrimaryClip(clip)
              Toast.makeText(context, "Copied ${character.name}'s bio to clipboard", Toast.LENGTH_SHORT).show()
            }
          )
        }
      }
    }
  }
}

@Composable
fun CharacterDossierCard(
  character: StoryCharacter,
  onSpeak: () -> Unit,
  onCopy: () -> Unit,
  modifier: Modifier = Modifier
) {
  val roleColor = when {
    character.role.contains("Protagonist", true) -> Color(0xFF6750A4)
    character.role.contains("Companion", true) || character.role.contains("Ally", true) -> Color(0xFF2E7D32)
    character.role.contains("Mentor", true) || character.role.contains("Guide", true) -> Color(0xFF0288D1)
    character.role.contains("Antagonist", true) || character.role.contains("Villain", true) -> Color(0xFFC2185B)
    character.role.contains("Creature", true) || character.role.contains("Dragon", true) || character.role.contains("Beast", true) -> Color(0xFFE65100)
    else -> Color(0xFF5C6BC0)
  }

  val roleBgColor = roleColor.copy(alpha = 0.12f)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .testTag("character_card_${character.name.replace(" ", "_")}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Avatar, Name, Role, Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Character Avatar Badge
        Surface(
          shape = CircleShape,
          color = roleBgColor,
          border = androidx.compose.foundation.BorderStroke(2.dp, roleColor.copy(alpha = 0.4f)),
          modifier = Modifier.size(54.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = character.emoji,
              fontSize = 28.sp
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = character.name,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color(0xFF1C1B1F)
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = roleBgColor
            ) {
              Text(
                text = character.role,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = roleColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF3EDF7)
            ) {
              Text(
                text = "Ch ${character.firstAppearedChapter}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF49454F),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        // Action Buttons: Speak & Copy
        Row {
          IconButton(
            onClick = onSpeak,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = "Hear Character Bio",
              tint = Primary,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onCopy,
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy Bio",
              tint = Color(0xFF79747E),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Visual Appearance Section
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F4FA),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Face,
              contentDescription = null,
              tint = Primary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Visual Appearance & Look",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Primary
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = character.appearance,
            style = MaterialTheme.typography.bodySmall.copy(
              color = Color(0xFF313033),
              lineHeight = 16.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Traits & Personality
      if (character.personality.isNotBlank()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = Secondary,
            modifier = Modifier
              .size(16.dp)
              .padding(top = 2.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "Personality & Demeanor",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Secondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = character.personality,
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF49454F),
                lineHeight = 16.sp
              )
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // Story Summary
      Text(
        text = character.summary,
        style = MaterialTheme.typography.bodySmall.copy(
          color = Color(0xFF1D1B20),
          lineHeight = 17.sp
        )
      )

      // Memorable Quote (if present)
      if (!character.quote.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFFF8E1),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.FormatQuote,
              contentDescription = null,
              tint = Color(0xFFF57F17),
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "\"${character.quote}\"",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF5D4037),
                fontStyle = FontStyle.Italic,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }
  }
}
