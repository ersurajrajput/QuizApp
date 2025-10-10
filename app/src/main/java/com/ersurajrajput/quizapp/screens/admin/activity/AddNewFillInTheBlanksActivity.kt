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
import com.ersurajrajput.quizapp.models.FillInTheBlanksModel
import com.ersurajrajput.quizapp.models.FillInTheBlanksQuestions
import com.ersurajrajput.quizapp.repo.FillInTheBlanksRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import java.util.*

class AddNewFillInTheBlanksActivity : ComponentActivity() {

    private val repo = FillInTheBlanksRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID")

        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                AddEditFillInTheBlanksScreen(
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
fun AddEditFillInTheBlanksScreen(
    repo: FillInTheBlanksRepo,
    isNew: Boolean,
    activityId: String?,
    onFinish: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<FillInTheBlanksQuestions>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNew) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val screenTitle = if (isNew) "Add New Quiz" else "Edit Quiz"

    LaunchedEffect(activityId) {
        if (!isNew && activityId != null) {
            repo.getQuizById(activityId) { quiz ->
                if (quiz != null) {
                    title = quiz.title
                    desc = quiz.desc
                    questions = quiz.questions
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Questions", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = { showAddQuestionDialog = true }) {
                        Text("Add Question")
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    items(questions, key = { it.id }) { question ->
                        QuestionItem(
                            question = question,
                            onDelete = {
                                questions = questions.filter { it.id != question.id }
                            }
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.isBlank() || desc.isBlank()) {
                            Toast.makeText(context, "Title and description cannot be empty.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val quizToSave = FillInTheBlanksModel(
                            id = if (isNew) "" else activityId!!,
                            title = title,
                            desc = desc,
                            questions = questions
                        )
                        repo.saveQuiz(quizToSave) { success, _ ->
                            if (success) {
                                Toast.makeText(context, "Quiz saved successfully!", Toast.LENGTH_SHORT).show()
                                onFinish()
                            } else {
                                Toast.makeText(context, "Failed to save quiz.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isNew) "Save Quiz" else "Update Quiz")
                }
            }
        }
    }

    if (showAddQuestionDialog) {
        AddQuestionDialog(
            onDismiss = { showAddQuestionDialog = false },
            onAddQuestion = { question: String, answer: String ->
                val newQuestion = FillInTheBlanksQuestions(
                    id = UUID.randomUUID().toString(),
                    text = question,
                    ans = answer
                )
                questions = questions + newQuestion
                showAddQuestionDialog = false
            }
        )
    }
}

@Composable
private fun QuestionItem(question: FillInTheBlanksQuestions, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(question.text, fontWeight = FontWeight.SemiBold)
                Text("Answer: ${question.ans}", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddQuestionDialog(
    onDismiss: () -> Unit,
    onAddQuestion: (question: String, answer: String) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }

    val blankSpaceCount = remember(questionText) {
        questionText.split("____").size - 1
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Question") },
        text = {
            Column {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question") }
                )
                TextButton(
                    enabled = blankSpaceCount == 0,
                    onClick = { questionText += "____" },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add Blank Space")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = answerText,
                    onValueChange = { answerText = it },
                    label = { Text("Correct Answer") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank() && answerText.isNotBlank()) {
                        onAddQuestion(questionText, answerText)
                    }
                },
                enabled = blankSpaceCount == 1 && answerText.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

