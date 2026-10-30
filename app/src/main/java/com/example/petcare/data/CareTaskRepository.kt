package com.example.petcare.data

import kotlinx.coroutines.flow.Flow

class CareTaskRepository(private val dao: CareTaskDao) {
    fun getTasksForPet(petId: Long): Flow<List<CareTask>> = dao.getTasksForPet(petId)
    fun getHistoryForPet(petId: Long): Flow<List<CareHistory>> = dao.getHistoryForPet(petId)

    suspend fun insertTask(task: CareTask): Long = dao.insertTask(task)
    suspend fun updateTask(task: CareTask) = dao.updateTask(task)
    suspend fun deleteTask(task: CareTask) = dao.deleteTask(task)
    suspend fun insertHistory(history: CareHistory) = dao.insertHistory(history)
    suspend fun getPetById(petId: Long): Pet? = dao.getPetById(petId)
}
