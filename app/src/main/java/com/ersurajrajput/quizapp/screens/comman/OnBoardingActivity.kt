package com.ersurajrajput.quizapp.screens.comman

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.screens.admin.AdminLoginActivity
//import com.ersurajrajput.quizapp.screens.admin.AdminLoginActivity
import com.ersurajrajput.quizapp.screens.comman.ui.theme.QuizAppTheme
import com.ersurajrajput.quizapp.screens.student.StudentHomeActivity

class OnBoardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnBoardingScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
// Helper function to save session data
fun saveSession(context: Context, isLoggedIn: Boolean, role: String) {
    val prefs = context.getSharedPreferences("SrijanQuizApp", Context.MODE_PRIVATE)
    prefs.edit()
        .putBoolean("isLoggedIn", isLoggedIn)
        .putString("Role", role)
        .apply()
}

@Composable
fun OnBoardingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Text(
            text = "Please select your role to continue",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        SelectionBox(
            text = "Student",
            icon = Icons.Default.School,
            onClick = {
//                 Navigate to StudentHomeActivity
                saveSession(context,true,"student")
                val intent = Intent(context, StudentHomeActivity::class.java)
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SelectionBox(
            text = "Admin",
            icon = Icons.Default.AdminPanelSettings,
            onClick = {
//                 Navigate to AdminLoginActivity
                val intent = Intent(context, AdminLoginActivity::class.java)
                context.startActivity(intent)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionBox(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun OnBoardingScreenPreview() {
    QuizAppTheme {
        OnBoardingScreen()
    }
}


