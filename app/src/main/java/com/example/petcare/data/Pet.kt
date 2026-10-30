package com.example.petcare.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String,
    val breed: String = "",
    val sex: String = "Male",
    val birthDate: String,
    val weight: String = "",
    val notes: String = "",
    val photoUri: String = ""
)
