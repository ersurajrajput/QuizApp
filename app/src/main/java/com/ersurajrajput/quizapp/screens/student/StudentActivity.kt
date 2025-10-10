package com.ersurajrajput.quizapp.screens.student

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.screens.comman.OnBoardingActivity
import com.ersurajrajput.quizapp.screens.student.activity.DragAndDropActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.activity.FillInTheBlanksActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.activity.MCQActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.activity.MCQImageBasedActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.activity.MatchTheFollowingActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.activity.MatchTheFollowingImageBasedActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.activity.MatchTheFollowingTextAndImageActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.activity.TrueFalseActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.student.ui.theme.QuizAppTheme

// Data class to represent each item in the grid
data class ActivityItem(val title: String, val icon: ImageVector, val type: String = "all")

// List of activities available to the student
val menuItems = listOf(
    ActivityItem("MCQ Activity", Icons.Default.MenuBook, type = "MCQ"),
    ActivityItem("MCQ Image Based", Icons.Default.Group, type = "IMAGE_BASED_MCQ"),
    ActivityItem("True or False ", Icons.Default.LocalActivity, type = "TRUE_FALSE"),
    ActivityItem("Fill in the blanks", Icons.Default.Games, type = "FILL_IN_BLANKS"),
    ActivityItem("Match the Followings", Icons.Default.Schema, type = "MATH_THE_FOLLOWING"),
    ActivityItem("Match the Followings Image based", Icons.Default.Group, "MATCH_THE_FOLLOWING_IMAGE_BASED"),
    ActivityItem("Match the Followings Text And Image ", Icons.Default.Group, "MATCH_THE_FOLLOWING_TEXT_AND_IMAGE_BASED"),
    ActivityItem("Drag and Drop", Icons.Default.Group, type = "DRAG_AND_DROP"),
)

class StudentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                StudentActivityScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentActivityScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select an Activity") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(menuItems) { item ->
                ActivityCard(item = item, onClick = {
                    // TODO: Replace Toast with actual navigation to student-side quiz player activities
                    Toast.makeText(context, "Opening ${item.title}", Toast.LENGTH_SHORT).show()

                    when (item.type) {
                        "MCQ" -> {
                            val intent = Intent(context, MCQActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        "IMAGE_BASED_MCQ"->{
                            val intent = Intent(context, MCQImageBasedActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        "TRUE_FALSE"->{
                            val intent = Intent(context, TrueFalseActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        "FILL_IN_BLANKS"->{
                            val intent = Intent(context, FillInTheBlanksActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        "MATH_THE_FOLLOWING"->{
                            val intent = Intent(context, MatchTheFollowingActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        "MATCH_THE_FOLLOWING_IMAGE_BASED"->{
                            val intent = Intent(context,
                                MatchTheFollowingImageBasedActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        "MATCH_THE_FOLLOWING_TEXT_AND_IMAGE_BASED"->{
                            val intent = Intent(context,
                                MatchTheFollowingTextAndImageActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        "DRAG_AND_DROP"->{
                            val intent = Intent(context, DragAndDropActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        else -> {
                           Toast.makeText(context, "${item.title} not implemented yet", Toast.LENGTH_SHORT).show()
                        }
                    }

                })
            }
        }
    }
}

@Composable
fun ActivityCard(item: ActivityItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentActivityScreenPreview() {
    QuizAppTheme {
        StudentActivityScreen()
    }
}
