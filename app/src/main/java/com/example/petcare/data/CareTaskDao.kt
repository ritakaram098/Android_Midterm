package com.example.petcare.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CareTaskDao {
    @Query("SELECT * FROM care_tasks WHERE petId = :petId ORDER BY isCompleted ASC, id DESC")
    fun getTasksForPet(petId: Long): Flow<List<CareTask>>

    @Query("SELECT * FROM care_tasks WHERE petId = :petId AND isCompleted = 0 ORDER BY id DESC")
    fun getPendingTasksForPet(petId: Long): Flow<List<CareTask>>

    @Query("SELECT * FROM care_tasks WHERE petId = :petId AND isCompleted = 1 ORDER BY id DESC")
    fun getCompletedTasksForPet(petId: Long): Flow<List<CareTask>>

    @Query("SELECT * FROM care_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): CareTask?

    @Query("SELECT * FROM care_history WHERE petId = :petId ORDER BY id DESC")
    fun getHistoryForPet(petId: Long): Flow<List<CareHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CareTask): Long

    @Update
    suspend fun updateTask(task: CareTask)

    @Delete
    suspend fun deleteTask(task: CareTask)

    @Query("DELETE FROM care_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    @Query("DELETE FROM care_tasks WHERE petId = :petId")
    suspend fun deleteAllTasksForPet(petId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: CareHistory)

    @Query("DELETE FROM care_history WHERE petId = :petId")
    suspend fun deleteHistoryForPet(petId: Long)

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: Long): Pet?
}
