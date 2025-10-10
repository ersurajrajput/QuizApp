package com.ersurajrajput.quizapp.screens.admin

import GameModel
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
//import com.ersurajrajput.quizapp.models.GameModel
import com.ersurajrajput.quizapp.repo.GamesRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class GamesManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                GamesManagementScreen()
            }
        }
    }
}

val gameTypes = listOf("Arrow Game", "Basket Ball Game", "Spell Bee", "Unscrambled words")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesManagementScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val gamesRepo = remember { GamesRepo() }

    var gameList by remember { mutableStateOf(mutableListOf<GameModel>()) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedGame by remember { mutableStateOf<GameModel?>(null) }
    var loading by remember { mutableStateOf(true) }

    var listener: ListenerRegistration? by remember { mutableStateOf(null) }

    // Load games
    LaunchedEffect(Unit) {
        listener = gamesRepo.getGameList { list ->
            gameList = list
            loading = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            listener?.remove()
        }
    }

    val filteredGames = remember(gameList, selectedFilter) {
        if (selectedFilter == "All") gameList else gameList.filter { it.gameType == selectedFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Manage Games", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(onClick = {
                selectedGame = null
                showAddEditDialog = false
                var intent = Intent(context, AddGameActivity::class.java)
                intent.putExtra("GAME_ID", "")
                intent.putExtra("NEW_GAME",true)
                context.startActivity(intent)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Game")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            FilterBar(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            if (loading) {
                Text("Loading games...", modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp))
            } else if (filteredGames.isEmpty()) {
                Text("No games found", modifier = Modifier.align(Alignment.CenterHorizontally).padding(20.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGames, key = { it.id }) { game ->
                        GameCard(
                            game = game,
                            onEditClick = {
                                selectedGame = it
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                selectedGame = it
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditGameDialog(
            game = selectedGame,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { name, type, description ->
                val newGame = selectedGame?.copy(
                    name = name,
                    gameType = type,
                    description = description
                ) ?: GameModel(
                    id = "",
                    name = name,
                    gameType = type,
                    description = description
                )

                gamesRepo.saveGame(newGame) { success, id ->
                    if (success) {
                        Toast.makeText(context, "Game saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to save game", Toast.LENGTH_SHORT).show()
                    }
                }

                showAddEditDialog = false
                selectedGame = null
            }
        )
    }

    if (showDeleteDialog) {
        DeleteGameConfirmationDialog(
            gameName = selectedGame?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                selectedGame?.let { gameToDelete ->
                    gamesRepo.deleteGame(gameToDelete.id) { success ->
                        if (success) {
                            Toast.makeText(context, "Game deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete game", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDeleteDialog = false
                selectedGame = null
            }
        )
    }
}

@Composable
fun FilterBar(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All") + gameTypes
    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            androidx.compose.material3.FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) }
            )
        }
    }
}

@Composable
fun GameCard(
    game: GameModel,
    onEditClick: (GameModel) -> Unit,
    onDeleteClick: (GameModel) -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = game.name, style = MaterialTheme.typography.titleLarge)
                Text(text = game.gameType, style = MaterialTheme.typography.bodyMedium)
                Text(text = game.description, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = {
                    var intent = Intent(context, AddGameActivity::class.java)
                    intent.putExtra("GAME_ID", game.id)
                    intent.putExtra("NEW_GAME",false)
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDeleteClick(game) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGameDialog(
    game: GameModel?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf(game?.name ?: "") }
    var description by remember { mutableStateOf(game?.description ?: "") }
    var type by remember { mutableStateOf(game?.gameType ?: gameTypes.first()) }
    var typeExpanded by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (game == null) "Add Game" else "Edit Game") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isNameError = false },
                    label = { Text("Game Name") },
                    isError = isNameError,
                    singleLine = true
                )
                if (isNameError) {
                    Text("Name cannot be empty", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Game Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                 ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        gameTypes.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection) },
                                onClick = {
                                    type = selection
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isNameError = true
                    } else {
                        onConfirm(name, type, description)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DeleteGameConfirmationDialog(
    gameName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Game") },
        text = { Text("Are you sure you want to delete '$gameName'?") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
