package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

data class TtsPlaybackState(
  val isPlaying: Boolean = false,
  val isPaused: Boolean = false,
  val currentPositionSeconds: Float = 0f,
  val totalDurationSeconds: Float = 0f,
  val currentText: String = "",
  val activeVoiceName: String = VoiceProfile.DEFAULT.displayName,
  val speed: Float = 1.0f
)

class TtsManager(private val context: Context) {

  private var ttsEngine: TextToSpeech? = null
  private var isTtsInitialized = false
  private var mediaPlayer: MediaPlayer? = null
  private var audioTrack: AudioTrack? = null
  private val scope = CoroutineScope(Dispatchers.Main + Job())
  private var progressTickerJob: Job? = null

  private val _playbackState = MutableStateFlow(TtsPlaybackState())
  val playbackState: StateFlow<TtsPlaybackState> = _playbackState.asStateFlow()

  private var currentPlaybackSpeed: Float = 1.0f
  private var currentVoiceProfile: VoiceProfile = VoiceProfile.DEFAULT
  private var currentPitchMultiplier: Float = 1.0f
  private var activeText: String = ""
  private var isNativeTtsActive = false

  init {
    ttsEngine = TextToSpeech(context.applicationContext) { status ->
      if (status == TextToSpeech.SUCCESS) {
        isTtsInitialized = true
        ttsEngine?.language = Locale.US
        setupTtsListener()
        applyVoiceSettings()
      } else {
        Log.e("TtsManager", "Failed to initialize Android TextToSpeech")
      }
    }
  }

