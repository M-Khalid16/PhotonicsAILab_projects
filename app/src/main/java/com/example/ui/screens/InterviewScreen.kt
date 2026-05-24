package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.InterviewMessage
import com.example.data.model.InterviewSession
import com.example.ui.viewmodel.FacultyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    viewModel: FacultyViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentInterview by viewModel.currentInterview.collectAsState()
    val messages by viewModel.interviewMessages.collectAsState()
    val isLoading by viewModel.interviewLoading.collectAsState()
    val pastInterviews by viewModel.interviews.collectAsState()
    val profile by viewModel.profile.collectAsState()

    var activeTopic by remember { mutableStateOf("") }
    var userMessageText by remember { mutableStateOf("") }

    // Synchronize default topic with profile expertise
    LaunchedEffect(profile) {
        profile?.let {
            if (activeTopic.isBlank()) {
                activeTopic = it.areaOfExpertise
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll to bottom on messages update
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Mock Interview Agent", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back")) {
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
            if (currentInterview == null) {
                // Topic Selector and Launching Board View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Prepare for Faculty Positions & Academic Advancement",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    OutlinedTextField(
                        value = activeTopic,
                        onValueChange = { activeTopic = it },
                        label = { Text("Set Academic Field or Interview Topic") },
                        placeholder = { Text("e.g. Molecular Medicine, Deep Learning, Civil Pedagogy") },
                        leadingIcon = { Icon(Icons.Default.Class, contentDescription = "Class topic") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("interview_topic_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (activeTopic.isNotBlank()) {
                                viewModel.startNewInterview(activeTopic) { }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_start_interview"),
                        enabled = activeTopic.isNotBlank(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play icon")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Initiate Professional AI Interview", fontWeight = FontWeight.Bold)
                    }

                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Text(
                        text = "Historic Interview Scores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (pastInterviews.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No prior mock sessions saved.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(pastInterviews) { session ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.loadInterviewSession(session.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = session.topic,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (session.isCompleted) "Completed" else "In Progress...",
                                                fontSize = 12.sp,
                                                color = if (session.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        if (session.score != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${session.score}%",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        IconButton(onClick = { viewModel.deleteInterviewSession(session.id) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete score",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Interactive Dialog chat interface with AI Agent
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Session Heading Status Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CastForEducation,
                                contentDescription = "Active chat",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentInterview?.topic ?: "AI Academic Interviewer",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Evaluation Loop (" + if (currentInterview?.isCompleted == true) "Completed" else "Evaluating..." + ")",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            // Reset Session
                            OutlinedButton(
                                onClick = { viewModel.startNewInterview(currentInterview?.topic ?: activeTopic) { } },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restart", fontSize = 11.sp)
                            }
                        }
                    }

                    // Chat messages scroll column
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages) { msg ->
                            val isAi = msg.sender == "AI"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isAi) 0.dp else 16.dp,
                                                bottomEnd = if (isAi) 16.dp else 0.dp
                                            )
                                        )
                                        .background(
                                            if (isAi) MaterialTheme.colorScheme.surfaceVariant
                                            else MaterialTheme.colorScheme.primary
                                        )
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (isAi) "AI Interviewer" else "Professor (You)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        if (isAi && currentInterview?.isCompleted == true && msg == messages.last()) {
                                            // Render nice markdown performance score card in the last message
                                            MarkdownText(markdown = msg.text)
                                        } else {
                                            Text(
                                                text = msg.text,
                                                fontSize = 14.sp,
                                                color = if (isAi) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                "Interviewer is analyzing your response...",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom message input panel
                    if (currentInterview?.isCompleted == false) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = userMessageText,
                                onValueChange = { userMessageText = it },
                                placeholder = { Text("Type your answer here...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_text"),
                                maxLines = 3,
                                shape = RoundedCornerShape(24.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (userMessageText.isNotBlank()) {
                                        viewModel.sendInterviewAnswer(userMessageText)
                                        userMessageText = ""
                                    }
                                },
                                enabled = userMessageText.isNotBlank() && !isLoading,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (userMessageText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .testTag("btn_send_message")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send answer",
                                    tint = if (userMessageText.isNotBlank() && !isLoading) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    } else {
                        // Finished view options
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startNewInterview(currentInterview?.topic ?: activeTopic) {} },
                                modifier = Modifier.fillMaxWidth().testTag("btn_done_interview")
                            ) {
                                Icon(Icons.Default.School, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Take Next Mock Interview")
                            }
                        }
                    }
                }
            }
        }
    }
}
