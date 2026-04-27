package com.example.budgettrackerapp.ui.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase
import com.example.budgettrackerapp.data.User
import java.util.concurrent.Executors

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val username = findViewById<EditText>(R.id.etUsername)
        val name = findViewById<EditText>(R.id.etName)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val db = AppDatabase.getDatabase(this)

        btnRegister.setOnClickListener {

            val user = username.text.toString()
            val fullName = name.text.toString()
            val pass = password.text.toString()

            if (user.isEmpty() || fullName.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Executors.newSingleThreadExecutor().execute {

                val exists = db.userDao().checkUser(user)

                runOnUiThread {

                    if (exists != null) {
                        Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show()
                    } else {

                        Executors.newSingleThreadExecutor().execute {
                            db.userDao().register(
                                User(user, fullName, pass)
                            )

                            runOnUiThread {
                                Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}