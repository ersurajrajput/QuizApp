package com.ersurajrajput.quizapp.screens.student.activity.player

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat // ADDED: Import for controlling window features
import androidx.core.view.WindowInsetsCompat // ADDED: Import for system bar types
import androidx.core.view.WindowInsetsControllerCompat // ADDED: Import for hiding system bars
import coil.compose.AsyncImage
import com.ersurajrajput.quizapp.models.DragAndDropModel
import com.ersurajrajput.quizapp.models.DragAndDropOptions
import com.ersurajrajput.quizapp.repo.DragAndDropRepo
import kotlin.math.roundToInt
import com.ersurajrajput.quizapp.R

// Using the R.raw resources provided by the user
private val R_RAW_EXCELLENT = R.raw.excellent
private val R_RAW_TRY_AGAIN = R.raw.common_u_can_do_batter_than_that

class DragAndDropPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- HIDE STATUS BAR LOGIC ---
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
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
    val activity = context as? Activity
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

        // --- BACKGROUND IMAGE ---
        Image(
            painter = painterResource(id = R.drawable.a_one_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        // --- BACK BUTTON ---
        // Adopting the style and positioning from the MatchTheFollowingImagePlayerActivity for consistency
        if (activity != null) {
            IconButton(
                onClick = { activity.finish() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    // FIX: Use windowInsetsPadding(WindowInsets.statusBars) to ensure the button respects
                    // the system bar area, preventing it from being covered, even when bars are hidden.
                    .windowInsetsPadding(WindowInsets.statusBars)
                    // MODIFIED: Updated padding to match the reference activity's positioning
                    .padding(start = 24.dp, top = 100.dp)
                    .size(48.dp) // Ensures a good touch target size
            ) {
                Image(
                    painter = painterResource(id = R.drawable.green_back_btn_img),
                    contentDescription = "Go Back",
                    contentScale = ContentScale.Fit
                )
            }
        }

        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            quizModel == null -> Text("Activity could not be loaded.", modifier = Modifier.align(Alignment.Center))
            else -> {
                val quiz = quizModel!!
                Column(
                    // MODIFIED: Increased top padding to ensure content starts well below the repositioned back button
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.TopCenter) // Anchor to the top
                        .padding(top = 140.dp),    // Push content down, clearing the back button area
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
                        // FIX: Added 'key' composable. This forces the DragAndDropGame to restart
                        // and re-initialize all its internal 'remember' state variables (like
                        // areAnswersChecked and the draggable/droppable items) whenever the page changes.
                        key(currentPageIndex) {
                            DragAndDropGame(
                                pageOptions = it.options,
                                onNextPage = {
                                    if (currentPageIndex < quiz.pages.size - 1) currentPageIndex++
                                    else Toast.makeText(context, "Quiz Completed!", Toast.LENGTH_SHORT).show()
                                },
                                isLastPage = currentPageIndex == quiz.pages.size - 1
                            )
                        }
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
    // These 'remember' states will now be correctly reset when key(currentPageIndex) changes.
    var draggables by remember { mutableStateOf(pageOptions.shuffled().map { DraggableItem(it) }) }
    var dropTargets by remember { mutableStateOf(pageOptions.shuffled().map { DropTarget(it) }) }
    var areAnswersChecked by remember { mutableStateOf(false) }

    // ADDED: State for managing the result dialog
    var showResultDialog by remember { mutableStateOf(false) }
    var resultIsCorrect by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val allDropped = dropTargets.all { it.droppedItem != null } // Determine if all targets are filled

    // NEW: Function to clear a drop target if a draggable item leaves it
    val clearDropTarget: (DraggableItem) -> Unit = { itemToClear ->
        // Find the target that currently holds this item
        val targetIndex = dropTargets.indexOfFirst { it.droppedItem?.option?.id == itemToClear.option.id }
        if (targetIndex != -1) {
            val newTargets = dropTargets.toMutableList()
            // Clear the dropped item and reset the correctness check for the target
            newTargets[targetIndex] = newTargets[targetIndex].copy(droppedItem = null, isCorrect = null)
            dropTargets = newTargets
        }
    }

    // NEW: Function to reset the entire game for the current page
    val resetGame: () -> Unit = {
        // 1. Reset dropTargets (clear dropped item and correctness)
        dropTargets = dropTargets.map { it.copy(droppedItem = null, isCorrect = null) }

        // 2. Reset draggables to their initial undropped state
        draggables = draggables.map { it.copy(offset = Offset.Zero, isDropped = false, isBeingDragged = false) }

        // 3. Reset check state and dismiss dialog
        areAnswersChecked = false
        showResultDialog = false
    }

    // ADDED: Function to manage sound playback and dialog dismissal
    fun playSoundAndDismiss(isCorrect: Boolean) {
        val soundResId = if (isCorrect) R_RAW_EXCELLENT else R.raw.common_u_can_do_batter_than_that
        try {
            val mediaPlayer = MediaPlayer.create(context, soundResId)
            mediaPlayer?.setOnCompletionListener { mp ->
                showResultDialog = false // Dismiss on sound completion
                mp.release()
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            showResultDialog = false // Dismiss immediately if sound fails
            Toast.makeText(context, "Sound playback failed", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Grid of images (drop targets) - Now centered within the game column
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
                        // Check if the target has a dropped item, and if so, display its name.
                        // The item being displayed here is pulled from the dropTargets state.
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
                    // LOCAL STATE FIX: Hold the live drag displacement locally.
                    var dragDelta by remember { mutableStateOf(Offset.Zero) }

                    // The animated offset now combines the final snapped position (item.offset)
                    // with the live movement during drag (dragDelta).
                    val animatedOffset by animateOffsetAsState(
                        targetValue = if (item.isBeingDragged) item.offset + dragDelta else item.offset,
                        label = "DraggableOffset"
                    )

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
                        // Determine shadow based on drag state
                        val shadowElevation = if (item.isBeingDragged) 8.dp else 4.dp
                        // Determine Z-index: drag it above everything else
                        val zIndexValue = if (item.isBeingDragged) 10f else 0f

                        Text(
                            text = item.option.name,
                            color = Color.White,
                            modifier = Modifier
                                .offset { IntOffset(animatedOffset.x.roundToInt(), animatedOffset.y.roundToInt()) }
                                .zIndex(zIndexValue)
                                .shadow(shadowElevation, RoundedCornerShape(50))
                                // Item is gray if it's currently dropped OR if it was just successfully dropped and is not currently being dragged.
                                .background(if (item.isDropped && !item.isBeingDragged) Color.Gray else MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .pointerInput(index) {
                                    if (areAnswersChecked) return@pointerInput // Disable dragging after check

                                    detectDragGestures(
                                        onDragStart = {
                                            // Handle drag start for an item already in a drop target
                                            if (item.isDropped) {
                                                // NEW LOGIC: Clear the item from its original drop target
                                                clearDropTarget(item)
                                            }

                                            // Step 1: Update main list state only once to flag item as being dragged (for Z-index/shadow)
                                            val newDraggables = draggables.toMutableList()
                                            newDraggables[index] = item.copy(
                                                isBeingDragged = true,
                                                isDropped = false, // Item is now 'picked up'
                                                offset = Offset.Zero // Reset visual offset back to initial position for drag calculation
                                            )
                                            draggables = newDraggables

                                            // Reset the live delta
                                            dragDelta = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            // Step 2: ONLY update the local delta state for live, smooth movement (high performance)
                                            dragDelta += dragAmount
                                        },
                                        onDragEnd = {
                                            val newDraggables = draggables.toMutableList()

                                            // The item's current position is its initial layout position + the dragDelta
                                            val draggableCenter = item.initialOffset + dragDelta + Offset(item.size.width / 2f, item.size.height / 2f)
                                            var targetFound: DropTarget? = null
                                            var targetIndex = -1

                                            // Check drop targets
                                            dropTargets.forEachIndexed { idx, target ->
                                                // Drop is valid if it contains the center AND the target is empty
                                                if (target.bounds.contains(draggableCenter) && target.droppedItem == null) {
                                                    targetFound = target
                                                    targetIndex = idx
                                                }
                                            }

                                            if (targetFound != null) {
                                                // Drop Success: Calculate new final snap offset
                                                val targetCenter = targetFound!!.bounds.center
                                                // Calculate the offset required to move the draggable item's center to the drop target's center,
                                                // relative to its initial layout position (item.initialOffset).
                                                val snapOffset = targetCenter - (item.initialOffset + Offset(item.size.width / 2f, item.size.height / 2f))

                                                // Update global state with final snap position
                                                newDraggables[index] = item.copy(
                                                    offset = snapOffset,
                                                    isDropped = true,
                                                    isBeingDragged = false // Stop dragging
                                                )

                                                // Update drop target state
                                                val newTargets = dropTargets.toMutableList()
                                                newTargets[targetIndex] = targetFound!!.copy(droppedItem = newDraggables[index], isCorrect = null) // Clear correctness check
                                                dropTargets = newTargets

                                            } else {
                                                // Snap Back (No drop): Reset offset to Zero and mark as not dropped.
                                                // This ensures the item animates back to its original position in the flow layout.
                                                newDraggables[index] = item.copy(
                                                    offset = Offset.Zero,
                                                    isDropped = false,
                                                    isBeingDragged = false
                                                )

                                                // IMPORTANT: If we picked up an item and then failed to drop it anywhere,
                                                // it should go back to the source position, which is now marked as empty.
                                                // No need to revert the target clearing, as it stays empty for a new item.
                                            }

                                            // Step 3: Update the main list state once (for drop or snap back)
                                            draggables = newDraggables

                                            // Crucial: Clear the local delta state. animateOffsetAsState handles the transition.
                                            dragDelta = Offset.Zero
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }

        // --- UPDATED BUTTONS BLOCK ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 1. Reset Button (Always Enabled)
            Button(onClick = { resetGame() }, enabled = true) {
                Text("Reset")
            }

            // 2. Submit / Next Button
            val buttonText = when {
                areAnswersChecked && isLastPage -> "Finish"
                areAnswersChecked -> "Next Page"
                else -> "Submit"
            }

            // Logic for Submit/Next click
            val onClickAction: () -> Unit = {
                if (areAnswersChecked) {
                    // If checked, move to next page
                    onNextPage()
                } else {
                    // Check Answer (Submit) logic
                    areAnswersChecked = true
                    val newTargets = dropTargets.map { target ->
                        // Check if the dropped item's ID matches the target's required ID
                        val isCorrect = target.droppedItem?.option?.id == target.option.id
                        target.copy(isCorrect = isCorrect)
                    }
                    dropTargets = newTargets

                    // Logic to show dialog and play sound
                    val allCorrect = newTargets.all { it.isCorrect == true }
                    resultIsCorrect = allCorrect
                    showResultDialog = true
                    playSoundAndDismiss(allCorrect)
                }
            }

            // Logic for Submit/Next enabled state
            val buttonEnabled = when {
                areAnswersChecked -> true // Next/Finish is always enabled once checked
                else -> allDropped // Submit is enabled only when all are dropped
            }

            Button(
                onClick = onClickAction,
                enabled = buttonEnabled
            ) {
                Text(buttonText)
            }
        }
        // --- END UPDATED BUTTONS BLOCK ---
    }

    // ADDED: Result Dialog Composable
    ShowResultDialog(isVisible = showResultDialog, isCorrect = resultIsCorrect) {
        showResultDialog = false
    }
}

// ---------------------
// Result Dialog Composable
// ---------------------
@Composable
fun ShowResultDialog(isVisible: Boolean, isCorrect: Boolean, onDismiss: () -> Unit) {
    if (isVisible) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(min = 150.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val title = if (isCorrect) "Excellent!" else "Keep Trying!"
                    val message = if (isCorrect) "You got all answers correct!" else "You can do better!"

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
