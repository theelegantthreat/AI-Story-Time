package com.example

import com.example.data.Chapter
import com.example.data.GeminiService
import com.example.data.ImageSize
import com.example.data.ReadingFontOption
import com.example.data.ReadingTheme
import com.example.data.Story
import com.example.data.StoryCharacter
import com.example.data.StoryGenre
import com.example.data.StoryLength
import com.example.data.StoryRepository
import com.example.data.VoiceProfile
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCharacterModel_andHeuristicExtraction() {
    val geminiService = GeminiService()
    val testStory = Story(
      id = "test-1",
      title = "The Dragon of Frost Peak",
      prompt = "A young brave warrior encounters a friendly frost dragon in ancient caverns.",
      genre = StoryGenre.FANTASY,
      length = StoryLength.MEDIUM,
      imageSize = ImageSize.SQUARE,
      voiceProfile = VoiceProfile.ZEPHYR,
      chapters = listOf(
        Chapter(
          chapterNumber = 1,
          title = "Frozen Echoes",
          content = "Eldrin the brave warrior walked into the shimmering cavern where Frostfang the gentle ancient dragon slept."
        )
      )
    )

    val extracted = geminiService.extractCharactersLocally(testStory)
    assertTrue("Should extract at least one character locally", extracted.isNotEmpty())
    val protagonist = extracted.firstOrNull { it.role.contains("Protagonist", ignoreCase = true) }
    assertNotNull("Should have protagonist", protagonist)
  }

  @Test
  fun testCharacterJsonParsing() {
    val geminiService = GeminiService()
    val testJson = """
      [
        {
          "name": "Nova Sparks",
          "role": "Protagonist / Tinker",
          "appearance": "Copper goggles, teal jumpsuit with utility belts.",
          "personality": "Inquisitive, fiercely loyal, bold.",
          "summary": "An eccentric starship mechanic seeking the core.",
          "quote": "Every broken gear has a story to tell!",
          "emoji": "🔧",
          "firstAppearedChapter": 1
        }
      ]
    """.trimIndent()

    val parsed = geminiService.parseCharactersJson(testJson)
    assertEquals(1, parsed.size)
    assertEquals("Nova Sparks", parsed[0].name)
    assertEquals("Protagonist / Tinker", parsed[0].role)
    assertEquals("🔧", parsed[0].emoji)
    assertEquals(1, parsed[0].firstAppearedChapter)
  }
}

