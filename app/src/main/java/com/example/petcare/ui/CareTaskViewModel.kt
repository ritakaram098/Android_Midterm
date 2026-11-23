package com.example.petcare.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

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

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter

    val filteredTasks: StateFlow<List<CareTask>> = combine(tasks, selectedFilter) { taskList, filter ->
        when (filter) {
            TaskFilter.ALL -> taskList
            TaskFilter.PENDING -> taskList.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> taskList.filter { it.isCompleted }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            try {
                _pet.value = repository.getPetById(petId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load pet details: ${e.localizedMessage}"
            }
        }
    }

    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun getTaskById(taskId: Long): CareTask? {
        return repository.getTaskById(taskId)
    }

    fun saveTask(
        taskId: Long = 0L,
        title: String,
        description: String = "",
        category: String,
        frequency: String = "Does not repeat",
        dueDate: String,
        dueTime: String,
        reminderDateTime: String = "",
        reminderEnabled: Boolean = true
    ) {
        if (title.isBlank()) {
            _errorMessage.value = "Task title cannot be empty."
            return
        }

        val formattedReminder = reminderDateTime.ifBlank { "$dueDate $dueTime" }.trim()

        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val existingTask = if (taskId > 0L) repository.getTaskById(taskId) else null

                val task = CareTask(
                    id = if (taskId > 0L) taskId else 0L,
                    petId = petId,
                    title = title.trim(),
                    description = description.trim(),
                    category = category.trim().ifBlank { "Feeding" },
                    frequency = frequency.trim().ifBlank { "Does not repeat" },
                    dueDate = dueDate.trim(),
                    dueTime = dueTime.trim(),
                    isCompleted = existingTask?.isCompleted ?: false,
                    completedAt = existingTask?.completedAt,
                    reminderDateTime = formattedReminder,
                    reminderEnabled = reminderEnabled,
                    createdAt = existingTask?.createdAt ?: now,
                    updatedAt = now
                )

                val savedId = if (taskId > 0L) {
                    repository.updateTask(task)
                    taskId
                } else {
                    repository.insertTask(task)
                }

                val finalTask = task.copy(id = savedId)

                if (reminderEnabled && !finalTask.isCompleted) {
                    scheduleReminder(finalTask)
                } else {
                    cancelReminder(finalTask.id)
                }
            } catch (e: Exception) {
                Log.e("CareTaskViewModel", "Error saving task", e)
                _errorMessage.value = "Failed to save task: ${e.localizedMessage}"
            }
        }
    }

    fun addTask(
        title: String,
        category: String,
        frequency: String,
        reminderDate: String
    ) {
        // Simple helper for quick addition compatible with legacy calls
        val parts = reminderDate.trim().split(" ")
        val datePart = parts.getOrNull(0) ?: ""
        val timePart = if (parts.size > 1) parts.subList(1, parts.size).joinToString(" ") else ""
        saveTask(
            title = title,
            category = category,
            frequency = frequency,
            dueDate = datePart,
            dueTime = timePart,
            reminderDateTime = reminderDate,
            reminderEnabled = true
        )
    }

    fun toggleTaskCompletion(task: CareTask) {
        viewModelScope.launch {
            try {
                val isNowCompleted = !task.isCompleted
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                val updated = task.copy(
                    isCompleted = isNowCompleted,
                    completedAt = if (isNowCompleted) timestamp else null,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateTask(updated)

                if (isNowCompleted) {
                    val historyRecord = CareHistory(
                        taskId = task.id,
                        petId = petId,
                        taskTitle = task.title,
                        category = task.category,
                        completedAt = timestamp
                    )
                    repository.insertHistory(historyRecord)
                    cancelReminder(task.id)
                } else {
                    if (task.reminderEnabled) {
                        scheduleReminder(updated)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update task: ${e.localizedMessage}"
            }
        }
    }

    fun completeTask(task: CareTask) {
        if (!task.isCompleted) {
            toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: CareTask) {
        viewModelScope.launch {
            try {
                repository.deleteTask(task)
                cancelReminder(task.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete task: ${e.localizedMessage}"
            }
        }
    }

    private fun scheduleReminder(task: CareTask) {
        val delayMillis = calculateDelayMillis(task.reminderDateTime)

        // Only schedule if reminder time is in the future or within the last 1 minute
        if (delayMillis <= -60000L) {
            Log.d("CareTaskViewModel", "Reminder date is in the past: ${task.reminderDateTime}")
            return
        }

        val effectiveDelay = delayMillis.coerceAtLeast(0L)

        val workData = Data.Builder()
            .putLong("taskId", task.id)
            .putString("taskTitle", task.title)
            .putString("petName", _pet.value?.name ?: "Pet")
            .putString("category", task.category)
            .putString("description", task.description)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(workData)
            .setInitialDelay(effectiveDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(getApplication()).enqueueUniqueWork(
            "task_reminder_${task.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun cancelReminder(taskId: Long) {
        WorkManager.getInstance(getApplication()).cancelUniqueWork("task_reminder_$taskId")
    }

    private fun calculateDelayMillis(reminderDateTimeStr: String): Long {
        if (reminderDateTimeStr.isBlank()) return -1L

        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        )

        for (format in formats) {
            try {
                val date = format.parse(reminderDateTimeStr)
                if (date != null) {
                    return date.time - System.currentTimeMillis()
                }
            } catch (_: Exception) {
            }
        }
        return -1L
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
