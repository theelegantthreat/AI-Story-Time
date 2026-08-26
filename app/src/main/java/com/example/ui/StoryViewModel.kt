package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Chapter
import com.example.data.ChatMessage
import com.example.data.GeminiService
import com.example.data.ImageSize
import com.example.data.ReadingFontOption
import com.example.data.ReadingTheme
import com.example.data.Story
import com.example.data.StoryCharacter
import com.example.data.StoryDatabase
import com.example.data.StoryGenre
import com.example.data.StoryLength
import com.example.data.StoryRepository
import com.example.data.TtsManager
import com.example.data.TtsPlaybackState
import com.example.data.VoiceProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
  HOME,
  READER,
  LIBRARY,
  CHAT
}

data class UiState(
  val currentScreen: AppScreen = AppScreen.HOME,
  val currentStory: Story? = null,
  val activeChapterIndex: Int = 0,
  val promptInput: String = "",
  val selectedGenre: StoryGenre = StoryGenre.FANTASY,
  val selectedLength: StoryLength = StoryLength.MEDIUM,
  val selectedImageSize: ImageSize = ImageSize.SIZE_1K,
  val selectedVoiceProfile: VoiceProfile = VoiceProfile.DEFAULT,
  val playbackSpeed: Float = 1.0f,
  val ttsPitch: Float = 1.0f,
  val readingTheme: ReadingTheme = ReadingTheme.DAYLIGHT,
  val readingFontSize: Float = 17f,
  val readingFontOption: ReadingFontOption = ReadingFontOption.SERIF,
  val isGeneratingStory: Boolean = false,
  val isGeneratingImage: Boolean = false,
  val isGeneratingAudio: Boolean = false,
  val isExportingPdf: Boolean = false,
  val generationStatusText: String = "",
  val errorBanner: String? = null,
  val preferOfflineCache: Boolean = false,
  val isLoadedFromCache: Boolean = false,
  val cacheMatchStory: Story? = null,
  val chatMessages: List<ChatMessage> = listOf(
    ChatMessage(
      role = "model",
      text = "Greetings, storyteller! I am the Story Weaver. What magical tale or daring adventure shall we craft today? You can ask me for plot twists, ideas, or to help develop your characters!"
    )
  ),
  val isChatLoading: Boolean = false,
  val showSettingsDialog: Boolean = false,
  val selectedAiModel: String = "gemini-3.5-flash",
  val isCharacterGalleryVisible: Boolean = false,
  val isExtractingCharacters: Boolean = false,
  val selectedCharacterDetail: StoryCharacter? = null
)

class StoryViewModel(application: Application) : AndroidViewModel(application) {

  private val geminiService = GeminiService()
  private val database = StoryDatabase.getDatabase(application)
  private val repository = StoryRepository(database.storyDao())
  val ttsManager = TtsManager(application)

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  val playbackState: StateFlow<TtsPlaybackState> = ttsManager.playbackState

