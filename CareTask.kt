package com.example.petcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_tasks")
data class CareTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val petId: Long,
    val title: String,
    val category: String, // e.g. "Feeding", "Grooming", "Medical"
    val frequency: String, // e.g. "Daily", "Weekly"
    val nextReminder: String, // e.g. "2026-03-01 09:00"
    val isCompleted: Boolean = false
)
