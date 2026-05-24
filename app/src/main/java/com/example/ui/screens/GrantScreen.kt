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
import com.example.data.model.GrantProposal
import com.example.ui.viewmodel.FacultyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrantScreen(
    viewModel: FacultyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val savedProposals by viewModel.proposals.collectAsState()
    val isLoading by viewModel.grantLoading.collectAsState()
    val latestResult by viewModel.generatedGrantResult.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var projectTitle by remember { mutableStateOf("") }
    var targetAgency by remember { mutableStateOf("") }
    var researchArea by remember { mutableStateOf("") }
    var abstractProblem by remember { mutableStateOf("") }
    var objectivesText by remember { mutableStateOf("") }

    var selectedProposalForRead by remember { mutableStateOf<GrantProposal?>(null) }

    // Init area from profile
    LaunchedEffect(profile) {
        profile?.let {
            if (researchArea.isBlank()) {
                researchArea = it.areaOfExpertise
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grant Proposal Writer", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedProposalForRead != null) {
                            selectedProposalForRead = null
                        } else {
                            onNavigateBack()
                        }
                    }, modifier = Modifier.testTag("grant_back_btn")) {
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
            
            if (selectedProposalForRead != null) {
                // Reading layout board
                val proposal = selectedProposalForRead!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = proposal.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "Target Agency: ${proposal.targetAgency} | Discipline: ${proposal.researchArea}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    MarkdownText(markdown = proposal.rawFullProposalText)

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { selectedProposalForRead = null },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                    ) {
                        Text("Back to Grant Dashboard")
                    }
                }
            } else {
                // Creation and List Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Creation form card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()) // Allow form scroll inside compact area
                                .weight(1f, false),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Generate Structured Collaborative Grant Proposals",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = projectTitle,
                                onValueChange = { projectTitle = it },
                                label = { Text("Proposed Project Title") },
                                placeholder = { Text("e.g. Next-Gen Federated Learning in Cancer Research") },
                                modifier = Modifier.fillMaxWidth().testTag("grant_title_field"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = targetAgency,
                                onValueChange = { targetAgency = it },
                                label = { Text("Target Funding Agency") },
                                placeholder = { Text("e.g. NSF, Horizon Europe, NIH, private foundations") },
                                modifier = Modifier.fillMaxWidth().testTag("grant_agency_field"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = researchArea,
                                onValueChange = { researchArea = it },
                                label = { Text("Research Scientific Domain") },
                                modifier = Modifier.fillMaxWidth().testTag("grant_area_field"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = abstractProblem,
                                onValueChange = { abstractProblem = it },
                                label = { Text("Problem Background / Project Abstract") },
                                placeholder = { Text("Describe the key societal challenge, gaps in current solutions, and your proposed approach.") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .testTag("grant_problem_field"),
                                singleLine = false,
                                maxLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = objectivesText,
                                onValueChange = { objectivesText = it },
                                label = { Text("Core Scientific Objectives") },
                                placeholder = { Text("Highlight specific milestones (e.g., Objective 1: Optimize latency; Objective 2: Clinical trial validation)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .testTag("grant_objectives_field"),
                                singleLine = false,
                                maxLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    if (projectTitle.isNotBlank() && targetAgency.isNotBlank()) {
                                        viewModel.submitGrantProposalQuery(
                                            projectTitle, targetAgency, researchArea, abstractProblem, objectivesText
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("grant_submit_btn"),
                                enabled = projectTitle.isNotBlank() && targetAgency.isNotBlank() && !isLoading,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Writing Proposal Draft (takes ~15s)...")
                                } else {
                                    Icon(Icons.Default.BorderColor, contentDescription = "Draft icon")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Synthesize Grant Proposal Draft")
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
                                        if (savedProposals.isNotEmpty()) {
                                            selectedProposalForRead = savedProposals.first()
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
                                        "Successful Proposal Synthesis!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "A 5-part detailed scientific proposal has been added to your draft list.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(Icons.Default.ArrowForward, contentDescription = "View", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Past Drafts Section
                    Text(
                        text = "Your Saved Funding Drafts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (savedProposals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No grant proposal drafts compiled yet.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(savedProposals) { proposal ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedProposalForRead = proposal },
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
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Description,
                                                contentDescription = "Document icon",
                                                tint = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = proposal.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Funding Agency: ${proposal.targetAgency}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteProposal(proposal.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete draft",
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
