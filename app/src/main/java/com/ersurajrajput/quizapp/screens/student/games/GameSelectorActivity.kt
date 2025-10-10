package com.ersurajrajput.quizapp.screens.student.games

import GameModel
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.repo.GamesRepo
import com.ersurajrajput.quizapp.screens.student.activity.games.ArrowGameActivity
import com.ersurajrajput.quizapp.screens.student.activity.games.BasketBallGameActivity
import com.ersurajrajput.quizapp.screens.student.activity.games.SpellBeeGameActivity
import com.ersurajrajput.quizapp.screens.student.games.player.UnscrambledWordsGameActivity
import com.ersurajrajput.quizapp.screens.student.games.ui.theme.QuizAppTheme
import com.google.firebase.firestore.ListenerRegistration

class GameSelectorActivity : ComponentActivity() {

    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val gameName = intent.getStringExtra("GAME_NAME") ?: "Unknown Game"

        setContent {
            QuizAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameSelectorScreen(
                        gameName = gameName, modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}

@Composable
fun GameSelectorScreen(gameName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var gameList by remember { mutableStateOf<List<GameModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 Fetch data from Firestore (realtime)
    LaunchedEffect(Unit) {
        val repo = GamesRepo()
        repo.getGameList { list ->
            gameList = list.filter { it.gameType == gameName }
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { (context as? ComponentActivity)?.finish() },
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = gameName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (gameList.isEmpty()) {
            Text(text = "No games found!", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(gameList) { game ->
                    GameItemCard(
                        gameModel = game, onItemClick = {

                            when (game.gameType) {
                                "Arrow Game" -> {
                                    var intent = Intent(context, ArrowGameActivity::class.java)
                                    intent.putExtra("GAME_ID", game.id)
                                    context.startActivity(intent)
                                }

                                "Spell Bee" -> {
                                    var intent = Intent(context, SpellBeeGameActivity::class.java)
                                    intent.putExtra("GAME_ID", game.id)
                                    context.startActivity(intent)
                                }

                                "Basket Ball Game" -> {
                                    var intent = Intent(context, BasketBallGameActivity::class.java)
                                    intent.putExtra("GAME_ID", game.id)
                                    context.startActivity(intent)
                                }
                                "Unscrambled words"->{
                                    var intent = Intent(context, UnscrambledWordsGameActivity::class.java)
                                    intent.putExtra("GAME_ID", game.id)
                                    context.startActivity(intent)
                                    Toast.makeText(context,"coming soon", Toast.LENGTH_SHORT).show()
                                }

                                else -> null
                            }
                        })
                }
            }
        }
    }
}

@Composable
fun GameItemCard(gameModel: GameModel, onItemClick: () -> Unit) {
    val icon: ImageVector = when (gameModel.gameType) {
        "Arrow Game" -> Icons.AutoMirrored.Filled.ArrowForward
        "Basket Ball Game" -> Icons.Default.SportsBasketball
        "Unscrambled words" -> Icons.Default.Shuffle
        "Spell Bee" -> Icons.Default.Spellcheck
        else -> Icons.Default.SportsEsports
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = gameModel.gameType,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = gameModel.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = gameModel.description, style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameSelectorScreenPreview() {
    QuizAppTheme {
        GameSelectorScreen(gameName = "Arrow Game")
    }
}
