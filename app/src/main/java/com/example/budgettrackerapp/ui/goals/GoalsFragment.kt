package com.example.budgettrackerapp.ui.goals

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase
import com.example.budgettrackerapp.data.Badge
import com.example.budgettrackerapp.data.BadgeRepository
import com.example.budgettrackerapp.data.SavingsGoal
import com.example.budgettrackerapp.data.SavingsGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_goals, container, false)

        val etMin = view.findViewById<EditText>(R.id.etMin)
        val etMax = view.findViewById<EditText>(R.id.etMax)
        val result = view.findViewById<TextView>(R.id.tvResult)
        val btn = view.findViewById<Button>(R.id.btnCheck)
        val badgeContainer = view.findViewById<LinearLayout>(R.id.badgeContainer)
        val tvBudgetSummary = view.findViewById<TextView>(R.id.tvBudgetSummary)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarStatus)
        val tvPerformanceStatus = view.findViewById<TextView>(R.id.tvPerformanceStatus)
        val etGoalName = view.findViewById<EditText>(R.id.etGoalName)
        val etGoalTarget = view.findViewById<EditText>(R.id.etGoalTarget)
        val etSavedAmount = view.findViewById<EditText>(R.id.etSavedAmount)
        val btnSaveGoal = view.findViewById<Button>(R.id.btnSaveGoal)
        val tvSavingsSummary = view.findViewById<TextView>(R.id.tvSavingsSummary)
        val savingsProgressBar = view.findViewById<ProgressBar>(R.id.savingsProgressBar)
        val tvSavingsStatus = view.findViewById<TextView>(R.id.tvSavingsStatus)
        val savingsGoalListContainer = view.findViewById<LinearLayout>(R.id.savingsGoalListContainer)

        val db = AppDatabase.getDatabase(requireContext())
        val badgeRepository = BadgeRepository.getInstance(requireContext())
        val savingsGoalRepository = SavingsGoalRepository.getInstance(requireContext())

        fun showBadge(badge: Badge) {
            val badgeView = TextView(requireContext()).apply {
                text = "${if (badge.earned) "🏆" else "⚪"} ${badge.name}: ${badge.description}"
                textSize = 14f
                setPadding(8, 8, 8, 8)
                setTextColor(
                    if (badge.earned) Color.parseColor("#388E3C")
                    else Color.parseColor("#999999")
                )
            }
            badgeContainer.addView(badgeView)
        }

        // Initialize badges once
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                badgeRepository.ensureDefaultBadges(
                    listOf(
                        Badge(name = "Budget Master", description = "Stayed within budget"),
                        Badge(name = "Saver", description = "Spent below 80% of budget"),
                        Badge(name = "Consistent Logger", description = "Logged spending activity"),
                        Badge(name = "Penny Pincher", description = "Spent below 50% of budget"),
                        Badge(name = "Goal Achiever", description = "Saved amount in a savings goal"),
                        Badge(name = "First Step", description = "Added your first expense"),
                        Badge(name = "Weekly Tracker", description = "Logged expenses for 7 days"),
                        Badge(name = "Monthly Planner", description = "Set up monthly budget limits")
                    )
                )
            }
        }

        // Collect badges whenever they update
        lifecycleScope.launch {
            badgeRepository.getBadges().collectLatest { badges ->
                badgeContainer.removeAllViews()
                if (badges.isNotEmpty()) {
                    badges.forEach { badge ->
                        showBadge(badge)
                    }
                }
            }
        }

        // Collect savings goals
        lifecycleScope.launch {
            savingsGoalRepository.getAllGoals().collectLatest { goals ->
                savingsGoalListContainer.removeAllViews()
                if (goals.isEmpty()) {
                    val emptyView = TextView(requireContext()).apply {
                        text = "No savings goals saved yet. Create one to track progress."
                        setPadding(0, 8, 0, 8)
                        textSize = 14f
                    }
                    savingsGoalListContainer.addView(emptyView)
                } else {
                    goals.forEach { goal ->
                        val message = String.format(
                            "%s: R%.2f / R%.2f %s",
                            goal.name,
                            goal.savedAmount,
                            goal.targetAmount,
                            if (goal.achieved) "(✓ Achieved)" else ""
                        )
                        val goalView = TextView(requireContext()).apply {
                            text = message
                            setPadding(0, 8, 0, 8)
                            textSize = 14f
                            setTextColor(
                                if (goal.achieved) Color.parseColor("#388E3C")
                                else Color.BLACK
                            )
                        }
                        savingsGoalListContainer.addView(goalView)
                    }
                }
            }
        }

        fun updateBudgetStatus(total: Double, min: Double, max: Double) {
            if (max > 0) {
                val percent = ((total / max) * 100).coerceIn(0.0, 100.0).toInt()
                progressBar.progress = percent
                tvBudgetSummary.text = String.format(
                    "%s of budget used: R%.2f / R%.2f (%d%%)",
                    if (total <= max) "On track" else "Over budget",
                    total,
                    max,
                    percent
                )

                when {
                    total <= max * 0.8 -> {
                        tvPerformanceStatus.text = "✓ Excellent budget control"
                        tvPerformanceStatus.setTextColor(Color.parseColor("#388E3C"))
                    }
                    total <= max -> {
                        tvPerformanceStatus.text = "⚠ On target for your budget"
                        tvPerformanceStatus.setTextColor(Color.parseColor("#FBC02D"))
                    }
                    else -> {
                        tvPerformanceStatus.text = "✗ Exceeded your budget"
                        tvPerformanceStatus.setTextColor(Color.parseColor("#D32F2F"))
                    }
                }
            } else {
                progressBar.progress = 0
                tvBudgetSummary.text = "Enter a maximum budget goal to see progress."
                tvPerformanceStatus.text = ""
            }
        }

        suspend fun checkWeeklyTrackerEligibility() {
            withContext(Dispatchers.IO) {
                val expenses = db.expenseDao().getAll()
                val today = System.currentTimeMillis()
                val sevenDaysAgo = today - (7 * 24 * 60 * 60 * 1000)

                val daysWithExpenses = expenses
                    .filter { it.timestamp in sevenDaysAgo..today }
                    .groupBy { expense ->
                        val cal = android.icu.util.Calendar.getInstance()
                        cal.timeInMillis = expense.timestamp
                        cal.get(android.icu.util.Calendar.DAY_OF_YEAR)
                    }
                    .size

                if (daysWithExpenses >= 7) {
                    badgeRepository.awardBadge(
                        Badge(
                            name = "Weekly Tracker",
                            description = "Logged expenses for 7 days",
                            earned = true,
                            earnedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        btn.setOnClickListener {
            lifecycleScope.launch {
                val total = withContext(Dispatchers.IO) { db.expenseDao().getAll().sumOf { it.amount } }
                val min = etMin.text.toString().toDoubleOrNull() ?: 0.0
                val max = etMax.text.toString().toDoubleOrNull() ?: 0.0

                val statusText = when {
                    min > 0 && total < min -> "Below minimum spending"
                    max > 0 && total > max -> "Exceeded maximum!"
                    else -> "Within budget"
                }

                result.text = statusText
                updateBudgetStatus(total, min, max)

                if (statusText == "Within budget" && max > 0) {
                    withContext(Dispatchers.IO) {
                        badgeRepository.awardBadge(
                            Badge(
                                name = "Budget Master",
                                description = "Stayed within budget",
                                earned = true,
                                earnedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                if (max > 0 && total <= max * 0.8) {
                    withContext(Dispatchers.IO) {
                        badgeRepository.awardBadge(
                            Badge(
                                name = "Saver",
                                description = "Spent below 80% of budget",
                                earned = true,
                                earnedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                if (max > 0 && total <= max * 0.5) {
                    withContext(Dispatchers.IO) {
                        badgeRepository.awardBadge(
                            Badge(
                                name = "Penny Pincher",
                                description = "Spent below 50% of budget",
                                earned = true,
                                earnedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                if (total > 0) {
                    withContext(Dispatchers.IO) {
                        badgeRepository.awardBadge(
                            Badge(
                                name = "Consistent Logger",
                                description = "Logged spending activity",
                                earned = true,
                                earnedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                if (max > 0) {
                    withContext(Dispatchers.IO) {
                        badgeRepository.awardBadge(
                            Badge(
                                name = "Monthly Planner",
                                description = "Set up monthly budget limits",
                                earned = true,
                                earnedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }

                checkWeeklyTrackerEligibility()
            }
        }

        fun updateSavingsUI(goal: SavingsGoal?) {
            if (goal == null || goal.targetAmount <= 0) {
                tvSavingsSummary.text = "Set a savings goal to track progress."
                savingsProgressBar.progress = 0
                tvSavingsStatus.text = ""
                return
            }

            val percent = ((goal.savedAmount / goal.targetAmount) * 100).coerceIn(0.0, 100.0).toInt()
            savingsProgressBar.progress = percent
            tvSavingsSummary.text = String.format(
                "%s: R%.2f / R%.2f (%d%%)",
                goal.name,
                goal.savedAmount,
                goal.targetAmount,
                percent
            )
            tvSavingsStatus.text = if (goal.achieved) "✓ Goal achieved!" else "Keep saving toward your goal."
            tvSavingsStatus.setTextColor(
                if (goal.achieved) Color.parseColor("#388E3C")
                else Color.parseColor("#1976D2")
            )
        }

        btnSaveGoal.setOnClickListener {
            lifecycleScope.launch {
                val name = etGoalName.text.toString().ifBlank { "My Savings Goal" }
                val target = etGoalTarget.text.toString().toDoubleOrNull() ?: 0.0
                val saved = etSavedAmount.text.toString().toDoubleOrNull() ?: 0.0
                val achieved = target > 0 && saved >= target

                if (target > 0) {
                    val goal = SavingsGoal(
                        name = name,
                        targetAmount = target,
                        savedAmount = saved,
                        achieved = achieved
                    )
                    withContext(Dispatchers.IO) {
                        savingsGoalRepository.saveGoal(goal)

                        if (achieved) {
                            badgeRepository.awardBadge(
                                Badge(
                                    name = "Goal Achiever",
                                    description = "Saved amount in a savings goal",
                                    earned = true,
                                    earnedAt = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                    updateSavingsUI(goal)
                    etGoalName.setText("")
                    etGoalTarget.setText("")
                    etSavedAmount.setText("")
                    Toast.makeText(requireContext(), "Goal saved!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }
}
