package com.ersurajrajput.quizapp.screens.admin.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ersurajrajput.quizapp.models.TrueFalseActivityModel
import com.ersurajrajput.quizapp.repo.TrueFalseActivityRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrueFalseActivityManagementActivity : ComponentActivity() {

    private val repo = TrueFalseActivityRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                TrueFalseActivityListScreen(repo = repo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrueFalseActivityListScreen(repo: TrueFalseActivityRepo) {
    var activities by remember { mutableStateOf<List<TrueFalseActivityModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var activityToDelete by remember { mutableStateOf<TrueFalseActivityModel?>(null) }
    val scope = rememberCoroutineScope()

    // Function to reload data from the repository
    val reloadActivities = {
        scope.launch {
            isLoading = true
            repo.getAllActivities { list ->
                activities = list
                isLoading = false
            }
        }
    }

    // This launcher starts the Add/Edit activity. When we return, its callback
    // is triggered, where we can reload the data to see changes.
    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        reloadActivities()
    }

    // Initial data fetch when the screen is first composed
    LaunchedEffect(Unit) {
        reloadActivities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("True/False Activities") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddNewTrueFalseActivityActivity::class.java)
                    intent.putExtra("NEW", true)
                    activityLauncher.launch(intent)
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add True/False Activity")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activities) { activity ->
                        TrueFalseActivityItemCard(
                            activity = activity,
                            onEdit = {
                                val intent =
                                    Intent(context, AddNewTrueFalseActivityActivity::class.java)
                                intent.putExtra("NEW", false)
                                intent.putExtra("ID", activity.id)
                                activityLauncher.launch(intent)
                            },
                            onDelete = { activityToDelete = activity }
                        )
                    }
                }
            }
        }

        // Confirmation Dialog for Deletion
        activityToDelete?.let { activity ->
            // Use the renamed dialog to avoid conflict
            ConfirmActivityDeletionDialog(
                activityTitle = activity.title,
                onConfirm = {
                    scope.launch(Dispatchers.IO) {
                        repo.deleteActivity(activity.id) { success ->
                            scope.launch {
                                if (success) {
                                    activities = activities.filter { it.id != activity.id }
                                    Toast.makeText(context, "Deleted ${activity.title}", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    activityToDelete = null
                },
                onDismiss = { activityToDelete = null }
            )
        }
    }
}

@Composable
fun TrueFalseActivityItemCard(
    activity: TrueFalseActivityModel,
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

// Renamed this function to resolve the overload conflict
@Composable
fun ConfirmActivityDeletionDialog(
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

