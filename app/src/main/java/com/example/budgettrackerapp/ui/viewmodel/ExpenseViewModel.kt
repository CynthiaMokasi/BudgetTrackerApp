package com.example.budgettrackerapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.budgettrackerapp.data.CategorySpend
import com.example.budgettrackerapp.data.ExpenseRepository

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ExpenseRepository.getInstance(application)

    fun getSpendingByCategory(start: Long, end: Long): LiveData<List<CategorySpend>> {
        return repo.getSumByCategory(start, end).asLiveData()
    }

    fun getBudgetLimits() = repo.getBudgetLimits().asLiveData()
}
