package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Profile
import com.example.ui.viewmodel.FacultyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: FacultyViewModel,
    onNavigateToDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val existingProfile by viewModel.profile.collectAsState()

    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var areaOfExpertise by remember { mutableStateOf("") }
    var phdWork by remember { mutableStateOf("") }
    var recentWork by remember { mutableStateOf("") }

    // Synchronize local edit state with saved profile
    LaunchedEffect(existingProfile) {
        existingProfile?.let {
            name = it.name
            department = it.department
            areaOfExpertise = it.areaOfExpertise
            phdWork = it.phdWork
            recentWork = it.recentWork
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Academic Portfolio Setup", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Helpful Guide Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Academic advice icon",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Complete Your Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "This intelligence will customize your AI Mock Interviews, research literature papers, and grant writing proposals.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Input Fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Faculty Full Name") },
                placeholder = { Text("e.g. Dr. Sarah Jenkins") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_input"),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = department,
                onValueChange = { department = it },
                label = { Text("University Department / School") },
                placeholder = { Text("e.g. School of Computer Engineering") },
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = "Dept icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("department_input"),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = areaOfExpertise,
                onValueChange = { areaOfExpertise = it },
                label = { Text("Core Area of Expertise") },
                placeholder = { Text("e.g. Generative AI, Quantum Cryptography, Molecular Physics") },
                leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = "Domain icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expertise_input"),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = phdWork,
                onValueChange = { phdWork = it },
                label = { Text("PhD Work / Doctoral Dissertation Summary") },
                placeholder = { Text("Describe the core hypotheses, breakthroughs, and theories developed during your doctoral studies.") },
                leadingIcon = { Icon(Icons.Default.AutoStories, contentDescription = "PhD icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("phd_input"),
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = recentWork,
                onValueChange = { recentWork = it },
                label = { Text("Recent Research / Selected Publications") },
                placeholder = { Text("Mention major research findings, patents, recent IEEE journals, or books you published recently.") },
                leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = "Work icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("recent_input"),
                singleLine = false,
                maxLines = 4,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save Action
            Button(
                onClick = {
                    if (name.isNotBlank() && areaOfExpertise.isNotBlank()) {
                        viewModel.updateProfile(name, department, areaOfExpertise, phdWork, recentWork)
                        onNavigateToDashboard()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_button"),
                enabled = name.isNotBlank() && areaOfExpertise.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Check Icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (existingProfile != null) "Update Faculty Profile" else "Save & Access Dashboard",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Skip/Back Button for convenience
            if (existingProfile != null) {
                OutlinedButton(
                    onClick = { onNavigateToDashboard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("cancel_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
