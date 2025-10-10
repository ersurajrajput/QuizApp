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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ersurajrajput.quizapp.models.MatchTextImagePair
import com.ersurajrajput.quizapp.models.MatchTextImagePage
import com.ersurajrajput.quizapp.models.MatchTheFollowingImageAndTextModel
import com.ersurajrajput.quizapp.repo.MatchTheFollowingImageAndTextRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import kotlinx.coroutines.launch
import java.util.UUID

class AddNewMatchTheFollowingImageAndTextActivity : ComponentActivity() {

    private val repo = MatchTheFollowingImageAndTextRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID")

        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                AddEditMatchTextImageScreen(
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
fun AddEditMatchTextImageScreen(
    repo: MatchTheFollowingImageAndTextRepo,
    isNew: Boolean,
    activityId: String?,
    onFinish: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf<List<MatchTextImagePage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNew) }
    var showAddPageDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val screenTitle = if (isNew) "Add Text-Image Match Quiz" else "Edit Text-Image Match Quiz"

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
                navigationIcon = { IconButton(onClick = onFinish) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
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
                        val quizToSave = MatchTheFollowingImageAndTextModel(
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
        AddPageWithTextImagesDialog(
            onDismiss = { showAddPageDialog = false },
            onSavePage = { newPairs ->
                pages = pages + MatchTextImagePage(id = UUID.randomUUID().toString(), pairs = newPairs)
                showAddPageDialog = false
            }
        )
    }
}

@Composable
private fun PageItem(page: MatchTextImagePage, onDeletePage: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Page", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDeletePage) { Icon(Icons.Default.Delete, "Delete Page", tint = MaterialTheme.colorScheme.error) }
            }
            page.pairs.forEach { pair ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(pair.leftText, modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pair.rightImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(pair.rightImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Matched Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Warning, contentDescription = "Image not available")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddPageWithTextImagesDialog(onDismiss: () -> Unit, onSavePage: (List<MatchTextImagePair>) -> Unit) {
    val pairs = remember { mutableStateListOf(MatchTextImagePair(id = UUID.randomUUID().toString())) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isUploading) {
                    CircularProgressIndicator()
                    Text("Uploading...")
                } else {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text("Add New Page", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))

                        pairs.forEachIndexed { index, pair ->
                            var text by remember { mutableStateOf(pair.leftText) }
                            var imageUri by remember { mutableStateOf<Uri?>(null) }
                            val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> imageUri = uri }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = text,
                                    onValueChange = {
                                        text = it
                                        pairs[index] = pairs[index].copy(leftText = it)
                                    },
                                    label = { Text("Text ${index + 1}") },
                                    modifier = Modifier.weight(1f)
                                )
                                TextImageUploadBox(
                                    imageUri = imageUri,
                                    onClick = { imageLauncher.launch("image/*") }
                                )
                            }
                            LaunchedEffect(imageUri) {
                                pairs[index] = pairs[index].copy(rightImageUrl = imageUri?.toString() ?: "")
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        IconButton(onClick = { pairs.add(MatchTextImagePair(id = UUID.randomUUID().toString())) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Icon(Icons.Default.Add, contentDescription = "Add another pair")
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = onDismiss) { Text("Cancel") }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        isUploading = true
                                        val uploadedPairs = mutableListOf<MatchTextImagePair>()
                                        var uploadsCompleted = 0
                                        val pairsToUpload = pairs.filter { it.leftText.isNotBlank() && it.rightImageUrl.isNotBlank() }

                                        if (pairsToUpload.isEmpty()) {
                                            Toast.makeText(context, "Add at least one valid text-image pair.", Toast.LENGTH_SHORT).show()
                                            isUploading = false
                                            return@launch
                                        }

                                        pairsToUpload.forEach { pair ->
                                            MediaManager.get().upload(Uri.parse(pair.rightImageUrl)).callback(object : UploadCallback {
                                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                                    var url = resultData["url"] as? String
                                                    if(url?.startsWith("http://") == true) {
                                                        url = url.replaceFirst("http://", "https://")
                                                    }
                                                    uploadedPairs.add(pair.copy(rightImageUrl = url ?: ""))
                                                    uploadsCompleted++
                                                    if (uploadsCompleted == pairsToUpload.size) {
                                                        isUploading = false
                                                        onSavePage(uploadedPairs)
                                                    }
                                                }
                                                override fun onError(requestId: String, error: ErrorInfo) {
                                                    uploadsCompleted++
                                                    if (uploadsCompleted == pairsToUpload.size) {
                                                        isUploading = false
                                                        Toast.makeText(context, "Some images failed to upload.", Toast.LENGTH_SHORT).show()
                                                        onSavePage(uploadedPairs)
                                                    }
                                                }
                                                override fun onReschedule(requestId: String, error: ErrorInfo) {}
                                                override fun onStart(requestId: String) {}
                                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                                            }).dispatch()
                                        }
                                    }
                                },
                                enabled = pairs.any { it.leftText.isNotBlank() && it.rightImageUrl.isNotBlank() }
                            ) { Text("Save Page") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextImageUploadBox(modifier: Modifier = Modifier, imageUri: Uri?, onClick: () -> Unit) {
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

