package com.example.petcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_tasks")
data class CareTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val petId: Long,
    val title: String,
    val description: String = "",
    val category: String, // Feeding, Grooming, Bathing, Flea & tick treatment, Medication, Exercise, Vet appointment, Other
    val frequency: String = "Does not repeat", // Does not repeat, Daily, Weekly, Monthly
    val dueDate: String = "", // e.g. "2026-03-01"
    val dueTime: String = "", // e.g. "08:30 AM"
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val reminderDateTime: String = "", // e.g. "2026-03-01 08:30 AM"
    val reminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
