package com.aistudio.classroll.jkmxlp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.classroll.jkmxlp.ui.ClassRollViewModel

@Composable
fun SettingsScreen(viewModel: ClassRollViewModel) {
    val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
    var yearInput by remember(currentYear) { mutableStateOf(currentYear) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = yearInput,
            onValueChange = { yearInput = it },
            label = { Text("Academic Year (e.g. 2026)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.updateSettings(yearInput) }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Settings")
        }
    }
}
