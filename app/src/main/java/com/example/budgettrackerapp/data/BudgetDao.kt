package com.example.budgettrackerapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budgetLimit: BudgetLimit)

    @Query("SELECT * FROM budget_limits WHERE category = :category LIMIT 1")
    fun getLimitForCategory(category: String): Flow<BudgetLimit?>

    @Query("SELECT * FROM budget_limits WHERE category = :category LIMIT 1")
    fun getLimitForCategoryBlocking(category: String): BudgetLimit?

    @Query("SELECT * FROM budget_limits")
    fun getAllLimits(): Flow<List<BudgetLimit>>

    @Query("SELECT * FROM budget_limits")
    fun getAllLimitsBlocking(): List<BudgetLimit>
}
