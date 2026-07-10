package com.aistudio.classroll.jkmxlp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.aistudio.classroll.jkmxlp.data.AppDatabase
import com.aistudio.classroll.jkmxlp.data.AttendanceRecordEntity
import com.aistudio.classroll.jkmxlp.data.ClassRollRepository
import com.aistudio.classroll.jkmxlp.data.NetworkModule
import com.aistudio.classroll.jkmxlp.data.RemoteStudent
import com.aistudio.classroll.jkmxlp.data.SettingsRepository
import com.aistudio.classroll.jkmxlp.data.StudentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import kotlinx.coroutines.flow.flatMapLatest

class ClassRollViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(application, AppDatabase::class.java, "classroll_db").build()
    private val settingsRepo = SettingsRepository(application)
    val repository = ClassRollRepository(db.classRollDao(), NetworkModule.apiService, settingsRepo)

    val webAppUrl = settingsRepo.webAppUrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://script.google.com/macros/s/AKfycbzTDiNJh4LEaIah19SVFaf6JlESbW5tf2ElwaMULTDENIAlXFOFI4QAXEmV1nYwrVdA/exec")
    val currentYear = settingsRepo.academicYearFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _availableYears = MutableStateFlow<List<String>>(emptyList())
    val availableYears: StateFlow<List<String>> = _availableYears.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    fun updateSettings(year: String) {
        viewModelScope.launch {
            settingsRepo.updateAcademicYear(year)
        }
    }

    fun fetchYears() {
        viewModelScope.launch {
            _isSyncing.value = true
            val years = repository.fetchYears()
            if (years.isNotEmpty()) {
                _availableYears.value = years
            }
            _isSyncing.value = false
        }
    }

    fun syncStudents() {
        viewModelScope.launch {
            _isSyncing.value = true
            repository.syncStudents()
            _isSyncing.value = false
        }
    }

    fun importStudents(year: String, students: List<RemoteStudent>, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.importStudents(year, students)
            _isSyncing.value = false
            onResult(result)
        }
    }

    fun submitAttendance(date: String, records: List<AttendanceRecordEntity>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSyncing.value = true
            val year = currentYear.value
            val success = repository.submitAttendance(year, date, records)
            _isSyncing.value = false
            onResult(success)
        }
    }
    
    fun updateCell(date: String, roll: String, status: String) {
        viewModelScope.launch {
            val year = currentYear.value
            repository.updateAttendanceCell(year, date, roll, status)
        }
    }
    
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val students: StateFlow<List<StudentEntity>> = currentYear
        .flatMapLatest { year ->
            repository.getStudentsForYear(year)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun getAttendanceForDate(date: String): StateFlow<List<AttendanceRecordEntity>> {
        val year = currentYear.value
        return repository.getAttendanceForDate(year, date).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getAttendanceForMonth(monthPrefix: String): StateFlow<List<AttendanceRecordEntity>> {
        val year = currentYear.value
        return repository.getAttendanceForMonth(year, monthPrefix).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}
