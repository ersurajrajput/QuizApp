package com.ersurajrajput.quizapp.screens.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

// --- DATA MODELS (All in one file) ---

/**
 * Represents a custom activity that can contain a mix of different question types.
 */
data class CustomActivityModel(
    val id: String = "",
    val title: String = "",
    val desc: String = "",
    // Note: This list is for local state. For Firestore, we convert it to a map.
    val questions: List<CustomQuestion> = emptyList()
)

/**
 * Enum to clearly identify the type of question.
 */
enum class QuestionType {
    TRUE_FALSE,
    FILL_IN_THE_BLANK,
    TEXT_MCQ,
    MATCH_THE_FOLLOWING,
    DRAG_AND_DROP,
    IMAGE_BASED_MCQ,
    MATCH_THE_FOLLOWING_IMAGE_BASED
}

/**
 * A sealed class representing a generic question.
 */
sealed class CustomQuestion {
    abstract val id: String
    abstract val text: String
    abstract val type: QuestionType
}

data class TrueFalseQuestion(
    override val id: String,
    override val text: String,
    val correctAnswer: Boolean,
    override val type: QuestionType = QuestionType.TRUE_FALSE
) : CustomQuestion()

data class FillInTheBlankQuestion(
    override val id: String,
    override val text: String,
    val correctAnswer: String,
    override val type: QuestionType = QuestionType.FILL_IN_THE_BLANK
) : CustomQuestion()

data class TextMCQQuestion(
    override val id: String,
    override val text: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    override val type: QuestionType = QuestionType.TEXT_MCQ
) : CustomQuestion()

data class MatchItem(val prompt: String, val answer: String)
data class MatchTheFollowingQuestion(
    override val id: String,
    override val text: String,
    val items: List<MatchItem>,
    override val type: QuestionType = QuestionType.MATCH_THE_FOLLOWING
) : CustomQuestion()

data class DragDropItem(val itemText: String, val targetZone: String)
data class DragAndDropQuestion(
    override val id: String,
    override val text: String,
    val items: List<DragDropItem>,
    val zones: List<String>,
    override val type: QuestionType = QuestionType.DRAG_AND_DROP
) : CustomQuestion()

data class ImageMCQQuestion(
    override val id: String,
    override val text: String,
    val options: List<String>, // URLs
    val correctOptionIndex: Int,
    override val type: QuestionType = QuestionType.IMAGE_BASED_MCQ
) : CustomQuestion()

data class ImageMatchItem(val promptImageUrl: String, val answerImageUrl: String)
data class MatchTheFollowingImageBasedQuestion(
    override val id: String,
    override val text: String,
    val items: List<ImageMatchItem>,
    override val type: QuestionType = QuestionType.MATCH_THE_FOLLOWING_IMAGE_BASED
) : CustomQuestion()


// --- FIRESTORE REPOSITORY ---

class CustomActivityRepo {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("custom")

    /**
     * Converts a CustomQuestion sealed class instance into a Map suitable for Firestore.
     */
    private fun questionToMap(question: CustomQuestion): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>(
            "id" to question.id,
            "text" to question.text,
            "type" to question.type.name
        )
        when (question) {
            is TrueFalseQuestion -> map["correctAnswer"] = question.correctAnswer
            is FillInTheBlankQuestion -> map["correctAnswer"] = question.correctAnswer
            is TextMCQQuestion -> {
                map["options"] = question.options
                map["correctOptionIndex"] = question.correctOptionIndex
            }
            is MatchTheFollowingQuestion -> map["items"] = question.items.map { mapOf("prompt" to it.prompt, "answer" to it.answer) }
            is DragAndDropQuestion -> {
                map["items"] = question.items.map { mapOf("itemText" to it.itemText, "targetZone" to it.targetZone) }
                map["zones"] = question.zones
            }
            is ImageMCQQuestion -> {
                map["options"] = question.options
                map["correctOptionIndex"] = question.correctOptionIndex
            }
            is MatchTheFollowingImageBasedQuestion -> map["items"] = question.items.map { mapOf("promptImageUrl" to it.promptImageUrl, "answerImageUrl" to it.answerImageUrl) }
        }
        return map
    }

    /**
     * Saves a CustomActivityModel to Firestore.
     */
    suspend fun addActivity(activity: CustomActivityModel): Boolean {
        return try {
            val activityMap = mapOf(
                "id" to activity.id,
                "title" to activity.title,
                "desc" to activity.desc,
                "questions" to activity.questions.map { questionToMap(it) }
            )
            collection.document(activity.id).set(activityMap).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}


// --- ACTIVITY ---

class CustomeActivityAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                CustomActivityScreen(repo = CustomActivityRepo())
            }
        }
    }
}

