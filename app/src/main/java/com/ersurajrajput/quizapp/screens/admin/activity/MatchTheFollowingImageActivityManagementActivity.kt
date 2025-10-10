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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.models.MatchTheFollowingImageModel
import com.ersurajrajput.quizapp.repo.MatchTheFollowingImageRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import kotlinx.coroutines.launch

class MatchTheFollowingImageActivityManagementActivity : ComponentActivity() {

    private val repo = MatchTheFollowingImageRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                MatchTheFollowingImageListScreen(repo = repo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchTheFollowingImageListScreen(repo: MatchTheFollowingImageRepo) {
    var activities by remember { mutableStateOf<List<MatchTheFollowingImageModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var activityToDelete by remember { mutableStateOf<MatchTheFollowingImageModel?>(null) }

    // Use DisposableEffect to manage the lifecycle of the real-time listener
    DisposableEffect(Unit) {
        val listener = repo.getQuizList { updatedList ->
            activities = updatedList
            if (isLoading) isLoading = false
        }
        onDispose {
            listener.remove()
        }
    }

    activityToDelete?.let { activity ->
        ImageMatchDeleteConfirmationDialog(
            activityTitle = activity.title,
            onConfirm = {
                scope.launch {
                    repo.deleteQuiz(activity.id) { success ->
                        scope.launch {
                            val message = if (success) "Deleted: ${activity.title}" else "Failed to delete item"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                activityToDelete = null
            },
            onDismiss = {
                activityToDelete = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image Matching Activities") },
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
            FloatingActionButton(
                onClick = {
                    var intent = Intent(context, AddNewMatchTheFollowingImageActivity::class.java)
                    intent.putExtra("NEW",true)
                    context.startActivity(intent)
                    Toast.makeText(context, "Navigate to Add Screen", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Activity")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activities, key = { it.id }) { activity ->
                    MatchTheFollowingImageItemCard(
                        activity = activity,
                        onEdit = {
                           var intent = Intent(context, AddNewMatchTheFollowingImageActivity::class.java)
                            intent.putExtra("NEW",false)
                            intent.putExtra("ID",activity.id)
                            context.startActivity(intent)
                            Toast.makeText(context, "Edit: ${activity.title}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = {
                            activityToDelete = activity
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MatchTheFollowingImageItemCard(
    activity: MatchTheFollowingImageModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Assuming your model has a 'desc' field. If not, you can remove this Text composable.
                Text(
                    text = activity.desc,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Activity")
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
fun ImageMatchDeleteConfirmationDialog(
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
                onClick = {
                    onConfirm()
                    onDismiss()
                },
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

@Preview(showBackground = true)
@Composable
fun MatchTheFollowingImageListScreenPreview() {
    QuizAppTheme {
        MatchTheFollowingImageListScreen(repo = MatchTheFollowingImageRepo())
    }
}
