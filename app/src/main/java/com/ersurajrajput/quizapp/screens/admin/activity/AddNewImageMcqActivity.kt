package com.ersurajrajput.quizapp.screens.admin.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ersurajrajput.quizapp.models.ImageMCQModel
import com.ersurajrajput.quizapp.models.ImageMCQOption
import com.ersurajrajput.quizapp.models.ImageMCQQuestion
import com.ersurajrajput.quizapp.repo.ImageMcqRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import kotlinx.coroutines.launch
import java.util.UUID

class AddNewImageMcqActivity : ComponentActivity() {

    private val repo = ImageMcqRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID")

        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                AddEditImageMcqScreen(
                    repo = repo,
                    isNew = isNew,
                    activityId = activityId,
                    onSaveComplete = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditImageMcqScreen(
    repo: ImageMcqRepo,
    isNew: Boolean,
    activityId: String?,
    onSaveComplete: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<ImageMCQQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNew) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(activityId) {
        if (!isNew && activityId != null) {
            repo.getActivityById(activityId) { fetchedActivity ->
                fetchedActivity?.let {
                    title = it.title
                    desc = it.desc
                    questions = it.questions
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Add New Activity" else "Edit Activity") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showAddQuestionDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = "Add Question", modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Add Question")
                    }

                    LazyColumn(modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp)) {
                        items(questions) { question ->
                            QuestionItemCard(question = question, onDelete = {
                                questions = questions.filter { it.id != question.id }
                            })
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isBlank() || desc.isBlank()) {
                                Toast.makeText(context, "Title and Description cannot be empty", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                val activityToSave = ImageMCQModel(
                                    id = if(isNew) UUID.randomUUID().toString() else activityId!!,
                                    title = title,
                                    desc = desc,
                                    questions = questions
                                )

                                val repoAction: (ImageMCQModel, (Boolean) -> Unit) -> Unit =
                                    if (isNew) repo::addActivity else repo::updateActivity
                                repoAction(activityToSave) { success ->
                                    val message = if (success) "Activity ${if (isNew) "added" else "updated"}!" else "Failed to save activity"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) onSaveComplete()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isNew) "Add Activity" else "Save Changes")
                    }
                }
            }
        }

        if (showAddQuestionDialog) {
            AddQuestionDialog(
                onDismiss = { showAddQuestionDialog = false },
                onAddQuestion = { newQuestion ->
                    questions = questions + newQuestion
                    showAddQuestionDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun QuestionItemCard(question: ImageMCQQuestion, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = question.text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Question", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Display options with images
            question.options.forEachIndexed { index, option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (option.imageUrl.isNotBlank()) {
                            GlideImage(
                                model = option.imageUrl,
                                contentDescription = "Option ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Warning, contentDescription = "Image not loaded")
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Option ${index + 1}",
                        fontWeight = if (index == question.correctOptionIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageUploadBox(
    modifier: Modifier = Modifier,
    imageUri: Uri?,
    imageUrl: String?,
    isUploading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isUploading -> CircularProgressIndicator()
            imageUri != null -> GlideImage(
                model = imageUri,
                contentDescription = "Selected Image Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            imageUrl != null -> GlideImage(
                model = imageUrl,
                contentDescription = "Uploaded Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            else -> Icon(
                imageVector = Icons.Default.Upload,
                contentDescription = "Upload Image",
                tint = Color.Gray
            )
        }
    }
}


@Composable
fun AddQuestionDialog(
    onDismiss: () -> Unit,
    onAddQuestion: (ImageMCQQuestion) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    val imageUrls = remember { mutableStateListOf<String?>(null, null, null, null) }
    val imageUris = remember { mutableStateListOf<Uri?>(null, null, null, null) }
    val isUploading = remember { mutableStateListOf(false, false, false, false) }
    var correctOptionIndex by remember { mutableStateOf(0) }
    val context = LocalContext.current
    var activeUploaderIndex by remember { mutableStateOf(-1) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && activeUploaderIndex != -1) {
            val index = activeUploaderIndex
            imageUris[index] = uri
            isUploading[index] = true
            MediaManager.get()
                .upload(uri)
                .option("folder", "quizzapp")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        var url = resultData["url"] as? String
                        if (url?.startsWith("http://") == true) {
                            url = url.replaceFirst("http://", "https://")
                        }
                        imageUrls[index] = url
                        isUploading[index] = false
                    }
                    override fun onError(requestId: String, error: ErrorInfo) {
                        Toast.makeText(context, "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                        isUploading[index] = false
                    }
                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        }
    }

    val isAddButtonEnabled = questionText.isNotBlank() && imageUrls.all { !it.isNullOrBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Question") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Options (upload 4 images):", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Image Uploaders in a 2x2 grid
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ImageUploadBox(imageUri = imageUris[0], imageUrl = imageUrls[0], isUploading = isUploading[0]) {
                        activeUploaderIndex = 0
                        imagePickerLauncher.launch("image/*")
                    }
                    ImageUploadBox(imageUri = imageUris[1], imageUrl = imageUrls[1], isUploading = isUploading[1]) {
                        activeUploaderIndex = 1
                        imagePickerLauncher.launch("image/*")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ImageUploadBox(imageUri = imageUris[2], imageUrl = imageUrls[2], isUploading = isUploading[2]) {
                        activeUploaderIndex = 2
                        imagePickerLauncher.launch("image/*")
                    }
                    ImageUploadBox(imageUri = imageUris[3], imageUrl = imageUrls[3], isUploading = isUploading[3]) {
                        activeUploaderIndex = 3
                        imagePickerLauncher.launch("image/*")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Correct Option:", fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (0..3).forEach { index ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Opt ${index + 1}")
                            RadioButton(
                                selected = correctOptionIndex == index,
                                onClick = { correctOptionIndex = index }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newQuestion = ImageMCQQuestion(
                        id = UUID.randomUUID().toString(),
                        text = questionText.trim(),
                        options = imageUrls.map { url ->
                            ImageMCQOption(id = UUID.randomUUID().toString(), imageUrl = url!!)
                        },
                        correctOptionIndex = correctOptionIndex
                    )
                    onAddQuestion(newQuestion)
                },
                enabled = isAddButtonEnabled
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

