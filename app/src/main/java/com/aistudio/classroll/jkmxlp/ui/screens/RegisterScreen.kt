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
    val students by viewModel.getStudents().collectAsStateWithLifecycle()
    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    val currentMonthStr = monthFormat.format(Date())
    
    val attendanceRecords by viewModel.getAttendanceForMonth(currentMonthStr).collectAsStateWithLifecycle()
    
    if (students.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students to display.")
        }
        return
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

    Column(Modifier.fillMaxSize()) {
        Text("Register: $currentMonthStr", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        
        Row(Modifier.weight(1f)) {
            // Left Sticky Column (Roll & Name)
            Column(Modifier.width(120.dp).border(1.dp, Color.Gray)) {
                // Header cell
                Box(Modifier.height(48.dp).fillMaxWidth().background(Color.LightGray).border(1.dp, Color.Gray), contentAlignment = Alignment.Center) {
                    Text("Roll / Name", style = MaterialTheme.typography.labelSmall)
                }
                // Names column
                Column(Modifier.verticalScroll(verticalScrollState)) {
                    students.forEach { student ->
                        Box(Modifier.height(48.dp).fillMaxWidth().border(1.dp, Color.Gray).padding(4.dp), contentAlignment = Alignment.CenterStart) {
                            Text("${student.roll} ${student.name}", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Grid (Dates & Attendance)
            Column(Modifier.fillMaxWidth().horizontalScroll(horizontalScrollState)) {
                // Header row (Dates)
                Row(Modifier.height(48.dp).background(Color.LightGray)) {
                    dates.forEach { date ->
                        val dayStr = date.substringAfterLast("-")
                        Box(Modifier.width(48.dp).fillMaxHeight().border(1.dp, Color.Gray), contentAlignment = Alignment.Center) {
                            Text(dayStr, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                // Attendance Cells
                Column(Modifier.verticalScroll(verticalScrollState)) {
                    students.forEach { student ->
                        Row(Modifier.height(48.dp)) {
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
