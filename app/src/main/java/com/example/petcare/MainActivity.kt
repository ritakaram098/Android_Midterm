package com.example.petcare

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petcare.ui.AddCareTaskScreen
import com.example.petcare.ui.AddPetScreen
import com.example.petcare.ui.CareTaskScreen
import com.example.petcare.ui.CareTaskViewModel
import com.example.petcare.ui.CareTaskViewModelFactory
import com.example.petcare.ui.PetListScreen
import com.example.petcare.ui.PetViewModel
import com.example.petcare.ui.theme.PetCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetCareTheme {
                PetCareApp()
            }
        }
    }
}

@Composable
fun PetCareApp() {
    val navController = rememberNavController()
    val petViewModel: PetViewModel = viewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = "pet_list"
        ) {
            composable("pet_list") {
                PetListScreen(
                    viewModel = petViewModel,
                    onAddPetClick = { navController.navigate("add_pet") },
                    onPetClick = { petId ->
                        navController.navigate("care_tasks/$petId")
                    }
                )
            }
            composable("add_pet") {
                AddPetScreen(
                    viewModel = petViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "care_tasks/{petId}",
                arguments = listOf(navArgument("petId") { type = NavType.LongType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getLong("petId") ?: 0L
                val context = LocalContext.current
                val careTaskViewModel: CareTaskViewModel = viewModel(
                    factory = CareTaskViewModelFactory(context.applicationContext as Application, petId)
                )
                CareTaskScreen(
                    viewModel = careTaskViewModel,
                    onAddTaskClick = { navController.navigate("add_care_task/$petId") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "add_care_task/{petId}",
                arguments = listOf(navArgument("petId") { type = NavType.LongType })
            ) { backStackEntry ->
                val petId = backStackEntry.arguments?.getLong("petId") ?: 0L
                val context = LocalContext.current
                val careTaskViewModel: CareTaskViewModel = viewModel(
                    factory = CareTaskViewModelFactory(context.applicationContext as Application, petId)
                )
                AddCareTaskScreen(
                    viewModel = careTaskViewModel,
                    pet = careTaskViewModel.pet.collectAsState().value,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
