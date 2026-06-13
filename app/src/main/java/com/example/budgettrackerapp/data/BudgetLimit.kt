package com.example.budgettrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_limits")
data class BudgetLimit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val minAmount: Double = 0.0,
    val maxAmount: Double = 0.0,
    val period: String = "MONTH" // simple period indicator
)
