package com.example.budgettrackerapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Insert
    fun register(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    fun checkUser(username: String): User?
}