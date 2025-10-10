package com.ersurajrajput.quizapp.screens.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme

// region Data Models for Questions
sealed class Question(val type: String) {
    data class Mcq(
        val questionText: String,
        val correctAnswer: String,
        val options: List<String>
    ) : Question("MCQ Activity")

    data class TrueFalse(
        val statement: String,
        val isTrue: Boolean
    ) : Question("True or False")

    data class FillInTheBlanks(
        val questionText: String,
        val correctAnswer: String
    ) : Question("Fill in the blanks")

    data class MatchTheFollowing(
        val pairs: Map<String, String>
    ) : Question("Match the Followings")
    // Add other question types here
}
// endregion

class ActivitysManagemantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                ActivityManagementScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityManagementScreen(modifier: Modifier = Modifier) {
    var activityName by remember { mutableStateOf("") }
    var activityDescription by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var showQuestionDialog by remember { mutableStateOf(false) }
    var questions by remember { mutableStateOf(listOf<Question>()) }

    val activityTypes = listOf(
        "MCQ Activity",
        "True or False",
        "Fill in the blanks",
        "Match the Followings",
        "Drag and Drop",
        "MCQ Image Based",
        "Match the Followings Image based"
    )
    var selectedActivityType by remember { mutableStateOf(activityTypes[0]) }

    if (showQuestionDialog) {
        AddQuestionDialog(
            activityType = selectedActivityType,
            onDismiss = { showQuestionDialog = false },
            onAddQuestion = { question ->
                questions = questions + question
                showQuestionDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Activity") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
        ) {
            OutlinedTextField(
                value = activityName,
                onValueChange = { activityName = it },
                label = { Text("Activity Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = activityDescription,
                onValueChange = { activityDescription = it },
                label = { Text("Activity Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )
            Spacer(Modifier.height(16.dp))
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = selectedActivityType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Activity Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    activityTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedActivityType = type
                                questions = emptyList() // Clear questions when type changes
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Button(onClick = { showQuestionDialog = true }) {
                Text("Add Question")
            }
            Spacer(Modifier.height(16.dp))

            Text("Questions:", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(questions) { question ->
                    QuestionItem(question = question, onDelete = { questions = questions - question })
                }
            }

            Button(
                onClick = { /* Handle save action */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Activity")
            }
        }
    }
}

@Composable
fun QuestionItem(question: Question, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                when (question) {
                    is Question.Mcq -> Text("MCQ: ${question.questionText}")
                    is Question.TrueFalse -> Text("T/F: ${question.statement}")
                    is Question.FillInTheBlanks -> Text("Fill-in: ${question.questionText}")
                    is Question.MatchTheFollowing -> Text("Match: ${question.pairs.size} pairs")
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete question")
            }
        }
    }
}

@Composable
fun AddQuestionDialog(
    activityType: String,
    onDismiss: () -> Unit,
    onAddQuestion: (Question) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Add New Question", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                when (activityType) {
                    "MCQ Activity" -> McqQuestionForm { onAddQuestion(it) }
                    "True or False" -> TrueFalseQuestionForm { onAddQuestion(it) }
                    "Fill in the blanks" -> FillInTheBlanksQuestionForm { onAddQuestion(it) }
                    "Match the Followings" -> MatchTheFollowingQuestionForm { onAddQuestion(it) }
                    else -> Text("This activity type is not yet supported.")
                }

                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}

// region Question Input Forms
@Composable
fun McqQuestionForm(onSave: (Question.Mcq) -> Unit) {
    var questionText by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var option1 by remember { mutableStateOf("") }
    var option2 by remember { mutableStateOf("") }
    var option3 by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = questionText, onValueChange = { questionText = it }, label = { Text("Question") })
        OutlinedTextField(value = correctAnswer, onValueChange = { correctAnswer = it }, label = { Text("Correct Answer") })
        OutlinedTextField(value = option1, onValueChange = { option1 = it }, label = { Text("Option 1 (Incorrect)") })
        OutlinedTextField(value = option2, onValueChange = { option2 = it }, label = { Text("Option 2 (Incorrect)") })
        OutlinedTextField(value = option3, onValueChange = { option3 = it }, label = { Text("Option 3 (Incorrect)") })
        Button(onClick = {
            val options = listOf(option1, option2, option3)
            onSave(Question.Mcq(questionText, correctAnswer, options))
        }) {
            Text("Save Question")
        }
    }
}

@Composable
fun TrueFalseQuestionForm(onSave: (Question.TrueFalse) -> Unit) {
    var statement by remember { mutableStateOf("") }
    var isTrue by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = statement, onValueChange = { statement = it }, label = { Text("Statement") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isTrue, onClick = { isTrue = true })
            Text("True")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = !isTrue, onClick = { isTrue = false })
            Text("False")
        }
        Button(onClick = { onSave(Question.TrueFalse(statement, isTrue)) }) {
            Text("Save Question")
        }
    }
}

@Composable
fun FillInTheBlanksQuestionForm(onSave: (Question.FillInTheBlanks) -> Unit) {
    var questionText by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = questionText, onValueChange = { questionText = it }, label = { Text("Question (use _ for the blank)") })
        OutlinedTextField(value = answer, onValueChange = { answer = it }, label = { Text("Answer") })
        Button(onClick = { onSave(Question.FillInTheBlanks(questionText, answer)) }) {
            Text("Save Question")
        }
    }
}

@Composable
fun MatchTheFollowingQuestionForm(onSave: (Question.MatchTheFollowing) -> Unit) {
    var item1 by remember { mutableStateOf("") }
    var match1 by remember { mutableStateOf("") }
    // In a real app, you'd have a dynamic list here. This is a simplified example.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Add a matching pair:")
        OutlinedTextField(value = item1, onValueChange = { item1 = it }, label = { Text("Item 1") })
        OutlinedTextField(value = match1, onValueChange = { match1 = it }, label = { Text("Matching Item 1") })
        Button(onClick = {
            if (item1.isNotBlank() && match1.isNotBlank()) {
                onSave(Question.MatchTheFollowing(mapOf(item1 to match1)))
            }
        }) {
            Text("Save Pair")
        }
    }
}
// endregion

@Preview(showBackground = true)
@Composable
fun ActivityManagementScreenPreview() {
    QuizAppTheme {
        ActivityManagementScreen()
    }
}

