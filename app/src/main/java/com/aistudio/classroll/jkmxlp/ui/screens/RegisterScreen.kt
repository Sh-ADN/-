package com.aistudio.classroll.jkmxlp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.classroll.jkmxlp.data.AttendanceRecordEntity
import com.aistudio.classroll.jkmxlp.ui.ClassRollViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RegisterScreen(viewModel: ClassRollViewModel) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
    val monthFormat = remember { SimpleDateFormat("yyyy-MM", Locale.US) }
    val currentMonthStr = remember { monthFormat.format(Date()) }
    
    val attendanceRecordsFlow = remember(currentMonthStr, currentYear) { viewModel.getAttendanceForMonth(currentMonthStr) }
    val attendanceRecords by attendanceRecordsFlow.collectAsStateWithLifecycle()
    
    var showStudentDialog by remember { mutableStateOf(false) }
    var editingStudentRoll by remember { mutableStateOf("") }
    var editingStudentName by remember { mutableStateOf("") }
    
    if (showStudentDialog) {
        AlertDialog(
            onDismissRequest = { showStudentDialog = false },
            title = { Text(if (editingStudentRoll.isNotBlank() && students.any { it.roll == editingStudentRoll }) "Edit Student" else "Add Student") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editingStudentRoll,
                        onValueChange = { editingStudentRoll = it },
                        label = { Text("Roll Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editingStudentName,
                        onValueChange = { editingStudentName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editingStudentRoll.isNotBlank()) {
                        viewModel.addOrUpdateStudent(editingStudentRoll, editingStudentName)
                    }
                    showStudentDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    if (editingStudentRoll.isNotBlank() && students.any { it.roll == editingStudentRoll }) {
                        TextButton(onClick = {
                            viewModel.deleteStudent(editingStudentRoll)
                            showStudentDialog = false
                        }) {
                            Text("Delete", color = Color.Red)
                        }
                    }
                    TextButton(onClick = { showStudentDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Get all dates in current month
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dates = (1..daysInMonth).map { day ->
        String.format(Locale.US, "%s-%02d", currentMonthStr, day)
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                editingStudentRoll = ""
                editingStudentName = ""
                showStudentDialog = true 
            }) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Text("Register: $currentMonthStr", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
            
            if (students.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students to display.")
                }
            } else {
                Box(
                    Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScrollState)
                        .verticalScroll(verticalScrollState)
                ) {
                    Column {
                        // Header Row
                        Row(Modifier.height(48.dp).background(Color.LightGray)) {
                            // Name header
                            Box(Modifier.width(120.dp).fillMaxHeight().border(1.dp, Color.Gray), contentAlignment = Alignment.Center) {
                                Text("Roll / Name", style = MaterialTheme.typography.labelSmall)
                            }
                            // Date headers
                            dates.forEach { date ->
                                val dayStr = date.substringAfterLast("-")
                                Box(Modifier.width(48.dp).fillMaxHeight().border(1.dp, Color.Gray), contentAlignment = Alignment.Center) {
                                    Text(dayStr, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Data Rows
                        students.forEach { student ->
                            Row(Modifier.height(48.dp)) {
                                // Name cell
                                Box(
                                    Modifier.width(120.dp).fillMaxHeight().border(1.dp, Color.Gray).padding(4.dp)
                                        .clickable {
                                            editingStudentRoll = student.roll
                                            editingStudentName = student.name
                                            showStudentDialog = true
                                        },
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text("${student.roll} ${student.name}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                                }

                                // Attendance cells
                                dates.forEach { date ->
                                    val record = attendanceRecords.find { it.roll == student.roll && it.date == date }
                                    val status = record?.status ?: ""
                                    val cellColor = when (status) {
                                        "P" -> Color(0xFFC8E6C9)
                                        "A" -> Color(0xFFFFCDD2)
                                        else -> Color.Transparent
                                    }

                                    Box(
                                        Modifier
                                            .width(48.dp)
                                            .fillMaxHeight()
                                            .background(cellColor)
                                            .border(1.dp, Color.Gray)
                                            .clickable {
                                                val nextStatus = when (status) {
                                                    "" -> "P"
                                                    "P" -> "A"
                                                    else -> ""
                                                }
                                                viewModel.updateCell(date, student.roll, nextStatus)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(status, textAlign = TextAlign.Center)
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
