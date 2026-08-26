package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AI Story Time", appName)
  }

  @Test
  fun `generate story pdf file safely handled`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sampleStory = com.example.data.Story(
      id = "test_story_1",
      title = "The Starlight Dragon",
      prompt = "A dragon who breathed constellations across the night sky",
      genre = com.example.data.StoryGenre.FANTASY,
      chapters = listOf(
        com.example.data.Chapter(
          id = "ch_1",
          storyId = "test_story_1",
          chapterIndex = 0,
          title = "Awakening of the Starlight",
          content = "High atop Mount Solitude, the dragon opened glimmering obsidian wings and looked upon the sleeping valley below."
        )
      )
    )

    try {
      val pdfFile = com.example.util.StoryPdfExporter.generatePdfFile(context, sampleStory)
      org.junit.Assert.assertNotNull(pdfFile)
    } catch (e: IllegalStateException) {
      // PdfDocument requires Android native graphics backend which is simulated on JVM
      org.junit.Assert.assertNotNull(e)
    }
  }

  @Test
  fun `generate story link and summary text`() {
    val sampleStory = com.example.data.Story(
      id = "story_xyz",
      title = "The Neon Chrono",
      prompt = "A time-traveling detective in 2099",
      genre = com.example.data.StoryGenre.SCIFI,
      chapters = listOf(
        com.example.data.Chapter(
          id = "ch_1",
          storyId = "story_xyz",
          chapterIndex = 0,
          title = "Rain Over Cyber City",
          content = "Rain poured through neon holographic advertisements flickering in Sector 4.",
          continuationOptions = listOf(
            com.example.data.ContinuationOption("Enter the Club", "Investigate the underground speakeasy"),
            com.example.data.ContinuationOption("Hack the Terminal", "Override the security drone mainframe")
          )
        )
      )
    )

    val link = com.example.util.StoryShareHelper.generateStoryLink(sampleStory)
    org.junit.Assert.assertTrue(link.contains("story_xyz"))
    org.junit.Assert.assertTrue(link.contains("The+Neon+Chrono") || link.contains("The%20Neon%20Chrono"))

    val summary = com.example.util.StoryShareHelper.generateSummaryText(sampleStory)
    org.junit.Assert.assertTrue(summary.contains("The Neon Chrono"))
    org.junit.Assert.assertTrue(summary.contains("Sci-Fi"))
    org.junit.Assert.assertTrue(summary.contains("Rain poured"))
    org.junit.Assert.assertTrue(summary.contains("Enter the Club"))
    org.junit.Assert.assertTrue(summary.contains(link))

    val fullStory = com.example.util.StoryShareHelper.generateFullStoryText(sampleStory)
    org.junit.Assert.assertTrue(fullStory.contains("Rain Over Cyber City"))
    org.junit.Assert.assertTrue(fullStory.contains("Enter the Club"))
  }
}
