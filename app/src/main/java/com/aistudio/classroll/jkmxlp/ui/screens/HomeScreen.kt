package com.aistudio.classroll.jkmxlp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.classroll.jkmxlp.data.AttendanceRecordEntity
import com.aistudio.classroll.jkmxlp.data.StudentEntity
import com.aistudio.classroll.jkmxlp.ui.ClassRollViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ClassRollViewModel) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
    var currentIndex by remember { mutableStateOf(0) }
    val records = remember { mutableStateListOf<AttendanceRecordEntity>() }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    var submissionMessage by remember { mutableStateOf("") }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)?.time
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it))
                        currentIndex = 0
                        records.clear()
                        submissionMessage = ""
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (students.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students found. Import students first.")
        }
        return
    }

    if (currentIndex >= students.size) {
        val presentCount = records.count { it.status == "P" }
        val absentCount = records.count { it.status == "A" }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Summary", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            Text("Present: $presentCount", style = MaterialTheme.typography.bodyLarge)
            Text("Absent: $absentCount", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))
            Button(onClick = { 
                submissionMessage = "Submitting..."
                viewModel.submitAttendance(date, records) { success ->
                    submissionMessage = if (success) "Saved successfully!" else "Failed to save."
                }
            }) {
                Text("Submit")
            }
            if (submissionMessage.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(submissionMessage)
            }
        }
        return
    }

    val currentStudent = students[currentIndex]
    
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Attendance for $date", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                }
                Text("${currentIndex + 1} / ${students.size}", style = MaterialTheme.typography.bodyLarge)
            }
            if (currentIndex > 0) {
                TextButton(onClick = {
                    currentIndex--
                    records.removeLast()
                }) {
                    Text("Undo")
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        key(currentStudent.roll) {
            SwipeableStudentCard(
                student = currentStudent,
                onSwiped = { status ->
                    records.add(AttendanceRecordEntity(currentYear, date, currentStudent.roll, status, false))
                    currentIndex++
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableStudentCard(
    student: StudentEntity,
    onSwiped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            dismissValue != SwipeToDismissBoxValue.Settled
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd -> onSwiped("P")
            SwipeToDismissBoxValue.EndToStart -> onSwiped("A")
            else -> {}
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336)
                else -> Color.LightGray
            }
            val alignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            Box(Modifier.fillMaxSize().background(color, MaterialTheme.shapes.large).padding(24.dp), contentAlignment = alignment) {
                Text(
                    when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.StartToEnd -> "PRESENT"
                        SwipeToDismissBoxValue.EndToStart -> "ABSENT"
                        else -> ""
                    },
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(student.roll, style = MaterialTheme.typography.displayLarge)
                Spacer(Modifier.height(24.dp))
                Text(student.name, style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}
