package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ClassRollRepository(
    private val dao: ClassRollDao,
    private val api: ApiService,
    private val settingsRepo: SettingsRepository
) {
    fun getStudentsForYear(year: String): Flow<List<StudentEntity>> {
        return dao.getStudentsForYear(year)
    }

    fun getAttendanceForDate(year: String, date: String): Flow<List<AttendanceRecordEntity>> {
        return dao.getAttendanceForDate(year, date)
    }
    
    fun getAttendanceForMonth(year: String, month: String): Flow<List<AttendanceRecordEntity>> {
        return dao.getAttendanceForMonth(year, month)
    }

    // Sync Students from Server
    suspend fun syncStudents() {
        val url = settingsRepo.webAppUrlFlow.first()
        val token = settingsRepo.appsScriptTokenFlow.first()
        val year = settingsRepo.academicYearFlow.first()
        if (url.isBlank() || token.isBlank() || year.isBlank()) return

        try {
            val response = api.getStudents(url, token = token, year = year)
            val entities = response.students.map {
                StudentEntity(year = year, roll = it.roll, name = it.name, active = it.active)
            }
            dao.insertStudents(entities)
        } catch (e: Exception) {
            e.printStackTrace()
            // In a production app, handle errors properly
        }
    }

    suspend fun importStudents(year: String, students: List<RemoteStudent>): String {
        val url = settingsRepo.webAppUrlFlow.first()
        val token = settingsRepo.appsScriptTokenFlow.first()
        if (url.isBlank() || token.isBlank()) return "URL or token is blank"

        return try {
            val request = ImportStudentsRequest(token = token, year = year, students = students)
            val response = api.importStudents(url, request = request)
            if (response.success) {
                // Save locally
                val entities = students.map {
                    StudentEntity(year = year, roll = it.roll, name = it.name, active = it.active)
                }
                dao.insertStudents(entities)
                "Success"
            } else {
                "API Error"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.message ?: "Unknown error"
        }
    }

    // Submit a single day's attendance
    suspend fun submitAttendance(year: String, date: String, records: List<AttendanceRecordEntity>): Boolean {
        // Save locally first (marked as unsynced initially)
        dao.insertAttendanceRecords(records)

        val url = settingsRepo.webAppUrlFlow.first()
        val token = settingsRepo.appsScriptTokenFlow.first()
        if (url.isBlank() || token.isBlank()) return false

        val remoteRecords = records.map { RemoteAttendanceRecord(roll = it.roll, status = it.status) }
        val request = SubmitAttendanceRequest(token = token, year = year, date = date, records = remoteRecords)
        
        return try {
            val response = api.submitAttendance(url, request = request)
            if (response.success) {
                // Mark as synced locally
                records.forEach { dao.markSynced(year, date, it.roll) }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Update single cell (from Register screen)
    suspend fun updateAttendanceCell(year: String, date: String, roll: String, status: String): Boolean {
        // Update locally
        dao.insertAttendanceRecords(listOf(AttendanceRecordEntity(year, date, roll, status, isSynced = false)))

        val url = settingsRepo.webAppUrlFlow.first()
        val token = settingsRepo.appsScriptTokenFlow.first()
        if (url.isBlank() || token.isBlank()) return false

        val request = UpdateCellRequest(token = token, year = year, date = date, roll = roll, status = status)
        return try {
            val response = api.updateAttendanceCell(url, request = request)
            if (response.success) {
                dao.markSynced(year, date, roll)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun fetchYears(): List<String> {
        val url = settingsRepo.webAppUrlFlow.first()
        val token = settingsRepo.appsScriptTokenFlow.first()
        if (url.isBlank() || token.isBlank()) return emptyList()
        return try {
            api.getYears(url, token = token).years
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
