package com.aistudio.classroll.jkmxlp.data

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

    fun getAllAttendanceForYear(year: String): Flow<List<AttendanceRecordEntity>> {
        return dao.getAllAttendanceForYear(year)
    }

    // Sync Students from Server
    suspend fun syncStudents() {
        // Feature disabled - local only
    }

    suspend fun importStudents(year: String, students: List<RemoteStudent>): String {
        return try {
            val entities = students.map {
                StudentEntity(year = year, roll = it.roll, name = it.name, active = it.active)
            }
            dao.insertStudents(entities)
            "Success"
        } catch (e: Exception) {
            e.printStackTrace()
            e.message ?: "Unknown error"
        }
    }

    // Submit a single day's attendance
    suspend fun submitAttendance(year: String, date: String, records: List<AttendanceRecordEntity>): Boolean {
        // Save locally 
        dao.insertAttendanceRecords(records)
        return true
    }

    // Update single cell (from Register screen)
    suspend fun updateAttendanceCell(year: String, date: String, roll: String, status: String): Boolean {
        // Update locally
        dao.insertAttendanceRecords(listOf(AttendanceRecordEntity(year, date, roll, status, isSynced = true)))
        return true
    }
    
    suspend fun fetchYears(): List<String> {
        // Local only fallback, or could query DB
        return emptyList()
    }
}
