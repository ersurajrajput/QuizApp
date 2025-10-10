package com.ersurajrajput.quizapp.screens.student.dictionary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ersurajrajput.quizapp.models.DictionaryModel
import com.ersurajrajput.quizapp.repo.DictionaryRepo

class DictionarySelectorActivity : ComponentActivity() {

    private val dictionaryRepo = DictionaryRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var dictionaryList by remember { mutableStateOf<List<DictionaryModel>>(emptyList()) }

            // Fetch dictionaries from Firestore
            LaunchedEffect(Unit) {
                dictionaryRepo.getDictionaryList { list ->
                    dictionaryList = list
                }
            }

            MaterialTheme {
                DictionarySelectorScreen(dictionaries = dictionaryList)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionarySelectorScreen(dictionaries: List<DictionaryModel>) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a Dictionary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dictionaries) { dictionary ->
                DictionaryCard(
                    dictionary = dictionary,
                    onClick = {
                        val intent = Intent(context, PlayerDictonaryActivity::class.java).apply {
                            putExtra("GAME_ID", dictionary.id)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun DictionaryCard(dictionary: DictionaryModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dictionary.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dictionary.desc,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
