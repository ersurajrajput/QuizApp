package com.ersurajrajput.quizapp.screens.admin.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.ersurajrajput.quizapp.models.MatchImagePair
import com.ersurajrajput.quizapp.models.MatchImagePairPage
import com.ersurajrajput.quizapp.models.MatchTheFollowingImageModel
import com.ersurajrajput.quizapp.repo.MatchTheFollowingImageRepo
import com.ersurajrajput.quizapp.screens.admin.activity.ui.theme.QuizAppTheme
import kotlinx.coroutines.launch
import java.util.UUID

class AddNewMatchTheFollowingImageActivity : ComponentActivity() {

    private val repo = MatchTheFollowingImageRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isNew = intent.getBooleanExtra("NEW", true)
        val activityId = intent.getStringExtra("ID")

        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                AddEditMatchTheFollowingImageScreen(
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
fun AddEditMatchTheFollowingImageScreen(
    repo: MatchTheFollowingImageRepo,
    isNew: Boolean,
    activityId: String?,
    onFinish: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf<List<MatchImagePairPage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(!isNew) }
    var isUploading by remember { mutableStateOf(false) }
    var showAddPageDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val screenTitle = if (isNew) "Add Image Match Quiz" else "Edit Image Match Quiz"

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
        if (isLoading || isUploading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                if (isUploading) Text("Uploading images...", modifier = Modifier.padding(top = 60.dp))
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
                        val quizToSave = MatchTheFollowingImageModel(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isNew) "Save Quiz" else "Update Quiz")
                }
            }
        }
    }

    if (showAddPageDialog) {
        AddImagePageDialog(
            onDismiss = { showAddPageDialog = false },
            onSavePage = { newPairs ->
                pages = pages + MatchImagePairPage(id = UUID.randomUUID().toString(), pairs = newPairs)
                showAddPageDialog = false
            }
        )
    }
}

@Composable
private fun PageItem(page: MatchImagePairPage, onDeletePage: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Page", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onDeletePage) { Icon(Icons.Default.Delete, "Delete Page", tint = MaterialTheme.colorScheme.error) }
            }
            page.pairs.forEach { pair ->
                Log.d("myApp",pair.leftImageUrl+":"+pair.rightImageUrl)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (pair.leftImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(pair.leftImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Left Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Warning, contentDescription = "Image not loaded")
                        }
                    }
                    Text("->")
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
                                contentDescription = "Right Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Warning, contentDescription = "Image not loaded")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddImagePageDialog(onDismiss: () -> Unit, onSavePage: (List<MatchImagePair>) -> Unit) {
    val pairs = remember { mutableStateListOf(MatchImagePair(id = UUID.randomUUID().toString())) }
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
                        Text("Add New Page with Image Pairs", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))

                        pairs.forEachIndexed { index, _ ->
                            var leftUri by remember { mutableStateOf<Uri?>(null) }
                            var rightUri by remember { mutableStateOf<Uri?>(null) }
                            val leftLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> leftUri = uri }
                            val rightLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> rightUri = uri }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ImageUploadBox(imageUri = leftUri, onClick = { leftLauncher.launch("image/*") }, modifier = Modifier.weight(1f))
                                ImageUploadBox(imageUri = rightUri, onClick = { rightLauncher.launch("image/*") }, modifier = Modifier.weight(1f))
                            }

                            LaunchedEffect(leftUri, rightUri) {
                                pairs[index] = pairs[index].copy(
                                    leftImageUrl = leftUri?.toString() ?: "", // Temporarily store Uri string
                                    rightImageUrl = rightUri?.toString() ?: ""
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }

                        IconButton(onClick = { pairs.add(MatchImagePair(id = UUID.randomUUID().toString())) }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
                                        val uploadedPairs = mutableListOf<MatchImagePair>()
                                        var uploadsCompleted = 0
                                        val pairsToUpload = pairs.filter { it.leftImageUrl.isNotBlank() && it.rightImageUrl.isNotBlank() }

                                        if (pairsToUpload.isEmpty()) {
                                            Toast.makeText(context, "Add at least one valid pair.", Toast.LENGTH_SHORT).show()
                                            isUploading = false
                                            return@launch
                                        }

                                        pairsToUpload.forEach { pair ->
                                            var leftUrl: String? = null
                                            var rightUrl: String? = null

                                            fun checkCompletion() {
                                                if (leftUrl != null && rightUrl != null) {
                                                    uploadedPairs.add(MatchImagePair(id = pair.id, leftImageUrl = leftUrl!!, rightImageUrl = rightUrl!!))
                                                    uploadsCompleted++
                                                    if (uploadsCompleted == pairsToUpload.size) {
                                                        isUploading = false
                                                        onSavePage(uploadedPairs)
                                                    }
                                                }
                                            }

                                            MediaManager.get().upload(Uri.parse(pair.leftImageUrl)).callback(object : UploadCallback {
                                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                                    var url = resultData["url"] as? String
                                                    if (url?.startsWith("http://") == true) {
                                                        url = url.replaceFirst("http://", "https://")
                                                    }
                                                    leftUrl = url
                                                    checkCompletion()
                                                }
                                                override fun onError(requestId: String, error: ErrorInfo) { /* handle error */ }
                                                override fun onReschedule(requestId: String, error: ErrorInfo) { /* handle error */ }
                                                override fun onStart(requestId: String) {}
                                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                                            }).dispatch()

                                            MediaManager.get().upload(Uri.parse(pair.rightImageUrl)).callback(object : UploadCallback {
                                                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                                    var url = resultData["url"] as? String
                                                    if (url?.startsWith("http://") == true) {
                                                        url = url.replaceFirst("http://", "https://")
                                                    }
                                                    rightUrl = url
                                                    checkCompletion()
                                                }
                                                override fun onError(requestId: String, error: ErrorInfo) { /* handle error */ }
                                                override fun onReschedule(requestId: String, error: ErrorInfo) { /* handle error */ }
                                                override fun onStart(requestId: String) {}
                                                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                                            }).dispatch()
                                        }
                                    }
                                },
                                enabled = pairs.any { it.leftImageUrl.isNotBlank() && it.rightImageUrl.isNotBlank() }
                            ) { Text("Save Page") }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ImageUploadBox(modifier: Modifier = Modifier, imageUri: Uri?, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(100.dp)
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

