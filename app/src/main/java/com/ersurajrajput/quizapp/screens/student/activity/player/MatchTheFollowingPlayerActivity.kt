package com.ersurajrajput.quizapp.screens.student.activity.player

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.models.MatchTheFollowingModel
import com.ersurajrajput.quizapp.repo.MatchTheFollowingRepo
import kotlinx.coroutines.delay

class MatchTheFollowingPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityId = intent.getStringExtra("ID")

        // Hide the system bars for an immersive experience
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MaterialTheme {
                if (activityId != null) {
                    MatchTheFollowingLoader(activityId = activityId)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Activity ID not provided.")
                    }
                }
            }
        }
    }
}

// ---------------------
// Data and State
// ---------------------
data class MatchItem(val id: String, val text: String)
enum class LineStatus { UNCHECKED, CORRECT, INCORRECT }
data class Connection(val startId: String, val endId: String, val status: LineStatus = LineStatus.UNCHECKED)


@Composable
fun MatchTheFollowingLoader(activityId: String) {
    val context = LocalContext.current
    val repo = remember { MatchTheFollowingRepo() }
    var quizModel by remember { mutableStateOf<MatchTheFollowingModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(activityId) {
        repo.getQuizById(activityId) { model ->
            if (model != null && model.pages.isNotEmpty()) {
                quizModel = model
            } else {
                Toast.makeText(context, "Failed to load or quiz is empty.", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.match_the_following_bg),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        // Back Button
        Image(
            painter = painterResource(id = R.drawable.green_back_btn_img),
            contentDescription = "Back",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 100.dp, start = 24.dp)
                .width(70.dp)
                .height(50.dp)
                .clickable { (context as? ComponentActivity)?.finish() }
        )
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            quizModel != null -> {
                MatchTheFollowingQuiz(quiz = quizModel!!)
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Could not load the quiz.")
                }
            }
        }
    }
}