  val savedStories = repository.getAllStories().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    emptyList()
  )

  val storyCount = repository.getStoryCount().stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    0
  )

  init {
    viewModelScope.launch {
      repository.prepopulateStarterStoriesIfNeeded()
    }
  }

  fun setScreen(screen: AppScreen) {
    _uiState.value = _uiState.value.copy(currentScreen = screen)
  }

  fun updatePromptInput(text: String) {
    _uiState.value = _uiState.value.copy(promptInput = text)
    checkPromptCache(text)
  }

  fun checkPromptCache(prompt: String) {
    if (prompt.trim().length < 3) {
      _uiState.value = _uiState.value.copy(cacheMatchStory = null)
      return
    }
    viewModelScope.launch {
      val match = repository.findCachedStoryByPrompt(prompt)
        ?: repository.findSimilarCachedStory(prompt)
      _uiState.value = _uiState.value.copy(cacheMatchStory = match)
    }
  }

  fun togglePreferOfflineCache(prefer: Boolean) {
    _uiState.value = _uiState.value.copy(preferOfflineCache = prefer)
  }

  fun setGenre(genre: StoryGenre) {
    _uiState.value = _uiState.value.copy(selectedGenre = genre)
  }

  fun setLength(length: StoryLength) {
    _uiState.value = _uiState.value.copy(selectedLength = length)
  }

  fun setImageSize(size: ImageSize) {
    _uiState.value = _uiState.value.copy(selectedImageSize = size)
  }

  fun setVoiceProfile(voice: VoiceProfile) {
    _uiState.value = _uiState.value.copy(selectedVoiceProfile = voice)
    ttsManager.updateSettings(voice, _uiState.value.playbackSpeed, _uiState.value.ttsPitch)
  }

  fun setPlaybackSpeed(speed: Float) {
    _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    ttsManager.updateSettings(_uiState.value.selectedVoiceProfile, speed, _uiState.value.ttsPitch)
  }

  fun setTtsPitch(pitch: Float) {
    _uiState.value = _uiState.value.copy(ttsPitch = pitch)
    ttsManager.updateSettings(_uiState.value.selectedVoiceProfile, _uiState.value.playbackSpeed, pitch)
  }

  fun setAiModel(model: String) {
    _uiState.value = _uiState.value.copy(selectedAiModel = model)
  }

  fun setReadingTheme(theme: ReadingTheme) {
    _uiState.value = _uiState.value.copy(readingTheme = theme)
  }

  fun setReadingFontSize(fontSizeSp: Float) {
    _uiState.value = _uiState.value.copy(readingFontSize = fontSizeSp)
  }

  fun setReadingFontOption(fontOption: ReadingFontOption) {
    _uiState.value = _uiState.value.copy(readingFontOption = fontOption)
  }

  fun toggleSettingsDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showSettingsDialog = show)
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorBanner = null)
  }

  fun startNewStory(customPrompt: String? = null, forceRegenerate: Boolean = false) {
    val promptToUse = customPrompt ?: _uiState.value.promptInput.ifBlank { "A magical forest made of glowing crystal trees" }
    val length = _uiState.value.selectedLength
    val genre = _uiState.value.selectedGenre
    val imgSize = _uiState.value.selectedImageSize
    val voice = _uiState.value.selectedVoiceProfile
    val model = _uiState.value.selectedAiModel
    val preferCache = _uiState.value.preferOfflineCache

    viewModelScope.launch {
      ttsManager.stop()

      // Check local Room cache first if not forced or if cache preference is on
      if (!forceRegenerate || preferCache) {
        val cachedStory = repository.findCachedStoryByPrompt(promptToUse)
          ?: if (preferCache) repository.findSimilarCachedStory(promptToUse) else null

        if (cachedStory != null) {
          _uiState.value = _uiState.value.copy(
            currentStory = cachedStory,
            activeChapterIndex = 0,
            selectedGenre = cachedStory.genre,
            selectedLength = cachedStory.length,
            selectedImageSize = cachedStory.imageSize,
            selectedVoiceProfile = cachedStory.voiceProfile,
            currentScreen = AppScreen.READER,
            isLoadedFromCache = true,
            isGeneratingStory = false,
            generationStatusText = ""
          )
          return@launch
        }
      }

      _uiState.value = _uiState.value.copy(
        isGeneratingStory = true,
        generationStatusText = "Weaving your tale with Gemini AI...",
        errorBanner = null,
        isLoadedFromCache = false
      )

      try {
        val result = geminiService.generateStory(
          prompt = promptToUse,
          length = length,
          genre = genre,
          previousChapters = emptyList(),
          modelName = model
        )

        val storyId = UUID.randomUUID().toString()
        val firstChapter = Chapter(
          id = UUID.randomUUID().toString(),
          storyId = storyId,
          chapterIndex = 1,
          title = result.chapterTitle,
          content = result.storyText,
          imagePrompt = result.imagePrompt,
          continuationOptions = result.continuationOptions
        )

        val newStory = Story(
          id = storyId,
          title = result.storyTitle,
          prompt = promptToUse,
          genre = genre,
          length = length,
          imageSize = imgSize,
          voiceProfile = voice,
          chapters = listOf(firstChapter)
        )

        _uiState.value = _uiState.value.copy(
          currentStory = newStory,
          activeChapterIndex = 0,
          currentScreen = AppScreen.READER,
          isGeneratingStory = false,
          generationStatusText = "Painting story illustration...",
          isLoadedFromCache = false
        )

        // Store to local Room database immediately for offline reading
        repository.saveStory(newStory)

        // Generate Image asynchronously
        generateChapterImage(newStory, 0, result.imagePrompt, imgSize)

        // Extract characters automatically in the background
        viewModelScope.launch {
          val characters = geminiService.extractCharacters(newStory, model)
          val storyWithChars = newStory.copy(characters = characters)
          _uiState.value = _uiState.value.copy(currentStory = storyWithChars)
          repository.saveStory(storyWithChars)
        }

      } catch (e: Exception) {
        // Offline / Network error fallback to Room cache
        val fallbackStory = repository.findCachedStoryByPrompt(promptToUse)
          ?: repository.findSimilarCachedStory(promptToUse)

        if (fallbackStory != null) {
          _uiState.value = _uiState.value.copy(
            currentStory = fallbackStory,
            activeChapterIndex = 0,
            selectedGenre = fallbackStory.genre,
            selectedLength = fallbackStory.length,
            selectedImageSize = fallbackStory.imageSize,
            selectedVoiceProfile = fallbackStory.voiceProfile,
            currentScreen = AppScreen.READER,
            isGeneratingStory = false,
            isLoadedFromCache = true,
            errorBanner = "Offline Mode: Loaded matching cached story from local Room database."
          )
        } else {
          _uiState.value = _uiState.value.copy(
            isGeneratingStory = false,
            errorBanner = "Could not contact Gemini: ${e.localizedMessage}. All saved stories in your Library remain accessible offline!"
          )
        }
      }
    }
  }

  fun continueStory(branchTitle: String, customHint: String? = null) {
    val story = _uiState.value.currentStory ?: return
    val hint = customHint ?: branchTitle
    val length = story.length
    val genre = story.genre
    val imgSize = story.imageSize
    val model = _uiState.value.selectedAiModel

    viewModelScope.launch {
      ttsManager.stop()
      _uiState.value = _uiState.value.copy(
        isGeneratingStory = true,
        generationStatusText = "Writing Chapter ${story.chapters.size + 1}: '$hint'...",
        errorBanner = null
      )

      try {
        val result = geminiService.generateStory(
          prompt = story.prompt,
          length = length,
          genre = genre,
          previousChapters = story.chapters,
          continuationHint = hint,
          modelName = model
        )

        val nextChapter = Chapter(
          id = UUID.randomUUID().toString(),
          storyId = story.id,
          chapterIndex = story.chapters.size + 1,
          title = result.chapterTitle,
          content = result.storyText,
          imagePrompt = result.imagePrompt,
          continuationOptions = result.continuationOptions
        )

        val updatedChapters = story.chapters + nextChapter
        val updatedStory = story.copy(chapters = updatedChapters, updatedAt = System.currentTimeMillis())

        val newIndex = updatedChapters.lastIndex
        _uiState.value = _uiState.value.copy(
          currentStory = updatedStory,
          activeChapterIndex = newIndex,
          isGeneratingStory = false,
          generationStatusText = ""
        )

        repository.saveStory(updatedStory)

        // Generate image for next chapter
        generateChapterImage(updatedStory, newIndex, result.imagePrompt, imgSize)

        // Update characters with the new chapter context
        viewModelScope.launch {
          val characters = geminiService.extractCharacters(updatedStory, model)
          val storyWithChars = updatedStory.copy(characters = characters)
          _uiState.value = _uiState.value.copy(currentStory = storyWithChars)
          repository.saveStory(storyWithChars)
        }

      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isGeneratingStory = false,
          errorBanner = "Failed to continue story: ${e.localizedMessage}"
        )
      }
    }
  }

  private fun generateChapterImage(story: Story, chapterIndex: Int, imagePrompt: String, size: ImageSize) {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isGeneratingImage = true)
      val imgResult = geminiService.generateStoryImage(imagePrompt, size)

      if (imgResult.base64 != null) {
        val currentChapters = story.chapters.toMutableList()
        if (chapterIndex in currentChapters.indices) {
          val updatedChapter = currentChapters[chapterIndex].copy(imageBase64 = imgResult.base64)
          currentChapters[chapterIndex] = updatedChapter
          val updatedStory = story.copy(chapters = currentChapters)

          _uiState.value = _uiState.value.copy(
            currentStory = updatedStory,
            isGeneratingImage = false
          )
          repository.saveStory(updatedStory)
        }
      } else {
        _uiState.value = _uiState.value.copy(isGeneratingImage = false)
      }
    }
  }

  fun toggleAudioNarration() {
    val story = _uiState.value.currentStory ?: return
    val chapterIndex = _uiState.value.activeChapterIndex
    val chapter = story.chapters.getOrNull(chapterIndex) ?: return
    val voice = _uiState.value.selectedVoiceProfile
    val speed = _uiState.value.playbackSpeed

    viewModelScope.launch {
      val playState = ttsManager.playbackState.value
      if (playState.isPlaying) {
        ttsManager.pause()
      } else if (playState.isPaused && playState.currentText == chapter.content) {
        ttsManager.resume()
      } else {
        // Try requesting high fidelity Gemini TTS audio first if connected
        _uiState.value = _uiState.value.copy(isGeneratingAudio = true)
        val audioBytes = geminiService.generateSpeechAudio(chapter.content, voice)
        _uiState.value = _uiState.value.copy(isGeneratingAudio = false)

        if (audioBytes != null) {
          ttsManager.playAudioBytes(audioBytes, chapter.content, voice, speed)
        } else {
          ttsManager.playText(chapter.content, voice, speed)
        }
      }
    }
  }

  fun seekAudio(ratio: Float) {
    ttsManager.seekTo(ratio)
  }

  fun setActiveChapter(index: Int) {
    ttsManager.stop()
    _uiState.value = _uiState.value.copy(activeChapterIndex = index)
  }

  fun toggleFavorite() {
    val story = _uiState.value.currentStory ?: return
    val newFav = !story.isFavorite
    val updated = story.copy(isFavorite = newFav)
    _uiState.value = _uiState.value.copy(currentStory = updated)
    viewModelScope.launch {
      repository.toggleFavorite(story.id, newFav)
    }
  }

  fun toggleStoryBookmark(story: Story) {
    val newFav = !story.isFavorite
    if (_uiState.value.currentStory?.id == story.id) {
      _uiState.value = _uiState.value.copy(currentStory = _uiState.value.currentStory?.copy(isFavorite = newFav))
    }
    viewModelScope.launch {
      repository.toggleFavorite(story.id, newFav)
    }
  }

  fun selectStory(story: Story) {
    ttsManager.stop()
    _uiState.value = _uiState.value.copy(
      currentStory = story,
      activeChapterIndex = 0,
      selectedGenre = story.genre,
      selectedLength = story.length,
      selectedImageSize = story.imageSize,
      selectedVoiceProfile = story.voiceProfile,
      currentScreen = AppScreen.READER,
      isCharacterGalleryVisible = false,
      selectedCharacterDetail = null
    )

    // If story has no characters yet, extract them automatically
    if (story.characters.isEmpty() && story.chapters.isNotEmpty()) {
      viewModelScope.launch {
        val characters = geminiService.extractCharacters(story, _uiState.value.selectedAiModel)
        val storyWithChars = story.copy(characters = characters)
        _uiState.value = _uiState.value.copy(currentStory = storyWithChars)
        repository.saveStory(storyWithChars)
      }
    }
  }

  fun toggleCharacterGallery(visible: Boolean? = null) {
    val shouldShow = visible ?: !_uiState.value.isCharacterGalleryVisible
    _uiState.value = _uiState.value.copy(isCharacterGalleryVisible = shouldShow)
    if (shouldShow) {
      val story = _uiState.value.currentStory
      if (story != null && story.characters.isEmpty() && story.chapters.isNotEmpty()) {
        extractCharactersForCurrentStory()
      }
    }
  }

  fun selectCharacterDetail(character: StoryCharacter?) {
    _uiState.value = _uiState.value.copy(selectedCharacterDetail = character)
  }

  fun extractCharactersForCurrentStory(forceAiScan: Boolean = false) {
    val story = _uiState.value.currentStory ?: return
    if (story.chapters.isEmpty()) return

    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isExtractingCharacters = true)
      val characters = if (forceAiScan || story.characters.isEmpty()) {
        geminiService.extractCharacters(story, _uiState.value.selectedAiModel)
      } else {
        story.characters
      }

      val updatedStory = story.copy(characters = characters, updatedAt = System.currentTimeMillis())
      _uiState.value = _uiState.value.copy(
        currentStory = updatedStory,
        isExtractingCharacters = false
      )
      repository.saveStory(updatedStory)
    }
  }

  fun speakCharacterBio(character: StoryCharacter) {
    val voice = _uiState.value.selectedVoiceProfile
    val speed = _uiState.value.playbackSpeed
    val textToSpeak = buildString {
      append("${character.name}. Role: ${character.role}. ")
      append("${character.summary} ")
      append("Visual Appearance: ${character.appearance}. ")
      if (!character.quote.isNullOrBlank()) {
        append("Character quote: \"${character.quote}\".")
      }
    }
    ttsManager.playText(textToSpeak, voice, speed)
  }

  fun deleteStory(storyId: String) {
    viewModelScope.launch {
      repository.deleteStory(storyId)
      if (_uiState.value.currentStory?.id == storyId) {
        _uiState.value = _uiState.value.copy(currentStory = null, currentScreen = AppScreen.HOME)
      }
    }
  }

  fun sendChatMessage(text: String) {
    if (text.isBlank()) return
    val userMsg = ChatMessage(role = "user", text = text.trim())
    val currentHistory = _uiState.value.chatMessages + userMsg
    _uiState.value = _uiState.value.copy(
      chatMessages = currentHistory,
      isChatLoading = true
    )

    viewModelScope.launch {
      val storyContext = _uiState.value.currentStory?.let {
        "Title: ${it.title}\nGenre: ${it.genre.label}\nPrompt: ${it.prompt}\nChapters count: ${it.chapters.size}"
      } ?: ""

      val modelResponse = geminiService.chatWithStoryWeaver(
        history = currentHistory,
        userMessage = text.trim(),
        currentStoryContext = storyContext,
        modelName = _uiState.value.selectedAiModel
      )

      val modelMsg = ChatMessage(role = "model", text = modelResponse)
      _uiState.value = _uiState.value.copy(
        chatMessages = currentHistory + modelMsg,
        isChatLoading = false
      )
    }
  }

  fun exportStoryPdf(context: android.content.Context, targetStory: Story? = null) {
    val story = targetStory ?: _uiState.value.currentStory ?: return
    _uiState.value = _uiState.value.copy(isExportingPdf = true)
    viewModelScope.launch {
      com.example.util.StoryPdfExporter.exportAndShareStory(context, story)
      _uiState.value = _uiState.value.copy(isExportingPdf = false)
    }
  }

  fun clearAllStoryCache() {
    viewModelScope.launch {
      repository.clearCache()
      _uiState.value = _uiState.value.copy(
        currentStory = null,
        currentScreen = AppScreen.HOME,
        cacheMatchStory = null
      )
    }
  }

  fun preloadStarterStories() {
    viewModelScope.launch {
      val starters = repository.getStarterStories()
      for (story in starters) {
        repository.saveStory(story)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    ttsManager.release()
  }
}
