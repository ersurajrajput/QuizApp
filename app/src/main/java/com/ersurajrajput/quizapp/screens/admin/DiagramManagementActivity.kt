package com.ersurajrajput.quizapp.screens.admin

import android.app.Activity
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
import com.ersurajrajput.quizapp.models.DiagramModel
import com.ersurajrajput.quizapp.repo.DiagramRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme
import com.google.firebase.firestore.ListenerRegistration

class DiagramManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                DiagramManagementScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagramManagementScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val repo = remember { DiagramRepo() }

    var diagrams by remember { mutableStateOf(mutableListOf<DiagramModel>()) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedDiagram by remember { mutableStateOf<DiagramModel?>(null) }
    var loading by remember { mutableStateOf(true) }

    var listener: ListenerRegistration? by remember { mutableStateOf(null) }

    // Load diagrams async
    LaunchedEffect(Unit) {
        listener = repo.getDiagramsList { list ->
            diagrams = list.toMutableList()
            loading = false
        }
    }

    DisposableEffect(Unit) {
        onDispose { listener?.remove() }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Manage Diagrams", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedDiagram = null
                showAddEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Diagram")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (loading) {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else if (diagrams.isEmpty()) {
                Text("No diagrams found", Modifier.align(Alignment.CenterHorizontally).padding(20.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(diagrams, key = { it.id }) { diagram ->
                        DiagramCard(
                            diagram = diagram,
                            onEditClick = { selectedDiagram = it; showAddEditDialog = true },
                            onDeleteClick = { selectedDiagram = it; showDeleteDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditDiagramDialog(
            diagram = selectedDiagram,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { name, desc, url ->
                val newDiagram = selectedDiagram?.copy(
                    title = name,
                    desc = desc,
                    url = url
                ) ?: DiagramModel(
                    id = "",
                    title = name,
                    desc = desc,
                    url = url
                )

                repo.saveDiagram(newDiagram) { success, ->
                    if (success) Toast.makeText(context, "Diagram saved", Toast.LENGTH_SHORT).show()
                    else Toast.makeText(context, "Failed to save diagram", Toast.LENGTH_SHORT).show()
                }

                showAddEditDialog = false
                selectedDiagram = null
            }
        )
    }

    if (showDeleteDialog) {
        DeleteDiagramConfirmationDialog(
            diagramName = selectedDiagram?.title ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                selectedDiagram?.let { diagram ->
                    repo.deleteDiagram(diagram.id) { success ->
                        if (success) Toast.makeText(context, "Diagram deleted", Toast.LENGTH_SHORT).show()
                        else Toast.makeText(context, "Failed to delete diagram", Toast.LENGTH_SHORT).show()
                    }
                }
                showDeleteDialog = false
                selectedDiagram = null
            }
        )
    }
}

@Composable
fun DiagramCard(
    diagram: DiagramModel,
    onEditClick: (DiagramModel) -> Unit,
    onDeleteClick: (DiagramModel) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(diagram.title, style = MaterialTheme.typography.titleLarge)
                Text(diagram.desc, style = MaterialTheme.typography.bodyMedium)
                Text(diagram.url, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = { onEditClick(diagram) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDeleteClick(diagram) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDiagramDialog(
    diagram: DiagramModel?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, desc: String, url: String) -> Unit
) {
    var name by remember { mutableStateOf(diagram?.title ?: "") }
    var desc by remember { mutableStateOf(diagram?.desc ?: "") }
    var url by remember { mutableStateOf(diagram?.url ?: "") }
    var isNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (diagram == null) "Add Diagram" else "Edit Diagram") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isNameError = false },
                    label = { Text("Name") },
                    isError = isNameError,
                    singleLine = true
                )
                if (isNameError) Text("Name cannot be empty", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Video URL") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) isNameError = true
                else onConfirm(name, desc, url)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DeleteDiagramConfirmationDialog(
    diagramName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Diagram") },
        text = { Text("Are you sure you want to delete '$diagramName'?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