@Composable
fun MatchTheFollowingQuiz(quiz: MatchTheFollowingModel) {
    var currentPageIndex by remember { mutableStateOf(0) }
    val currentPage = quiz.pages[currentPageIndex]
    val context = LocalContext.current

    // States that reset when the page changes
    var connections by remember(currentPageIndex) { mutableStateOf(listOf<Connection>()) }
    var draggingLine by remember(currentPageIndex) { mutableStateOf<Pair<Offset, Offset>?>(null) }
    var answersChecked by remember(currentPageIndex) { mutableStateOf(false) }
    var showResultDialog by remember(currentPageIndex) { mutableStateOf(false) }
    var allAnswersCorrect by remember(currentPageIndex) { mutableStateOf(false) }


    val leftItems = remember(currentPageIndex) {
        currentPage.pairs.map { MatchItem(it.id, it.leftOption) }
    }
    val rightItems = remember(currentPageIndex) {
        currentPage.pairs.map { MatchItem(it.id, it.rightOption) }.shuffled()
    }

    val leftPositions = remember { mutableStateMapOf<String, Offset>() }
    val rightPositions = remember { mutableStateMapOf<String, Offset>() }
    var canvasPosition by remember { mutableStateOf(Offset.Zero) }

    if (showResultDialog) {
        ResultDialog(isSuccess = allAnswersCorrect, onDismissRequest = { showResultDialog = false })
    }

    // Main layout
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game area
        Box(
            modifier = Modifier
                .weight(1f)
                .onGloballyPositioned {
                    canvasPosition = it.positionInRoot()
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quiz Description
                Text(text = quiz.desc, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        leftItems.forEach { item ->
                            MatchItemBox(item, Alignment.CenterStart) {
                                ConnectingCircle(
                                    itemId = item.id,
                                    positionsMap = leftPositions,
                                    alignment = Alignment.CenterEnd,
                                    modifier = Modifier
                                        .offset(x = (-16).dp)
                                        .pointerInput(item.id, answersChecked) {
                                            if (answersChecked) return@pointerInput
                                            detectDragGestures(
                                                onDragStart = {
                                                    leftPositions[item.id]?.let { startPos -> draggingLine = startPos to startPos }
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    draggingLine = draggingLine?.let { it.first to (it.second + dragAmount) }
                                                },
                                                onDragEnd = {
                                                    val matchedRight = rightPositions.entries.find { (_, pos) ->
                                                        draggingLine?.second?.distanceTo(pos) ?: Float.MAX_VALUE < 40f
                                                    }
                                                    matchedRight?.let { (rightId, _) ->
                                                        connections = connections
                                                            .filter { it.startId != item.id && it.endId != rightId } + Connection(item.id, rightId)
                                                    }
                                                    draggingLine = null
                                                }
                                            )
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        rightItems.forEach { item ->
                            MatchItemBox(item, Alignment.CenterEnd) {
                                ConnectingCircle(
                                    itemId = item.id,
                                    positionsMap = rightPositions,
                                    alignment = Alignment.CenterStart,
                                    modifier = Modifier.offset(x = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
            DrawingCanvas(connections, draggingLine, leftPositions, rightPositions, canvasPosition)
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!answersChecked) {
                Button(
                    onClick = {
                        if (connections.isNotEmpty()) {
                            answersChecked = true
                            val updatedConnections = connections.map { conn ->
                                conn.copy(status = if (conn.startId == conn.endId) LineStatus.CORRECT else LineStatus.INCORRECT)
                            }
                            connections = updatedConnections

                            allAnswersCorrect = updatedConnections.isNotEmpty() && updatedConnections.all { it.status == LineStatus.CORRECT }
                            showResultDialog = true

                            val soundResId = if (allAnswersCorrect) R.raw.excellent else R.raw.common_u_can_do_batter_than_that
                            try {
                                val mediaPlayer = MediaPlayer.create(context, soundResId)
                                mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                                mediaPlayer?.start()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    enabled = connections.isNotEmpty()
                ) { Text("Submit") }

                Button(onClick = {
                    answersChecked = true
                    connections = leftItems.map { Connection(it.id, it.id, LineStatus.CORRECT) }
                    allAnswersCorrect = true
                    showResultDialog = true
                    try {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.excellent)
                        mediaPlayer?.setOnCompletionListener { mp -> mp.release() }
                        mediaPlayer?.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }) { Text("Show Ans") }
            } else {
                Button(onClick = {
                    // Reset current page
                    answersChecked = false
                    connections = emptyList()
                }) { Text("Retry") }

                Button(
                    onClick = {
                        if (currentPageIndex < quiz.pages.size - 1) {
                            currentPageIndex++
                        } else {
                            // Quiz finished
                        }
                    },
                    enabled = answersChecked // Only allow next when answers are checked
                ) {
                    Text(if (currentPageIndex < quiz.pages.size - 1) "Next Page" else "Finish")
                }
            }
        }
    }
}

@Composable
fun ResultDialog(isSuccess: Boolean, onDismissRequest: () -> Unit) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        LaunchedEffect(Unit) {
            delay(2500) // Auto-dismiss after 2.5 seconds
            onDismissRequest()
        }

        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isSuccess) "Excellent!" else "Keep Trying!",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSuccess) "All answers are correct!" else "You can do better!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}


@Composable
fun MatchItemBox(item: MatchItem, alignment: Alignment, circleContent: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
            .background(if (alignment == Alignment.CenterStart) Color(0xFFFFE0E0) else Color(0xFFE0E0FF), RoundedCornerShape(8.dp)),
        contentAlignment = alignment
    ) {
        val textPadding = if (alignment == Alignment.CenterStart) PaddingValues(start = 16.dp) else PaddingValues(end = 16.dp)
        Text(text = item.text, modifier = Modifier.padding(textPadding))
        circleContent()
    }
}

@Composable
fun BoxScope.ConnectingCircle(itemId: String, positionsMap: MutableMap<String, Offset>, alignment: Alignment, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(16.dp)
            .align(alignment)
            .onGloballyPositioned {
                val pos = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f)
                positionsMap[itemId] = pos
            }
    ) {
        drawCircle(Color.Red, radius = size.minDimension / 2)
    }
}

@Composable
fun DrawingCanvas(
    connections: List<Connection>,
    draggingLine: Pair<Offset, Offset>?,
    leftPositions: Map<String, Offset>,
    rightPositions: Map<String, Offset>,
    canvasPosition: Offset
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        connections.forEach { connection ->
            val start = leftPositions[connection.startId]
            val end = rightPositions[connection.endId]
            if (start != null && end != null) {
                val color = when (connection.status) {
                    LineStatus.UNCHECKED -> Color.Black
                    LineStatus.CORRECT -> Color.Green
                    LineStatus.INCORRECT -> Color.Red
                }
                drawLine(color, start - canvasPosition, end - canvasPosition, strokeWidth = 6f, cap = StrokeCap.Round)
            }
        }
        draggingLine?.let { (start, end) ->
            drawLine(Color.DarkGray, start - canvasPosition, end - canvasPosition, strokeWidth = 6f, cap = StrokeCap.Round)
        }
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    return kotlin.math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
}

