package com.aistudio.classroll.jkmxlp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.classroll.jkmxlp.ui.ClassRollViewModel

@Composable
fun SummaryDashboardScreen(viewModel: ClassRollViewModel) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allAttendance by viewModel.allAttendanceForYear.collectAsStateWithLifecycle()

    if (students.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No students found.")
        }
        return
    }

    // Calculate present and absent counts for each student
    val studentStats = remember(students, allAttendance) {
        students.map { student ->
            val records = allAttendance.filter { it.roll == student.roll }
            val presentCount = records.count { it.status == "P" }
            val absentCount = records.count { it.status == "A" }
            StudentStat(student.roll, student.name, presentCount, absentCount)
        }
    }

    val maxTotal = studentStats.maxOfOrNull { it.presentCount + it.absentCount } ?: 1
    val chartMax = if (maxTotal == 0) 1 else maxTotal

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Attendance Summary", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(16.dp).background(Color(0xFF4CAF50)))
            Spacer(Modifier.width(8.dp))
            Text("Present")
            Spacer(Modifier.width(24.dp))
            Box(Modifier.size(16.dp).background(Color(0xFFF44336)))
            Spacer(Modifier.width(8.dp))
            Text("Absent")
        }
        Spacer(Modifier.height(24.dp))

        LazyColumn(Modifier.fillMaxSize()) {
            items(studentStats) { stat ->
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("${stat.roll} - ${stat.name}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(24.dp)) {
                            Canvas(Modifier.fillMaxSize()) {
                                val presentRatio = stat.presentCount.toFloat() / chartMax.toFloat()
                                val absentRatio = stat.absentCount.toFloat() / chartMax.toFloat()
                                
                                val presentWidth = size.width * presentRatio
                                val absentWidth = size.width * absentRatio
                                
                                drawRoundRect(
                                    color = Color.LightGray,
                                    size = Size(size.width, size.height),
                                    cornerRadius = CornerRadius(4.dp.toPx())
                                )
                                
                                if (presentWidth > 0) {
                                    drawRoundRect(
                                        color = Color(0xFF4CAF50),
                                        size = Size(presentWidth, size.height),
                                        cornerRadius = CornerRadius(4.dp.toPx())
                                    )
                                }
                                
                                if (absentWidth > 0) {
                                    drawRoundRect(
                                        color = Color(0xFFF44336),
                                        topLeft = Offset(presentWidth, 0f),
                                        size = Size(absentWidth, size.height),
                                        cornerRadius = CornerRadius(4.dp.toPx())
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Text("P:${stat.presentCount} A:${stat.absentCount}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

data class StudentStat(
    val roll: String,
    val name: String,
    val presentCount: Int,
    val absentCount: Int
)
