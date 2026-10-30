package com.example.petcare.data

import kotlinx.coroutines.flow.Flow

class PetRepository(private val petDao: PetDao) {
    val allPets: Flow<List<Pet>> = petDao.getAllPets()

    suspend fun insert(pet: Pet) {
        petDao.insertPet(pet)
    }

    suspend fun delete(pet: Pet) {
        petDao.deletePet(pet)
    }

    suspend fun getPetById(petId: Long): Pet? {
        return petDao.getPetById(petId)
    }
}
