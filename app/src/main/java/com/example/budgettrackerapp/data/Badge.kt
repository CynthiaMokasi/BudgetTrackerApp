package com.example.budgettrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val name: String,
    val description: String,
    val earned: Boolean = false,
    val earnedAt: Long? = null
)
