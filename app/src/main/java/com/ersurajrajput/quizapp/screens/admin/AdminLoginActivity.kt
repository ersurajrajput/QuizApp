package com.ersurajrajput.quizapp.screens.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import android.util.Log
import com.ersurajrajput.quizapp.models.StaffModel
import com.ersurajrajput.quizapp.screens.admin.AdminHomeActivity // Import for navigation

// ----------------------------
// REPO (Corrected for Admin Login)
// ----------------------------
class StaffRepo {

    fun findStaffByIdAndPass(
        email: String,
        pass: String,
        onSuccess: (StaffModel) -> Unit,
        onResult: (Boolean, String) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        // --- FIX APPLIED HERE ---
        // 1. Corrected collection name from "staf" to "staffs"
        // 2. Corrected field names from "id" and "pass" to "email" and "password"
        db.collection("staffs")
            .whereEqualTo("email", trimmedEmail)
            .whereEqualTo("password", trimmedPass)
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val staff = querySnapshot.documents[0].toObject(StaffModel::class.java)
                    if (staff != null) {
                        onSuccess(staff)
                        onResult(true, "Login successful")
                    } else {
                        onResult(false, "Invalid staff data")
                    }
                } else {
                    onResult(false, "No staff found with provided credentials")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error querying staff", exception)
                onResult(false, "Error: ${exception.message}")
            }
    }
}

// ----------------------------
// SESSION SAVE
// ----------------------------
fun saveSession(context: Context, isLoggedIn: Boolean, role: String) {
    val prefs = context.getSharedPreferences("SrijanQuizApp", Context.MODE_PRIVATE)
    prefs.edit()
        .putBoolean("isLoggedIn", isLoggedIn)
        .putString("Role", role)
        .apply()
}

// ----------------------------
// MAIN SCREEN
// ----------------------------
class AdminLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdminLoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AdminLoginScreen(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    fun validateForm(): Boolean {
        emailError = if (email.isBlank()) "Email cannot be empty"
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            "Invalid email format"
        else null

        passwordError = if (password.isBlank()) "Password cannot be empty" else null
        return emailError == null && passwordError == null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Admin Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailError = null },
            label = { Text("Email") },
            isError = emailError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        emailError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = { Text("Password") },
            isError = passwordError != null,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        passwordError?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (validateForm()) {
                    loading = true
                    StaffRepo().findStaffByIdAndPass(
                        email = email,
                        pass = password, // Note: pass parameter is used to query the password field
                        onSuccess = { staff ->
                            // 1. Save session with successful login and the correct role
                            saveSession(context, true, staff.role.ifEmpty { "Admin" })

                            // 2. Navigate
                            val intent = Intent(context, AdminHomeActivity::class.java).apply {
                                // Clear the login activity from the back stack
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                            Toast.makeText(context, "Welcome ${staff.name}", Toast.LENGTH_SHORT).show()
                            loading = false
                        },
                        onResult = { success, message ->
                            loading = false
                            if (!success) {
                                // Show dialog and toast only on failure
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                showDialog = true
                            }
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Logging in..." else "Login")
        }
    }

    if (showDialog) {
        ErrorDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun ErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login Failed") },
        text = { Text("You have entered wrong credentials. Please try again.") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AdminLoginScreenPreview() {
    QuizAppTheme {
        AdminLoginScreen()
    }
}
