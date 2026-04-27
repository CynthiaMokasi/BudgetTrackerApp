package com.example.budgettrackerapp.data;

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpenseDao {

    @Insert
    fun insert(expense: Expense)

    @Query("SELECT * FROM expenses")
    fun getAll(): List<Expense>

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    fun getTotalByCategory(category: String): Double?

    @Delete
    fun delete(expense: Expense)
}