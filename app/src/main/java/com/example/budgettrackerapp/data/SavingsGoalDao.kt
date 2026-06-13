package com.example.budgettrackerapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(savingsGoal: SavingsGoal)

    @Update
    suspend fun update(savingsGoal: SavingsGoal)

    @Query("SELECT * FROM savings_goals WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): SavingsGoal?

    @Query("SELECT * FROM savings_goals")
    fun getAllGoals(): Flow<List<SavingsGoal>>
}
