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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ersurajrajput.quizapp.models.AnswerOption
import com.ersurajrajput.quizapp.models.MCQActivityModel
import com.ersurajrajput.quizapp.models.Questions
import com.ersurajrajput.quizapp.repo.MCQActivityRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import kotlinx.coroutines.launch
import java.util.*

class AddNewMCQActivity : ComponentActivity() {

    private val repo = MCQActivityRepo()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID") ?: ""

        setContent {
            QuizAppTheme {
                val context = LocalContext.current
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (isNew) "Add MCQ" else "Edit MCQ") },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            navigationIcon = {
                                IconButton(onClick = {
                                    (context as? Activity)?.finish()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            }

                        )
                    }
                ) { padding ->
                    AddOrEditMCQScreen(
                        isNew = isNew,
                        activityId = activityId,
                        repo = repo,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun AddOrEditMCQScreen(
    isNew: Boolean,
    activityId: String,
    repo: MCQActivityRepo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<Questions>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog state
    var showDialog by remember { mutableStateOf(false) }
    var dialogQTitle by remember { mutableStateOf("") }
    val dialogAnswers = remember { mutableStateListOf<AnswerOption>() }

    // Load existing MCQ activity if editing
    LaunchedEffect(activityId) {
        if (!isNew) {
            val activity = repo.getActivityById(activityId)
            if (activity != null) {
                title = activity.title
                desc = activity.desc
                questions = activity.qList
            }
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(modifier = modifier.padding(16.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Questions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                items(questions) { question ->
                    QuestionItemCard(
                        question = question,
                        onDelete = {
                            questions = questions.toMutableList().also { it.remove(question) }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    dialogQTitle = ""
                    dialogAnswers.clear()
                    dialogAnswers.addAll(
                        listOf(
                            AnswerOption(UUID.randomUUID().toString(), "", false),
                            AnswerOption(UUID.randomUUID().toString(), "", false),
                            AnswerOption(UUID.randomUUID().toString(), "", false),
                            AnswerOption(UUID.randomUUID().toString(), "", false)
                        )
                    )
                    showDialog = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Question")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        val activity = MCQActivityModel(
                            id = if (activityId.isEmpty()) UUID.randomUUID().toString() else activityId,
                            title = title,
                            desc = desc,
                            qList = questions
                        )
                        val success = repo.addOrUpdateActivity(activity)
                        Toast.makeText(
                            context,
                            if (success) "Saved Successfully" else "Error Saving",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save MCQ Activity")
            }
        }
    }

    // Add Question Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Question") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = dialogQTitle,
                        onValueChange = { dialogQTitle = it },
                        label = { Text("Question Text") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Options:")

                    dialogAnswers.forEachIndexed { index, answer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = answer.correct,
                                onCheckedChange = {
                                    for (i in dialogAnswers.indices) {
                                        dialogAnswers[i] =
                                            dialogAnswers[i].copy(correct = i == index && it)
                                    }
                                }
                            )
                            OutlinedTextField(
                                value = answer.text,
                                onValueChange = { newText ->
                                    dialogAnswers[index] = dialogAnswers[index].copy(text = newText)
                                },
                                label = { Text("Option ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (dialogQTitle.isNotBlank() && dialogAnswers.any { it.correct }) {
                        questions = questions + Questions(
                            id = UUID.randomUUID().toString(),
                            qTitle = dialogQTitle,
                            answerOption = dialogAnswers.toList()
                        )
                        showDialog = false
                    } else {
                        Toast.makeText(context, "Add question and select correct option", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("Add Question") }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun QuestionItemCard(question: Questions, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Question: ${question.qTitle}", modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete question")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            question.answerOption.forEach { answer ->
                Text("- ${answer.text} ${if (answer.correct) "(Correct)" else ""}")
            }
        }
    }
}
