package com.ersurajrajput.quizapp.screens.student.diagrams

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ersurajrajput.quizapp.R

class DiagramPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_diagram_player)

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        var url = intent.getStringExtra("VIDEO_URL")?:""
        val webView = findViewById<WebView>(R.id.webView)
        val backbtn = findViewById<ImageView>(R.id.bakcBtn)
        backbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Enable JavaScript if needed
        webView.settings.javaScriptEnabled = true

        // Keep links inside the WebView
        webView.webViewClient = WebViewClient()

        // Load your link
          // replace with your URL
        webView.loadUrl(url)
    }
}
