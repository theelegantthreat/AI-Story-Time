package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.Story

object StoryShareHelper {

  private const val BASE_SHARE_URL = "https://ai.studio/apps/story-weaver"

  /**
   * Generates a web / deep link to view or read the story.
   */
  fun generateStoryLink(story: Story): String {
    val encodedTitle = java.net.URLEncoder.encode(story.title, "UTF-8")
    return "$BASE_SHARE_URL?storyId=${story.id}&title=$encodedTitle&genre=${story.genre.name.lowercase()}"
  }

  /**
   * Generates a rich, text-based summary of the story including metadata, synopsis hook,
   * continuation teasers, and a share link.
   */
  fun generateSummaryText(story: Story): String {
    val link = generateStoryLink(story)
    val sb = StringBuilder()

    sb.appendLine("✨ *${story.title}* ✨")
    sb.appendLine("${story.genre.icon} Genre: ${story.genre.label} | 🎙 Narrator: ${story.voiceProfile.displayName}")
    sb.appendLine("📖 ${story.chapters.size} ${if (story.chapters.size == 1) "Chapter" else "Chapters"}")
    sb.appendLine()

    if (story.prompt.isNotBlank()) {
      sb.appendLine("💡 *Inspiration:* “${story.prompt}”")
      sb.appendLine()
    }

    // Synopsis / Hook from Chapter 1
    val firstChapter = story.chapters.firstOrNull()
    if (firstChapter != null) {
      val excerpt = firstChapter.content
        .split("\n")
        .firstOrNull { it.isNotBlank() }
        ?.take(260) ?: firstChapter.content.take(260)

      val cleanExcerpt = if (firstChapter.content.length > 260) "$excerpt..." else excerpt
      sb.appendLine("📜 *Story Hook:*")
      sb.appendLine("“$cleanExcerpt”")
      sb.appendLine()
    }

    // Continuation options if available in latest chapter
    val latestChapter = story.chapters.lastOrNull()
    if (latestChapter != null && latestChapter.continuationOptions.isNotEmpty()) {
      sb.appendLine("🌟 *Branching Paths / Next Choices:*")
      latestChapter.continuationOptions.forEachIndexed { i, opt ->
        sb.appendLine("  ${i + 1}. ${opt.branchTitle} - ${opt.teaser}")
      }
      sb.appendLine()
    }

    sb.appendLine("🔗 *Read or continue this story here:*")
    sb.appendLine(link)
    sb.appendLine()
    sb.append("Crafted with AI Story Weaver ✨")

    return sb.toString()
  }

  /**
   * Generates the complete formatted text of all story chapters.
   */
  fun generateFullStoryText(story: Story): String {
    val link = generateStoryLink(story)
    val sb = StringBuilder()

    sb.appendLine("══════════════════════════════════════")
    sb.appendLine("  ${story.title.uppercase()}  ")
    sb.appendLine("  ${story.genre.icon} ${story.genre.label} • Narrated by ${story.voiceProfile.displayName}")
    sb.appendLine("══════════════════════════════════════")
    sb.appendLine()

    if (story.prompt.isNotBlank()) {
      sb.appendLine("Prompt: “${story.prompt}”")
      sb.appendLine()
    }

    story.chapters.forEachIndexed { idx, chapter ->
      sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
      sb.appendLine("Chapter ${idx + 1}: ${chapter.title.ifBlank { "Untitled" }}")
      sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
      sb.appendLine()
      sb.appendLine(chapter.content.trim())
      sb.appendLine()

      if (chapter.continuationOptions.isNotEmpty()) {
        sb.appendLine("✦ Continuation Paths:")
        chapter.continuationOptions.forEachIndexed { optIdx, opt ->
          sb.appendLine("  ${optIdx + 1}. ${opt.branchTitle}: ${opt.teaser}")
        }
        sb.appendLine()
      }
    }

    sb.appendLine("══════════════════════════════════════")
    sb.appendLine("Read & create interactive tales at:")
    sb.appendLine(link)
    sb.appendLine("══════════════════════════════════════")

    return sb.toString()
  }

  /**
   * Launches Android Share Intent with story summary and link.
   */
  fun shareSummary(context: Context, story: Story) {
    val text = generateSummaryText(story)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, "Story: ${story.title}")
      putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Share Story Summary & Link")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  }

  /**
   * Launches Android Share Intent with full story text.
   */
  fun shareFullStory(context: Context, story: Story) {
    val text = generateFullStoryText(story)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(Intent.EXTRA_SUBJECT, story.title)
      putExtra(Intent.EXTRA_TEXT, text)
    }
    val chooser = Intent.createChooser(intent, "Share Full Story Text")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
  }

  /**
   * Copies text to system clipboard and shows a helpful Toast.
   */
  fun copyToClipboard(context: Context, text: String, label: String = "Story Link") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    if (clipboard != null) {
      val clip = ClipData.newPlainText(label, text)
      clipboard.setPrimaryClip(clip)
      Toast.makeText(context, "$label copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
  }
}
