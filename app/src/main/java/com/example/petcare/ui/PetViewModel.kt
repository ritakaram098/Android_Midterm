package com.example.petcare.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcare.data.AppDatabase
import com.example.petcare.data.Pet
import com.example.petcare.data.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PetRepository

    init {
        val petDao = AppDatabase.getDatabase(application).petDao()
        repository = PetRepository(petDao)
    }

    val pets: StateFlow<List<Pet>> = repository.allPets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addPet(
        name: String,
        type: String,
        breed: String,
        sex: String,
        birthDate: String,
        weight: String,
        notes: String,
        photoUri: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || type.isBlank() || birthDate.isBlank()) return
        viewModelScope.launch {
            repository.insert(
                Pet(
                    name = name.trim(),
                    type = type.trim(),
                    breed = breed.trim().ifBlank { "Mixed / Unknown" },
                    sex = sex.trim(),
                    birthDate = birthDate.trim(),
                    weight = weight.trim(),
                    notes = notes.trim(),
                    photoUri = photoUri.trim()
                )
            )
            onSuccess()
        }
    }

    fun deletePet(pet: Pet) {
        viewModelScope.launch {
            repository.delete(pet)
        }
    }
}
