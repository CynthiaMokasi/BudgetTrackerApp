package com.example.budgettrackerapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ExpenseRepository private constructor(private val db: AppDatabase) {
    private val expenseDao = db.expenseDao()
    private val budgetDao = db.budgetDao()

    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)

    fun getExpensesBetween(start: Long, end: Long): Flow<List<Expense>> = expenseDao.getExpensesBetween(start, end)

    fun getSumByCategory(start: Long, end: Long): Flow<List<CategorySpend>> = expenseDao.getSumByCategory(start, end)

    fun getBudgetLimits(): Flow<List<BudgetLimit>> = budgetDao.getAllLimits()

    suspend fun insertBudgetLimit(limit: BudgetLimit) = budgetDao.insert(limit)

    companion object {
        @Volatile
        private var INSTANCE: ExpenseRepository? = null

        fun getInstance(context: Context): ExpenseRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ExpenseRepository(AppDatabase.getDatabase(context))
                INSTANCE = instance
                instance
            }
        }
    }
}
