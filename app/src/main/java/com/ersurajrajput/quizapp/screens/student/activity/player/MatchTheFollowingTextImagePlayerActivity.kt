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
import androidx.compose.ui.draw.clip
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
import coil.compose.rememberAsyncImagePainter
import com.ersurajrajput.quizapp.R
import com.ersurajrajput.quizapp.models.MatchTheFollowingImageAndTextModel
import com.ersurajrajput.quizapp.repo.MatchTheFollowingImageAndTextRepo
import kotlinx.coroutines.delay

class MatchTheFollowingTextImagePlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityId = intent.getStringExtra("ID")

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MaterialTheme {
                if (activityId != null) {
                    MatchTextImageLoader(activityId = activityId)
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Activity ID not provided.")
                    }
                }
            }
        }
    }
}

// Data classes for UI state
data class MatchTextItem(val id: String, val text: String)
data class MatchTextImageItem(val id: String, val imageUrl: String)
enum class TextImageLineStatus { UNCHECKED, CORRECT, INCORRECT }
data class TextImageConnection(val startId: String, val endId: String, val status: TextImageLineStatus = TextImageLineStatus.UNCHECKED)

@Composable
fun MatchTextImageLoader(activityId: String) {
    val context = LocalContext.current
    val repo = remember { MatchTheFollowingImageAndTextRepo() }
    var quizModel by remember { mutableStateOf<MatchTheFollowingImageAndTextModel?>(null) }
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
                MatchTextImageQuiz(quiz = quizModel!!)
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
fun MatchTextImageQuiz(quiz: MatchTheFollowingImageAndTextModel) {
    var currentPageIndex by remember { mutableStateOf(0) }
    val currentPage = quiz.pages[currentPageIndex]
    val context = LocalContext.current

    var connections by remember(currentPageIndex) { mutableStateOf(listOf<TextImageConnection>()) }
    var draggingLine by remember(currentPageIndex) { mutableStateOf<Pair<Offset, Offset>?>(null) }
    var answersChecked by remember(currentPageIndex) { mutableStateOf(false) }
    var showResultDialog by remember(currentPageIndex) { mutableStateOf(false) }
    var allAnswersCorrect by remember(currentPageIndex) { mutableStateOf(false) }

    val leftItems = remember(currentPageIndex) {
        currentPage.pairs.map { MatchTextItem(it.id, it.leftText) }
    }
    val rightItems = remember(currentPageIndex) {
        currentPage.pairs.map { MatchTextImageItem(it.id, it.rightImageUrl) }.shuffled()
    }

    val leftPositions = remember { mutableStateMapOf<String, Offset>() }
    val rightPositions = remember { mutableStateMapOf<String, Offset>() }
    var canvasPosition by remember { mutableStateOf(Offset.Zero) }

    if (showResultDialog) {
        TextImageResultDialog(isSuccess = allAnswersCorrect, onDismissRequest = { showResultDialog = false })
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                Text(text = quiz.desc, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left column (Text)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        leftItems.forEach { item ->
                            MatchTextItemBox(item = item) {
                                TextImageConnectingCircle(
                                    itemId = item.id,
                                    positionsMap = leftPositions,
                                    alignment = Alignment.CenterEnd,
                                    modifier = Modifier
                                        .offset(x = (-8).dp)
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
                                                            .filter { it.startId != item.id && it.endId != rightId } + TextImageConnection(item.id, rightId)
                                                    }
                                                    draggingLine = null
                                                }
                                            )
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(32.dp))

                    // Right column (Images)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        rightItems.forEach { item ->
                            MatchTextImageBox(item = item) {
                                TextImageConnectingCircle(
                                    itemId = item.id,
                                    positionsMap = rightPositions,
                                    alignment = Alignment.CenterStart,
                                    modifier = Modifier.offset(x = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
            TextImageDrawingCanvas(connections, draggingLine, leftPositions, rightPositions, canvasPosition)
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
                                conn.copy(status = if (conn.startId == conn.endId) TextImageLineStatus.CORRECT else TextImageLineStatus.INCORRECT)
                            }
                            connections = updatedConnections

                            allAnswersCorrect = updatedConnections.isNotEmpty() && updatedConnections.all { it.status == TextImageLineStatus.CORRECT }
                            showResultDialog = true

                            val soundResId = if (allAnswersCorrect) R.raw.excellent else R.raw.common_u_can_do_batter_than_that
                            try {
                                MediaPlayer.create(context, soundResId)?.apply {
                                    setOnCompletionListener { mp -> mp.release() }
                                    start()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    enabled = connections.isNotEmpty()
                ) { Text("Submit") }

                Button(onClick = {
                    answersChecked = true
                    connections = leftItems.map { TextImageConnection(it.id, it.id, TextImageLineStatus.CORRECT) }
                    allAnswersCorrect = true
                    showResultDialog = true
                    try {
                        MediaPlayer.create(context, R.raw.excellent)?.apply {
                            setOnCompletionListener { mp -> mp.release() }
                            start()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }) { Text("Show Ans") }
            } else {
                Button(onClick = {
                    answersChecked = false
                    connections = emptyList()
                }) { Text("Retry") }

                Button(
                    onClick = {
                        if (currentPageIndex < quiz.pages.size - 1) {
                            currentPageIndex++
                        } else {
                            (context as? ComponentActivity)?.finish()
                        }
                    },
                    enabled = answersChecked
                ) {
                    Text(if (currentPageIndex < quiz.pages.size - 1) "Next Page" else "Finish")
                }
            }
        }
    }
}

@Composable
fun TextImageResultDialog(isSuccess: Boolean, onDismissRequest: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2500)
        onDismissRequest()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(16.dp)) {
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
                    text = if (isSuccess) "All matches are correct!" else "You can do better!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun MatchTextItemBox(item: MatchTextItem, circleContent: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .background(Color(0xFFFFE0E0), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = item.text, style = MaterialTheme.typography.bodyLarge)
        circleContent()
    }
}

@Composable
fun MatchTextImageBox(item: MatchTextImageItem, circleContent: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(item.imageUrl),
            contentDescription = "Match Image",
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )
        circleContent()
    }
}

@Composable
fun BoxScope.TextImageConnectingCircle(itemId: String, positionsMap: MutableMap<String, Offset>, alignment: Alignment, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(20.dp)
            .align(alignment)
            .onGloballyPositioned {
                val pos = it.positionInRoot() + Offset(it.size.width / 2f, it.size.height / 2f)
                positionsMap[itemId] = pos
            }
    ) {
        drawCircle(Color.Red, radius = size.minDimension / 2)
        drawCircle(Color.White, radius = size.minDimension / 4)
    }
}

@Composable
fun TextImageDrawingCanvas(
    connections: List<TextImageConnection>,
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
                    TextImageLineStatus.UNCHECKED -> Color.Black
                    TextImageLineStatus.CORRECT -> Color.Green
                    TextImageLineStatus.INCORRECT -> Color.Red
                }
                drawLine(color, start - canvasPosition, end - canvasPosition, strokeWidth = 8f, cap = StrokeCap.Round)
            }
        }
        draggingLine?.let { (start, end) ->
            drawLine(Color.DarkGray, start - canvasPosition, end - canvasPosition, strokeWidth = 8f, cap = StrokeCap.Round)
        }
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    return kotlin.math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y))
}
