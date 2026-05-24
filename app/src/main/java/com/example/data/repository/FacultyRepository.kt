package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FacultyRepository(private val db: AppDatabase) {

    private val profileDao = db.profileDao()
    private val interviewDao = db.interviewDao()
    private val researchPaperDao = db.researchPaperDao()
    private val grantProposalDao = db.grantProposalDao()
    private val areaTrainingDao = db.areaTrainingDao()

    // --- Profile ---
    fun getProfile(): Flow<Profile?> = profileDao.getProfileAsList().map { it.firstOrNull() }
    suspend fun getProfileOneShot(): Profile? = profileDao.getProfileOneShot()
    suspend fun saveProfile(profile: Profile) = profileDao.insertProfile(profile)

    // --- Interviews ---
    fun getAllInterviews(): Flow<List<InterviewSession>> = interviewDao.getAllInterviews()
    fun getInterviewById(id: Int): Flow<InterviewSession?> = interviewDao.getInterviewById(id)
    suspend fun getInterviewByIdOneShot(id: Int): InterviewSession? = interviewDao.getInterviewByIdOneShot(id)
    suspend fun saveInterview(session: InterviewSession): Int {
        return interviewDao.insertInterview(session).toInt()
    }
    suspend fun deleteInterview(id: Int) = interviewDao.deleteInterview(id)

    // --- Research Papers ---
    fun getAllPapers(): Flow<List<ResearchPaper>> = researchPaperDao.getAllPapers()
    suspend fun savePaper(paper: ResearchPaper): Int {
        return researchPaperDao.insertPaper(paper).toInt()
    }
    suspend fun deletePaper(id: Int) = researchPaperDao.deletePaper(id)

    // --- Grant Proposals ---
    fun getAllProposals(): Flow<List<GrantProposal>> = grantProposalDao.getAllProposals()
    fun getProposalById(id: Int): Flow<GrantProposal?> = grantProposalDao.getProposalById(id)
    suspend fun saveProposal(proposal: GrantProposal): Int {
        return grantProposalDao.insertProposal(proposal).toInt()
    }
    suspend fun deleteProposal(id: Int) = grantProposalDao.deleteProposal(id)

    // --- Trainings ---
    fun getAllAreaTrainings(): Flow<List<AreaTraining>> = areaTrainingDao.getAllTrainings()
    suspend fun saveAreaTraining(training: AreaTraining): Int {
        return areaTrainingDao.insertTraining(training).toInt()
    }
    suspend fun deleteAreaTraining(id: Int) = areaTrainingDao.deleteTraining(id)

    // --- Gemini API integrations ---

    private suspend fun callGemini(
        prompt: String,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API Key is missing. Please configure it in the Secrets panel in AI Studio."
        }

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            },
            generationConfig = GenerationConfig(temperature = 0.7f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No response from Gemini"
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "Failed to contact Gemini API. Please check your network connection."}"
        }
    }

    suspend fun generateNextInterviewTurn(
        history: List<InterviewMessage>,
        profile: Profile
    ): String {
        val systemPrompt = """
            You are an expert academic interviewer conducting a thorough interview/assessment for a university faculty position or academic advancement.
            The candidate's details are:
            Name: ${profile.name}
            Department: ${profile.department}
            Area of Expertise: ${profile.areaOfExpertise}
            PhD Work: ${profile.phdWork}
            Recent Work/Publications: ${profile.recentWork}
            
            GUIDELINES:
            1. Conduct a deep, highly specialized, and rigorous academic dialogue in their specific scientific/humanities area.
            2. Ask exactly ONE deep, challenging technical, pedagogical, or research-oriented question at a time.
            3. Respectfully evaluate the candidate's responses briefly, noting precision or missing factors before asking the next question.
            4. Transition naturally through their: 
               - PhD Thesis / Core Theory (Question 1)
               - Recent Publications / Current Technical Innovation (Question 2)
               - Future Research Direction and Collaboration Potential (Question 3)
               - Teaching Pedagogy and Student Mentorship approach (Question 4)
            5. In the final response (after candidate answers the 4th question, making this turn the 5th prompt), write a detailed feedback summary. 
               The feedback must evaluate core competencies: technical mastery, pedagogical depth, and grant competitiveness.
               Critically, you MUST prefix the overall summary with the header '## INTERVIEW SUMMARY ##'.
               And on the final lines, output their technical grade out of 100 in the exact format: 'SCORE: <X>/100'.
        """.trimIndent()

        // Build dialogue transcript
        val transcript = StringBuilder()
        if (history.isEmpty()) {
            transcript.append("Start the interview by greeting the candidate professionally, acknowledging their area of expertise, and asking the first question regarding their PhD Work.")
        } else {
            for (msg in history) {
                transcript.append("${msg.sender}: ${msg.text}\n\n")
            }
            transcript.append("Candidate just gave their response. Evaluate it and ask the next question or draft the '## INTERVIEW SUMMARY ##' if 4 questions have already been answered.")
        }

        return callGemini(prompt = transcript.toString(), systemInstruction = systemPrompt)
    }

    suspend fun generatePaperSummaryAndProjects(
        topic: String,
        field: String
    ): String {
        val prompt = """
            Please search/synthesize detailed scientific information regarding the latest high-impact research papers from 2024-2026 in the field of '$field' relating to the topic '$topic'.
            
            Synthesize and format your response in a highly professional and academic layout, matching this exact structure:
            
            # CORE SCIENTIFIC PAPERS SUMMARY
            Provide a summary of 2-3 influential publications, including authorship, years, major scientific breakthroughs, methodology, and theoretical findings.
            
            # BSc THESIS PROJECT IDEAS (Undergraduate)
            Provide 2 highly relevant, practical, and implementable project titles and brief 3-sentence descriptions suitable for undergraduate final-year students (requires implementation, coding, or standard experiments).
            
            # MSc THESIS PROJECT IDEAS (Graduate)
            Provide 2 professional project titles with comprehensive methodologies and comparative analytical objectives, suitable for MSc students (requires research, simulation, comparative analysis, or architecture design).
            
            # PhD DISSERTATION DIRECTIONS
            Provide 2 novel, high-difficulty research directions with deep mathematical modeling, algorithmic formulation or architectural expansion possibilities, suitable for PhD research.
        """.trimIndent()

        return callGemini(prompt = prompt)
    }

    suspend fun generateGrantProposal(
        title: String,
        agency: String,
        area: String,
        abstractText: String,
        objectives: String
    ): String {
        val prompt = """
            You are a senior principal investigator and academic grant consultant specializing in drafting successful scientific proposals.
            Please write a comprehensive, highly persuasive research grant proposal draft targeting the '$agency' for a project in '$area'.
            
            PROPOSAL METADATA:
            Project Title: $title
            Research Objectives: $objectives
            Problem Statement / Abstract background: $abstractText
            
            Draft a complete proposal covering the following academic sections, fleshing out technical specs, methodologies, and clear scientific jargon:
            
            1. EXECUTIVE SUMMARY & NOVELTY
            Draft a strong, compelling page abstract highlighting why this research is urgent and highly disruptive.
            
            2. SCIENTIFIC & TECHNICAL OBJECTIVES
            Detail a minimum of 3 precise scientific/pedagogical milestones or research objectives.
            
            3. WORK PLAN & METHODOLOGICAL FRAMEWORK
            Detail a phase-based methodology (Phase I, II, III) with technical methodologies, experimental tools, algorithms, or research instruments.
            
            4. BROADER STRATEGIC IMPACTS
            Highlight educational value, student mentorship inclusion (BSc, MSc, PhD involvement), university-industry translation, and economic benefits.
            
            5. DETAILED BUDGET AND JUSTIFICATION
            Provide a clear, formatted text markdown table representing the budget breakdown (Personnel, Equipment, Consumables, Travel, Overhead) summing to a realistic grant budget, with 1-sentence justifications for each.
        """.trimIndent()

        return callGemini(prompt = prompt)
    }

    suspend fun generateSyllabus(
        area: String,
        difficulty: String
    ): String {
        val prompt = """
            Produce a modern, comprehensive professional development training curriculum and syllabus in English designed to upscale university level professors, instructors, and faculty in the area of '$area'.
            Target faculty profile level: $difficulty.
            
            Format the syllabus beautifully including the following distinct sections in high-fidelity markdown:
            
            # COURSE METADATA
            Title, brief pedagogical philosophy, prerequisites, and learning objectives.
            
            # MODULE-BY-MODULE SYLLABUS
            Provide 4 distinct modules (equivalent to 4 weeks or a comprehensive semester training). Each module must list:
            - Weekly topics (theoretical + pedagogical application)
            - Required scientific papers and books to read (from top-tier journals)
            - Faculty Workshop/Hands-on laboratory activity (e.g., designing curriculum, running simulations)
            - Interactive seminar discussion questions
            
            # RECOMMENDED READING & REFERENCES
            A curated bibliography of 4-5 high-impact papers and textbooks.
            
            # ASSESSMENT FRAMEWORK & CERTIFICATION
            How the faculty's progress is tested (e.g. peer review, sample class presentation), ensuring they are certified and interview-ready.
        """.trimIndent()

        return callGemini(prompt = prompt)
    }
}
