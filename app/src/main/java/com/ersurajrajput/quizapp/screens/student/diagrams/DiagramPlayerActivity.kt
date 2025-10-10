package com.ersurajrajput.quizapp.screens.student.diagrams

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ersurajrajput.quizapp.screens.student.diagrams.ui.theme.QuizAppTheme
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class DiagramPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force landscape for fullscreen effect
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val url = intent.getStringExtra("VIDEO_URL") ?: ""

        // Extract videoId from url (YouTube links like https://www.youtube.com/watch?v=abcd1234)
        val videoId = extractVideoId(url)

        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        val youTubePlayerView = YouTubePlayerView(context).apply {
                            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    if (videoId != null) {
                                        youTubePlayer.loadVideo(videoId, 0f) // autoplay from start
                                    }
                                }
                            })
                        }
                        lifecycle.addObserver(youTubePlayerView) // lifecycle aware
                        youTubePlayerView
                    }
                )
            }
        }
    }

    private fun extractVideoId(url: String): String? {
        return when {
            "v=" in url -> url.substringAfter("v=").substringBefore("&")
            "youtu.be/" in url -> url.substringAfter("youtu.be/")
            else -> null
        }
    }
}
