package com.ersurajrajput.quizapp.screens.comman

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

// Assumed imports for target activities based on typical project structure
import com.ersurajrajput.quizapp.screens.admin.AdminHomeActivity
import com.ersurajrajput.quizapp.screens.student.StudentHomeActivity
import com.ersurajrajput.quizapp.screens.student.activity.games.ArrowGameActivity


class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        DummyRepo().populateFillInTheBlanks()
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
            animationSpec = tween(durationMillis = 1500),
            label = "ScaleAnimation"
        )

        // Animate the alpha of the text
        val alphaAnimation by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(durationMillis = 1500),
            label = "AlphaAnimation"
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

                // Navigate based on login status and role
                navigateHome(this@SplashActivity)
//                var intent = Intent(this@SplashActivity, OnBoardingActivity::class.java)
////                intent.putExtra("GAME_ID","ftsZ97mL7R5AjV6TgrSy")
//                startActivity(intent)
                finish()

            } else {
                Toast.makeText(
                    this@SplashActivity,
                    "No internet connection. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                // If no internet, close the app after the toast duration
                delay(3000L)
                finish()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6A1B9A),
                            Color(0xFF8E24AA),
                            Color(0xFFAB47BC)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Quiz,
                    contentDescription = "Quiz App Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scaleAnimation),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Srijan Quiz App",
                    color = Color.White.copy(alpha = alphaAnimation),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // NEW: Function to handle navigation based on shared preferences (isLoggedIn and Role)
    private fun navigateHome(context: Context) {
        // Use a consistent name for shared preferences storage
        val prefs = context.getSharedPreferences("SrijanQuizApp", Context.MODE_PRIVATE)

        // 1. Retrieve stored values. Default role is empty string, default isLoggedIn is false.
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        val role = prefs.getString("Role", "")

        val targetActivity: Class<*> = when {
            // Rule 1: If logged in AND role is admin -> Admin Home
            isLoggedIn && (role.equals("admin", ignoreCase = true)||role.equals("staff",ignoreCase = true)) -> AdminHomeActivity::class.java

            // Rule 2: If logged in AND role is student -> Student Home
            isLoggedIn && role.equals("student", ignoreCase = true) -> StudentHomeActivity::class.java

            // Rule 3 (Default/Fallback): If not logged in or role is unknown -> OnBoarding
            else -> OnBoardingActivity::class.java
        }

        context.startActivity(Intent(context, targetActivity))
        // Finish the splash screen activity so the user cannot navigate back to it
        (context as Activity).finish()
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
