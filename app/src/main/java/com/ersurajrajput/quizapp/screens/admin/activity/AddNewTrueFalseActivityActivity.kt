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
import androidx.compose.foundation.selection.selectable
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
import com.ersurajrajput.quizapp.models.TrueFalseActivityModel
import com.ersurajrajput.quizapp.models.TrueFalseQuestion
import com.ersurajrajput.quizapp.repo.TrueFalseActivityRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddNewTrueFalseActivityActivity : ComponentActivity() {

    private val repo = TrueFalseActivityRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID")

        setContent {
            QuizAppTheme {
                AddEditTrueFalseScreen(
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
fun AddEditTrueFalseScreen(
    repo: TrueFalseActivityRepo,
    isNew: Boolean,
    activityId: String?,
    onFinish: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<TrueFalseQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNew) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val screenTitle = if (isNew) "Add New Activity" else "Edit Activity"

    LaunchedEffect(activityId) {
        if (!isNew && activityId != null) {
            repo.getActivityById(activityId) { activity ->
                if (activity != null) {
                    title = activity.title
                    description = activity.desc
                    questions = activity.questions
                } else {
                    Toast.makeText(context, "Activity not found", Toast.LENGTH_SHORT).show()
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
                    label = { Text("Activity Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Activity Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
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
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(questions) { question ->
                        QuestionItemCard(question = question, onDelete = {
                            questions = questions.filter { it.id != question.id }
                            Toast.makeText(context, "Question removed locally", Toast.LENGTH_SHORT).show()
                        })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val activityToSave = TrueFalseActivityModel(
                            id = if (isNew) UUID.randomUUID().toString() else activityId!!,
                            title = title.trim(),
                            desc = description.trim(),
                            questions = questions
                        )

                        scope.launch(Dispatchers.IO) {
                            val action = if (isNew) "added" else "updated"
                            val operation = if (isNew) repo::addActivity else repo::updateActivity

                            operation(activityToSave) { success ->
                                scope.launch {
                                    if (success) {
                                        Toast.makeText(context, "Activity successfully $action", Toast.LENGTH_SHORT).show()
                                        onFinish()
                                    } else {
                                        Toast.makeText(context, "Failed to $action activity", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isNew) "Add Activity" else "Save Changes")
                }
            }
        }
        if (showAddQuestionDialog) {
            AddQuestionDialog(
                onDismiss = { showAddQuestionDialog = false },
                onAddQuestion = { questionText: String, isTrue: Boolean ->
                    val newQuestion = TrueFalseQuestion(
                        id = UUID.randomUUID().toString(),
                        text = questionText,
                        correctAnswer = isTrue
                    )
                    questions = questions + newQuestion
                    showAddQuestionDialog = false
                }
            )
        }

    }
}

@Composable
fun QuestionItemCard(question: TrueFalseQuestion, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (question.correctAnswer) "TRUE" else "FALSE",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (question.correctAnswer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Question",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}


@Composable
fun AddQuestionDialog(
    onDismiss: () -> Unit,
    onAddQuestion: (questionText: String, isTrue: Boolean) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Question") },
        text = {
            Column {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Correct Answer:", style = MaterialTheme.typography.bodyLarge)
                Row(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .selectable(
                                selected = (selectedOption),
                                onClick = { onOptionSelected(true) }
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedOption),
                            onClick = { onOptionSelected(true) }
                        )
                        Text(text = "True")
                    }
                    Row(
                        Modifier
                            .selectable(
                                selected = (!selectedOption),
                                onClick = { onOptionSelected(false) }
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (!selectedOption),
                            onClick = { onOptionSelected(false) }
                        )
                        Text(text = "False")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank()) {
                        onAddQuestion(questionText.trim(), selectedOption)
                    }
                }
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

