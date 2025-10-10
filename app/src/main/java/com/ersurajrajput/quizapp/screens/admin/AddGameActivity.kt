package com.ersurajrajput.quizapp.screens.admin

import GameAnswerOption
import GameModel
import GameQuestion
import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.repo.GamesRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme

val myGameTypes = listOf("Arrow Game", "Basket Ball Game", "Spell Bee", "Unscrambled words")

class AddGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gameId = intent.getStringExtra("GAME_ID") ?: ""
        val newGame = intent.getBooleanExtra("NEW_GAME", true)

        setContent {
            QuizAppTheme {
                AddGameScreen(gameId = gameId, isNewGame = newGame)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameScreen(
    gameId: String,
    isNewGame: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var gameName by remember { mutableStateOf("") }
    var gameDescription by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedGameType by remember { mutableStateOf(myGameTypes[0]) }
    var questions by remember { mutableStateOf(listOf<GameQuestion>()) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) } // ← Loading state

    // Load existing game if editing
    LaunchedEffect(gameId) {
        if (!isNewGame && gameId.isNotBlank()) {
            GamesRepo().getGameById(gameId) { game ->
                game?.let {
                    gameName = it.name
                    gameDescription = it.description
                    selectedGameType = it.gameType
                    questions = it.questions
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Add Games", fontWeight = FontWeight.Bold) },
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = gameName,
                onValueChange = { gameName = it },
                label = { Text("Game Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = gameDescription,
                onValueChange = { gameDescription = it },
                label = { Text("About Game (Description)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedGameType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Game Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    myGameTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                if (selectedGameType != type) {
                                    // When game type changes, clear the old questions
                                    questions = emptyList()
                                }
                                selectedGameType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { showAddQuestionDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Questions")
            }

            // Display added questions
            questions.forEachIndexed { index, question ->
                QuestionItem(
                    gameType = selectedGameType,
                    question = question,
                    onDelete = { questions = questions.toMutableList().also { it.removeAt(index) } }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { (context as? Activity)?.finish() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (gameName.isBlank()) {
                            Toast.makeText(context, "Game name cannot be empty.", Toast.LENGTH_SHORT).show()
                        } else if (questions.isEmpty()) {
                            Toast.makeText(context, "Please add at least one question.", Toast.LENGTH_SHORT).show()
                        } else {
                            val gameModel = GameModel(
                                id = if (isNewGame) "" else gameId,
                                name = gameName,
                                description = gameDescription,
                                gameType = selectedGameType,
                                questions = questions
                            )
                            GamesRepo().saveGame(gameModel) { success, _ ->
                                if (success) {
                                    Toast.makeText(context, "Game saved successfully", Toast.LENGTH_SHORT).show()
                                    (context as? Activity)?.finish()
                                } else {
                                    Toast.makeText(context, "Failed to save game", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

            }
        }
    }
// Show loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showAddQuestionDialog) {
        AddQuestionDialog(
            gameType = selectedGameType,
            onDismiss = { showAddQuestionDialog = false },
            onAddQuestion = { question: GameQuestion ->
                questions = questions + question
                showAddQuestionDialog = false
            }
        )
    }
}

@Composable
fun QuestionItem(gameType: String, question: GameQuestion, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (gameType == "Unscrambled words") "Hint: ${question.questionText}" else question.questionText,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete question")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (gameType == "Unscrambled words") {
                question.options.firstOrNull()?.let {
                    Text(
                        text = "Answer: ${it.text}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            } else {
                question.options.forEach { option ->
                    Text(
                        text = "• ${option.text}",
                        fontWeight = if (option.correct) FontWeight.Bold else FontWeight.Normal,
                        color = if (option.correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddQuestionDialog(
    gameType: String,
    onDismiss: () -> Unit,
    onAddQuestion: (GameQuestion) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    val options = remember { mutableStateOf(listOf("", "", "", "")) }
    var correctOptions by remember { mutableStateOf(listOf(false, false, false, false)) }

    var scrambledWord by remember { mutableStateOf("") }
    var correctWord by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (gameType == "Unscrambled words") "Add Unscrambled Word" else "Add New Question") },
        text = {
            Column {
                if (gameType == "Unscrambled words") {
                    OutlinedTextField(
                        value = scrambledWord,
                        onValueChange = { scrambledWord = it },
                        label = { Text("Scrambled Word / Hint") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = correctWord,
                        onValueChange = { correctWord = it },
                        label = { Text("Correct Word") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (showError) {
                        Text(
                            "Please fill both fields.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Options (select correct answer/s):", fontWeight = FontWeight.Bold)
                    options.value.forEachIndexed { index, optionText ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = correctOptions[index],
                                onCheckedChange = {
                                    correctOptions = correctOptions.toMutableList().also { it[index] = !it[index] }
                                }
                            )
                            OutlinedTextField(
                                value = optionText,
                                onValueChange = {
                                    val newOptions = options.value.toMutableList()
                                    newOptions[index] = it
                                    options.value = newOptions
                                },
                                label = { Text("Option ${index + 1}") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (showError) {
                        Text(
                            "Please fill all fields and select at least one correct answer.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (gameType == "Unscrambled words") {
                    val allFilled = scrambledWord.isNotBlank() && correctWord.isNotBlank()
                    if (allFilled) {
                        val unscrambledQuestion = GameQuestion(
                            questionText = scrambledWord,
                            options = listOf(GameAnswerOption(text = correctWord, correct = true))
                        )
                        onAddQuestion(unscrambledQuestion)
                        onDismiss()
                    } else {
                        showError = true
                    }
                } else {
                    val allFilled = questionText.isNotBlank() &&
                            options.value.all { it.isNotBlank() } &&
                            correctOptions.any { it }
                    if (allFilled) {
                        val answerOptions = options.value.mapIndexed { index, text ->
                            GameAnswerOption(text = text, correct = correctOptions[index])
                        }
                        onAddQuestion(
                            GameQuestion(
                                questionText = questionText,
                                options = answerOptions
                            )
                        )
                        onDismiss()
                    } else {
                        showError = true
                    }
                }
            }) {
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

@Preview(showBackground = true)
@Composable
fun AddGameScreenPreview() {
    QuizAppTheme {
        AddGameScreen(gameId = "", isNewGame = true)
    }
}

