package com.example.petcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "care_history")
data class CareHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val taskId: Long,
    val petId: Long,
    val taskTitle: String,
    val completedAt: String // formatted date/time
)
