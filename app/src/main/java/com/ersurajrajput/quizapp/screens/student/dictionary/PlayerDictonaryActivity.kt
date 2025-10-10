package com.ersurajrajput.quizapp.screens.student.dictionary

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ersurajrajput.quizapp.models.DictionaryModel
import com.ersurajrajput.quizapp.models.VocabularyWord
import com.ersurajrajput.quizapp.repo.DictionaryRepo

// --- Custom Colors ---
val Parchment = Color(0xFFF5EEDC)
val DarkBrown = Color(0xFF5D4037)
val MidBrown = Color(0xFF8D6E63)
val SelectedBrown = Color(0xFFBCAAA4)

class PlayerDictonaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()

        val gameId = intent.getStringExtra("GAME_ID")

        setContent {
            MaterialTheme {
                if (gameId.isNullOrBlank()) {
                    Toast.makeText(this, "Dictionary ID not found.", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    PlayerDictionaryScreen(gameId = gameId)
                }
            }
        }
    }
}

// --- Main Screen ---
@Composable
fun PlayerDictionaryScreen(gameId: String) {
    var dictionary by remember { mutableStateOf<DictionaryModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(gameId) {
        DictionaryRepo().getDictionaryById(gameId) { fetchedDictionary ->
            dictionary = fetchedDictionary
            isLoading = false
        }
    }

    Surface(
        color = Parchment,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkBrown)
                }
            }
            dictionary != null -> {
                PlayerDictionaryContent(dictionary!!)
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Dictionary not found.", color = DarkBrown, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun PlayerDictionaryContent(dictionary: DictionaryModel) {
    val vocabulary = dictionary.vocabularyWord
    var selectedWord by remember { mutableStateOf(vocabulary.firstOrNull()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    val context = LocalContext.current as? Activity

    val filteredList = remember(searchQuery, selectedLetter, vocabulary) {
        var tempList = vocabulary
        selectedLetter?.let { letter ->
            tempList = tempList.filter { it.word.startsWith(letter.toString(), ignoreCase = true) }
        }
        if (searchQuery.isNotEmpty()) {
            tempList = tempList.filter { it.word.contains(searchQuery, ignoreCase = true) }
        }
        tempList
    }

    LaunchedEffect(filteredList) {
        if (filteredList.isNotEmpty() && !filteredList.contains(selectedWord)) {
            selectedWord = filteredList.first()
        } else if (filteredList.isEmpty()) {
            selectedWord = null
        }
    }

    val dashedBorder = BorderStroke(2.dp, Brush.horizontalGradient(listOf(DarkBrown, MidBrown)))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .border(dashedBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        VocabularyHeader(
            title = dictionary.title,
            onBackClicked = { context?.finish() }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MainContent(word = selectedWord, modifier = Modifier.weight(0.65f))
            Sidebar(
                words = filteredList,
                selectedWord = selectedWord,
                onWordSelected = { selectedWord = it },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedLetter = selectedLetter,
                onLetterSelected = { letter ->
                    selectedLetter = if (selectedLetter == letter) null else letter
                },
                modifier = Modifier.weight(0.35f)
            )
        }
    }
}


@Composable
fun VocabularyHeader(title: String, onBackClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBrown, shape = RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Parchment
            )
        }
        Text(
            text = title,
            color = Parchment,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MainContent(word: VocabularyWord?, modifier: Modifier = Modifier) {
    var showFullScreenImage by remember { mutableStateOf(false) }
    val brownBorder = BorderStroke(4.dp, MidBrown)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (word != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box {
                GlideImage(
                    model = word.imageUrl,
                    contentDescription = word.word,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(brownBorder, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = { showFullScreenImage = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Full Screen",
                        tint = Color.White
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = brownBorder,
                colors = CardDefaults.cardColors(containerColor = MidBrown)
            ) {
                Text(
                    text = word.word,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = brownBorder,
                colors = CardDefaults.cardColors(containerColor = MidBrown)
            ) {
                Text(
                    text = word.definition,
                    fontSize = 20.sp,
                    color = Parchment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(brownBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No words to display.",
                    color = DarkBrown,
                    fontSize = 22.sp
                )
            }
        }
    }

    if (showFullScreenImage && word != null) {
        FullScreenImageDialog(
            imageUrl = word.imageUrl,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            GlideImage(
                model = imageUrl,
                contentDescription = "Full screen image",
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.9f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
fun Sidebar(
    words: List<VocabularyWord>,
    selectedWord: VocabularyWord?,
    onWordSelected: (VocabularyWord) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val brownBorder = BorderStroke(2.dp, Brush.horizontalGradient(listOf(DarkBrown, MidBrown)))
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(brownBorder, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkBrown,
                unfocusedBorderColor = MidBrown,
                cursorColor = DarkBrown
            )
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(('A'..'Z').toList()) { letter ->
                val isLetterSelected = letter == selectedLetter
                Button(
                    onClick = { onLetterSelected(letter) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLetterSelected) DarkBrown else MidBrown
                    )
                ) {
                    Text(letter.toString(), color = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (words.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No words found.",
                            modifier = Modifier.padding(16.dp),
                            color = DarkBrown
                        )
                    }
                }
            } else {
                items(words) { word ->
                    val isSelected = word.word == selectedWord?.word
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SelectedBrown else Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onWordSelected(word) }
                    ) {
                        Text(
                            text = word.word,
                            fontSize = 18.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = DarkBrown,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

