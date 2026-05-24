package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.ResearchPaper
import com.example.ui.viewmodel.FacultyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    viewModel: FacultyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedPapers by viewModel.papers.collectAsState()
    val isLoading by viewModel.researchLoading.collectAsState()
    val latestResult by viewModel.generatedPaperResult.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var topicQuery by remember { mutableStateOf("") }
    var domainField by remember { mutableStateOf("") }
    var selectedPaperForRead by remember { mutableStateOf<ResearchPaper?>(null) }

    // Init domainField from profile
    LaunchedEffect(profile) {
        profile?.let {
            if (domainField.isBlank()) {
                domainField = it.areaOfExpertise
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Research Literature & Projects", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedPaperForRead != null) {
                            selectedPaperForRead = null
                        } else {
                            onNavigateBack()
                        }
                    }, modifier = Modifier.testTag("research_back_btn")) {
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
            
            if (selectedPaperForRead != null) {
                // Reading View Pane
                val paper = selectedPaperForRead!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = paper.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Field: ${paper.queryTopic} | ${paper.authors}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MarkdownText(markdown = paper.summary)

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { selectedPaperForRead = null },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                    ) {
                        Text("Back to Literature Explorer")
                    }
                }
            } else {
                // Main split page layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Input search Form Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Synthesize Papers & Curate Student Thesis Projects",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = domainField,
                                onValueChange = { domainField = it },
                                label = { Text("Academic Major / Research Area") },
                                placeholder = { Text("e.g. Biomedical Science, Software Systems") },
                                modifier = Modifier.fillMaxWidth().testTag("research_field_input"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = topicQuery,
                                onValueChange = { topicQuery = it },
                                label = { Text("Specific Focus Topic / Literature Query") },
                                placeholder = { Text("e.g. CRISPR interference in therapeutics, Zero-knowledge proofs") },
                                modifier = Modifier.fillMaxWidth().testTag("research_topic_input"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (topicQuery.isNotBlank() && domainField.isNotBlank()) {
                                        viewModel.submitResearchQuery(topicQuery, domainField)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("research_submit_btn"),
                                enabled = topicQuery.isNotBlank() && domainField.isNotBlank() && !isLoading,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Synthesizing Research (takes ~15s)...")
                                } else {
                                    Icon(Icons.Default.ManageSearch, contentDescription = "Search icon")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fetch Literature & Projects")
                                }
                            }
                        }
                    }

                    // Active result showcase
                    if (latestResult != null && !isLoading) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val freshlySaved = savedPapers.firstOrNull { it.queryTopic == topicQuery }
                                        if (freshlySaved != null) {
                                            selectedPaperForRead = freshlySaved
                                        } else if (savedPapers.isNotEmpty()) {
                                            selectedPaperForRead = savedPapers.first()
                                        }
                                    }
                                    .padding(16.dp),
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
                                        "New Literature Compilation ready!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "Generated detailed BSc, MSc, and PhD project architectures.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "View", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Stored lists
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your Saved Publication Briefings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (savedPapers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No literature summary saved offline.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(savedPapers) { paper ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedPaperForRead = paper },
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
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = "Book icon",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = paper.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Synthesized Topic: ${paper.queryTopic}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deletePaper(paper.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete entry",
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
