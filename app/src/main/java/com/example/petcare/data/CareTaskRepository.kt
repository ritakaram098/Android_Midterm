package com.example.petcare.data

import kotlinx.coroutines.flow.Flow

class CareTaskRepository(private val dao: CareTaskDao) {
    fun getTasksForPet(petId: Long): Flow<List<CareTask>> = dao.getTasksForPet(petId)
    fun getPendingTasksForPet(petId: Long): Flow<List<CareTask>> = dao.getPendingTasksForPet(petId)
    fun getCompletedTasksForPet(petId: Long): Flow<List<CareTask>> = dao.getCompletedTasksForPet(petId)
    fun getHistoryForPet(petId: Long): Flow<List<CareHistory>> = dao.getHistoryForPet(petId)

    suspend fun getTaskById(taskId: Long): CareTask? = dao.getTaskById(taskId)
    suspend fun insertTask(task: CareTask): Long = dao.insertTask(task)
    suspend fun updateTask(task: CareTask) = dao.updateTask(task)
    suspend fun deleteTask(task: CareTask) = dao.deleteTask(task)
    suspend fun deleteTaskById(taskId: Long) = dao.deleteTaskById(taskId)
    suspend fun deleteAllTasksForPet(petId: Long) = dao.deleteAllTasksForPet(petId)

    suspend fun insertHistory(history: CareHistory) = dao.insertHistory(history)
    suspend fun deleteHistoryForPet(petId: Long) = dao.deleteHistoryForPet(petId)
    suspend fun getPetById(petId: Long): Pet? = dao.getPetById(petId)
}
