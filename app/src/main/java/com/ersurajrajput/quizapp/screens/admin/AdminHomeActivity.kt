package com.ersurajrajput.quizapp.screens.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.ersurajrajput.quizapp.screens.admin.ui.theme.ActivitySelectorActivity
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme
import com.ersurajrajput.quizapp.screens.comman.OnBoardingActivity

class AdminHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                AdminHomeScreen()
            }
        }
    }
}

data class AdminMenuItem(val title: String, val icon: ImageVector)

val menuItems = listOf(
    AdminMenuItem("Dictionary", Icons.Default.MenuBook),
    AdminMenuItem("Activities", Icons.Default.LocalActivity),
    AdminMenuItem("Games", Icons.Default.Games),
    AdminMenuItem("Diagram", Icons.Default.Schema),
    AdminMenuItem("Staff", Icons.Default.Group)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {

            TopAppBar(
                title = {
                   Row(
                       verticalAlignment = Alignment.CenterVertically,
                       horizontalArrangement = Arrangement.Center
                   ) {
                       Icon(
                           imageVector = Icons.Default.Logout,
                           contentDescription = "Logout",
                           modifier = Modifier
                               .graphicsLayer {
                                   rotationY = 180f // Flip the icon horizontally
                               }
                               .size(24.dp)
                               .clickable {
                                   // Handle logout action
                                   val intent = Intent(context, OnBoardingActivity::class.java)
                                   intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                   context.startActivity(intent)
                               },
                           tint = MaterialTheme.colorScheme.onPrimaryContainer
                       )
                       Spacer(modifier = Modifier.width(80.dp))
                       Text(
                           text = "Admin Dashboard",
                           fontWeight = FontWeight.Bold,
                           color = MaterialTheme.colorScheme.onPrimaryContainer
                       )
                   }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(menuItems) { item ->
                AdminMenuCard(
                    item = item,
                    onClick = {
                        if (item.title.lowercase().contains("staff")) {
                            var intent = Intent(context, StaffActivity::class.java)
                            context.startActivity(intent)
                        } else if (item.title.lowercase().contains("games")) {
                            var intent = Intent(context, GamesManagementActivity::class.java)
                            context.startActivity(intent)
                        }else if (item.title.lowercase().contains("diagram")){
                            var intent = Intent(context, DiagramManagementActivity::class.java)
                            context.startActivity(intent)
                        }else if (item.title.lowercase().contains("dictionary")){
                            var intent = Intent(context, DictonaryManagementActivity::class.java)
                            context.startActivity(intent)
                        }else if (item.title.lowercase().contains("activities")){
                            var intent = Intent(context, ActivitySelectorActivity::class.java)
                            context.startActivity(intent)
                        }
                        Toast.makeText(context, "${item.title} clicked", Toast.LENGTH_SHORT).show()
                        // TODO: Navigate to the respective screen
                    }
                )
            }
        }
    }
}

@Composable
fun AdminMenuCard(item: AdminMenuItem, onClick: () -> Unit) {
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
fun AdminHomeScreenPreview() {
    QuizAppTheme {
        AdminHomeScreen()
    }
}

