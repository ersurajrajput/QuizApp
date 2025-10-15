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
import com.ersurajrajput.quizapp.models.FillInTheBlanksModel
import com.ersurajrajput.quizapp.repo.FillInTheBlanksRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import com.google.firebase.firestore.ListenerRegistration

class FillInTheBlanksActivityManagementActivity : ComponentActivity() {

    private val repo = FillInTheBlanksRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                val context = LocalContext.current
                var quizList by remember { mutableStateOf<List<FillInTheBlanksModel>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                // Only one Firestore listener
                DisposableEffect(Unit) {
                    val listenerRegistration: ListenerRegistration = repo.getQuizList { updatedList ->
                        quizList = updatedList
                        isLoading = false
                    }
                    onDispose {
                        listenerRegistration.remove()
                    }
                }

                FillInTheBlanksManagementScreen(
                    quizList = quizList,
                    isLoading = isLoading,
                    onDeleteQuiz = { quizId ->
                        repo.deleteQuiz(quizId) { success ->
                            if (success) {
                                Toast.makeText(context, "Quiz deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onAddQuiz = {
                        var intent = Intent(context, AddNewFillInTheBlanksActivity::class.java)
                        intent.putExtra("NEW",true)
                        context.startActivity(intent)
                    },
                    onEditQuiz = { quiz ->
                        Toast.makeText(context, "Editing: ${quiz.title}", Toast.LENGTH_SHORT).show()
                        var intent = Intent(context, AddNewFillInTheBlanksActivity::class.java)
                        intent.putExtra("NEW",false)
                        intent.putExtra("ID",quiz.id)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillInTheBlanksManagementScreen(
    quizList: List<FillInTheBlanksModel>,
    isLoading: Boolean,
    onDeleteQuiz: (String) -> Unit,
    onAddQuiz: () -> Unit,
    onEditQuiz: (FillInTheBlanksModel) -> Unit
) {
    var context = LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Manage Quizzes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddQuiz) {
                Icon(Icons.Filled.Add, contentDescription = "Add Quiz")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                quizList.isEmpty() -> Text("No quizzes found. Tap the '+' button to add one!")
                else -> QuizList(
                    quizList = quizList,
                    onDeleteQuiz = onDeleteQuiz,
                    onEditQuiz = onEditQuiz
                )
            }
        }
    }
}

@Composable
fun QuizList(
    quizList: List<FillInTheBlanksModel>,
    onDeleteQuiz: (String) -> Unit,
    onEditQuiz: (FillInTheBlanksModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(quizList, key = { it.id }) { quiz ->
            QuizListItem(
                quiz = quiz,
                onDeleteClick = { onDeleteQuiz(quiz.id) },
                onEditClick = { onEditQuiz(quiz) }
            )
        }
    }
}

@Composable
fun QuizListItem(
    quiz: FillInTheBlanksModel,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = quiz.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = quiz.desc, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${quiz.questions.size} questions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit Quiz")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Quiz", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FillInTheBlanksManagementScreenPreview() {
    QuizAppTheme {
        val sampleQuestions1 = listOf(
            com.ersurajrajput.quizapp.models.FillInTheBlanksQuestions("q1", "The capital of France is ____.", "Paris"),
            com.ersurajrajput.quizapp.models.FillInTheBlanksQuestions("q2", "____ is the largest planet in our solar system.", "Jupiter")
        )
        val sampleQuestions2 = listOf(
            com.ersurajrajput.quizapp.models.FillInTheBlanksQuestions("q3", "Water is composed of hydrogen and ____.", "Oxygen")
        )

        val sampleQuizzes = listOf(
            FillInTheBlanksModel("1", "European Capitals", "A quiz about famous capitals in Europe.", sampleQuestions1),
            FillInTheBlanksModel("2", "Basic Science", "A simple quiz on elementary science concepts.", sampleQuestions2)
        )

        FillInTheBlanksManagementScreen(
            quizList = sampleQuizzes,
            isLoading = false,
            onDeleteQuiz = {},
            onAddQuiz = {},
            onEditQuiz = {}
        )
    }
}
