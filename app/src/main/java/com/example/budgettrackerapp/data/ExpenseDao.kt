package com.example.budgettrackerapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Insert
    fun insertBlocking(expense: Expense)

    @Query("SELECT * FROM expenses WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getExpensesBetween(start: Long, end: Long): Flow<List<Expense>>

    @Query("SELECT category AS category, SUM(amount) AS total FROM expenses WHERE timestamp BETWEEN :start AND :end GROUP BY category")
    fun getSumByCategory(start: Long, end: Long): Flow<List<CategorySpend>>

    @Query("SELECT * FROM expenses")
    fun getAll(): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    fun getTotalByCategory(category: String): Double?

    @Delete
    fun delete(expense: Expense)
}