package com.example.budgettrackerapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.budgettrackerapp.MainActivity
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.etUsername)
        val password = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val db = AppDatabase.getDatabase(this)

        btnLogin.setOnClickListener {

            val user = username.text.toString()
            val pass = password.text.toString()

            Executors.newSingleThreadExecutor().execute {

                val result = db.userDao().login(user, pass)

                runOnUiThread {
                    if (result != null) {
                        Toast.makeText(
                            this,
                            "Welcome ${result.name}",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid login", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Registration screen
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}