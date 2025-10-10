package com.ersurajrajput.quizapp.screens.admin

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.ersurajrajput.quizapp.models.DictionaryModel
import com.ersurajrajput.quizapp.models.VocabularyWord
import com.ersurajrajput.quizapp.repo.DictionaryRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme

class AddNewDictionaryActivity : ComponentActivity() {

    private val dictionaryRepo = DictionaryRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isNew = intent.getBooleanExtra("NEW", true)
        val dictId = intent.getStringExtra("ID") ?: ""

        if (!isNew && dictId.isNotBlank()) {
            var loading by mutableStateOf(true)
            var dictionary by mutableStateOf<DictionaryModel?>(null)

            dictionaryRepo.getDictionaryById(dictId) { dict ->
                dictionary = dict
                loading = false
            }

            setContent {
                QuizAppTheme {
                    if (loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        dictionary?.let {
                            AddDictionaryScreen(
                                isNew = false,
                                existingDictionary = it
                            ) { title, desc, words ->
                                val updatedDict = DictionaryModel(
                                    id = it.id,
                                    title = title,
                                    desc = desc,
                                    vocabularyWord = words
                                )
                                dictionaryRepo.saveDictionary(updatedDict) { success, sms ->
                                    if (success) {
                                        Toast.makeText(
                                            this,
                                            "Dictionary updated successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        finish()
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "Failed to update dictionary: $sms",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Dictionary not found!")
                            }
                        }
                    }
                }
            }
        } else {
            setContent {
                QuizAppTheme {
                    AddDictionaryScreen(isNew = true) { title, desc, words ->
                        val newDict = DictionaryModel(
                            title = title,
                            desc = desc,
                            vocabularyWord = words
                        )
                        dictionaryRepo.saveDictionary(newDict) { success, sms ->
                            if (success) {
                                Toast.makeText(this, "Dictionary added successfully!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Failed to add dictionary: $sms", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDictionaryScreen(
    isNew: Boolean,
    existingDictionary: DictionaryModel? = null,
    onSave: (title: String, desc: String, words: List<VocabularyWord>) -> Unit
) {
    var title by remember { mutableStateOf(existingDictionary?.title ?: "") }
    var description by remember { mutableStateOf(existingDictionary?.desc ?: "") }
    var wordsList by remember { mutableStateOf(existingDictionary?.vocabularyWord ?: emptyList()) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    var context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Dictionary", fontWeight = FontWeight.Bold) },
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
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Dictionary Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddWordDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Vocabulary Word")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showAddWordDialog) {
                AddWordDialog(
                    onDismiss = { showAddWordDialog = false },
                    onSave = { newWord ->
                        wordsList = wordsList + newWord
                        showAddWordDialog = false
                    }
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(wordsList) { index, word ->
                    VocabularyWordItem(
                        word = word,
                        onWordChange = { updatedWord ->
                            val newList = wordsList.toMutableList()
                            newList[index] = updatedWord
                            wordsList = newList
                        },
                        onDelete = {
                            wordsList = wordsList.toMutableList().also { it.removeAt(index) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onSave(title.trim(), description.trim(), wordsList)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && description.isNotBlank() && wordsList.isNotEmpty()
            ) {
                Text(if (isNew) "Save Dictionary" else "Update Dictionary")
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VocabularyWordItem(
    word: VocabularyWord,
    onWordChange: (VocabularyWord) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (word.imageUrl.isNotBlank()) {
                    GlideImage(
                        model = word.imageUrl,
                        contentDescription = word.word,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "No Image",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Word",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                OutlinedTextField(
                    value = word.word,
                    onValueChange = { onWordChange(word.copy(word = it)) },
                    label = { Text("Word") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = word.definition,
                    onValueChange = { onWordChange(word.copy(definition = it)) },
                    label = { Text("Definition") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AddWordDialog(
    onDismiss: () -> Unit,
    onSave: (VocabularyWord) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var definition by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadedImageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            isUploading = true
            MediaManager.get()
                .upload(it)
                .option("folder", "quizzapp")
                .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    uploadedImageUrl = resultData["secure_url"] as String
                    isUploading = false
                    Toast.makeText(context, "Image uploaded!", Toast.LENGTH_SHORT).show()
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    isUploading = false
                    Toast.makeText(context, "Upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add New Word", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Word") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    label = { Text("Definition") },
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .clickable {
                            if (!isUploading) {
                                when (PackageManager.PERMISSION_GRANTED) {
                                    ContextCompat.checkSelfPermission(context, permissionToRequest) -> {
                                        imagePickerLauncher.launch("image/*")
                                    }
                                    else -> {
                                        permissionLauncher.launch(permissionToRequest)
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo")
                            Text("Select Image (Optional)")
                        }
                    } else {
                        GlideImage(
                            model = imageUri,
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (isUploading) {
                        CircularProgressIndicator()
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (word.isNotBlank() && definition.isNotBlank()) {
                                onSave(VocabularyWord(word.trim(), definition.trim(), uploadedImageUrl))
                            }
                        },
                        enabled = word.isNotBlank() && definition.isNotBlank() && !isUploading
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
