package com.example.rtsp_player

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.rtsp_player.databinding.ActivityMainBinding
import com.google.android.material.slider.Slider
import androidx.core.view.isGone

class MainActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var isFullScreen = false
    private var currentItem = 0
    private var playbackPosition = 0L
    private var originalVideoHeight = 0

    private val viewBinding: ActivityMainBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(viewBinding.root)

        // Store original video height
        viewBinding.videoView.viewTreeObserver.addOnGlobalLayoutListener {
            if (originalVideoHeight == 0) {
                originalVideoHeight = viewBinding.videoView.height
            }
        }

        initializePlayer()
        setupCustomControls()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
                val mediaItem = MediaItem.Builder()
                    .setUri(getString(R.string.media_url_rtsp))
                    .setMimeType(MimeTypes.APPLICATION_RTSP)
                    .build()

                exoPlayer.setMediaItems(listOf(mediaItem), currentItem, playbackPosition)
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.prepare()
                exoPlayer.play()
            }
    }

    @OptIn(UnstableApi::class)
    private fun setupCustomControls() {
        val playButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_play)
        val pauseButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_pause)
        val volumeButton = viewBinding.videoView.findViewById<ImageButton>(R.id.volume_button)
        val volumeSlider = viewBinding.videoView.findViewById<Slider>(R.id.volume_slider)
        val fullScreenButton = viewBinding.videoView.findViewById<ImageButton>(R.id.exo_minimal_fullscreen)
        val progressBar = viewBinding.videoView.findViewById<ProgressBar>(R.id.progress_bar)

        // --- Play / Pause toggle ---
        playButton.setOnClickListener {
            player?.play()
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
            viewBinding.videoView.showController()
        }

        pauseButton.setOnClickListener {
            player?.pause()
            playButton.visibility = View.VISIBLE
            pauseButton.visibility = View.INVISIBLE
            viewBinding.videoView.showController()
        }

        volumeButton.setOnClickListener {
            volumeSlider.visibility = if (volumeSlider.isGone) {
                View.VISIBLE
            } else {
                View.GONE
            }
            viewBinding.videoView.showController()
        }

        // --- Volume slider handling ---
        volumeSlider.addOnChangeListener { _, value, _ ->
            player?.volume = value
            viewBinding.videoView.showController()
        }

        // --- Fullscreen toggle ---
        fullScreenButton.visibility = View.VISIBLE
        fullScreenButton.setOnClickListener {
            if (!isFullScreen) {
                // Enter fullscreen mode
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        )
                supportActionBar?.hide()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                viewBinding.videoView.animate().scaleX(1f).scaleY(1f).duration = 300
                val params = viewBinding.videoView.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = ViewGroup.LayoutParams.MATCH_PARENT
                viewBinding.videoView.layoutParams = params

            } else {
                // Exit fullscreen mode
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                supportActionBar?.show()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                val params = viewBinding.videoView.layoutParams
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                params.height = if (originalVideoHeight > 0) {
                    originalVideoHeight
                } else {
                    (200 * resources.displayMetrics.density).toInt()
                }
                viewBinding.videoView.layoutParams = params
            }
            isFullScreen = !isFullScreen

            viewBinding.videoView.showController()
        }
        progressBar.visibility = View.VISIBLE
    }


    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT > 23) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23 || player == null) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT <= 23) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT > 23) releasePlayer()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
        }
        player = null
    }
}