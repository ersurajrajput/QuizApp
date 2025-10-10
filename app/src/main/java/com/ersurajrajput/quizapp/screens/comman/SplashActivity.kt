package com.ersurajrajput.quizapp.screens.comman

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ersurajrajput.quizapp.repo.DummyRepo
import com.ersurajrajput.quizapp.ui.theme.QuizAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DummyRepo().populateFillInTheBlanks()
        setContent {
            QuizAppTheme {
                SplashScreen()
            }
        }
    }

    @Composable
    private fun SplashScreen() {
        var startAnimation by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // Animate the scale of the logo
        val scaleAnimation by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0.5f,
            animationSpec = tween(durationMillis = 1500)
        )

        // Animate the alpha of the text
        val alphaAnimation by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(durationMillis = 1500)
        )

        LaunchedEffect(key1 = true) {
            startAnimation = true
            delay(1500L) // wait for initial animation

            // Check internet before moving forward
            if (isInternetAvailable(this@SplashActivity)) {
                // Optionally, populate MCQActivity
                scope.launch {

                }
                delay(1500L) // wait a bit for data to populate
                startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this@SplashActivity,
                    "No internet connection. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
                .background(
                    brush = Brush.Companion.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A),
                            Color(0xFF8E24AA),
                            Color(0xFFAB47BC)
                        )
                    )
                ),
            contentAlignment = Alignment.Companion.Center
        ) {
            Column(
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Quiz,
                    contentDescription = "Quiz App Logo",
                    modifier = Modifier.Companion
                        .size(120.dp)
                        .scale(scaleAnimation),
                    tint = Color.Companion.White
                )
                Spacer(modifier = Modifier.Companion.height(24.dp))
                Text(
                    text = "QuizApp",
                    color = Color.Companion.White.copy(alpha = alphaAnimation),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Companion.Bold
                )
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}