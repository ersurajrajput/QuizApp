package com.ersurajrajput.quizapp.screens.admin

import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ersurajrajput.quizapp.models.StaffModel
import com.ersurajrajput.quizapp.repo.StaffRepo
import com.ersurajrajput.quizapp.screens.admin.ui.theme.QuizAppTheme

class StaffActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                StaffManagementScreen()
            }
        }
    }
}

val roles = listOf("Admin",  "Staff")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val staffRepo = StaffRepo()
    var staffList by remember { mutableStateOf(mutableListOf<StaffModel>()) }
    var loading by remember { mutableStateOf(true) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedStaffMember by remember { mutableStateOf<StaffModel?>(null) }

    LaunchedEffect(Unit) {
        staffRepo.getStaffList { list ->
            staffList = list
            loading = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Manage Staff", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedStaffMember = null
                showAddEditDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Staff")
            }
        }
    ) { innerPadding ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            staffList.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No staff members found. Add one!")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(staffList, key = { it.id }) { staff ->
                        StaffMemberCard(
                            staffMember = staff,
                            onEditClick = {
                                selectedStaffMember = it
                                showAddEditDialog = true
                            },
                            onDeleteClick = {
                                selectedStaffMember = it
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditStaffDialog(
            staffMember = selectedStaffMember,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { id, name, phone, email, role, password ->
                if (selectedStaffMember == null) {
                    // ADD
                    val newDocId = staffRepo.staffCollection.document().id
                    val newStaff = StaffModel(
                        id = newDocId,
                        name = name,
                        mobile = phone,
                        email = email,
                        role = role,
                        password = password
                    )
                    staffRepo.addStaff(newStaff) { success, returnedId ->
                        if (success && returnedId != null) {
                            newStaff.id = returnedId
                            staffList = (staffList + newStaff).toMutableList()
                            Toast.makeText(context, "Staff Added", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to add staff", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // EDIT
                    val updatedStaff = selectedStaffMember!!.copy(
                        name = name,
                        mobile = phone,
                        email = email,
                        role = role,
                        password = if (password.isNotEmpty()) password else selectedStaffMember!!.password
                    )
                    staffRepo.updateStaff(updatedStaff.id, updatedStaff) { success ->
                        if (success) {
                            staffList = staffList.map {
                                if (it.id == updatedStaff.id) updatedStaff else it
                            }.toMutableList()
                            Toast.makeText(context, "Staff Updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to update staff", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showAddEditDialog = false
                selectedStaffMember = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            staffMemberName = selectedStaffMember?.name ?: "",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                selectedStaffMember?.let { memberToDelete ->
                    staffRepo.deleteStaff(memberToDelete.id) { success ->
                        if (success) {
                            staffList = staffList.filterNot { it.id == memberToDelete.id }.toMutableList()
                            Toast.makeText(context, "Deleted: ${memberToDelete.name}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to delete staff", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDeleteDialog = false
                selectedStaffMember = null
            }
        )
    }
}

@Composable
fun StaffMemberCard(
    staffMember: StaffModel,
    onEditClick: (StaffModel) -> Unit,
    onDeleteClick: (StaffModel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = staffMember.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "${staffMember.email} • ${staffMember.role}", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = { onEditClick(staffMember) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { onDeleteClick(staffMember) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStaffDialog(
    staffMember: StaffModel?,
    onDismiss: () -> Unit,
    onConfirm: (id: String, name: String, phone: String, email: String, role: String, password: String) -> Unit
) {
    var name by remember { mutableStateOf(staffMember?.name ?: "") }
    var phone by remember { mutableStateOf(staffMember?.mobile ?: "") }
    var email by remember { mutableStateOf(staffMember?.email ?: "") }
    var password by remember { mutableStateOf(staffMember?.password ?: "") }
    var confirmPassword by remember { mutableStateOf(staffMember?.password ?: "") }
    var role by remember { mutableStateOf(staffMember?.role ?: roles.first()) }
    var roleExpanded by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (staffMember == null) "Add Staff" else "Edit Staff") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = null },
                    label = { Text("Name") },
                    isError = nameError != null,
                    singleLine = true
                )
                nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; phoneError = null },
                    label = { Text("Phone No") },
                    isError = phoneError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; emailError = null },
                    label = { Text("Email") },
                    isError = emailError != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                // Dropdown for Role
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roles.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    role = selectionOption
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = { Text("Password") },
                    isError = passwordError != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; passwordError = null },
                    label = { Text("Confirm Password") },
                    isError = passwordError != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validation
                    nameError = if (name.isBlank()) "Name cannot be empty" else null
                    phoneError = if (phone.length != 10) "Phone must be 10 digits" else null
                    emailError = if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Enter a valid email" else null
                    passwordError = if (password != confirmPassword) "Passwords do not match" else null

                    if (nameError == null && phoneError == null && emailError == null && passwordError == null) {
                        onConfirm(staffMember?.id ?: "", name, phone, email, role, password)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    staffMemberName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Staff") },
        text = { Text("Are you sure you want to delete '$staffMemberName'?") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
