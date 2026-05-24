package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*
import com.example.ui.theme.FacultyAidTheme
import com.example.ui.viewmodel.FacultyViewModel
import com.example.ui.viewmodel.ProfileState

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FacultyAidTheme {
        val navController = rememberNavController()
        val viewModel: FacultyViewModel = viewModel()
        
        NavHost(
            navController = navController,
            startDestination = "router",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("router") {
                val state by viewModel.profileState.collectAsState()
                
                LaunchedEffect(state) {
                    when (state) {
                        ProfileState.Loading -> { /* Wait */ }
                        ProfileState.Empty -> {
                            navController.navigate("profile") {
                                popUpTo("router") { inclusive = true }
                            }
                        }
                        is ProfileState.Success -> {
                            navController.navigate("dashboard") {
                                popUpTo("router") { inclusive = true }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToDashboard = {
                        navController.navigate("dashboard") {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { navController.navigate("profile") },
                    onNavigateToInterview = { navController.navigate("interview") },
                    onNavigateToResearch = { navController.navigate("research") },
                    onNavigateToGrant = { navController.navigate("grant") },
                    onNavigateToTraining = { navController.navigate("training") }
                )
            }
            composable("interview") {
                InterviewScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("research") {
                ResearchScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("grant") {
                GrantScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("training") {
                TrainingScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
      }
    }
  }
}