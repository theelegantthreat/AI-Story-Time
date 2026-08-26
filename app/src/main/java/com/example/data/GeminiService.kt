package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(90, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  val isApiKeyConfigured: Boolean
    get() {
      val key = BuildConfig.GEMINI_API_KEY
      return key.isNotBlank() && key != "MY_GEMINI_API_KEY" && !key.startsWith("placeholder", ignoreCase = true)
    }

  private fun getApiKey(): String {
    return BuildConfig.GEMINI_API_KEY
  }

  suspend fun generateStory(
    prompt: String,
    length: StoryLength,
    genre: StoryGenre,
    previousChapters: List<Chapter> = emptyList(),
    continuationHint: String? = null,
    modelName: String = "gemini-3.5-flash"
  ): StoryGenerationResult = withContext(Dispatchers.IO) {
    val apiKey = getApiKey()
    if (!isApiKeyConfigured) {
      return@withContext generateLocalSampleStory(prompt, length, genre, previousChapters, continuationHint)
    }

    try {
      val isContinuation = previousChapters.isNotEmpty()
      val chapterNum = previousChapters.size + 1

      val systemPrompt = """
        You are a master storyteller and children's/fantasy author for the app 'AI Story Time'.
        Your job is to craft an enchanting, vivid, and deeply engaging story chapter.
        Story Genre: ${genre.label}
        Target word count: around ${length.approximateWords} words.
        
        You MUST respond ONLY with a valid JSON object matching this exact schema:
        {
          "storyTitle": "Catchy and poetic overall story title",
          "chapterTitle": "Chapter $chapterNum: Title for this chapter",
          "storyText": "The actual full story content written with sensory details, dialogue, and emotion. Approximately ${length.approximateWords} words.",
          "imagePrompt": "A highly detailed, vibrant artistic prompt describing the key visual scene of this chapter for an image generator. Mention lighting, colors, art style (e.g. whimsical digital storybook art), and character actions.",
          "continuationOptions": [
            {"branchTitle": "Option 1 short title", "teaser": "A 1-sentence teaser of what could happen next if the reader chooses this."},
            {"branchTitle": "Option 2 short title", "teaser": "A 1-sentence teaser of what could happen next if the reader chooses this."},
            {"branchTitle": "Option 3 short title", "teaser": "A 1-sentence teaser of what could happen next if the reader chooses this."}
          ]
        }
      """.trimIndent()

      val userPromptContent = StringBuilder()
      if (isContinuation) {
        userPromptContent.append("PREVIOUS CHAPTERS RECAP:\n")
        previousChapters.forEach {
          userPromptContent.append("Chapter ${it.chapterIndex}: ${it.title}\n${it.content.take(300)}...\n\n")
        }
        userPromptContent.append("NOW WRITE CHAPTER $chapterNum based on this continuation choice/hint: '${continuationHint ?: "Advance the story dynamically"}'\n")
      } else {
        userPromptContent.append("START A NEW STORY based on this prompt or hint: '$prompt'\n")
      }

      val payload = JSONObject().apply {
        put("system_instruction", JSONObject().apply {
          put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
        })
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", userPromptContent.toString())))
          })
        })
        put("generationConfig", JSONObject().apply {
          put("response_mime_type", "application/json")
          put("temperature", 0.85)
        })
      }

      val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
      val request = Request.Builder()
        .url(requestUrl)
        .post(payload.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (!response.isSuccessful) {
        Log.e("GeminiService", "Story generation error ${response.code}: $responseBody")
        return@withContext generateLocalSampleStory(prompt, length, genre, previousChapters, continuationHint)
      }

      val json = JSONObject(responseBody)
      val candidates = json.optJSONArray("candidates")
      if (candidates != null && candidates.length() > 0) {
        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val rawText = parts.getJSONObject(0).getString("text")

        // Parse story JSON
        val parsed = parseStoryJson(rawText, prompt, genre, chapterNum)
        return@withContext parsed
      } else {
        return@withContext generateLocalSampleStory(prompt, length, genre, previousChapters, continuationHint)
      }

    } catch (e: Exception) {
      Log.e("GeminiService", "Exception in generateStory", e)
      return@withContext generateLocalSampleStory(prompt, length, genre, previousChapters, continuationHint)
    }
  }

  private fun parseStoryJson(
    rawJson: String,
    prompt: String,
    genre: StoryGenre,
    chapterNum: Int
  ): StoryGenerationResult {
    try {
      val cleaned = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
      val obj = JSONObject(cleaned)

      val storyTitle = obj.optString("storyTitle", if (prompt.isNotBlank()) prompt.take(30) else "The Enchanted Tale")
      val chapterTitle = obj.optString("chapterTitle", "Chapter $chapterNum")
      val storyText = obj.optString("storyText", "Once upon a time...")
      val imagePrompt = obj.optString("imagePrompt", "Vibrant storybook illustration of $storyTitle")

      val continuationList = mutableListOf<ContinuationOption>()
      val opts = obj.optJSONArray("continuationOptions")
      if (opts != null) {
        for (i in 0 until opts.length()) {
          val item = opts.getJSONObject(i)
          continuationList.add(
            ContinuationOption(
              branchTitle = item.optString("branchTitle", "Branch ${i + 1}"),
              teaser = item.optString("teaser", "See what happens next...")
            )
          )
        }
      }

      if (continuationList.isEmpty()) {
        continuationList.add(ContinuationOption("Explore the unknown path", "Venture into the mysterious surroundings."))
        continuationList.add(ContinuationOption("Seek help from a friendly ally", "Find an unexpected guide along the way."))
        continuationList.add(ContinuationOption("Uncover the ancient relic", "Inspect the hidden secret right before you."))
      }

      return StoryGenerationResult(
        storyTitle = storyTitle,
        chapterTitle = chapterTitle,
        storyText = storyText,
        imagePrompt = imagePrompt,
        continuationOptions = continuationList
      )
    } catch (e: Exception) {
      Log.e("GeminiService", "Failed to parse story json, extracting text fallback", e)
      return StoryGenerationResult(
        storyTitle = "The Chronicle of $prompt",
        chapterTitle = "Chapter $chapterNum",
        storyText = rawJson,
        imagePrompt = "Storybook fantasy illustration of $prompt",
        continuationOptions = listOf(
          ContinuationOption("Continue the quest", "Follow the trail ahead."),
          ContinuationOption("Face the surprise", "An unexpected visitor appears."),
          ContinuationOption("Discover hidden magic", "Unleash a dormant power.")
        )
      )
    }
  }

  suspend fun generateStoryImage(
    imagePrompt: String,
    imageSize: ImageSize
  ): ImageGenerationResult = withContext(Dispatchers.IO) {
    val apiKey = getApiKey()
    if (!isApiKeyConfigured) {
      return@withContext ImageGenerationResult(null, "No API key configured")
    }

    try {
      // Model requirement: gemini-3-pro-image-preview
      val promptWithStyle = "Children's storybook vibrant illustration, Disney/Pixar magical concept art style, rich cinematic lighting, 8k resolution, crisp details: $imagePrompt"
      
      // Try gemini-3-pro-image-preview / Imagen endpoint
      val payload = JSONObject().apply {
        put("prompt", promptWithStyle)
        put("output_options", JSONObject().apply {
          put("aspect_ratio", "4:3")
          put("image_size", imageSize.resolution) // 1024x1024, 2048x2048, 4096x4096
        })
      }

      val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-image-preview:generateImages?key=$apiKey"
      val request = Request.Builder()
        .url(requestUrl)
        .post(payload.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val json = JSONObject(responseBody)
        val images = json.optJSONArray("generated_images") ?: json.optJSONArray("images")
        if (images != null && images.length() > 0) {
          val firstImg = images.getJSONObject(0)
          val base64 = firstImg.optString("image_bytes")
            .ifBlank { firstImg.optJSONObject("image")?.optString("image_bytes") ?: "" }
          if (base64.isNotBlank()) {
            return@withContext ImageGenerationResult(base64 = base64)
          }
        }
      }

      // Secondary fallback endpoint for Imagen 3
      val imagenPayload = JSONObject().apply {
        put("instances", JSONArray().put(JSONObject().put("prompt", promptWithStyle)))
        put("parameters", JSONObject().apply {
          put("sampleCount", 1)
          put("aspectRatio", "4:3")
        })
      }
      val imagenUrl = "https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:predict?key=$apiKey"
      val imagenReq = Request.Builder()
        .url(imagenUrl)
        .post(imagenPayload.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val imagenResp = client.newCall(imagenReq).execute()
      val imagenBody = imagenResp.body?.string() ?: ""
      if (imagenResp.isSuccessful) {
        val obj = JSONObject(imagenBody)
        val predictions = obj.optJSONArray("predictions")
        if (predictions != null && predictions.length() > 0) {
          val b64 = predictions.getJSONObject(0).optString("bytesBase64Encoded")
          if (b64.isNotBlank()) {
            return@withContext ImageGenerationResult(base64 = b64)
          }
        }
      }

      Log.w("GeminiService", "Image generation returned non-200 or empty: $responseBody")
      return@withContext ImageGenerationResult(null, "Could not generate image")
    } catch (e: Exception) {
      Log.e("GeminiService", "Image generation error", e)
      return@withContext ImageGenerationResult(null, e.localizedMessage)
    }
  }

  suspend fun generateSpeechAudio(
    text: String,
    voiceProfile: VoiceProfile
  ): ByteArray? = withContext(Dispatchers.IO) {
    val apiKey = getApiKey()
    if (!isApiKeyConfigured) return@withContext null

    try {
      // Model requirement: gemini-3.1-flash-tts-preview
      val payload = JSONObject().apply {
        put("contents", JSONArray().put(
          JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", text.take(1200))))
          }
        ))
        put("generationConfig", JSONObject().apply {
          put("response_modalities", JSONArray().put("AUDIO"))
          put("speechConfig", JSONObject().apply {
            put("voiceConfig", JSONObject().apply {
              put("prebuiltVoiceConfig", JSONObject().apply {
                put("voiceName", voiceProfile.id)
              })
            })
          })
        })
      }

      val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-tts-preview:generateContent?key=$apiKey"
      val request = Request.Builder()
        .url(requestUrl)
        .post(payload.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
          val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
          for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)
            val inlineData = part.optJSONObject("inline_data") ?: part.optJSONObject("inlineData")
            if (inlineData != null) {
              val dataB64 = inlineData.optString("data")
              if (dataB64.isNotBlank()) {
                return@withContext android.util.Base64.decode(dataB64, android.util.Base64.DEFAULT)
              }
            }
          }
        }
      }
      return@withContext null
    } catch (e: Exception) {
      Log.e("GeminiService", "TTS generateContent exception", e)
      return@withContext null
    }
  }

  suspend fun chatWithStoryWeaver(
    history: List<ChatMessage>,
    userMessage: String,
    currentStoryContext: String = "",
    modelName: String = "gemini-3.5-flash"
  ): String = withContext(Dispatchers.IO) {
    val apiKey = getApiKey()
    if (!isApiKeyConfigured) {
      return@withContext "I am the Story Weaver! To connect my magical powers, configure your Gemini API key in Secrets. In the meantime, I can brainstorm story plots, character arcs, and exciting continuations with you!"
    }

    try {
      val systemInstruction = """
        You are 'Story Weaver', the whimsical and wise AI storytelling companion for 'AI Story Time'.
        Your role is to help the reader/writer brainstorm exciting story ideas, develop characters, suggest plot twists, rewrite chapters in different tones, or answer questions about their story.
        Be encouraging, creative, magical, and friendly.
        ${if (currentStoryContext.isNotBlank()) "Current Story Context:\n$currentStoryContext" else ""}
      """.trimIndent()

      val contents = JSONArray()
      history.takeLast(10).forEach { msg ->
        if (msg.role != "system") {
          contents.put(JSONObject().apply {
            put("role", if (msg.role == "user") "user" else "model")
            put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
          })
        }
      }
      contents.put(JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
      })

      val payload = JSONObject().apply {
        put("system_instruction", JSONObject().apply {
          put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
        })
        put("contents", contents)
        put("generationConfig", JSONObject().apply {
          put("temperature", 0.8)
        })
      }

      val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
      val request = Request.Builder()
        .url(requestUrl)
        .post(payload.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
          val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
          return@withContext parts.getJSONObject(0).getString("text")
        }
      }
      return@withContext "The story winds whispered softly, but the message got lost in the stars. Please try again!"
    } catch (e: Exception) {
      Log.e("GeminiService", "Chat exception", e)
      return@withContext "A magical disturbance occurred: ${e.localizedMessage}. Please try again."
    }
  }

  private fun generateLocalSampleStory(
    prompt: String,
    length: StoryLength,
    genre: StoryGenre,
    previousChapters: List<Chapter>,
    continuationHint: String?
  ): StoryGenerationResult {
    val chapterNum = previousChapters.size + 1
    val baseSubject = if (prompt.isNotBlank()) prompt else "The Crystal Grove"
    
    val sampleTitle = if (previousChapters.isNotEmpty()) "The Chronicles of $baseSubject" else "The Mystery of $baseSubject"
    val chapterTitle = "Chapter $chapterNum: ${if (continuationHint != null) continuationHint.take(25) else "The Journey Begins"}"
    
    val storyText = """
      The starlight filtered through the emerald leaves of the Crystal Grove, reflecting off the glassy branches in a thousand shimmering rainbows. Pip stepped forward, his paws clicking softly on the diamond-dust floor of the ancient woodland.
      
      "Listen," whispered Zephyr, the pocket dragon perched on Pip's shoulder. A faint musical chime reverberated through the mist—not of wind, but of an enchanted music box nestled deep inside the Hollow Oak.
      
      Every step revealed glowing blossoms that opened only under moonlight, releasing scents of honeysuckle and wild stardust. As Pip reached the grove's heart, a circular stone pedestal rose quietly from the moss. Resting atop it was a glowing celestial sphere, pulsing gently in harmony with the woodland chimes.
      
      What secret did this sphere hold, and who had left it here centuries ago? Pip took a deep breath, reaching out his trembling paw...
    """.trimIndent()

    return StoryGenerationResult(
      storyTitle = sampleTitle,
      chapterTitle = chapterTitle,
      storyText = storyText,
      imagePrompt = "A magical glowing forest made of crystal glass trees, emerald foliage, sparkling starlight, cute animal explorer with a tiny shoulder pocket dragon, fantasy storybook art style, highly detailed 3D Pixar render",
      continuationOptions = listOf(
        ContinuationOption("Touch the celestial sphere", "Unlock ancient memories stored within the glowing orb."),
        ContinuationOption("Investigate the music box sound", "Follow the melody deeper into the hollow ancient oak tree."),
        ContinuationOption("Summon the forest guardian", "Use the pocket dragon's flame to signal the spirit of the grove.")
      )
    )
  }

  suspend fun extractCharacters(
    story: Story,
    modelName: String = "gemini-2.5-flash"
  ): List<StoryCharacter> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    val allChaptersText = story.chapters.joinToString("\n\n") { "Chapter ${it.chapterIndex} (${it.title}):\n${it.content}" }

    if (apiKey.isBlank() || apiKey == "YOUR_API_KEY" || allChaptersText.isBlank()) {
      return@withContext extractCharactersLocally(story)
    }

    try {
      val systemPrompt = """
        You are an expert literary character analyst and concept artist.
        Analyze all the chapters of the story and extract a complete character gallery containing every distinct character, companion, mythical creature, mentor, or notable figure mentioned in the text.
        
        For EACH character, provide:
        - name: The character's full name or title
        - role: Character's narrative archetype, e.g. "Protagonist", "Loyal Companion", "Wise Mentor", "Antagonist", "Mythical Creature", "Guiding Spirit", "Supporting"
        - appearance: Rich visual and physical description (fur/scales/metal, eyes, clothing, accessories, distinctive colors, expressions)
        - personality: Behavioral temperament, key quirks, emotional traits
        - summary: 1-2 sentence background summary of their role and motivation in this story
        - emoji: A single expressive emoji matching their identity (e.g. 🐉, 🤖, 🧙, 🦊, 🦉, 👑, 🐳, 👧, 🧚)
        - firstAppearedChapter: Integer of the earliest chapter they appeared in (e.g. 1, 2, 3)
        - quote: A memorable quote spoken by them or an iconic narrative excerpt describing them (or null if none)

        Output ONLY a valid JSON Array conforming to this schema:
        [
          {
            "name": "string",
            "role": "string",
            "appearance": "string",
            "personality": "string",
            "summary": "string",
            "emoji": "string",
            "firstAppearedChapter": 1,
            "quote": "string or null"
          }
        ]
      """.trimIndent()

      val payload = JSONObject().apply {
        put("system_instruction", JSONObject().apply {
          put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
        })
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", "Story Title: ${story.title}\nGenre: ${story.genre.label}\nPrompt: ${story.prompt}\n\nSTORY TEXT:\n$allChaptersText")))
          })
        })
        put("generationConfig", JSONObject().apply {
          put("response_mime_type", "application/json")
          put("temperature", 0.3)
        })
      }

      val requestUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
      val request = Request.Builder()
        .url(requestUrl)
        .post(payload.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string() ?: ""

      if (response.isSuccessful) {
        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
          val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
          val rawJson = parts.getJSONObject(0).getString("text")
          val parsedList = parseCharactersJson(rawJson)
          if (parsedList.isNotEmpty()) {
            return@withContext parsedList
          }
        }
      }
      return@withContext extractCharactersLocally(story)
    } catch (e: Exception) {
      Log.e("GeminiService", "Error extracting characters", e)
      return@withContext extractCharactersLocally(story)
    }
  }

  private fun parseCharactersJson(rawJson: String): List<StoryCharacter> {
    return try {
      val trimmed = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
      val jsonArray = JSONArray(trimmed)
      val list = mutableListOf<StoryCharacter>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val name = obj.optString("name", "Mysterious Character").trim()
        val role = obj.optString("role", "Protagonist").trim()
        val appearance = obj.optString("appearance", "A notable figure in the tale.").trim()
        val personality = obj.optString("personality", "Curious and brave.").trim()
        val summary = obj.optString("summary", "A key figure in this journey.").trim()
        val emoji = obj.optString("emoji", "✨").trim()
        val chapter = obj.optInt("firstAppearedChapter", 1)
        val quote = if (obj.has("quote") && !obj.isNull("quote")) obj.optString("quote") else null

        if (name.isNotBlank()) {
          list.add(
            StoryCharacter(
              name = name,
              role = role,
              appearance = appearance,
              personality = personality,
              summary = summary,
              emoji = if (emoji.isNotBlank()) emoji else "✨",
              firstAppearedChapter = chapter,
              quote = quote
            )
          )
        }
      }
      list
    } catch (e: Exception) {
      Log.e("GeminiService", "Failed to parse characters JSON: $rawJson", e)
      emptyList()
    }
  }

  fun extractCharactersLocally(story: Story): List<StoryCharacter> {
    if (story.characters.isNotEmpty()) {
      return story.characters
    }

    val characters = mutableListOf<StoryCharacter>()
    val combinedText = story.chapters.joinToString(" ") { it.content }

    // Check genre / keyword heuristics
    when (story.genre) {
      StoryGenre.FANTASY -> {
        characters.add(
          StoryCharacter(
            name = if (story.title.contains("Dragon", true)) "The Dragon Protagonist" else "The Arcane Wanderer",
            role = "Protagonist",
            appearance = "Luminous eyes, shimmering robes woven from enchanted threads, and an aura of soft magic.",
            personality = "Inquisitive, courageous, and deeply connected to magical creatures.",
            summary = "The central adventurer navigating enchanted realms and unraveling mythical secrets.",
            emoji = if (story.title.contains("Dragon", true)) "🐉" else "🧙‍♂️",
            firstAppearedChapter = 1
          )
        )
        if (combinedText.contains("dragon", true) || combinedText.contains("fairy", true) || combinedText.contains("spirit", true)) {
          characters.add(
            StoryCharacter(
              name = "The Forest Familiar",
              role = "Loyal Companion",
              appearance = "A miniature winged guardian with glowing iridescent wings and playful eyes.",
              personality = "Loyal, quick-witted, and protective.",
              summary = "Guides the protagonist along uncharted forest paths.",
              emoji = "🧚",
              firstAppearedChapter = 1
            )
          )
        }
      }
      StoryGenre.SCIFI -> {
        characters.add(
          StoryCharacter(
            name = if (combinedText.contains("robot", true) || combinedText.contains("Unit", true)) "Explorer Unit" else "Captain Navigator",
            role = "Protagonist",
            appearance = "Sleek metallic chassis with polished brass accents and sapphire optical sensors.",
            personality = "Analytical, daring, and observant.",
            summary = "Commands deep space explorations across uncharted planetary sectors.",
            emoji = "🤖",
            firstAppearedChapter = 1
          )
        )
      }
      StoryGenre.BEDTIME -> {
        characters.add(
          StoryCharacter(
            name = "The Starlight Guardian",
            role = "Guardian / Guide",
            appearance = "A gentle celestial being glowing with warm pastel starlight and soothing moonlight reflections.",
            personality = "Serene, kind, and deeply calming.",
            summary = "Watches over the nighttime skies and brings peaceful dreams to sleepy worlds.",
            emoji = "🌙",
            firstAppearedChapter = 1
          )
        )
      }
      else -> {
        characters.add(
          StoryCharacter(
            name = "The Main Adventurer",
            role = "Protagonist",
            appearance = "A spirited traveler equipped with a weather-worn satchel and keen, bright eyes.",
            personality = "Brave, resourceful, and kind-hearted.",
            summary = "Embarks on an unexpected quest to discover hidden wonders.",
            emoji = "🌟",
            firstAppearedChapter = 1
          )
        )
      }
    }

    return characters
  }
}

data class StoryGenerationResult(
  val storyTitle: String,
  val chapterTitle: String,
  val storyText: String,
  val imagePrompt: String,
  val continuationOptions: List<ContinuationOption>
)

data class ImageGenerationResult(
  val base64: String?,
  val error: String? = null
)
