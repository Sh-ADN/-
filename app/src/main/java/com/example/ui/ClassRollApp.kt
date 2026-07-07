package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.*

@Composable
fun ClassRollApp(viewModel: ClassRollViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.Home

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Routes.Home,
                    onClick = { navController.navigate(Routes.Home) },
                    icon = { Icon(Icons.Default.Checklist, "Take Attendance") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.Register,
                    onClick = { navController.navigate(Routes.Register) },
                    icon = { Icon(Icons.Default.GridOn, "Register") },
                    label = { Text("Register") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.ImportStudents,
                    onClick = { navController.navigate(Routes.ImportStudents) },
                    icon = { Icon(Icons.Default.UploadFile, "Import") },
                    label = { Text("Import") }
                )
                NavigationBarItem(
                    selected = currentRoute == Routes.Settings,
                    onClick = { navController.navigate(Routes.Settings) },
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Routes.Home, Modifier.padding(innerPadding)) {
            composable(Routes.Home) { HomeScreen(viewModel) }
            composable(Routes.Register) { RegisterScreen(viewModel) }
            composable(Routes.ImportStudents) { ImportStudentsScreen(viewModel) }
            composable(Routes.Settings) { SettingsScreen(viewModel) }
        }
    }
}
