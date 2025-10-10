package com.ersurajrajput.quizapp.screens.admin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.models.DictionaryModel
import com.ersurajrajput.quizapp.repo.DictionaryRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme

class DictonaryManagementActivity : ComponentActivity() {
    private val dictionaryRepo = DictionaryRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                DictonaryManagementScreen(dictionaryRepo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictonaryManagementScreen(repo: DictionaryRepo) {
    val context = LocalContext.current
    var dictionaries by remember { mutableStateOf(listOf<DictionaryModel>()) }

    // Load dictionaries from repo (Firestore or Dummy)
    LaunchedEffect(Unit) {
        repo.getDictionaryList { list ->
            dictionaries = list
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Dictionary", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    var intent = Intent(context, AddNewDictionaryActivity::class.java)
                    intent.putExtra("NEW",true)
                    context.startActivity(intent)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(dictionaries) { dictionary ->
                DictionaryItem(
                    dictionary = dictionary,
                    onEdit = {
                        var intent = Intent(context, AddNewDictionaryActivity::class.java)
                        intent.putExtra("NEW",false)
                        intent.putExtra("ID",dictionary.id)
                        context.startActivity(intent)
                    },
                    onDelete = {
                        // Delete from Firestore via repo
                        repo.deleteDictionary(dictionary.id) { success ->
                            if (success) {
                                dictionaries = dictionaries.filter { it.id != dictionary.id }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DictionaryItem(
    dictionary: DictionaryModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dictionary.title, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(dictionary.desc, color = Color.Gray)
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}
