
package com.example.petcare.ui

import android.app.Application
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CareTaskViewModel(
    application: Application,
    val petId: Long
) : AndroidViewModel(application) {

    private val repository: CareTaskRepository

    private val workManager =
        WorkManager.getInstance(application)

    init {
        val dao = AppDatabase
            .getDatabase(application)
            .careTaskDao()

        repository = CareTaskRepository(dao)
    }

    val tasks: StateFlow<List<CareTask>> =
        repository.getTasksForPet(petId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<CareHistory>> =
        repository.getHistoryForPet(petId).stateIn(
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

    fun addTask(
        title: String,
        category: String,
        frequency: String,
        reminderDate: String
    ) {
        if (title.isBlank()) return

        viewModelScope.launch {

            val task = CareTask(
                petId = petId,
                title = title.trim(),
                category = category.trim().ifBlank {
                    "General"
                },
                frequency = frequency.trim().ifBlank {
                    "Does not repeat"
                },
                nextReminder = reminderDate.trim(),
                isCompleted = false
            )

            val taskId = repository.insertTask(task)

            scheduleReminder(
                taskId = taskId,
                reminderDate = task.nextReminder
            )
        }
    }

    fun completeTask(task: CareTask) {

        if (task.isCompleted) return

        viewModelScope.launch {

            val currentTask =
                repository.getTaskById(task.id) ?: return@launch

            if (currentTask.isCompleted) return@launch

            val timestamp = SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
            ).format(Date())

            val historyRecord = CareHistory(
                taskId = currentTask.id,
                petId = petId,
                taskTitle = currentTask.title,
                completedAt = timestamp
            )

            val nextDate = calculateNextReminder(
                currentTask.nextReminder,
                currentTask.frequency
            )

            if (nextDate == null) {

                // One-time task: stays completed.
                repository.updateTask(
                    currentTask.copy(
                        isCompleted = true
                    )
                )

                workManager.cancelUniqueWork(
                    "care_reminder_${currentTask.id}"
                )

            } else {

                // Recurring task: completed for this period.
                // Keep the next scheduled reminder.
                repository.updateTask(
                    currentTask.copy(
                        isCompleted = true,
                        nextReminder = nextDate
                    )
                )

                scheduleReminder(
                    taskId = currentTask.id,
                    reminderDate = nextDate
                )
            }

            repository.insertHistory(historyRecord)
        }
    }

    fun deleteTask(task: CareTask) {

        viewModelScope.launch {

            workManager.cancelUniqueWork(
                "care_reminder_${task.id}"
            )

            repository.deleteTask(task)
        }
    }

    private fun scheduleReminder(
        taskId: Long,
        reminderDate: String
    ) {

        val reminderMillis =
            parseReminderDate(reminderDate) ?: return

        val delay = (
                reminderMillis - System.currentTimeMillis()
                ).coerceAtLeast(0L)

        val workData = Data.Builder()
            .putLong("taskId", taskId)
            .putLong("dueAt", reminderMillis)
            .build()

        val workRequest =
            OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(workData)
                .setInitialDelay(
                    delay,
                    TimeUnit.MILLISECONDS
                )
                .build()

        workManager.enqueueUniqueWork(
            "care_reminder_$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun calculateNextReminder(
        reminderDate: String,
        frequency: String
    ): String? {

        if (frequency !in listOf(
                "Daily",
                "Weekly",
                "Monthly"
            )
        ) {
            return null
        }

        val originalMillis =
            parseReminderDate(reminderDate) ?: return null

        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = originalMillis
        }

        // If the next reminder is already in the future,
        // keep that date without adding another period.
        if (calendar.timeInMillis > now) {

            return SimpleDateFormat(
                "yyyy-MM-dd h:mm a",
                Locale.ENGLISH
            ).format(calendar.time)
        }

        // Otherwise, advance to the next future date.
        do {
            when (frequency) {

                "Daily" -> {
                    calendar.add(
                        Calendar.DAY_OF_YEAR,
                        1
                    )
                }

                "Weekly" -> {
                    calendar.add(
                        Calendar.WEEK_OF_YEAR,
                        1
                    )
                }

                "Monthly" -> {
                    calendar.add(
                        Calendar.MONTH,
                        1
                    )
                }
            }

        } while (calendar.timeInMillis <= now)

        return SimpleDateFormat(
            "yyyy-MM-dd h:mm a",
            Locale.ENGLISH
        ).format(calendar.time)
    }

    private fun parseReminderDate(
        value: String
    ): Long? {

        val formats = listOf(
            "yyyy-MM-dd h:mm a",
            "yyyy-MM-dd hh:mm a",
            "yyyy-MM-dd HH:mm"
        )

        for (pattern in formats) {

            try {

                val formatter = SimpleDateFormat(
                    pattern,
                    Locale.ENGLISH
                ).apply {
                    isLenient = false
                }

                val date = formatter.parse(
                    value.trim()
                )

                if (date != null) {
                    return date.time
                }

            } catch (_: Exception) {
                // Try the next format.
            }
        }

        return null
    }
}

class CareTaskViewModelFactory(
    private val application: Application,
    private val petId: Long
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(
                CareTaskViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return CareTaskViewModel(
                application,
                petId
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
