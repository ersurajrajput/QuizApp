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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ersurajrajput.quizapp.models.DragAndDropModel
import com.ersurajrajput.quizapp.models.DragAndDropOptions
import com.ersurajrajput.quizapp.models.DragAndDropPages
import com.ersurajrajput.quizapp.repo.DragAndDropRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme
import kotlinx.coroutines.launch
import java.util.UUID

class AddNewDragAndDropActivity : ComponentActivity() {

    private val repo = DragAndDropRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID")

        setContent {
            QuizAppTheme {
                AddEditDragAndDropScreen(
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
fun AddEditDragAndDropScreen(
    repo: DragAndDropRepo,
    isNew: Boolean,
    activityId: String?,
    onFinish: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf<List<DragAndDropPages>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNew) }
    var showAddPageDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val screenTitle = if (isNew) "Add Drag & Drop Quiz" else "Edit Drag & Drop Quiz"

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Quiz Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Quiz Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showAddPageDialog = true }) { Text("Add Page") }

                LazyColumn(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                    items(pages, key = { it.id }) { page ->
                        PageItem(page = page, onDeletePage = { pages = pages.filter { it.id != page.id } })
                    }
                }

                Button(
                    onClick = {
                        val quizToSave = DragAndDropModel(
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
        AddDragDropPageDialog(
            onDismiss = { showAddPageDialog = false },
            onSavePage = { newOptions ->
                pages = pages + DragAndDropPages(id = UUID.randomUUID().toString(), options = newOptions)
                showAddPageDialog = false
            }
        )
    }
}

@Composable
private fun PageItem(page: DragAndDropPages, onDeletePage: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Page", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDeletePage) { Icon(Icons.Default.Delete, "Delete Page", tint = MaterialTheme.colorScheme.error) }
            }
            page.options.forEach { option ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(option.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    AsyncImage(
                        model = option.imageUri,
                        contentDescription = "Option Image",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun AddDragDropPageDialog(onDismiss: () -> Unit, onSavePage: (List<DragAndDropOptions>) -> Unit) {
    val options = remember { mutableStateListOf(DragAndDropOptions(id = UUID.randomUUID().toString())) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isUploading) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                        CircularProgressIndicator()
                        Text("Uploading...", modifier = Modifier.padding(top = 80.dp))
                    }
                } else {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text("Add New Page with Options", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))

                        options.forEachIndexed { index, option ->
                            var name by remember { mutableStateOf(option.name) }
                            var imageUri by remember { mutableStateOf<Uri?>(null) }
                            val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageUri = uri }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        options[index] = options[index].copy(name = it)
                                    },
                                    label = { Text("Option Name ${index + 1}") },
                                    modifier = Modifier.weight(1f)
                                )
                                DragDropImageUploadBox(
                                    imageUri = imageUri,
                                    onClick = { imageLauncher.launch("image/*") }
                                )
                            }
                            LaunchedEffect(imageUri) {
                                options[index] = options[index].copy(imageUri = imageUri?.toString() ?: "")
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        IconButton(onClick = { options.add(DragAndDropOptions(id = UUID.randomUUID().toString())) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Icon(Icons.Default.Add, contentDescription = "Add another option")
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onDismiss) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isUploading = true
                                        val uploadedOptions = mutableListOf<DragAndDropOptions>()
                                        var uploadsCompleted = 0
                                        val optionsToUpload = options.filter { it.name.isNotBlank() && it.imageUri.isNotBlank() }

                                        if (optionsToUpload.isEmpty()) {
                                            Toast.makeText(context, "Add at least one valid option with name and image.", Toast.LENGTH_SHORT).show()
                                            isUploading = false
                                            return@launch
                                        }

                                        optionsToUpload.forEach { option ->
                                            MediaManager.get().upload(Uri.parse(option.imageUri)).callback(object : UploadCallback {
                                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                                    var url = resultData["url"] as? String
                                                    if(url?.startsWith("http://") == true) {
                                                        url = url.replaceFirst("http://", "https://")
                                                    }
                                                    uploadedOptions.add(option.copy(imageUri = url ?: ""))
                                                    uploadsCompleted++
                                                    if (uploadsCompleted == optionsToUpload.size) {
                                                        onSavePage(uploadedOptions)
                                                    }
                                                }
                                                override fun onError(requestId: String, error: ErrorInfo) {
                                                    uploadsCompleted++
                                                    if (uploadsCompleted == optionsToUpload.size) {
                                                        Toast.makeText(context, "Some images failed to upload.", Toast.LENGTH_SHORT).show()
                                                        onSavePage(uploadedOptions)
                                                    }
                                                }
                                                override fun onStart(requestId: String) {}
                                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                                                override fun onReschedule(requestId: String, error: ErrorInfo) {}
                                            }).dispatch()
                                        }
                                    }
                                },
                                enabled = options.any { it.name.isNotBlank() && it.imageUri.isNotBlank() }
                            ) { Text("Save Page") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DragDropImageUploadBox(modifier: Modifier = Modifier, imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(Icons.Default.Upload, contentDescription = "Upload Image")
        }
    }
}
