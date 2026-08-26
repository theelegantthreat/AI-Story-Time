package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoryRepository(
  private val storyDao: StoryDao
) {
  private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
  private val chaptersType = Types.newParameterizedType(List::class.java, Chapter::class.java)
  private val chaptersAdapter = moshi.adapter<List<Chapter>>(chaptersType)
  private val charactersType = Types.newParameterizedType(List::class.java, StoryCharacter::class.java)
  private val charactersAdapter = moshi.adapter<List<StoryCharacter>>(charactersType)

  fun getAllStories(): Flow<List<Story>> {
    return storyDao.getAllStories().map { entities ->
      entities.map { mapEntityToStory(it) }
    }
  }

  fun getStoryCount(): Flow<Int> = storyDao.getStoryCount()

  suspend fun getStoryById(id: String): Story? {
    val entity = storyDao.getStoryById(id) ?: return null
    return mapEntityToStory(entity)
  }

  /**
   * Looks up a cached story in Room by exact or trimmed prompt.
   */
  suspend fun findCachedStoryByPrompt(prompt: String): Story? {
    if (prompt.isBlank()) return null
    val entity = storyDao.findStoryByExactPrompt(prompt.trim()) ?: return null
    return mapEntityToStory(entity)
  }

  /**
   * Looks up a cached story in Room by matching keyword in prompt or title.
   */
  suspend fun findSimilarCachedStory(query: String): Story? {
    if (query.isBlank()) return null
    val entity = storyDao.findStoryByKeyword(query.trim()) ?: return null
    return mapEntityToStory(entity)
  }

  suspend fun saveStory(story: Story) {
    val entity = mapStoryToEntity(story)
    storyDao.insertOrUpdateStory(entity)
  }

  suspend fun deleteStory(id: String) {
    storyDao.deleteStory(id)
  }

  suspend fun clearCache() {
    storyDao.clearAllStories()
  }

  suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
    storyDao.setFavorite(id, isFavorite)
  }

  /**
   * Seeds high quality offline starter stories into Room if the database is currently empty.
   */
  suspend fun prepopulateStarterStoriesIfNeeded() {
    val count = storyDao.getStoryCountDirect()
    if (count == 0) {
      val starterStories = getStarterStories()
      val entities = starterStories.map { mapStoryToEntity(it) }
      storyDao.insertAll(entities)
    }
  }

  fun getStarterStories(): List<Story> {
    return listOf(
      Story(
        id = "seed_fantasy_dragon",
        title = "The Starlight Dragon of Solitude",
        prompt = "A curious baby dragon who wanted to bake cupcakes for the village",
        genre = StoryGenre.FANTASY,
        length = StoryLength.MEDIUM,
        voiceProfile = VoiceProfile.ALL.find { it.id == "Seraphina" } ?: VoiceProfile.DEFAULT,
        isFavorite = true,
        characters = listOf(
          StoryCharacter(
            id = "char_pyra",
            name = "Pyra the Starlight Dragon",
            role = "Protagonist",
            appearance = "A miniature emerald-scaled dragon with luminous starry purple eyes, sporting a tiny powdered-sugar coated chef hat and a moon-wood rolling pin.",
            personality = "Gentle, imaginative, warm-hearted, passionate baker who loves cinnamon and vanilla scents.",
            summary = "Rejects ancient fire-breathing dragon traditions to bake molten berry tarts and pastries atop Mount Glimmer for the unaware village below.",
            emoji = "🐉",
            firstAppearedChapter = 1,
            quote = "The sweetest magic is kneaded with kindness and dusted with stardust."
          ),
          StoryCharacter(
            id = "char_lumina_folk",
            name = "Lumina Village Folk",
            role = "Supporting / Community",
            appearance = "Warm and cozy villagers in hand-spun woolen cloaks and aprons living in the valley beneath Mount Glimmer.",
            personality = "Curious, grateful, and appreciative of sweet midnight treats.",
            summary = "Townspeople who wake up to delightful pastry aromas every morning and whisper of a mythical mountain baker.",
            emoji = "🏘️",
            firstAppearedChapter = 1
          )
        ),
        chapters = listOf(
          Chapter(
            id = "seed_ch1_dragon",
            storyId = "seed_fantasy_dragon",
            chapterIndex = 1,
            title = "Chapter 1: The Flour-Dusted Cave",
            content = "High atop Mount Glimmer, where purple dusk-clouds touched jagged obsidian peaks, lived Pyra. Unlike her great ancestors who hoarded gold and breathed scorching thunder-fire, Pyra hoarded cinnamon bark, vanilla pods, and golden bags of enchanted cloud-flour. Her tiny dragon snout was perpetually coated in powdered sugar.\n\nEvery evening, as the silver moon rose over Lumina Village below, delicious aromas of molten berry tarts and honey-glazed buns drifted down the valley. The villagers whispered of a secret mountain chef, never suspecting the emerald-scaled dragon with starry purple eyes was rolling dough with a wooden rolling pin carved from moon-wood.",
            imagePrompt = "A whimsical baby emerald dragon with starry purple eyes wearing a tiny chef hat, dusting powdered sugar over golden cupcakes in a glowing crystal cave, digital storybook illustration style",
            continuationOptions = listOf(
              ContinuationOption("Deliver to the Festival", "Fly down disguised with a basket to the annual Lumina Bakeoff"),
              ContinuationOption("Seek the Star-Vanilla", "Journey into the Celestial Forest to find legendary moon-vanilla orchids"),
              ContinuationOption("Invite the Village Baker", "Leave an anonymous golden invitation on the village bakery doorstep")
            )
          )
        )
      ),
      Story(
        id = "seed_scifi_chrono",
        title = "The Clockwork Compass of Sector 7",
        prompt = "A pocket-sized robot discovering an enchanted glass forest on a distant moon",
        genre = StoryGenre.SCIFI,
        length = StoryLength.MEDIUM,
        voiceProfile = VoiceProfile.ALL.find { it.id == "Charon" } ?: VoiceProfile.DEFAULT,
        characters = listOf(
          StoryCharacter(
            id = "char_unit_7b",
            name = "Unit 7-B",
            role = "Protagonist",
            appearance = "A miniature antique-brass automaton no larger than a pocket watch, fitted with dual sapphire optical lenses, polished bronze cogwheels, and folding solar wings.",
            personality = "Curious, observant, resilient, and deeply fascinated by extraterrestrial botanical phenomena.",
            summary = "An autonomous explorer drone that crash-landed on Moon Asteria and navigated into the resonant Silica Grove.",
            emoji = "🤖",
            firstAppearedChapter = 1,
            quote = "Beacon frequency locked: wonder protocol engaged."
          )
        ),
        chapters = listOf(
          Chapter(
            id = "seed_ch1_chrono",
            storyId = "seed_scifi_chrono",
            chapterIndex = 1,
            title = "Chapter 1: Prisms in the Stardust",
            content = "Unit 7-B was no larger than an antique pocket watch, equipped with brass gearwheels, dual optical lenses, and tiny solar wings. When its explorer pod crash-landed on Moon Asteria, it stepped onto iridescent sand beneath a ringed sapphire planet.\n\nAhead stretched the legendary Silica Grove: towering crystalline trees whose leaves chimed like silver bells with every solar wind. At the center of the grove, a pulsating violet beacon broadcast a message in ancient machine code.",
            imagePrompt = "A cute miniature brass steampunk robot standing before glowing crystalline prism trees on an alien moon with blue ringed planet in the starry sky, storybook art",
            continuationOptions = listOf(
              ContinuationOption("Decode the Beacon", "Interface with the crystal terminal using brass antenna"),
              ContinuationOption("Climb the Prism Tree", "Fly up to the highest glass canopy to survey the moons landscape"),
              ContinuationOption("Search the Pod Wreckage", "Gather emergency solar batteries before nightfall")
            )
          )
        )
      ),
      Story(
        id = "seed_bedtime_lumina",
        title = "The Sleepy Starlight Whale",
        prompt = "A gentle bedtime story about a starlight whale swimming across the midnight galaxy",
        genre = StoryGenre.BEDTIME,
        length = StoryLength.SHORT,
        voiceProfile = VoiceProfile.ALL.find { it.id == "Kore" } ?: VoiceProfile.DEFAULT,
        isFavorite = true,
        characters = listOf(
          StoryCharacter(
            id = "char_orion_whale",
            name = "Orion the Celestial Whale",
            role = "Guardian / Protagonist",
            appearance = "A gentle cosmic whale whose translucent sapphire skin sparkles with clusters of distant constellations and flowing lavender stardust fins.",
            personality = "Serene, patient, soothing, and deeply calming.",
            summary = "Swims gracefully through cosmic nebula tides, singing harmonic lullabies that guide all young minds into tranquil sleep.",
            emoji = "🐳",
            firstAppearedChapter = 1,
            quote = "Rest easy, little star; the night sky will keep you safe."
          )
        ),
        chapters = listOf(
          Chapter(
            id = "seed_ch1_lumina",
            storyId = "seed_bedtime_lumina",
            chapterIndex = 1,
            title = "Chapter 1: The Silver Sea of Dreams",
            content = "Across the peaceful midnight sky, where nebula clouds glow in pastel indigo and lavender, swims Orion the Celestial Whale. His vast fins glide gently through rivers of stardust, creating soft ripples of warm moonlight that sprinkle down onto sleepy windows.\n\nWith every deep, rhythmic breath, Orion sings a low lullaby that calms the wandering winds and bids all young dreamers to close their eyes and drift into peaceful slumber.",
            imagePrompt = "A glowing cosmic blue whale gliding gracefully through lavender nebula clouds and starry night sky over peaceful sleeping houses, warm dreamy storybook art",
            continuationOptions = listOf(
              ContinuationOption("Follow the Star Trail", "Drift alongside Orion toward the Island of Gentle Dreams"),
              ContinuationOption("Listen to the Lullaby", "Let the celestial song weave cozy dreams of floating islands")
            )
          )
        )
      )
    )
  }

  private fun mapEntityToStory(entity: StoryEntity): Story {
    val chapters = try {
      chaptersAdapter.fromJson(entity.chaptersJson) ?: emptyList()
    } catch (e: Exception) {
      emptyList()
    }

    val characters = try {
      charactersAdapter.fromJson(entity.charactersJson) ?: emptyList()
    } catch (e: Exception) {
      emptyList()
    }

    val genre = StoryGenre.values().find { it.name == entity.genreName } ?: StoryGenre.FANTASY
    val length = StoryLength.values().find { it.name == entity.lengthName } ?: StoryLength.MEDIUM
    val imageSize = ImageSize.values().find { it.name == entity.imageSizeName } ?: ImageSize.SIZE_1K
    val voice = VoiceProfile.ALL.find { it.id == entity.voiceProfileId } ?: VoiceProfile.DEFAULT

    return Story(
      id = entity.id,
      title = entity.title,
      prompt = entity.prompt,
      genre = genre,
      length = length,
      imageSize = imageSize,
      voiceProfile = voice,
      chapters = chapters,
      characters = characters,
      isFavorite = entity.isFavorite,
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt
    )
  }

  private fun mapStoryToEntity(story: Story): StoryEntity {
    val chaptersJson = chaptersAdapter.toJson(story.chapters)
    val charactersJson = charactersAdapter.toJson(story.characters)
    return StoryEntity(
      id = story.id,
      title = story.title,
      prompt = story.prompt,
      genreName = story.genre.name,
      lengthName = story.length.name,
      imageSizeName = story.imageSize.name,
      voiceProfileId = story.voiceProfile.id,
      chaptersJson = chaptersJson,
      charactersJson = charactersJson,
      isFavorite = story.isFavorite,
      createdAt = story.createdAt,
      updatedAt = System.currentTimeMillis()
    )
  }
}

data class Story(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val prompt: String,
  val genre: StoryGenre = StoryGenre.FANTASY,
  val length: StoryLength = StoryLength.MEDIUM,
  val imageSize: ImageSize = ImageSize.SIZE_1K,
  val voiceProfile: VoiceProfile = VoiceProfile.DEFAULT,
  val chapters: List<Chapter> = emptyList(),
  val characters: List<StoryCharacter> = emptyList(),
  val isFavorite: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
