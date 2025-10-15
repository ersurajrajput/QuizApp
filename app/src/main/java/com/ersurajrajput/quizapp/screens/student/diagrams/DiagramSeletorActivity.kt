package com.ersurajrajput.quizapp.screens.student.diagrams

import android.annotation.SuppressLint
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ersurajrajput.quizapp.repo.DummyRepo
import com.ersurajrajput.quizapp.models.DiagramModel
import com.ersurajrajput.quizapp.repo.DiagramRepo
import com.ersurajrajput.quizapp.screens.admin.AddGameActivity
import com.ersurajrajput.quizapp.screens.student.diagrams.ui.theme.QuizAppTheme

class DiagramSelectorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = DiagramRepo()

        setContent {
            QuizAppTheme {
                // Delegate all UI and data fetching logic to the new Composable screen
                DiagramSelectorScreen(repo = repo)
            }
        }
    }
}

// NEW COMPOSABLE: Encapsulates the screen structure and logic
@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramSelectorScreen(repo: DiagramRepo) {
    var diagrams by remember { mutableStateOf<List<DiagramModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val activity = LocalContext.current as? Activity // Get activity reference for finish()

    // fetch data when Composable is first launched
    LaunchedEffect(Unit) {
        repo.getDiagramsList { list ->
            diagrams = list
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Select a Diagram") },
                // Back Button Implementation
                navigationIcon = {
                    if (activity != null) {
                        IconButton(onClick = { activity.finish() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            LoadingScreen()
        } else {
            DiagramList(
                diagrams = diagrams,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}


@Composable
fun DiagramList(diagrams: List<DiagramModel>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(diagrams) { diagram ->
            DiagramItem(diagram)
        }
    }
}

@Composable
fun DiagramItem(diagram: DiagramModel) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable {
                var intent = Intent(context, DiagramPlayerActivity::class.java)
                intent.putExtra("VIDEO_URL", diagram.url)
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schema,
                contentDescription = "Diagram Icon",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = diagram.title.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = diagram.desc.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiagramListPreview() {
    val dummyDiagrams = listOf(
        DiagramModel("d1", "Rabbit", "How to draw a rabbit?", "https://www.youtube.com/watch?v=FArfY7-K_O0"),
        DiagramModel("d2", "Butterfly", "How to draw a butterfly?", "https://www.youtube.com/watch?v=mqR3ulVl2RI")
    )
    QuizAppTheme {
        DiagramList(diagrams = dummyDiagrams)
    }
}
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
