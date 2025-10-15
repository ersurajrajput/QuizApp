package com.ersurajrajput.quizapp.screens.student

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.screens.admin.deleteSession
import com.ersurajrajput.quizapp.screens.admin.ui.theme.ActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.comman.OnBoardingActivity
import com.ersurajrajput.quizapp.screens.student.dictionary.DictionarySelectorActivity
import com.ersurajrajput.quizapp.screens.student.diagrams.DiagramSelectorActivity
import com.ersurajrajput.quizapp.screens.student.games.GameTypeSelectorActivity
import com.ersurajrajput.quizapp.screens.student.ui.theme.QuizAppTheme

class StudentHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                StudentHomeScreen()
            }
        }
    }
}
fun deleteSession(context: Context){
    val prefs = context.getSharedPreferences("SrijanQuizApp", Context.MODE_PRIVATE)
    prefs.edit()
        .clear()
        .apply()

}
// --- Data Model ---
data class Feature(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val features = listOf(
        Feature("Activities", Icons.Filled.Extension),
        Feature("Games", Icons.Filled.SportsEsports),
        Feature("Dictionary", Icons.Filled.AutoStories),
        Feature("Diagram", Icons.Filled.Schema)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Logout",
                            modifier = Modifier
                                .graphicsLayer { rotationY = 180f } // Flip icon
                                .size(24.dp)
                                .clickable {
                                    deleteSession(context)

                                    // 2. Navigate to OnBoardingActivity and clear back stack
                                    val intent = Intent(context, OnBoardingActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    context.startActivity(intent)
                                },
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(80.dp))
                        Text(
                            text = "Student Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(features) { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = {
                            when (feature.name) {
                                "Dictionary" -> {
                                    val intent = Intent(context, DictionarySelectorActivity::class.java)
                                    context.startActivity(intent)
                                }
                                "Diagram" -> {
                                    val intent = Intent(context, DiagramSelectorActivity::class.java)
                                    context.startActivity(intent)
                                }
                                "Games" -> {
                                    val intent = Intent(context, GameTypeSelectorActivity::class.java)
                                    context.startActivity(intent)
                                }
                                "Activities"->{
                                    val intent = Intent(context, StudentActivity::class.java)
                                    context.startActivity(intent)
                                }
                                else -> {
                                    // Handle other features
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(
    feature: Feature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(150.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.name,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = feature.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentHomeScreenPreview() {
    QuizAppTheme {
        StudentHomeScreen()
    }
}