// --- MAIN SCREEN COMPOSABLE ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomActivityScreen(repo: CustomActivityRepo) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<CustomQuestion>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Custom Activity") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
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
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Activity Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Question")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Questions", style = MaterialTheme.typography.titleLarge)
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(questions, key = { it.id }) { question ->
                    QuestionItemCard(question = question) {
                        questions = questions.filter { it.id != question.id }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (title.isBlank() || desc.isBlank()) {
                        Toast.makeText(context, "Title and description cannot be empty.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val activityModel = CustomActivityModel(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        desc = desc,
                        questions = questions
                    )
                    scope.launch {
                        val success = repo.addActivity(activityModel)
                        if (success) {
                            Toast.makeText(context, "Activity saved successfully!", Toast.LENGTH_SHORT).show()
                            activity?.finish()
                        } else {
                            Toast.makeText(context, "Failed to save activity.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Activity")
            }
        }
    }

    if (showDialog) {
        AddQuestionDialogController(
            onDismiss = { showDialog = false },
            onQuestionAdded = { newQuestion ->
                questions = questions + newQuestion
                showDialog = false
            }
        )
    }
}

// --- DYNAMIC DIALOG CONTROLLER ---

@Composable
fun AddQuestionDialogController(
    onDismiss: () -> Unit,
    onQuestionAdded: (CustomQuestion) -> Unit
) {
    var selectedType by remember { mutableStateOf<QuestionType?>(null) }

    if (selectedType == null) {
        SelectQuestionTypeDialog(
            onTypeSelected = { selectedType = it },
            onDismiss = onDismiss
        )
    } else {
        // Based on the selected type, show the correct input form
        when (selectedType) {
            QuestionType.TRUE_FALSE -> TrueFalseInputDialog(onDismiss, onQuestionAdded)
            QuestionType.FILL_IN_THE_BLANK -> FillInTheBlankInputDialog(onDismiss, onQuestionAdded)
            QuestionType.TEXT_MCQ -> TextMCQInputDialog(onDismiss, onQuestionAdded)
            QuestionType.MATCH_THE_FOLLOWING -> MatchTheFollowingInputDialog(onDismiss, onQuestionAdded)
            QuestionType.DRAG_AND_DROP -> DragAndDropInputDialog(onDismiss, onQuestionAdded)
            QuestionType.IMAGE_BASED_MCQ -> ImageMCQInputDialog(onDismiss, onQuestionAdded)
            QuestionType.MATCH_THE_FOLLOWING_IMAGE_BASED -> MatchTheFollowingImageBasedInputDialog(onDismiss, onQuestionAdded)
            else -> {} // This else branch makes the 'when' exhaustive.
        }
    }
}

// --- DIALOGS FOR EACH QUESTION TYPE ---

@Composable
fun SelectQuestionTypeDialog(
    onTypeSelected: (QuestionType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Question Type") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                QuestionType.values().forEach { type ->
                    Text(
                        text = type.name.replace('_', ' '),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTypeSelected(type) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TrueFalseInputDialog(
    onDismiss: () -> Unit,
    onQuestionAdded: (TrueFalseQuestion) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isTrue by remember { mutableStateOf(true) }

    FormDialog(
        title = "Add True/False Question",
        onDismiss = onDismiss,
        onConfirm = {
            val question = TrueFalseQuestion(UUID.randomUUID().toString(), text, isTrue)
            onQuestionAdded(question)
        }
    ) {
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Question Text") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isTrue, onClick = { isTrue = true })
            Text("True")
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = !isTrue, onClick = { isTrue = false })
            Text("False")
        }
    }
}

@Composable
fun FillInTheBlankInputDialog(
    onDismiss: () -> Unit,
    onQuestionAdded: (FillInTheBlankQuestion) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    FormDialog(
        title = "Add Fill in the Blank Question",
        onDismiss = onDismiss,
        onConfirm = {
            val question = FillInTheBlankQuestion(UUID.randomUUID().toString(), text, answer)
            onQuestionAdded(question)
        }
    ) {
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Question (use '_' for blank)") })
        OutlinedTextField(value = answer, onValueChange = { answer = it }, label = { Text("Correct Answer") })
    }
}

@Composable
fun TextMCQInputDialog(
    onDismiss: () -> Unit,
    onQuestionAdded: (TextMCQQuestion) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "", "", "") }
    var correctIndex by remember { mutableStateOf(0) }

    FormDialog(
        title = "Add Text MCQ Question",
        onDismiss = onDismiss,
        onConfirm = {
            val question = TextMCQQuestion(UUID.randomUUID().toString(), text, options.toList(), correctIndex)
            onQuestionAdded(question)
        }
    ) {
        OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Question Text") })
        options.forEachIndexed { index, option ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = correctIndex == index, onClick = { correctIndex = index })
                OutlinedTextField(
                    value = option,
                    onValueChange = { options[index] = it },
                    label = { Text("Option ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MatchTheFollowingInputDialog(onDismiss: () -> Unit, onQuestionAdded: (MatchTheFollowingQuestion) -> Unit) {
    var text by remember { mutableStateOf("") }
    val prompts = remember { mutableStateListOf("", "") }
    val answers = remember { mutableStateListOf("", "") }
    val matches = remember { mutableStateMapOf<Int, Int>() } // maps prompt index to answer index
    var selectedPromptIndex by remember { mutableStateOf<Int?>(null) }
    val promptCoordinates = remember { mutableStateMapOf<Int, Offset>() }
    val answerCoordinates = remember { mutableStateMapOf<Int, Offset>() }
    val boxLayoutCoordinates = remember { mutableStateOf<Offset?>(null) }
    val context = LocalContext.current
    val lineColor = MaterialTheme.colorScheme.primary


    FormDialog(
        title = "Add Match The Following",
        onDismiss = onDismiss,
        onConfirm = {
            val validItems = matches.mapNotNull { (promptIndex, answerIndex) ->
                val prompt = prompts.getOrNull(promptIndex)?.trim()
                val answer = answers.getOrNull(answerIndex)?.trim()
                if (!prompt.isNullOrBlank() && !answer.isNullOrBlank()) MatchItem(prompt, answer) else null
            }

            if (text.isNotBlank() && validItems.isNotEmpty()) {
                val question = MatchTheFollowingQuestion(UUID.randomUUID().toString(), text, validItems)
                onQuestionAdded(question)
            } else {
                Toast.makeText(context, "Please provide instruction text and match at least one pair.", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Instruction Text") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .onGloballyPositioned { layoutCoordinates ->
                    boxLayoutCoordinates.value = layoutCoordinates.localToWindow(Offset.Zero)
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                matches.forEach { (promptIndex, answerIndex) ->
                    val start = promptCoordinates[promptIndex]
                    val end = answerCoordinates[answerIndex]
                    val boxOffset = boxLayoutCoordinates.value ?: Offset.Zero
                    if (start != null && end != null) {
                        // Correctly translate window coordinates to local canvas coordinates
                        val startInCanvas = start - boxOffset
                        val endInCanvas = end - boxOffset

                        drawLine(
                            color = lineColor,
                            start = startInCanvas,
                            end = endInCanvas,
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Prompts Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Prompts", fontWeight = FontWeight.Bold)
                    prompts.forEachIndexed { index, prompt ->
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompts[index] = it },
                            label = { Text("Prompt ${index + 1}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    promptCoordinates[index] = coordinates.localToWindow(Offset(coordinates.size.width.toFloat(), coordinates.size.height / 2f))
                                }
                                .clickable { selectedPromptIndex = index }
                                .border(
                                    width = if (selectedPromptIndex == index) 2.dp else 0.dp,
                                    color = if (selectedPromptIndex == index) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Answers Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Answers", fontWeight = FontWeight.Bold)
                    answers.forEachIndexed { index, answer ->
                        val isMatched = matches.containsValue(index)
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answers[index] = it },
                            label = { Text("Answer ${index + 1}") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    answerCoordinates[index] = coordinates.localToWindow(Offset(0f, coordinates.size.height / 2f))
                                }
                                .clickable {
                                    selectedPromptIndex?.let { promptIndex ->
                                        if (!isMatched) {
                                            matches[promptIndex] = index
                                            selectedPromptIndex = null
                                        }
                                    }
                                }
                                .background(
                                    if (isMatched) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = {
                prompts.add("")
                answers.add("")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Pair")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Add Pair")
        }
    }
}

@Composable
fun DragAndDropInputDialog(onDismiss: () -> Unit, onQuestionAdded: (DragAndDropQuestion) -> Unit) {
    var text by remember { mutableStateOf("") }
    val zones = remember { mutableStateListOf("", "", "") }
    val items = remember { mutableStateListOf(DragDropItem("", ""), DragDropItem("", ""), DragDropItem("", "")) }

    FormDialog(
        title = "Add Drag and Drop Question",
        onDismiss = onDismiss,
        onConfirm = {
            val validZones = zones.filter { it.isNotBlank() }
            val validItems = items.filter { it.itemText.isNotBlank() && it.targetZone.isNotBlank() }

            if (text.isNotBlank() && validZones.isNotEmpty() && validItems.isNotEmpty()) {
                val question = DragAndDropQuestion(
                    id = UUID.randomUUID().toString(),
                    text = text,
                    zones = validZones,
                    items = validItems
                )
                onQuestionAdded(question)
            }
        }
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Instruction Text") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Drop Zones", fontWeight = FontWeight.Bold)
        zones.forEachIndexed { index, zone ->
            OutlinedTextField(
                value = zone,
                onValueChange = { zones[index] = it },
                label = { Text("Zone ${index + 1}") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Draggable Items & Correct Zone", fontWeight = FontWeight.Bold)
        items.forEachIndexed { index, item ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = item.itemText,
                    onValueChange = { items[index] = items[index].copy(itemText = it) },
                    label = { Text("Item ${index + 1}") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = item.targetZone,
                    onValueChange = { items[index] = items[index].copy(targetZone = it) },
                    label = { Text("Correct Zone") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ImageMCQInputDialog(onDismiss: () -> Unit, onQuestionAdded: (ImageMCQQuestion) -> Unit) {
    FormDialog(title = "Add Image MCQ", onDismiss = onDismiss, onConfirm = { /* ... */ }) {
        Text("Image upload functionality needs to be implemented here.")
    }
}

@Composable
fun MatchTheFollowingImageBasedInputDialog(onDismiss: () -> Unit, onQuestionAdded: (MatchTheFollowingImageBasedQuestion) -> Unit) {
    FormDialog(title = "Add Image Matching", onDismiss = onDismiss, onConfirm = { /* ... */ }) {
        Text("Image upload functionality for matching pairs needs to be implemented here.")
    }
}

// --- GENERIC DIALOG & ITEM CARD COMPOSABLES ---

@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Button(onClick = onConfirm) { Text("Add") }
                }
            }
        }
    }
}


@Composable
fun QuestionItemCard(question: CustomQuestion, onDelete: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(question.text, fontWeight = FontWeight.Bold)
                    Text(question.type.name.replace('_', ' '), style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            // Display specific details based on question type
            when (question) {
                is TrueFalseQuestion -> Text("Answer: ${if (question.correctAnswer) "True" else "False"}")
                is FillInTheBlankQuestion -> Text("Answer: ${question.correctAnswer}")
                is TextMCQQuestion -> {
                    question.options.forEachIndexed { index, option ->
                        Text(
                            text = "- $option",
                            fontWeight = if (index == question.correctOptionIndex) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                is MatchTheFollowingQuestion -> {
                    question.items.forEach { item ->
                        Text("- ${item.prompt} -> ${item.answer}")
                    }
                }
                is ImageMCQQuestion -> Text("Image MCQ - Details not shown in list.")
                is DragAndDropQuestion -> {
                    Text("Zones: ${question.zones.joinToString()}", fontWeight = FontWeight.SemiBold)
                    question.items.forEach { item ->
                        Text("- Drag '${item.itemText}' to '${item.targetZone}'")
                    }
                }
                is MatchTheFollowingImageBasedQuestion -> Text("Image Matching - Details not shown in list.")
            }
        }
    }
}

