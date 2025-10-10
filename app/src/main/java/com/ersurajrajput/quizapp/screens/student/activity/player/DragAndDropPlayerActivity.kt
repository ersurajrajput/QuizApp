package com.ersurajrajput.quizapp.screens.student.activity.player

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.compose.AsyncImage
import com.ersurajrajput.quizapp.models.DragAndDropModel
import com.ersurajrajput.quizapp.models.DragAndDropOptions
import com.ersurajrajput.quizapp.repo.DragAndDropRepo
import kotlin.math.roundToInt

class DragAndDropPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val activityId = intent.getStringExtra("ID")
        setContent {
            MaterialTheme {
                if (activityId == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Activity ID not found.")
                    }
                } else {
                    DragAndDropScreen(activityId = activityId)
                }
            }
        }
    }
}

// ---------------------
// Data classes
// ---------------------
private data class DraggableItem(
    val option: DragAndDropOptions,
    var offset: Offset = Offset.Zero,
    var size: IntSize = IntSize.Zero,
    var initialOffset: Offset = Offset.Zero,
    var isDropped: Boolean = false,
    var isBeingDragged: Boolean = false
)

private data class DropTarget(
    val option: DragAndDropOptions,
    var bounds: androidx.compose.ui.geometry.Rect = androidx.compose.ui.geometry.Rect.Zero,
    var droppedItem: DraggableItem? = null,
    var isCorrect: Boolean? = null
)

// ---------------------
// Main Screen
// ---------------------
@Composable
fun DragAndDropScreen(activityId: String) {
    val context = LocalContext.current
    val repo = remember { DragAndDropRepo() }
    var quizModel by remember { mutableStateOf<DragAndDropModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentPageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(activityId) {
        repo.getQuizById(activityId) { model ->
            if (model != null) quizModel = model
            else Toast.makeText(context, "Failed to load activity", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            quizModel == null -> Text("Activity could not be loaded.", modifier = Modifier.align(Alignment.Center))
            else -> {
                val quiz = quizModel!!
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = quiz.title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = quiz.desc,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    val page = quiz.pages.getOrNull(currentPageIndex)
                    page?.let {
                        DragAndDropGame(
                            pageOptions = it.options,
                            onNextPage = {
                                if (currentPageIndex < quiz.pages.size - 1) currentPageIndex++
                                else Toast.makeText(context, "Quiz Completed!", Toast.LENGTH_SHORT).show()
                            },
                            isLastPage = currentPageIndex == quiz.pages.size - 1
                        )
                    } ?: Text("No more pages", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

// ---------------------
// Drag & Drop Game
// ---------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DragAndDropGame(
    pageOptions: List<DragAndDropOptions>,
    onNextPage: () -> Unit,
    isLastPage: Boolean
) {
    var draggables by remember { mutableStateOf(pageOptions.shuffled().map { DraggableItem(it) }) }
    var dropTargets by remember { mutableStateOf(pageOptions.shuffled().map { DropTarget(it) }) }
    var areAnswersChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Grid of images (drop targets)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(dropTargets) { index, target ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = target.option.imageUri,
                        contentDescription = target.option.name,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val borderColor = when (target.isCorrect) {
                        true -> Color.Green
                        false -> Color.Red
                        null -> Color.Gray
                    }
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(50.dp)
                            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .onGloballyPositioned {
                                val newTargets = dropTargets.toMutableList()
                                newTargets[index] = target.copy(bounds = androidx.compose.ui.geometry.Rect(it.positionInRoot(), it.size.toSize()))
                                dropTargets = newTargets
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = target.droppedItem?.option?.name ?: "",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Draggable names
        Box(modifier = Modifier.fillMaxWidth()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                draggables.forEachIndexed { index, item ->
                    val animatedOffset by animateOffsetAsState(targetValue = item.offset)
                    Box(modifier = Modifier
                        .onGloballyPositioned {
                            if (item.initialOffset == Offset.Zero) {
                                val newDraggables = draggables.toMutableList()
                                newDraggables[index] = item.copy(
                                    initialOffset = it.positionInRoot(),
                                    size = it.size
                                )
                                draggables = newDraggables
                            }
                        }
                    ) {
                        Text(
                            text = item.option.name,
                            color = Color.White,
                            modifier = Modifier
                                .offset { IntOffset(animatedOffset.x.roundToInt(), animatedOffset.y.roundToInt()) }
                                .shadow(4.dp, RoundedCornerShape(50))
                                .background(if (item.isDropped) Color.Gray else MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .pointerInput(index) {
                                    detectDragGestures(
                                        onDragStart = {
                                            val newDraggables = draggables.toMutableList()
                                            newDraggables[index] = item.copy(isBeingDragged = true)
                                            draggables = newDraggables
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val newDraggables = draggables.toMutableList()
                                            newDraggables[index] = item.copy(offset = item.offset + dragAmount)
                                            draggables = newDraggables
                                        },
                                        onDragEnd = {
                                            val draggableCenter = item.initialOffset + item.offset + Offset(item.size.width / 2f, item.size.height / 2f)
                                            var targetFound: DropTarget? = null
                                            var targetIndex = -1
                                            dropTargets.forEachIndexed { idx, target ->
                                                if (target.bounds.contains(draggableCenter) && target.droppedItem == null) {
                                                    targetFound = target
                                                    targetIndex = idx
                                                }
                                            }

                                            if (targetFound != null) {
                                                val targetCenter = targetFound!!.bounds.center
                                                val snapOffset = targetCenter - (item.initialOffset + Offset(item.size.width / 2f, item.size.height / 2f))

                                                val newDraggables = draggables.toMutableList()
                                                newDraggables[index] = item.copy(
                                                    offset = snapOffset,
                                                    isDropped = true,
                                                    isBeingDragged = false
                                                )
                                                draggables = newDraggables

                                                val newTargets = dropTargets.toMutableList()
                                                newTargets[targetIndex] = targetFound!!.copy(droppedItem = newDraggables[index])
                                                dropTargets = newTargets
                                            } else {
                                                val newDraggables = draggables.toMutableList()
                                                newDraggables[index] = item.copy(offset = Offset.Zero, isBeingDragged = false)
                                                draggables = newDraggables
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val allDropped = dropTargets.all { it.droppedItem != null }
            Button(onClick = {
                areAnswersChecked = true
                val newTargets = dropTargets.map { target ->
                    val isCorrect = target.droppedItem?.option?.id == target.option.id
                    target.copy(isCorrect = isCorrect)
                }
                dropTargets = newTargets
            }, enabled = allDropped && !areAnswersChecked) {
                Text("Check Answer")
            }

            Button(onClick = onNextPage, enabled = areAnswersChecked) {
                Text(if (isLastPage) "Finish" else "Next Page")
            }
        }
    }
}
