package com.ersurajrajput.quizapp.screens.student.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.models.MatchTheFollowingImageModel
import com.ersurajrajput.quizapp.repo.MatchTheFollowingImageRepo
import com.ersurajrajput.quizapp.screens.student.activity.player.MatchTheFollowingImagePlayerActivity
import com.ersurajrajput.quizapp.screens.student.activity.player.MatchTheFollowingTextImagePlayerActivity
import com.ersurajrajput.quizapp.screens.student.activity.ui.theme.QuizAppTheme

class MatchTheFollowingImageBasedActivitySelectorActivity : ComponentActivity() {

    private val repo = MatchTheFollowingImageRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                MatchImageBasedQuizListScreen(repo = repo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchImageBasedQuizListScreen(repo: MatchTheFollowingImageRepo) {
    var activities by remember { mutableStateOf<List<MatchTheFollowingImageModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Use DisposableEffect to manage the lifecycle of the real-time listener
    DisposableEffect(Unit) {
        val listener = repo.getQuizList { fetchedActivities ->
            activities = fetchedActivities
            isLoading = false
        }
        // This will be called when the composable leaves the screen
        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Image Matching Quiz") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (activities.isEmpty()) {
                Text("No quizzes found.", modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(activities, key = { index, activity -> "${activity.id}_$index" }) { _, activity ->
                        MatchImageBasedQuizItemCard(
                            activity = activity,
                            onClick = {
                                var intent = Intent(context,
                                    MatchTheFollowingImagePlayerActivity::class.java)
                                intent.putExtra("ID",activity.id)
                                context.startActivity(intent)
                                Toast.makeText(context, "Starting: ${activity.title}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatchImageBasedQuizItemCard(
    activity: MatchTheFollowingImageModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activity.desc,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
