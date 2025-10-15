package com.ersurajrajput.quizapp.screens.student.diagrams

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.ersurajrajput.quizapp.screens.student.diagrams.ui.theme.QuizAppTheme
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class DiagramPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force landscape for fullscreen effect
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val url = intent.getStringExtra("VIDEO_URL") ?: ""
        val videoId = extractVideoId(url)

        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                DiagramPlayerScreen(videoId = videoId)
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

@SuppressLint("ContextCastToActivity")
@Composable
fun DiagramPlayerScreen(videoId: String?) {
    val activity = LocalContext.current as ComponentActivity

    Box(modifier = Modifier.fillMaxSize()) {

        // YouTube Player View
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f), // Keep below back button
            factory = { context ->
                val youTubePlayerView = YouTubePlayerView(context).apply {
                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            videoId?.let {
                                youTubePlayer.loadVideo(it, 0f)
                            }
                        }
                    })
                    clipToOutline = false
                }
                activity.lifecycle.addObserver(youTubePlayerView)
                youTubePlayerView
            }
        )

//        // Back Button (Clickable now ✅)
//        IconButton(
//            onClick = { activity.finish() },
//            modifier = Modifier
//                .align(Alignment.TopStart)
//                .padding(16.dp)
//                .zIndex(2f) // Bring above the YouTubePlayerView
//        ) {
//            Icon(
//                imageVector = Icons.Default.ArrowBack,
//                contentDescription = "Go back",
//                tint = Color.White
//            )
//        }
    }
}
