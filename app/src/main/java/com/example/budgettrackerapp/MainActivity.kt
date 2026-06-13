package com.example.budgettrackerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.budgettrackerapp.databinding.ActivityMainBinding
import com.example.budgettrackerapp.ui.auth.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        // Attach navigation
        binding.navView.setupWithNavController(navController)

        // Create notification channel and schedule daily reminders
        com.example.budgettrackerapp.data.NotificationManager.createNotificationChannel(this)
        com.example.budgettrackerapp.data.NotificationManager.scheduleExpenseReminder(this)

        // IMPORTANT: override bottom nav clicks
        binding.navView.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_logout -> {
                    logout()
                    true
                }

                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}