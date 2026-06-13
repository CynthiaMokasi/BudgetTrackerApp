package com.example.budgettrackerapp.ui.achievements

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.budgettrackerapp.R
import com.example.budgettrackerapp.data.AppDatabase
import com.example.budgettrackerapp.data.Badge
import com.example.budgettrackerapp.data.BadgeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AchievementsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
        }

        val mainContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(24, 100, 24, 24)
        }

        val titleView = TextView(requireContext()).apply {
            text = "Achievements"
            textSize = 24f
            setTextColor(Color.BLACK)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        mainContainer.addView(titleView)

        val contentContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        mainContainer.addView(contentContainer)
        scrollView.addView(mainContainer)

        val badgeRepository = BadgeRepository.getInstance(requireContext())

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
            badgeRepository.getBadges().collectLatest { badges ->
                contentContainer.removeAllViews()
                if (badges.isEmpty()) {
                    val emptyView = TextView(requireContext()).apply {
                        text = "No achievements yet. Start using the app to earn badges!"
                        setPadding(0, 16, 0, 16)
                    }
                    contentContainer.addView(emptyView)
                } else {
                    val earnedBadges = badges.filter { it.earned }
                    val lockedBadges = badges.filter { !it.earned }

                    if (earnedBadges.isNotEmpty()) {
                        val earnedTitle = TextView(requireContext()).apply {
                            text = "Unlocked (${earnedBadges.size})"
                            textSize = 16f
                            setTextColor(Color.parseColor("#388E3C"))
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { topMargin = 12; bottomMargin = 8 }
                        }
                        contentContainer.addView(earnedTitle)

                        earnedBadges.forEach { badge ->
                            val badgeView = createBadgeCard(badge, true)
                            contentContainer.addView(badgeView)
                        }
                    }

                    if (lockedBadges.isNotEmpty()) {
                        val lockedTitle = TextView(requireContext()).apply {
                            text = "Locked (${lockedBadges.size})"
                            textSize = 16f
                            setTextColor(Color.parseColor("#1976D2"))
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { topMargin = 16; bottomMargin = 8 }
                        }
                        contentContainer.addView(lockedTitle)

                        lockedBadges.forEach { badge ->
                            val badgeView = createBadgeCard(badge, false)
                            contentContainer.addView(badgeView)
                        }
                    }
                }
            }
        }

        return scrollView
    }

    private fun createBadgeCard(badge: Badge, earned: Boolean): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12 }
            setPadding(12, 12, 12, 12)
            setBackgroundColor(
                if (earned) Color.parseColor("#E8F5E9")
                else Color.parseColor("#E3F2FD")
            )
        }

        val icon = TextView(requireContext()).apply {
            text = if (earned) "🏆" else "🔒"
            textSize = 24f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { rightMargin = 12 }
        }
        card.addView(icon)

        val content = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val nameView = TextView(requireContext()).apply {
            text = badge.name
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(if (earned) Color.parseColor("#388E3C") else Color.parseColor("#1976D2"))
        }
        content.addView(nameView)

        val descView = TextView(requireContext()).apply {
            text = badge.description
            textSize = 14f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        }
        content.addView(descView)

        if (earned && badge.earnedAt != null) {
            val dateView = TextView(requireContext()).apply {
                val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
                    .format(java.util.Date(badge.earnedAt))
                text = "Unlocked: $date"
                textSize = 12f
                setTextColor(Color.parseColor("#666666"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            }
            content.addView(dateView)
        }

        card.addView(content)
        return card
    }
}
