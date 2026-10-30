package com.example.petcare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.petcare.data.AppDatabase
import com.example.petcare.data.CareHistory
import com.example.petcare.data.CareTask
import com.example.petcare.data.CareTaskRepository
import com.example.petcare.data.Pet
import com.example.petcare.worker.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CareTaskViewModel(application: Application, val petId: Long) : AndroidViewModel(application) {
    private val repository: CareTaskRepository

    init {
        val dao = AppDatabase.getDatabase(application).careTaskDao()
        repository = CareTaskRepository(dao)
    }

    val tasks: StateFlow<List<CareTask>> = repository.getTasksForPet(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<CareHistory>> = repository.getHistoryForPet(petId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _pet = MutableStateFlow<Pet?>(null)
    val pet: StateFlow<Pet?> = _pet

    init {
        viewModelScope.launch {
            _pet.value = repository.getPetById(petId)
        }
    }

    fun addTask(title: String, category: String, frequency: String, reminderDate: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = CareTask(
                petId = petId,
                title = title.trim(),
                category = category.trim().ifBlank { "General" },
                frequency = frequency.trim().ifBlank { "Daily" },
                nextReminder = reminderDate.trim().ifBlank { "Tomorrow" },
                isCompleted = false
            )
            repository.insertTask(task)

            // Schedule WorkManager reminder
            scheduleReminder(task.title, _pet.value?.name ?: "Pet")
        }
    }

    fun completeTask(task: CareTask) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = true)
            repository.updateTask(updated)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val historyRecord = CareHistory(
                taskId = task.id,
                petId = petId,
                taskTitle = task.title,
                completedAt = timestamp
            )
            repository.insertHistory(historyRecord)
        }
    }

    fun deleteTask(task: CareTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    private fun scheduleReminder(taskTitle: String, petName: String) {
        val workData = Data.Builder()
            .putString("taskTitle", taskTitle)
            .putString("petName", petName)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(workRequest)
    }
}

class CareTaskViewModelFactory(
    private val application: Application,
    private val petId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CareTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CareTaskViewModel(application, petId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
