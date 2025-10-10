package com.ersurajrajput.quizapp.screens.admin.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.models.ImageMCQModel
import com.ersurajrajput.quizapp.repo.ImageMcqRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageMcqManagementActivity : ComponentActivity() {
    private val repo = ImageMcqRepo()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                ImageMcqListScreen(repo = repo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageMcqListScreen(repo: ImageMcqRepo) {
    var activities by remember { mutableStateOf<List<ImageMCQModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var activityToDelete by remember { mutableStateOf<ImageMCQModel?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repo.getAllActivities { fetchedActivities ->
            activities = fetchedActivities
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image MCQ Activities") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent = Intent(context, AddNewImageMcqActivity::class.java)
                intent.putExtra("NEW", true)
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activities) { activity ->
                        ImageMcqItemCard(
                            activity = activity,
                            onEdit = {
                                val intent = Intent(context, AddNewImageMcqActivity::class.java)
                                intent.putExtra("NEW", false)
                                intent.putExtra("ID", activity.id)
                                context.startActivity(intent)
                            },
                            onDelete = { activityToDelete = activity }
                        )
                    }
                }
            }
        }

        activityToDelete?.let { activity ->
            ConfirmImageMcqDeletionDialog(
                activityTitle = activity.title,
                onConfirm = {
                    scope.launch(Dispatchers.IO) {
                        repo.deleteActivity(activity.id) { success ->
                            scope.launch {
                                if (success) {
                                    activities = activities.filter { it.id != activity.id }
                                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                }
                                activityToDelete = null
                            }
                        }
                    }
                },
                onDismiss = { activityToDelete = null }
            )
        }
    }
}

@Composable
fun ImageMcqItemCard(
    activity: ImageMCQModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Activity")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Activity",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmImageMcqDeletionDialog(
    activityTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete '$activityTitle'?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