  private fun setupTtsListener() {
    ttsEngine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
      override fun onStart(utteranceId: String?) {
        _playbackState.value = _playbackState.value.copy(
          isPlaying = true,
          isPaused = false
        )
        startSimulatedProgress(activeText)
      }

      override fun onDone(utteranceId: String?) {
        stopProgressTicker()
        _playbackState.value = _playbackState.value.copy(
          isPlaying = false,
          isPaused = false,
          currentPositionSeconds = _playbackState.value.totalDurationSeconds
        )
      }

      @Deprecated("Deprecated in Java")
      override fun onError(utteranceId: String?) {
        stopProgressTicker()
        _playbackState.value = _playbackState.value.copy(
          isPlaying = false,
          isPaused = false
        )
      }

      override fun onError(utteranceId: String?, errorCode: Int) {
        stopProgressTicker()
        _playbackState.value = _playbackState.value.copy(
          isPlaying = false,
          isPaused = false
        )
      }
    })
  }

  fun updateSettings(voiceProfile: VoiceProfile, speed: Float, pitch: Float = 1.0f) {
    currentVoiceProfile = voiceProfile
    currentPlaybackSpeed = speed
    currentPitchMultiplier = pitch
    applyVoiceSettings()
  }

  private fun applyVoiceSettings() {
    if (!isTtsInitialized || ttsEngine == null) return
    try {
      // Calculate effective speed & pitch
      val effectiveSpeed = currentPlaybackSpeed * currentVoiceProfile.baseSpeed
      val effectivePitch = currentPitchMultiplier * currentVoiceProfile.pitchMultiplier
      ttsEngine?.setSpeechRate(effectiveSpeed.coerceIn(0.5f, 2.0f))
      ttsEngine?.setPitch(effectivePitch.coerceIn(0.5f, 2.0f))

      // Try matching Android system voice if available
      val voices = ttsEngine?.voices
      if (!voices.isNullOrEmpty()) {
        val matchedVoice = voices.find { voice ->
          val nameLower = voice.name.lowercase()
          if (currentVoiceProfile.gender.equals("Female", ignoreCase = true)) {
            nameLower.contains("female") || nameLower.contains("f00") || nameLower.contains("en-us-x-sfg")
          } else {
            nameLower.contains("male") || nameLower.contains("m00") || nameLower.contains("en-us-x-iol")
          }
        } ?: voices.find { it.locale == Locale.US }
        if (matchedVoice != null) {
          ttsEngine?.voice = matchedVoice
        }
      }
    } catch (e: Exception) {
      Log.e("TtsManager", "Error applying voice settings", e)
    }
  }

  fun playText(text: String, voiceProfile: VoiceProfile = currentVoiceProfile, speed: Float = currentPlaybackSpeed) {
    stop()
    activeText = text
    currentVoiceProfile = voiceProfile
    currentPlaybackSpeed = speed
    isNativeTtsActive = true

    applyVoiceSettings()

    val estimatedDuration = (text.split("\\s+".toRegex()).size / (2.5f * speed)).coerceAtLeast(5f)
    _playbackState.value = TtsPlaybackState(
      isPlaying = true,
      isPaused = false,
      currentPositionSeconds = 0f,
      totalDurationSeconds = estimatedDuration,
      currentText = text,
      activeVoiceName = voiceProfile.displayName,
      speed = speed
    )

    if (isTtsInitialized) {
      val params = Bundle()
      params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "story_utterance_${System.currentTimeMillis()}")
      ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "story_utterance")
    } else {
      startSimulatedProgress(text)
    }
  }

  fun playAudioBytes(audioBytes: ByteArray, text: String, voiceProfile: VoiceProfile, speed: Float) {
    stop()
    activeText = text
    currentVoiceProfile = voiceProfile
    currentPlaybackSpeed = speed
    isNativeTtsActive = false

    try {
      val tempFile = File.createTempFile("tts_audio", ".wav", context.cacheDir)
      FileOutputStream(tempFile).use { it.write(audioBytes) }

      mediaPlayer = MediaPlayer().apply {
        setDataSource(tempFile.absolutePath)
        playbackParams = playbackParams.setSpeed(speed.coerceIn(0.5f, 2.0f))
        prepare()
        val durationSec = (duration / 1000f).coerceAtLeast(1f)
        _playbackState.value = TtsPlaybackState(
          isPlaying = true,
          isPaused = false,
          currentPositionSeconds = 0f,
          totalDurationSeconds = durationSec,
          currentText = text,
          activeVoiceName = voiceProfile.displayName,
          speed = speed
        )
        setOnCompletionListener {
          stopProgressTicker()
          _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = false, currentPositionSeconds = durationSec)
        }
        start()
      }

      startMediaPlayerProgress()
    } catch (e: Exception) {
      Log.e("TtsManager", "Error playing audio bytes, falling back to Native TTS", e)
      playText(text, voiceProfile, speed)
    }
  }

  fun pause() {
    if (_playbackState.value.isPlaying) {
      if (isNativeTtsActive) {
        ttsEngine?.stop()
        stopProgressTicker()
      } else {
        mediaPlayer?.pause()
        stopProgressTicker()
      }
      _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = true)
    }
  }

  fun resume() {
    if (_playbackState.value.isPaused) {
      if (isNativeTtsActive) {
        playText(activeText, currentVoiceProfile, currentPlaybackSpeed)
      } else {
        mediaPlayer?.start()
        startMediaPlayerProgress()
        _playbackState.value = _playbackState.value.copy(isPlaying = true, isPaused = false)
      }
    }
  }

  fun togglePlayPause(text: String, voiceProfile: VoiceProfile = currentVoiceProfile, speed: Float = currentPlaybackSpeed) {
    val state = _playbackState.value
    if (state.isPlaying) {
      pause()
    } else if (state.isPaused && state.currentText == text) {
      resume()
    } else {
      playText(text, voiceProfile, speed)
    }
  }

  fun seekTo(ratio: Float) {
    val duration = _playbackState.value.totalDurationSeconds
    val newPos = duration * ratio.coerceIn(0f, 1f)
    _playbackState.value = _playbackState.value.copy(currentPositionSeconds = newPos)
    if (!isNativeTtsActive && mediaPlayer != null) {
      mediaPlayer?.seekTo((newPos * 1000).toInt())
    }
  }

  fun stop() {
    stopProgressTicker()
    try {
      ttsEngine?.stop()
      mediaPlayer?.stop()
      mediaPlayer?.release()
      mediaPlayer = null
    } catch (e: Exception) {
      Log.e("TtsManager", "Error stopping audio", e)
    }
    _playbackState.value = _playbackState.value.copy(isPlaying = false, isPaused = false, currentPositionSeconds = 0f)
  }

  private fun startSimulatedProgress(text: String) {
    stopProgressTicker()
    val words = text.split("\\s+".toRegex()).size
    val totalSec = (words / (2.5f * currentPlaybackSpeed)).coerceAtLeast(5f)
    _playbackState.value = _playbackState.value.copy(totalDurationSeconds = totalSec)

    progressTickerJob = scope.launch {
      var current = _playbackState.value.currentPositionSeconds
      while (isActive && current < totalSec && _playbackState.value.isPlaying) {
        delay(200)
        current += 0.2f
        _playbackState.value = _playbackState.value.copy(
          currentPositionSeconds = current.coerceAtMost(totalSec)
        )
      }
    }
  }

  private fun startMediaPlayerProgress() {
    stopProgressTicker()
    progressTickerJob = scope.launch {
      while (isActive && mediaPlayer != null && mediaPlayer?.isPlaying == true) {
        val currentSec = (mediaPlayer?.currentPosition ?: 0) / 1000f
        _playbackState.value = _playbackState.value.copy(currentPositionSeconds = currentSec)
        delay(250)
      }
    }
  }

  private fun stopProgressTicker() {
    progressTickerJob?.cancel()
    progressTickerJob = null
  }

  fun release() {
    stop()
    try {
      ttsEngine?.shutdown()
    } catch (e: Exception) {
      Log.e("TtsManager", "Error shutting down TTS", e)
    }
  }
}
