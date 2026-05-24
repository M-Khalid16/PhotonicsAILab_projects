package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.RetrofitClient
import com.example.data.repository.FacultyRepository
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FacultyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FacultyRepository
    
    // Core database flows
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState = _profileState.asStateFlow()

    val profile: StateFlow<Profile?>
    val interviews: StateFlow<List<InterviewSession>>
    val papers: StateFlow<List<ResearchPaper>>
    val proposals: StateFlow<List<GrantProposal>>
    val trainings: StateFlow<List<AreaTraining>>

    // Active screen tracking states
    private val _currentInterview = MutableStateFlow<InterviewSession?>(null)
    val currentInterview = _currentInterview.asStateFlow()

    private val _interviewMessages = MutableStateFlow<List<InterviewMessage>>(emptyList())
    val interviewMessages = _interviewMessages.asStateFlow()

    private val _interviewLoading = MutableStateFlow(false)
    val interviewLoading = _interviewLoading.asStateFlow()

    // Research operations states
    private val _researchLoading = MutableStateFlow(false)
    val researchLoading = _researchLoading.asStateFlow()

    private val _generatedPaperResult = MutableStateFlow<String?>(null)
    val generatedPaperResult = _generatedPaperResult.asStateFlow()

    // Grant operations states
    private val _grantLoading = MutableStateFlow(false)
    val grantLoading = _grantLoading.asStateFlow()

    private val _generatedGrantResult = MutableStateFlow<String?>(null)
    val generatedGrantResult = _generatedGrantResult.asStateFlow()

    // Training operations states
    private val _trainingLoading = MutableStateFlow(false)
    val trainingLoading = _trainingLoading.asStateFlow()

    private val _generatedTrainingResult = MutableStateFlow<String?>(null)
    val generatedTrainingResult = _generatedTrainingResult.asStateFlow()

    // Moshi serializers
    private val messageListAdapter = RetrofitClient.moshi.adapter<List<InterviewMessage>>(
        Types.newParameterizedType(List::class.java, InterviewMessage::class.java)
    )

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FacultyRepository(db)

        profile = repository.getProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        viewModelScope.launch {
            repository.getProfile().collect { prof ->
                _profileState.value = if (prof == null) ProfileState.Empty else ProfileState.Success(prof)
            }
        }

        interviews = repository.getAllInterviews()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        papers = repository.getAllPapers()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        proposals = repository.getAllProposals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        trainings = repository.getAllAreaTrainings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    private fun serializeMessages(messages: List<InterviewMessage>): String {
        return try {
            messageListAdapter.toJson(messages)
        } catch (e: Exception) {
            "[]"
        }
    }

    private fun deserializeMessages(json: String): List<InterviewMessage> {
        return try {
            messageListAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Profile Management ---
    fun updateProfile(name: String, department: String, area: String, phd: String, recent: String) {
        viewModelScope.launch {
            val updated = Profile(
                id = 1,
                name = name,
                department = department,
                areaOfExpertise = area,
                phdWork = phd,
                recentWork = recent
            )
            repository.saveProfile(updated)
        }
    }

    // --- Interview Operations ---
    fun startNewInterview(topic: String, onStarted: (Int) -> Unit) {
        viewModelScope.launch {
            _interviewLoading.value = true
            val initialSession = InterviewSession(
                topic = topic,
                isCompleted = false,
                serializedMessages = "[]"
            )
            val newId = repository.saveInterview(initialSession)
            
            // Get user profile context
            val currentProfile = repository.getProfileOneShot() ?: Profile(
                name = "Professor",
                department = "General Academic",
                areaOfExpertise = topic
            )

            // Get initial system question from Gemini
            val initialAiText = repository.generateNextInterviewTurn(emptyList(), currentProfile)
            val initialMessage = InterviewMessage(sender = "AI", text = initialAiText)
            
            val updatedMessages = listOf(initialMessage)
            val updatedSession = initialSession.copy(
                id = newId,
                serializedMessages = serializeMessages(updatedMessages)
            )
            
            repository.saveInterview(updatedSession)
            
            _currentInterview.value = updatedSession
            _interviewMessages.value = updatedMessages
            _interviewLoading.value = false
            onStarted(newId)
        }
    }

    fun loadInterviewSession(id: Int) {
        viewModelScope.launch {
            _interviewLoading.value = true
            val session = repository.getInterviewByIdOneShot(id)
            if (session != null) {
                _currentInterview.value = session
                _interviewMessages.value = deserializeMessages(session.serializedMessages)
            }
            _interviewLoading.value = false
        }
    }

    fun sendInterviewAnswer(userText: String) {
        val session = _currentInterview.value ?: return
        val profileContext = profile.value ?: Profile(name = "Faculty Member", areaOfExpertise = session.topic)
        
        viewModelScope.launch {
            _interviewLoading.value = true
            
            // Append User reply
            val userMsg = InterviewMessage(sender = "USER", text = userText)
            val newMessages = _interviewMessages.value + userMsg
            _interviewMessages.value = newMessages
            
            // Save immediately to DB
            var updatedSession = session.copy(
                serializedMessages = serializeMessages(newMessages)
            )
            repository.saveInterview(updatedSession)

            // API Call: Gemini generates next question OR final score summary
            val aiResponse = repository.generateNextInterviewTurn(newMessages, profileContext)
            
            // Analyze if Gemini finalized the interview summary
            val isSummary = aiResponse.contains("## INTERVIEW SUMMARY ##")
            var finalScore: Int? = null
            var feedbackText: String? = null
            
            if (isSummary) {
                // Parse score from format SCORE: X/100
                val scoreQuery = Regex("SCORE:\\s*(\\d+)")
                val match = scoreQuery.find(aiResponse)
                finalScore = match?.groupValues?.getOrNull(1)?.toIntOrNull()
                feedbackText = aiResponse.replace("## INTERVIEW SUMMARY ##", "").trim()
            }

            val aiMsg = InterviewMessage(sender = "AI", text = aiResponse)
            val finalMessages = newMessages + aiMsg
            _interviewMessages.value = finalMessages

            updatedSession = updatedSession.copy(
                serializedMessages = serializeMessages(finalMessages),
                isCompleted = isSummary,
                score = finalScore ?: updatedSession.score,
                generalFeedback = feedbackText ?: updatedSession.generalFeedback
            )
            
            repository.saveInterview(updatedSession)
            _currentInterview.value = updatedSession
            _interviewLoading.value = false
        }
    }

    fun deleteInterviewSession(id: Int) {
        viewModelScope.launch {
            repository.deleteInterview(id)
        }
    }

    // --- Research summarizes & Projects ---
    fun submitResearchQuery(topic: String, field: String) {
        viewModelScope.launch {
            _researchLoading.value = true
            _generatedPaperResult.value = null
            val response = repository.generatePaperSummaryAndProjects(topic, field)
            _generatedPaperResult.value = response
            
            // Automatically save to database for offline learning (upgradability/cache stability)
            // Parse out structured titles from response (or use defaults if complex)
            val paper = ResearchPaper(
                title = "Research Insights: $topic",
                authors = "Synthesized Academic literature (2024-2026)",
                queryTopic = topic,
                summary = response,
                bscProjects = "Curated BSc projects",
                mscProjects = "Curated MSc projects",
                phdProjects = "Curated PhD directions"
            )
            repository.savePaper(paper)
            _researchLoading.value = false
        }
    }

    fun deletePaper(id: Int) {
        viewModelScope.launch {
            repository.deletePaper(id)
        }
    }

    // --- Grant Proposal Writer ---
    fun submitGrantProposalQuery(title: String, agency: String, area: String, abstractText: String, objectives: String) {
        viewModelScope.launch {
            _grantLoading.value = true
            _generatedGrantResult.value = null
            val response = repository.generateGrantProposal(title, agency, area, abstractText, objectives)
            _generatedGrantResult.value = response

            val proposal = GrantProposal(
                title = title,
                targetAgency = agency,
                researchArea = area,
                abstractText = abstractText,
                objectives = objectives,
                methodology = "Generated Methodology Framework",
                budgetPlan = "Generated Budget plan",
                rawFullProposalText = response
            )
            repository.saveProposal(proposal)
            _grantLoading.value = false
        }
    }

    fun deleteProposal(id: Int) {
        viewModelScope.launch {
            repository.deleteProposal(id)
        }
    }

    // --- Professional Area training ---
    fun submitTrainingQuery(area: String, difficulty: String) {
        viewModelScope.launch {
            _trainingLoading.value = true
            _generatedTrainingResult.value = null
            val response = repository.generateSyllabus(area, difficulty)
            _generatedTrainingResult.value = response

            val training = AreaTraining(
                field = area,
                title = "Faculty Training: $area ($difficulty)",
                description = "Modern English-focused Curriculum for upskilling",
                syllabusMarkdown = response,
                difficultyLevel = difficulty
            )
            repository.saveAreaTraining(training)
            _trainingLoading.value = false
        }
    }

    fun deleteTraining(id: Int) {
        viewModelScope.launch {
            repository.deleteAreaTraining(id)
        }
    }
}

sealed interface ProfileState {
    object Loading : ProfileState
    object Empty : ProfileState
    data class Success(val profile: Profile) : ProfileState
}
