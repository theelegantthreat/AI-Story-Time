package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class StoryLength(val label: String, val approximateWords: Int, val description: String) {
  SHORT("Short", 180, "~1-2 min read (180 words)"),
  MEDIUM("Medium", 380, "~3-4 min read (380 words)"),
  LONG("Long", 650, "~5-7 min read (650 words)"),
  EPIC("Epic", 1000, "~8-10 min read (1000 words)")
}

enum class ImageSize(val label: String, val resolution: String, val description: String) {
  SIZE_1K("1K", "1024x1024", "Standard High-Definition (1024x1024)"),
  SIZE_2K("2K", "2048x2048", "Ultra Crisp (2048x2048)"),
  SIZE_4K("4K", "4096x4096", "Maximum Detail (4096x4096)")
}

enum class StoryGenre(val label: String, val icon: String, val colorHex: Long) {
  FANTASY("Fantasy & Magic", "✨", 0xFF6750A4),
  SCIFI("Sci-Fi & Cosmic", "🚀", 0xFF0288D1),
  BEDTIME("Calm & Bedtime", "🌙", 0xFF5C6BC0),
  ADVENTURE("Adventure & Quest", "🗺️", 0xFFE65100),
  MYSTERY("Mystery & Secrets", "🔍", 0xFF455A64),
  FAIRYTALE("Fairy Tale", "👑", 0xFFAD1457),
  FUNNY("Humorous & Playful", "🎉", 0xFFF57C00)
}

enum class ReadingTheme(
  val label: String,
  val icon: String,
  val description: String,
  val backgroundColor: Long,
  val cardColor: Long,
  val textColor: Long,
  val subtitleColor: Long,
  val accentColor: Long,
  val dividerColor: Long,
  val isDark: Boolean
) {
  DAYLIGHT(
    label = "Daylight",
    icon = "☀️",
    description = "Crisp lavender & classic light contrast",
    backgroundColor = 0xFFFDF8FF,
    cardColor = 0xFFF3EDF7,
    textColor = 0xFF1D1B20,
    subtitleColor = 0xFF49454F,
    accentColor = 0xFF6750A4,
    dividerColor = 0xFFCAC4D0,
    isDark = false
  ),
  NIGHT(
    label = "Night Mode",
    icon = "🌙",
    description = "Obsidian dark background with soft amber text",
    backgroundColor = 0xFF121214,
    cardColor = 0xFF1E1B24,
    textColor = 0xFFFFE082,
    subtitleColor = 0xFFFFD54F,
    accentColor = 0xFFFFCA28,
    dividerColor = 0xFF3E3846,
    isDark = true
  ),
  PARCHMENT(
    label = "Warm Parchment",
    icon = "📜",
    description = "Warm sepia cream with rich espresso ink",
    backgroundColor = 0xFFFBF3E4,
    cardColor = 0xFFF4E7CE,
    textColor = 0xFF3E2723,
    subtitleColor = 0xFF5D4037,
    accentColor = 0xFF8D6E63,
    dividerColor = 0xFFD7CCC8,
    isDark = false
  ),
  FOREST(
    label = "Midnight Forest",
    icon = "🌲",
    description = "Deep velvet emerald with soothing mint text",
    backgroundColor = 0xFF0D1814,
    cardColor = 0xFF132620,
    textColor = 0xFFC8E6C9,
    subtitleColor = 0xFFA5D6A7,
    accentColor = 0xFF81C784,
    dividerColor = 0xFF244438,
    isDark = true
  ),
  OLED_DARK(
    label = "OLED Pure Dark",
    icon = "🌑",
    description = "True pitch black with soft silver typography",
    backgroundColor = 0xFF000000,
    cardColor = 0xFF141414,
    textColor = 0xFFE6E1E5,
    subtitleColor = 0xFF938F99,
    accentColor = 0xFFD0BCFF,
    dividerColor = 0xFF2C2C2C,
    isDark = true
  )
}

enum class ReadingFontOption(val label: String) {
  SERIF("Serif"),
  SANS_SERIF("Sans"),
  MONOSPACE("Mono")
}

data class VoiceProfile(
  val id: String,
  val displayName: String,
  val description: String,
  val gender: String,
  val pitchMultiplier: Float = 1.0f,
  val baseSpeed: Float = 1.0f
) {
  companion object {
    val ALL = listOf(
      VoiceProfile("Seraphina", "Seraphina", "Warm, expressive & whimsical narrator", "Female", 1.05f, 1.0f),
      VoiceProfile("Puck", "Puck", "Playful, energetic & lively voice", "Male", 1.15f, 1.05f),
      VoiceProfile("Aoede", "Aoede", "Melodic, calm & storybook tone", "Female", 0.95f, 0.95f),
      VoiceProfile("Charon", "Charon", "Deep, epic & legendary storyteller", "Male", 0.82f, 0.92f),
      VoiceProfile("Kore", "Kore", "Gentle, soothing & bedtime voice", "Female", 1.0f, 0.9f),
      VoiceProfile("Fenrir", "Fenrir", "Bold, dramatic & action-packed", "Male", 0.9f, 1.05f),
      VoiceProfile("Zephyr", "Zephyr", "Soft, breezy & friendly narrator", "Neutral", 1.0f, 1.0f),
      VoiceProfile("Jasper", "Jasper", "Wise, grandfatherly & animated", "Male", 0.88f, 0.95f)
    )
    val DEFAULT = ALL[0]
  }
}

data class ContinuationOption(
  val branchTitle: String,
  val teaser: String
)

data class Chapter(
  val id: String,
  val storyId: String,
  val chapterIndex: Int,
  val title: String,
  val content: String,
  val imagePrompt: String = "",
  val imageBase64: String? = null,
  val imageUrl: String? = null,
  val continuationOptions: List<ContinuationOption> = emptyList(),
  val createdAt: Long = System.currentTimeMillis()
)

data class StoryCharacter(
  val id: String = java.util.UUID.randomUUID().toString(),
  val name: String,
  val role: String, // e.g. "Protagonist", "Companion", "Mentor", "Antagonist", "Mythical Creature", "Supporting"
  val appearance: String, // Physical/visual description (colors, features, clothes, gear)
  val personality: String, // Key traits and temperament
  val summary: String, // Background & narrative role in the story
  val emoji: String = "✨",
  val firstAppearedChapter: Int = 1,
  val quote: String? = null
)

@Entity(tableName = "stories")
data class StoryEntity(
  @PrimaryKey val id: String,
  val title: String,
  val prompt: String,
  val genreName: String,
  val lengthName: String,
  val imageSizeName: String,
  val voiceProfileId: String,
  val chaptersJson: String,
  val charactersJson: String = "[]",
  val isFavorite: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val role: String, // "user", "model", "system"
  val text: String,
  val timestamp: Long = System.currentTimeMillis()
)
