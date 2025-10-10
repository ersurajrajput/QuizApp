package com.ersurajrajput.quizapp.screens.admin.activity

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ersurajrajput.quizapp.models.MatchPair
import com.ersurajrajput.quizapp.models.MatchPairPage
import com.ersurajrajput.quizapp.models.MatchTheFollowingModel
import com.ersurajrajput.quizapp.repo.MatchTheFollowingRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import java.util.UUID

class AddNewMatchTheFollowingActivity : ComponentActivity() {

    private val repo = MatchTheFollowingRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID")

        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                AddEditMatchTheFollowingScreen(
                    repo = repo,
                    isNew = isNew,
                    activityId = activityId,
                    onFinish = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMatchTheFollowingScreen(
    repo: MatchTheFollowingRepo,
    isNew: Boolean,
    activityId: String?,
    onFinish: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf<List<MatchPairPage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNew) }
    var showAddPairDialogForPageId by remember { mutableStateOf<String?>(null) }
    var showAddPageDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val screenTitle = if (isNew) "Add New Quiz" else "Edit Quiz"

    LaunchedEffect(activityId) {
        if (!isNew && activityId != null) {
            repo.getQuizById(activityId) { quiz ->
                if (quiz != null) {
                    title = quiz.title
                    desc = quiz.desc
                    pages = quiz.pages
                } else {
                    Toast.makeText(context, "Quiz not found", Toast.LENGTH_SHORT).show()
                    onFinish()
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quiz Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Quiz Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Button(onClick = { showAddPageDialog = true }) {
                    Text("Add Page")
                }

                LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    items(pages, key = { it.id }) { page ->
                        PageItem(
                            page = page,
                            onAddPair = { showAddPairDialogForPageId = page.id },
                            onDeletePair = { pairId ->
                                pages = pages.map { p ->
                                    if (p.id == page.id) {
                                        p.copy(pairs = p.pairs.filter { it.id != pairId })
                                    } else p
                                }
                            },
                            onDeletePage = {
                                pages = pages.filter { it.id != page.id }
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        val quizToSave = MatchTheFollowingModel(
                            id = if (isNew) "" else activityId!!,
                            title = title,
                            desc = desc,
                            pages = pages
                        )
                        repo.saveQuiz(quizToSave) { success, _ ->
                            if (success) {
                                Toast.makeText(context, "Quiz saved!", Toast.LENGTH_SHORT).show()
                                onFinish()
                            } else {
                                Toast.makeText(context, "Failed to save.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(if (isNew) "Save Quiz" else "Update Quiz")
                }
            }
        }
    }

    if (showAddPageDialog) {
        AddPageWithPairsDialog(
            onDismiss = { showAddPageDialog = false },
            onSavePage = { newPairs ->
                pages = pages + MatchPairPage(id = UUID.randomUUID().toString(), pairs = newPairs)
                showAddPageDialog = false
            }
        )
    }

    if (showAddPairDialogForPageId != null) {
        AddPairDialog(
            onDismiss = { showAddPairDialogForPageId = null },
            onAddPair = { left, right ->
                val newPair = MatchPair(id = UUID.randomUUID().toString(), leftOption = left, rightOption = right)
                pages = pages.map {
                    if (it.id == showAddPairDialogForPageId) {
                        it.copy(pairs = it.pairs + newPair)
                    } else it
                }
                showAddPairDialogForPageId = null
            }
        )
    }
}

@Composable
private fun PageItem(
    page: MatchPairPage,
    onAddPair: () -> Unit,
    onDeletePair: (String) -> Unit,
    onDeletePage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Page", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDeletePage) {
                    Icon(Icons.Default.Delete, "Delete Page", tint = MaterialTheme.colorScheme.error)
                }
            }
            page.pairs.forEach { pair ->
                PairItem(pair = pair, onDelete = { onDeletePair(pair.id) })
            }
            Button(onClick = onAddPair, modifier = Modifier.align(Alignment.End)) {
                Text("Add Pair")
            }
        }
    }
}

@Composable
private fun PairItem(pair: MatchPair, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${pair.leftOption} -> ${pair.rightOption}", modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Delete Pair", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AddPairDialog(
    onDismiss: () -> Unit,
    onAddPair: (left: String, right: String) -> Unit
) {
    var left by remember { mutableStateOf("") }
    var right by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Pair") },
        text = {
            Column {
                OutlinedTextField(value = left, onValueChange = { left = it }, label = { Text("Left Option") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = right, onValueChange = { right = it }, label = { Text("Right Option") })
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddPair(left, right) },
                enabled = left.isNotBlank() && right.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddPageWithPairsDialog(
    onDismiss: () -> Unit,
    onSavePage: (List<MatchPair>) -> Unit
) {
    val pairs = remember { mutableStateListOf(MatchPair(id = UUID.randomUUID().toString())) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add New Page", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(16.dp))

                pairs.forEachIndexed { index, pair ->
                    var left by remember { mutableStateOf(pair.leftOption) }
                    var right by remember { mutableStateOf(pair.rightOption) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = left,
                            onValueChange = {
                                left = it
                                pairs[index] = pairs[index].copy(leftOption = it)
                            },
                            label = { Text("Left ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = right,
                            onValueChange = {
                                right = it
                                pairs[index] = pairs[index].copy(rightOption = it)
                            },
                            label = { Text("Right ${index + 1}") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                IconButton(
                    onClick = { pairs.add(MatchPair(id = UUID.randomUUID().toString())) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add another pair")
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val filledPairs = pairs.filter { it.leftOption.isNotBlank() && it.rightOption.isNotBlank() }
                            onSavePage(filledPairs)
                        },
                        enabled = pairs.any { it.leftOption.isNotBlank() && it.rightOption.isNotBlank() }
                    ) { Text("Save Page") }
                }
            }
        }
    }
}

