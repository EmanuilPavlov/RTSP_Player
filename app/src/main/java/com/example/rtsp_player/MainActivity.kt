package com.example.rtsp_player

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.rtsp_player.databinding.ActivityMainBinding
import com.google.android.material.slider.Slider
import androidx.core.view.isGone
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.util.ArrayList
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private var libVLC: LibVLC? = null
    private var vlcMediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private var isFullScreen = false
    private var volumeReceiver: BroadcastReceiver? = null
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null

    private val viewBinding: ActivityMainBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(viewBinding.root)

        setupAudio()
        initializeVLCPlayer()
        setupCustomControls()
    }

    private fun setupAudio() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Setup audio focus listener
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    // Resume playback
                    vlcMediaPlayer?.play()
                    setVolumeNormal()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    // Lost audio focus for a long time - stop playback
                    vlcMediaPlayer?.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    // Lost audio focus for a short time - pause playback
                    vlcMediaPlayer?.pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Lost audio focus but can play at lower volume
                    setVolumeDucked()
                }
            }
        }

        volumeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                // Update volume slider if visible
                findViewById<Slider>(R.id.volume_slider)?.value = currentVolume.toFloat()
            }
        }
    }

    private fun setVolumeNormal() {
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10
        vlcMediaPlayer?.volume = ((currentVolume.toFloat() / maxVolume) * 100).toInt()
    }

    private fun setVolumeDucked() {
        // Reduce volume to 30% when ducking
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10
        vlcMediaPlayer?.volume = ((currentVolume.toFloat() / maxVolume) * 30).toInt()
    }

    private fun requestAudioFocus(): Boolean {
        return audioManager?.requestAudioFocus(
            audioFocusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun initializeVLCPlayer() {
        try {
            val rtspUrl = getString(R.string.media_url_rtsp)

            // VLC options for stable audio
            val options = ArrayList<String>().apply {
                add("--rtsp-tcp")
                add("--network-caching=300")
                add("--clock-jitter=0")
                add("--clock-synchro=0")
                add("--aout=opensles")
                add("--audio-time-stretch")
                add("--avcodec-hw=any")
                add("--no-stats")  // Disable stats to reduce CPU
                add("--audio-resampler=soxr")  // Better audio resampler
            }

            // Initialize LibVLC
            libVLC = LibVLC(this, options)

            // Initialize MediaPlayer with audio persistence
            vlcMediaPlayer = MediaPlayer(libVLC).apply {
                // Set event listener to monitor audio state
                setEventListener { event ->
                    when (event.type) {
                        MediaPlayer.Event.Playing -> {
                            android.util.Log.d("VLC", "Player is PLAYING - Audio should be active")
                            // Ensure audio focus when playing
                            requestAudioFocus()
                        }
                        MediaPlayer.Event.Paused -> {
                            android.util.Log.d("VLC", "Player is PAUSED")
                        }
                        MediaPlayer.Event.EncounteredError -> {
                            android.util.Log.e("VLC", "Player encountered error")
                        }
                        MediaPlayer.Event.MediaChanged -> {
                            android.util.Log.d("VLC", "Media changed")
                        }
                        MediaPlayer.Event.Opening -> {
                            android.util.Log.d("VLC", "Media opening")
                        }
                        MediaPlayer.Event.Buffering -> {
                            android.util.Log.d("VLC", "Buffering: ${event.buffering}")
                        }
                    }
                }
            }

            // Attach the video view
            vlcMediaPlayer?.attachViews(viewBinding.videoView, null, false, false)

            // Load and play media with audio persistence
            val media = Media(libVLC, rtspUrl.toUri()).apply {
                setHWDecoderEnabled(true, true)
                addOption(":network-caching=300")
                addOption(":rtsp-tcp")
                addOption(":no-audio-timeout=0")
                addOption(":audio-time-stretch")
                addOption(":audio-resampler=soxr")  // Consistent audio resampling
            }

            vlcMediaPlayer?.media = media

            // Request audio focus before playing
            if (requestAudioFocus()) {
                vlcMediaPlayer?.play()
                setInitialVolume()
            } else {
                android.util.Log.e("VLC", "Failed to get audio focus")
            }

        } catch (e: Exception) {
            android.util.Log.e("VLC", "Failed to initialize VLC player", e)
        }
    }

    private fun setInitialVolume() {
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10

        // Set VLC volume (0-100 scale, where 100 is normal)
        vlcMediaPlayer?.volume = ((currentVolume.toFloat() / maxVolume) * 100).toInt()
    }

    @SuppressLint("SetTextI18n")
    private fun setupCustomControls() {
        val playButton = findViewById<ImageButton>(R.id.exo_play)
        val pauseButton = findViewById<ImageButton>(R.id.exo_pause)
        val volumeButton = findViewById<ImageButton>(R.id.volume_button)
        val volumeSlider = findViewById<Slider>(R.id.volume_slider)
        val fullScreenButton = findViewById<ImageButton>(R.id.exo_minimal_fullscreen)

        // Toggle on video tap
        viewBinding.videoView.setOnClickListener {
            val controls = findViewById<View>(R.id.controller_container)
            if (controls?.visibility == View.VISIBLE) {
                controls.visibility = View.GONE
            } else {
                showControlsTemporarily()
            }
        }

        // Play
        playButton?.setOnClickListener {
            if (requestAudioFocus()) {
                if (vlcMediaPlayer?.isPlaying == false) {
                    // If we previously stopped, media will be null → re‑set it
                    if (vlcMediaPlayer?.media == null) {
                        val rtspUrl = getString(R.string.media_url_rtsp)
                        val media = Media(libVLC, rtspUrl.toUri()).apply {
                            setHWDecoderEnabled(true, true)
                            addOption(":network-caching=300")
                            addOption(":rtsp-tcp")
                        }
                        vlcMediaPlayer?.media = media
                    }
                    // If paused, just resume without re‑setting media
                    vlcMediaPlayer?.play()
                }
                playButton.visibility = View.INVISIBLE
                pauseButton?.visibility = View.VISIBLE
            }
        }


        // Pause
        pauseButton?.setOnClickListener {
            vlcMediaPlayer?.pause()
            vlcMediaPlayer?.media = null
            playButton?.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE
        }

        // Volume
        val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10

        volumeSlider?.valueFrom = 0f
        volumeSlider?.valueTo = maxVolume.toFloat()
        volumeSlider?.value = currentVolume.toFloat()

        volumeButton?.setOnClickListener {
            volumeSlider?.visibility = if (volumeSlider.isGone) View.VISIBLE else View.GONE
        }

        volumeSlider?.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, value.toInt(), 0)
                vlcMediaPlayer?.volume = ((value / maxVolume) * 100).toInt()
            }
        }

        // Fullscreen toggle
        fullScreenButton?.setOnClickListener {
            toggleFullscreen()
        }
    }

    private fun toggleFullscreen() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        if (!isFullScreen) {
            // Enter fullscreen
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            supportActionBar?.hide()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        } else {
            // Exit fullscreen
            controller.show(WindowInsetsCompat.Type.systemBars())
            supportActionBar?.show()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        isFullScreen = !isFullScreen
    }



    private val hideControlsRunnable = Runnable {
        findViewById<View>(R.id.controller_container)?.visibility = View.GONE
    }

    private fun showControlsTemporarily() {
        val controls = findViewById<View>(R.id.controller_container)
        controls?.visibility = View.VISIBLE
        controls?.removeCallbacks(hideControlsRunnable)
        controls?.postDelayed(hideControlsRunnable, 5000)
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(volumeReceiver, IntentFilter("android.media.VOLUME_CHANGED_ACTION"))
        // Request audio focus when resuming
        if (vlcMediaPlayer?.isPlaying == true) {
            requestAudioFocus()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(volumeReceiver)
        // Don't pause here to avoid audio issues, let audio focus handle it
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release audio focus
        audioManager?.abandonAudioFocus(audioFocusChangeListener)
        // VLC cleanup
        vlcMediaPlayer?.stop()
        vlcMediaPlayer?.detachViews()
        vlcMediaPlayer?.release()
        libVLC?.release()
    }
}