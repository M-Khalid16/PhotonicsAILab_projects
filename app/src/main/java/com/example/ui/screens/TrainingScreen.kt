package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AreaTraining
import com.example.ui.viewmodel.FacultyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    viewModel: FacultyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedTrainings by viewModel.trainings.collectAsState()
    val isLoading by viewModel.trainingLoading.collectAsState()
    val latestResult by viewModel.generatedTrainingResult.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var activeDomain by remember { mutableStateOf("") }
    var difficultyLevel by remember { mutableStateOf("MSc Advisor") }
    var selectedTrainingForRead by remember { mutableStateOf<AreaTraining?>(null) }

    val levels = listOf(
        "BSc Class Instructor",
        "MSc Advisor/Lab Setup",
        "PhD Supervisor & Peer Review",
        "Senior Scientific Principal Investigator"
    )

    // Init area from profile
    LaunchedEffect(profile) {
        profile?.let {
            if (activeDomain.isBlank()) {
                activeDomain = it.areaOfExpertise
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Faulty Trainings", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedTrainingForRead != null) {
                            selectedTrainingForRead = null
                        } else {
                            onNavigateBack()
                        }
                    }, modifier = Modifier.testTag("training_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back icon")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            
            if (selectedTrainingForRead != null) {
                // Read Detailed Syllabus
                val course = selectedTrainingForRead!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Focus: ${course.field} | Target Level: ${course.difficultyLevel}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MarkdownText(markdown = course.syllabusMarkdown)

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { selectedTrainingForRead = null },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                    ) {
                        Text("Back to Training Dashboard")
                    }
                }
            } else {
                // Launchpad & Course Archives View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Form setup card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Generate Bespoke Professional Syllabi & Instructor Manuals",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = activeDomain,
                                onValueChange = { activeDomain = it },
                                label = { Text("Specialized Domain to Teach / Study") },
                                placeholder = { Text("e.g. Advanced Nanotechnology, Machine Learning Theory") },
                                modifier = Modifier.fillMaxWidth().testTag("training_domain_field"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Text(
                                "Target Faculty Leadership Profile:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            // Horizontally compact scroll level chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                levels.take(2).forEach { level ->
                                    val isSelected = difficultyLevel == level
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { difficultyLevel = level },
                                        label = { Text(level, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                levels.drop(2).forEach { level ->
                                    val isSelected = difficultyLevel == level
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { difficultyLevel = level },
                                        label = { Text(level, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (activeDomain.isNotBlank()) {
                                        viewModel.submitTrainingQuery(activeDomain, difficultyLevel)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("training_submit_btn"),
                                enabled = activeDomain.isNotBlank() && !isLoading,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Compiling Syllabus (takes ~15s)...")
                                } else {
                                    Icon(Icons.Default.School, contentDescription = "School icon")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Synthesize Upscaling Faculty Curriculum")
                                }
                            }
                        }
                    }

                    // Active result indicator
                    if (latestResult != null && !isLoading) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (savedTrainings.isNotEmpty()) {
                                            selectedTrainingForRead = savedTrainings.first()
                                        }
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success icon",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Academic Syllabus Synced!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "Detailed 4-week module breakdown has been cached offline.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "View", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Curricular Archives list
                    Text(
                        text = "Cached Professional Development Curricula",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (savedTrainings.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No professional curricula cached offline.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(savedTrainings) { course ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTrainingForRead = course },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.HistoryEdu,
                                                contentDescription = "Academic history icon",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = course.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Specialty: ${course.field}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteTraining(course.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete training",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
