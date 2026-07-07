package com.aistudio.classroll.jkmxlp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.classroll.jkmxlp.ui.ClassRollViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ClassRollViewModel) {
    val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    
    var yearInput by remember(currentYear) { mutableStateOf(currentYear) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = yearInput,
                onValueChange = { yearInput = it },
                label = { Text("Academic Year") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableYears.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year) },
                        onClick = {
                            yearInput = year
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { viewModel.updateSettings(yearInput) }) {
                Text("Save Settings")
            }
            Button(onClick = { 
                viewModel.updateSettings(yearInput)
                viewModel.fetchYears() 
            }) {
                Text("Fetch Years")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { viewModel.syncStudents() }, modifier = Modifier.fillMaxWidth()) {
            Text("Sync Students from Server")
        }
    }
}
