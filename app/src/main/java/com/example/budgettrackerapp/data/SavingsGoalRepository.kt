package com.example.budgettrackerapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class SavingsGoalRepository private constructor(private val db: AppDatabase) {

    private val savingsGoalDao = db.savingsGoalDao()

    suspend fun saveGoal(goal: SavingsGoal) = savingsGoalDao.insert(goal)
    suspend fun updateGoal(goal: SavingsGoal) = savingsGoalDao.update(goal)
    suspend fun getGoal(name: String): SavingsGoal? = savingsGoalDao.getByName(name)
    fun getAllGoals(): Flow<List<SavingsGoal>> = savingsGoalDao.getAllGoals()

    companion object {
        @Volatile
        private var INSTANCE: SavingsGoalRepository? = null

        fun getInstance(context: Context): SavingsGoalRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = SavingsGoalRepository(AppDatabase.getDatabase(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
